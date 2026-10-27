import { beforeEach, describe, expect, it } from '@jest/globals';
import { stubOf } from '../../../../test/support/stubs.js';
import { OrderStatus, PartyRole } from '../../../common/domain/enums.js';
import {
  ConflictAppException,
  ForbiddenAppException,
  UnprocessableAppException,
} from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import { Database } from '../../../database/database.service.js';
import { PartyService } from '../../party-data-management/party.service.js';
import type { OrderRow } from '../domain/order.types.js';
import { AcceptanceRepository } from '../repositories/acceptance.repository.js';
import { OrderRepository } from '../repositories/order.repository.js';
import { OrderAccessService } from './order-access.service.js';
import { OrderCommandService } from './order-command.service.js';
import { OrderQueryService } from './order-query.service.js';

const neighbor = { id: 'n1', role: PartyRole.NEIGHBOR };
const items = [{ description: 'Botellas', weightKg: 4.5 }];
const order = (status: OrderStatus): OrderRow => ({
  id: 'o1',
  neighborId: 'n1',
  status,
  createdAt: new Date(),
  zone: 'Huanchaco',
  address: null,
  latitude: 0,
  longitude: 0,
});

describe('OrderCommandService', () => {
  let db: { transaction: <T>(work: () => Promise<T>) => Promise<T> };
  let access: ReturnType<typeof stubOf<OrderAccessService>>;
  let orders: ReturnType<typeof stubOf<OrderRepository>>;
  let acceptances: ReturnType<typeof stubOf<AcceptanceRepository>>;
  let parties: ReturnType<typeof stubOf<PartyService>>;
  let query: ReturnType<typeof stubOf<OrderQueryService>>;
  let service: OrderCommandService;

  beforeEach(() => {
    db = { transaction: (work) => work() };
    access = stubOf<OrderAccessService>('load', 'loadOwned');
    orders = stubOf<OrderRepository>('insert', 'insertItems', 'replaceItems', 'transition', 'addHistory');
    acceptances = stubOf<AcceptanceRepository>('count', 'choose');
    parties = stubOf<PartyService>('isProfileComplete');
    query = stubOf<OrderQueryService>('present');
    access.load.mockResolvedValue({ order: order(OrderStatus.PENDING), acceptance: null });
    query.present.mockResolvedValue({ id: 'o1' });
    service = new OrderCommandService(db as unknown as Database, access, orders, acceptances, parties, query);
  });

  describe('create', () => {
    it('inserts order, items and history in one transaction and returns the owner view', async () => {
      parties.isProfileComplete.mockResolvedValue(true);
      orders.insert.mockResolvedValue('o1');
      expect(await service.create(neighbor, items)).toEqual({ id: 'o1' });
      expect(orders.insertItems).toHaveBeenCalledWith('o1', items);
      expect(orders.addHistory).toHaveBeenCalledWith('o1', null, OrderStatus.PENDING, 'n1');
    });

    it('validates the items before touching the database', async () => {
      await expect(service.create(neighbor, [])).rejects.toBeInstanceOf(UnprocessableAppException);
      expect(parties.isProfileComplete).not.toHaveBeenCalled();
    });

    it('requires a complete profile (RN20)', async () => {
      parties.isProfileComplete.mockResolvedValue(false);
      const error = await service.create(neighbor, items).catch((e: unknown) => e);
      expect((error as UnprocessableAppException).code).toBe(ErrorCode.PROFILE_INCOMPLETE);
      expect(orders.insert).not.toHaveBeenCalled();
    });

    it('fails when the location vanished between the check and the insert', async () => {
      parties.isProfileComplete.mockResolvedValue(true);
      orders.insert.mockResolvedValue(null);
      await expect(service.create(neighbor, items)).rejects.toBeInstanceOf(UnprocessableAppException);
      expect(orders.insertItems).not.toHaveBeenCalled();
    });
  });

  describe('update (RN11)', () => {
    it('replaces the items when PENDING and without acceptances', async () => {
      access.loadOwned.mockResolvedValue(order(OrderStatus.PENDING));
      acceptances.count.mockResolvedValue(0);
      await service.update(neighbor, 'o1', items);
      expect(access.loadOwned).toHaveBeenCalledWith(neighbor, 'o1', true);
      expect(orders.replaceItems).toHaveBeenCalledWith('o1', items);
    });

    it.each([
      [OrderStatus.PENDING, 1],
      [OrderStatus.CONFIRMED, 0],
    ])('rejects %s with %i acceptances as ORDER_NOT_EDITABLE', async (status, count) => {
      access.loadOwned.mockResolvedValue(order(status));
      acceptances.count.mockResolvedValue(count);
      const error = await service.update(neighbor, 'o1', items).catch((e: unknown) => e);
      expect(error).toBeInstanceOf(ConflictAppException);
      expect((error as ConflictAppException).code).toBe(ErrorCode.ORDER_NOT_EDITABLE);
      expect(orders.replaceItems).not.toHaveBeenCalled();
    });
  });

  describe('cancel (RN16)', () => {
    it.each([OrderStatus.PENDING, OrderStatus.CONFIRMED])(
      'cancels from %s and records the history',
      async (status) => {
        access.loadOwned.mockResolvedValue(order(status));
        await service.cancel(neighbor, 'o1');
        expect(orders.transition).toHaveBeenCalledWith('o1', [status], OrderStatus.CANCELLED, 'cancelled_at');
        expect(orders.addHistory).toHaveBeenCalledWith('o1', status, OrderStatus.CANCELLED, 'n1');
      },
    );

    it.each([OrderStatus.COLLECTED, OrderStatus.CANCELLED])(
      'rejects %s as ORDER_NOT_CANCELLABLE',
      async (status) => {
        access.loadOwned.mockResolvedValue(order(status));
        const error = await service.cancel(neighbor, 'o1').catch((e: unknown) => e);
        expect((error as ConflictAppException).code).toBe(ErrorCode.ORDER_NOT_CANCELLABLE);
        expect(orders.transition).not.toHaveBeenCalled();
      },
    );
  });

  describe('markCollected (RN15)', () => {
    it('moves CONFIRMED to COLLECTED', async () => {
      access.loadOwned.mockResolvedValue(order(OrderStatus.CONFIRMED));
      await service.markCollected(neighbor, 'o1');
      expect(orders.transition).toHaveBeenCalledWith(
        'o1',
        [OrderStatus.CONFIRMED],
        OrderStatus.COLLECTED,
        'collected_at',
      );
    });

    it.each([OrderStatus.PENDING, OrderStatus.COLLECTED, OrderStatus.CANCELLED])(
      'rejects %s as ORDER_NOT_CONFIRMED',
      async (status) => {
        access.loadOwned.mockResolvedValue(order(status));
        const error = await service.markCollected(neighbor, 'o1').catch((e: unknown) => e);
        expect((error as ConflictAppException).code).toBe(ErrorCode.ORDER_NOT_CONFIRMED);
      },
    );
  });

  describe('chooseRecycler (RN12)', () => {
    it('delegates the atomic choice to the database function after checking ownership', async () => {
      access.loadOwned.mockResolvedValue(order(OrderStatus.PENDING));
      await service.chooseRecycler(neighbor, 'o1', 'a1');
      expect(acceptances.choose).toHaveBeenCalledWith('o1', 'a1', 'n1');
    });

    it('does not choose when the caller does not own the order', async () => {
      access.loadOwned.mockRejectedValue(new ForbiddenAppException(ErrorCode.NOT_ORDER_OWNER));
      await expect(service.chooseRecycler(neighbor, 'o1', 'a1')).rejects.toBeInstanceOf(
        ForbiddenAppException,
      );
      expect(acceptances.choose).not.toHaveBeenCalled();
    });
  });
});
