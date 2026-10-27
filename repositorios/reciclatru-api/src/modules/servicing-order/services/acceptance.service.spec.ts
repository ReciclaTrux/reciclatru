import { beforeEach, describe, expect, it } from '@jest/globals';
import { stubOf } from '../../../../test/support/stubs.js';
import { AcceptanceStatus, OrderStatus, PartyRole } from '../../../common/domain/enums.js';
import {
  ConflictAppException,
  NotFoundAppException,
  UnprocessableAppException,
} from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import { PartyService } from '../../party-data-management/party.service.js';
import type { AcceptanceRow, OrderRow } from '../domain/order.types.js';
import { AcceptanceRepository } from '../repositories/acceptance.repository.js';
import { OrderRepository } from '../repositories/order.repository.js';
import { AcceptanceService } from './acceptance.service.js';
import { OrderAccessService } from './order-access.service.js';
import { OrderQueryService } from './order-query.service.js';

const recycler = { id: 'r1', role: PartyRole.RECYCLER };
const order = (status: OrderStatus): OrderRow => ({
  id: 'o1',
  neighborId: 'n1',
  status,
  createdAt: new Date(),
  zone: null,
  address: null,
  latitude: 0,
  longitude: 0,
});
const accepted: AcceptanceRow = {
  id: 'a1',
  orderId: 'o1',
  recyclerId: 'r1',
  status: AcceptanceStatus.ACCEPTED,
  acceptedAt: new Date('2026-10-26T15:10:00Z'),
};

describe('AcceptanceService', () => {
  let access: ReturnType<typeof stubOf<OrderAccessService>>;
  let orders: ReturnType<typeof stubOf<OrderRepository>>;
  let acceptances: ReturnType<typeof stubOf<AcceptanceRepository>>;
  let parties: ReturnType<typeof stubOf<PartyService>>;
  let query: ReturnType<typeof stubOf<OrderQueryService>>;
  let service: AcceptanceService;

  beforeEach(() => {
    access = stubOf<OrderAccessService>('load', 'loadOwned');
    orders = stubOf<OrderRepository>('findById');
    acceptances = stubOf<AcceptanceRepository>('find', 'insert', 'listByOrder');
    parties = stubOf<PartyService>('isProfileComplete', 'getContacts');
    query = stubOf<OrderQueryService>('present');
    query.present.mockResolvedValue({ id: 'o1' });
    access.load.mockResolvedValue({ order: order(OrderStatus.PENDING), acceptance: accepted });
    service = new AcceptanceService(access, orders, acceptances, parties, query);
  });

  describe('accept', () => {
    it('creates the acceptance and reports it as new', async () => {
      orders.findById.mockResolvedValue(order(OrderStatus.PENDING));
      acceptances.find.mockResolvedValue(null);
      parties.isProfileComplete.mockResolvedValue(true);
      acceptances.insert.mockResolvedValue(true);
      expect(await service.accept(recycler, 'o1', null)).toEqual({ created: true, record: { id: 'o1' } });
    });

    it('is idempotent: a second tap returns the existing acceptance without inserting', async () => {
      orders.findById.mockResolvedValue(order(OrderStatus.CONFIRMED));
      acceptances.find.mockResolvedValue(accepted);
      const result = await service.accept(recycler, 'o1', null);
      expect(result.created).toBe(false);
      expect(acceptances.insert).not.toHaveBeenCalled();
    });

    it('reports created=false when a parallel request inserted first', async () => {
      orders.findById.mockResolvedValue(order(OrderStatus.PENDING));
      acceptances.find.mockResolvedValue(null);
      parties.isProfileComplete.mockResolvedValue(true);
      acceptances.insert.mockResolvedValue(false);
      expect((await service.accept(recycler, 'o1', null)).created).toBe(false);
    });

    it('requires a complete profile', async () => {
      orders.findById.mockResolvedValue(order(OrderStatus.PENDING));
      acceptances.find.mockResolvedValue(null);
      parties.isProfileComplete.mockResolvedValue(false);
      const error = await service.accept(recycler, 'o1', null).catch((e: unknown) => e);
      expect(error).toBeInstanceOf(UnprocessableAppException);
      expect((error as UnprocessableAppException).code).toBe(ErrorCode.PROFILE_INCOMPLETE);
    });

    it.each([OrderStatus.CONFIRMED, OrderStatus.COLLECTED, OrderStatus.CANCELLED])(
      'rejects an order already %s with ORDER_NOT_AVAILABLE',
      async (status) => {
        orders.findById.mockResolvedValue(order(status));
        acceptances.find.mockResolvedValue(null);
        parties.isProfileComplete.mockResolvedValue(true);
        const error = await service.accept(recycler, 'o1', null).catch((e: unknown) => e);
        expect(error).toBeInstanceOf(ConflictAppException);
        expect((error as ConflictAppException).code).toBe(ErrorCode.ORDER_NOT_AVAILABLE);
      },
    );

    it('returns 404 for an unknown order', async () => {
      orders.findById.mockResolvedValue(null);
      await expect(service.accept(recycler, 'o1', null)).rejects.toBeInstanceOf(NotFoundAppException);
    });
  });

  describe('listForOwner', () => {
    it('joins each acceptance with the real contact data and keeps the order', async () => {
      acceptances.listByOrder.mockResolvedValue([
        accepted,
        { ...accepted, id: 'a2', recyclerId: 'r2' },
        { ...accepted, id: 'a3', recyclerId: 'r3' },
      ]);
      parties.getContacts.mockResolvedValue(
        new Map([
          ['r1', { fullName: 'Jorge', phone: '944123456' }],
          ['r2', { fullName: 'Marleni', phone: '955876543' }],
        ]),
      );
      const { items } = await service.listForOwner({ id: 'n1', role: PartyRole.NEIGHBOR }, 'o1');
      expect(items.map((i) => i.fullName)).toEqual(['Jorge', 'Marleni']);
      expect(items[0]).toEqual({
        id: 'a1',
        status: 'ACCEPTED',
        fullName: 'Jorge',
        phone: '944123456',
        acceptedAt: '2026-10-26T15:10:00.000Z',
      });
    });
  });
});
