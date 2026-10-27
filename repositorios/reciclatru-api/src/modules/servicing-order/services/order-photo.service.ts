import { randomUUID } from 'node:crypto';
import { Inject, Injectable, Logger } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { OrderStatus, PartyRole } from '../../../common/domain/enums.js';
import {
  BadRequestAppException,
  ConflictAppException,
  NotFoundAppException,
  PayloadTooLargeAppException,
  UnprocessableAppException,
} from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import type { AuthenticatedUser } from '../../../common/utils/request-context.js';
import { appConfig } from '../../../config/app.config.js';
import { PhotoStorage } from '../../photo-storage/photo-storage.js';
import { MAX_PHOTOS, OrderRules } from '../domain/order.rules.js';
import { AcceptanceRepository } from '../repositories/acceptance.repository.js';
import { PhotoRepository } from '../repositories/photo.repository.js';
import { OrderViewAssembler } from '../views/order-view.assembler.js';
import { OrderAccessService } from './order-access.service.js';

export interface UploadedPhoto {
  buffer: Buffer;
  size: number;
}

interface ImageKind {
  contentType: string;
  extension: string;
}

const JPEG_SIGNATURE = [0xff, 0xd8, 0xff];
const PNG_SIGNATURE = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a];

/** Photo upload, signed retrieval and removal (HU-09). The database only keeps the object key. */
@Injectable()
export class OrderPhotoService {
  private readonly logger = new Logger(OrderPhotoService.name);

  constructor(
    private readonly access: OrderAccessService,
    private readonly photos: PhotoRepository,
    private readonly acceptances: AcceptanceRepository,
    private readonly storage: PhotoStorage,
    private readonly assembler: OrderViewAssembler,
    @Inject(appConfig.KEY) private readonly config: ConfigType<typeof appConfig>,
  ) {}

  /** Uploads to the bucket first and inserts the row second; a failed insert removes the object. */
  async upload(user: AuthenticatedUser, orderId: string, file?: UploadedPhoto) {
    if (!file)
      throw new BadRequestAppException('Falta el archivo.', [
        { field: 'file', message: 'file es obligatorio' },
      ]);
    if (file.size > this.config.photos.maxBytes) throw new PayloadTooLargeAppException();
    const kind = this.detectKind(file.buffer);
    if (!kind)
      throw new UnprocessableAppException(
        ErrorCode.PHOTO_INVALID_TYPE,
        'Solo se aceptan imágenes JPEG o PNG.',
      );

    await this.assertEditable(user, orderId);
    if ((await this.photos.findByOrder(orderId)).length >= MAX_PHOTOS) {
      throw new UnprocessableAppException(ErrorCode.PHOTO_LIMIT_REACHED);
    }

    const photoId = randomUUID();
    const key = `${orderId}/${photoId}.${kind.extension}`;
    await this.storage.put(key, file.buffer, kind.contentType);
    try {
      const saved = await this.photos.insert(photoId, orderId, key, kind.contentType, file.size);
      if (!saved) throw new UnprocessableAppException(ErrorCode.PHOTO_LIMIT_REACHED);
    } catch (error) {
      await this.removeQuietly(key);
      throw error;
    }
    return { id: photoId, url: this.assembler.photoUrl(orderId, photoId) };
  }

  /** Returns a short-lived signed URL for a photo the caller is allowed to see. */
  async signedUrlFor(user: AuthenticatedUser, orderId: string, photoId: string): Promise<string> {
    const { order, acceptance } = await this.access.load(user, orderId);
    if (user.role === PartyRole.RECYCLER && !acceptance && order.status !== OrderStatus.PENDING)
      throw new NotFoundAppException();
    const photo = await this.photos.findOne(orderId, photoId);
    if (!photo) throw new NotFoundAppException();
    return this.storage.signedUrl(photo.fileName, this.config.photos.urlTtlSeconds);
  }

  async remove(user: AuthenticatedUser, orderId: string, photoId: string): Promise<void> {
    await this.assertEditable(user, orderId);
    const photo = await this.photos.findOne(orderId, photoId);
    if (!photo) throw new NotFoundAppException();
    await this.photos.delete(photo.id);
    await this.removeQuietly(photo.fileName);
  }

  private async assertEditable(user: AuthenticatedUser, orderId: string): Promise<void> {
    const order = await this.access.loadOwned(user, orderId);
    if (!OrderRules.isEditable(order.status, await this.acceptances.count(orderId))) {
      throw new ConflictAppException(ErrorCode.ORDER_NOT_EDITABLE);
    }
  }

  /** Looks at the first bytes, not at the file name or the declared content type. */
  private detectKind(buffer: Buffer): ImageKind | null {
    if (this.startsWith(buffer, JPEG_SIGNATURE)) return { contentType: 'image/jpeg', extension: 'jpg' };
    if (this.startsWith(buffer, PNG_SIGNATURE)) return { contentType: 'image/png', extension: 'png' };
    return null;
  }

  private startsWith(buffer: Buffer, signature: number[]): boolean {
    return buffer.length >= signature.length && signature.every((byte, index) => buffer[index] === byte);
  }

  private async removeQuietly(key: string): Promise<void> {
    try {
      await this.storage.remove(key);
    } catch (error) {
      this.logger.warn(`Could not remove object ${key}: ${(error as Error).message}`);
    }
  }
}
