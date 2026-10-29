import { beforeEach, describe, expect, it } from '@jest/globals';
import { stubOf } from '../../../../test/support/stubs.js';
import { testConfig } from '../../../../test/support/config.js';
import { AcceptanceStatus, OrderStatus, PartyRole } from '../../../common/domain/enums.js';
import {
  BadRequestAppException,
  ConflictAppException,
  NotFoundAppException,
  PayloadTooLargeAppException,
  UnprocessableAppException,
} from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import { PhotoStorage } from '../../photo-storage/photo-storage.js';
import type { OrderRow, PhotoRow } from '../domain/order.types.js';
import { AcceptanceRepository } from '../repositories/acceptance.repository.js';
import { PhotoRepository } from '../repositories/photo.repository.js';
import { OrderViewAssembler } from '../views/order-view.assembler.js';
import { OrderAccessService } from './order-access.service.js';
import { OrderPhotoService } from './order-photo.service.js';

const JPEG = Buffer.from([0xff, 0xd8, 0xff, 0xe0, 1, 2, 3]);
const PNG = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 1]);
const neighbor = { id: 'n1', role: PartyRole.NEIGHBOR };
const recycler = { id: 'r1', role: PartyRole.RECYCLER };
const order = (status = OrderStatus.PENDING): OrderRow => ({
  id: 'o1',
  neighborId: 'n1',
  status,
  createdAt: new Date(),
  zone: null,
  address: null,
  latitude: 0,
  longitude: 0,
});
const photo: PhotoRow = { id: 'p1', orderId: 'o1', fileName: 'o1/p1.jpg', contentType: 'image/jpeg' };

describe('OrderPhotoService', () => {
  let access: ReturnType<typeof stubOf<OrderAccessService>>;
  let photos: ReturnType<typeof stubOf<PhotoRepository>>;
  let acceptances: ReturnType<typeof stubOf<AcceptanceRepository>>;
  let storage: ReturnType<typeof stubOf<PhotoStorage>>;
  let service: OrderPhotoService;

  beforeEach(() => {
    access = stubOf<OrderAccessService>('load', 'loadOwned');
    photos = stubOf<PhotoRepository>('findByOrder', 'findOne', 'insert', 'delete');
    acceptances = stubOf<AcceptanceRepository>('count');
    storage = stubOf<PhotoStorage>('put', 'remove', 'signedUrl');
    access.loadOwned.mockResolvedValue(order());
    acceptances.count.mockResolvedValue(0);
    photos.findByOrder.mockResolvedValue([]);
    photos.insert.mockResolvedValue(photo);
    storage.put.mockResolvedValue(undefined);
    storage.remove.mockResolvedValue(undefined);
    service = new OrderPhotoService(
      access,
      photos,
      acceptances,
      storage,
      new OrderViewAssembler(),
      testConfig(),
    );
  });

  describe('upload', () => {
    it('stores the object under {orderId}/{uuid}.jpg and keeps only that key in the database', async () => {
      const result = await service.upload(neighbor, 'o1', { buffer: JPEG, size: JPEG.length });
      const [key, , contentType] = storage.put.mock.calls[0] as [string, Buffer, string];
      expect(key).toMatch(/^o1\/[0-9a-f-]{36}\.jpg$/);
      expect(key.length).toBeLessThanOrEqual(80);
      expect(contentType).toBe('image/jpeg');
      expect((photos.insert.mock.calls[0] as unknown[])[2]).toBe(key);
      expect(result.url).toBe(`/api/v1/servicing-order/o1/photo/${result.id}/retrieval`);
    });

    it('detects PNG from the bytes', async () => {
      await service.upload(neighbor, 'o1', { buffer: PNG, size: PNG.length });
      expect((storage.put.mock.calls[0] as unknown[])[0]).toMatch(/\.png$/);
    });

    it('rejects a file whose bytes are not JPEG or PNG, whatever its name says', async () => {
      const html = Buffer.from('<html></html>');
      const error = await service
        .upload(neighbor, 'o1', { buffer: html, size: html.length })
        .catch((e: unknown) => e);
      expect((error as UnprocessableAppException).code).toBe(ErrorCode.PHOTO_INVALID_TYPE);
      expect(storage.put).not.toHaveBeenCalled();
    });

    it('rejects a missing file and an oversized one', async () => {
      await expect(service.upload(neighbor, 'o1', undefined)).rejects.toBeInstanceOf(BadRequestAppException);
      await expect(
        service.upload(neighbor, 'o1', { buffer: JPEG, size: 2 * 1024 * 1024 + 1 }),
      ).rejects.toBeInstanceOf(PayloadTooLargeAppException);
    });

    it('rejects a fourth photo before uploading anything (RN07)', async () => {
      photos.findByOrder.mockResolvedValue([photo, photo, photo]);
      const error = await service.upload(neighbor, 'o1', { buffer: JPEG, size: 7 }).catch((e: unknown) => e);
      expect((error as UnprocessableAppException).code).toBe(ErrorCode.PHOTO_LIMIT_REACHED);
      expect(storage.put).not.toHaveBeenCalled();
    });

    it('rejects when the order is no longer editable', async () => {
      acceptances.count.mockResolvedValue(1);
      const error = await service.upload(neighbor, 'o1', { buffer: JPEG, size: 7 }).catch((e: unknown) => e);
      expect(error).toBeInstanceOf(ConflictAppException);
      expect(storage.put).not.toHaveBeenCalled();
    });

    it('removes the uploaded object when the insert fails', async () => {
      photos.insert.mockRejectedValue(new Error('db down'));
      await expect(service.upload(neighbor, 'o1', { buffer: JPEG, size: 7 })).rejects.toThrow('db down');
      expect(storage.remove).toHaveBeenCalledWith((storage.put.mock.calls[0] as unknown[])[0]);
    });

    it('removes the uploaded object when a parallel upload took the last slot', async () => {
      photos.insert.mockResolvedValue(null);
      await expect(service.upload(neighbor, 'o1', { buffer: JPEG, size: 7 })).rejects.toBeInstanceOf(
        UnprocessableAppException,
      );
      expect(storage.remove).toHaveBeenCalledTimes(1);
    });
  });

  describe('signedUrlFor', () => {
    it('returns a short lived URL for the owner', async () => {
      access.load.mockResolvedValue({ order: order(), acceptance: null });
      photos.findOne.mockResolvedValue(photo);
      storage.signedUrl.mockResolvedValue('https://storage/o1/p1.jpg?sig=1');
      expect(await service.signedUrlFor(neighbor, 'o1', 'p1')).toBe('https://storage/o1/p1.jpg?sig=1');
      expect(storage.signedUrl).toHaveBeenCalledWith('o1/p1.jpg', 300);
    });

    it('hides photos of orders a recycler cannot see anymore', async () => {
      access.load.mockResolvedValue({ order: order(OrderStatus.CONFIRMED), acceptance: null });
      await expect(service.signedUrlFor(recycler, 'o1', 'p1')).rejects.toBeInstanceOf(NotFoundAppException);
    });

    it('lets a recycler with an acceptance see photos of a confirmed order', async () => {
      access.load.mockResolvedValue({
        order: order(OrderStatus.CONFIRMED),
        acceptance: {
          id: 'a1',
          orderId: 'o1',
          recyclerId: 'r1',
          status: AcceptanceStatus.CHOSEN,
          acceptedAt: new Date(),
        },
      });
      photos.findOne.mockResolvedValue(photo);
      storage.signedUrl.mockResolvedValue('url');
      await expect(service.signedUrlFor(recycler, 'o1', 'p1')).resolves.toBe('url');
    });

    it('returns 404 for a photo of another order', async () => {
      access.load.mockResolvedValue({ order: order(), acceptance: null });
      photos.findOne.mockResolvedValue(null);
      await expect(service.signedUrlFor(neighbor, 'o1', 'zzz')).rejects.toBeInstanceOf(NotFoundAppException);
    });
  });

  describe('remove', () => {
    it('deletes the row and then the object', async () => {
      photos.findOne.mockResolvedValue(photo);
      await service.remove(neighbor, 'o1', 'p1');
      expect(photos.delete).toHaveBeenCalledWith('p1');
      expect(storage.remove).toHaveBeenCalledWith('o1/p1.jpg');
    });

    it('does not fail when the bucket cannot delete the object', async () => {
      photos.findOne.mockResolvedValue(photo);
      storage.remove.mockRejectedValue(new Error('503 SlowDown'));
      await expect(service.remove(neighbor, 'o1', 'p1')).resolves.toBeUndefined();
    });

    it('returns 404 when the photo does not exist and 409 when the order is locked', async () => {
      photos.findOne.mockResolvedValue(null);
      await expect(service.remove(neighbor, 'o1', 'p1')).rejects.toBeInstanceOf(NotFoundAppException);
      acceptances.count.mockResolvedValue(2);
      await expect(service.remove(neighbor, 'o1', 'p1')).rejects.toBeInstanceOf(ConflictAppException);
    });
  });
});
