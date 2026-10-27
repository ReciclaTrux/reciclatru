import { describe, expect, it } from '@jest/globals';
import { AcceptanceLabel, AcceptanceStatus, OrderAction, OrderStatus } from '../../../common/domain/enums.js';
import type { AcceptanceRow, OrderRow, PhotoRow, SummaryRow } from '../domain/order.types.js';
import { OrderViewAssembler } from './order-view.assembler.js';

const assembler = new OrderViewAssembler();
const order = (status: OrderStatus): OrderRow => ({
  id: 'o1',
  neighborId: 'n1',
  status,
  createdAt: new Date('2026-10-26T14:05:00Z'),
  zone: 'Huanchaco',
  address: 'Av. Víctor Larco 1280',
  latitude: -8.077806,
  longitude: -79.122183,
});
const items = [
  { description: 'Botellas', weightKg: 4.5 },
  { description: 'Cartón', weightKg: 8 },
  { description: 'Latas', weightKg: 1.2 },
];
const photos: PhotoRow[] = [{ id: 'p1', orderId: 'o1', fileName: 'o1/p1.jpg', contentType: 'image/jpeg' }];
const acceptance = (status: AcceptanceStatus): AcceptanceRow => ({
  id: 'a1',
  orderId: 'o1',
  recyclerId: 'r1',
  status,
  acceptedAt: new Date(),
});
const neighborContact = { fullName: 'Rosa Elena', phone: '987654321' };

function recycler(status: OrderStatus, accepted: AcceptanceRow | null, distanceKm: number | null = null) {
  return assembler.recyclerRecord({
    order: order(status),
    items,
    photos,
    acceptanceCount: 2,
    acceptance: accepted,
    distanceKm,
    neighborContact,
    reference: 'Portón verde',
  });
}

describe('OrderViewAssembler', () => {
  it('builds stable photo URLs that never expose the object key', () => {
    const record = recycler(OrderStatus.PENDING, null);
    expect(record.photos).toEqual([{ id: 'p1', url: '/api/v1/servicing-order/o1/photo/p1/retrieval' }]);
    expect(JSON.stringify(record)).not.toContain('o1/p1.jpg');
  });

  describe('recycler without acceptance (RN13)', () => {
    it('sees items, zone and distance but no contact, address or reference', () => {
      const record = recycler(OrderStatus.PENDING, null, 3.449);
      expect(record).toMatchObject({
        totalWeightKg: 13.7,
        zone: 'Huanchaco',
        approximateDistanceKm: 3.4,
        alreadyAccepted: false,
        acceptanceStatus: null,
        counterpart: null,
        pickup: null,
        availableActions: [OrderAction.ACCEPT],
      });
      expect(JSON.stringify(record)).not.toMatch(/Larco|987654321|Portón|latitude/);
    });
  });

  describe('recycler with acceptance', () => {
    it('ACCEPTED on a PENDING order reveals the contact only', () => {
      const record = recycler(OrderStatus.PENDING, acceptance(AcceptanceStatus.ACCEPTED));
      expect(record.counterpart).toEqual(neighborContact);
      expect(record.pickup).toBeNull();
      expect(record.availableActions).toEqual([]);
    });

    it('CHOSEN on a CONFIRMED order reveals the address, reference and coordinates', () => {
      const record = recycler(OrderStatus.CONFIRMED, acceptance(AcceptanceStatus.CHOSEN));
      expect(record.counterpart).toEqual(neighborContact);
      expect(record.pickup).toEqual({
        address: 'Av. Víctor Larco 1280',
        reference: 'Portón verde',
        latitude: -8.077806,
        longitude: -79.122183,
      });
      expect(record.availableActions).toEqual([OrderAction.GO_PICKUP]);
    });

    it('DISCARDED recycler loses the contact and never sees the address', () => {
      const record = recycler(OrderStatus.CONFIRMED, acceptance(AcceptanceStatus.DISCARDED));
      expect(record.counterpart).toBeNull();
      expect(record.pickup).toBeNull();
    });

    it.each([OrderStatus.COLLECTED, OrderStatus.CANCELLED])(
      'hides contact and address once the order is %s',
      (status) => {
        const record = recycler(status, acceptance(AcceptanceStatus.CHOSEN));
        expect(record.counterpart).toBeNull();
        expect(record.pickup).toBeNull();
        expect(record.availableActions).toEqual([]);
      },
    );
  });

  describe('neighbor record', () => {
    it('exposes the chosen recycler only through counterpart and name', () => {
      const chosen = { fullName: 'Jorge Luis', phone: '944123456' };
      const record = assembler.neighborRecord({
        order: order(OrderStatus.CONFIRMED),
        items,
        photos,
        acceptanceCount: 2,
        chosenRecycler: chosen,
      });
      expect(record).toMatchObject({
        chosenRecyclerName: 'Jorge Luis',
        counterpart: chosen,
        availableActions: [OrderAction.MARK_COLLECTED, OrderAction.CANCEL],
      });
      expect(record).not.toHaveProperty('alreadyAccepted');
    });
  });

  describe('summaries', () => {
    const row = (status: OrderStatus): SummaryRow => ({
      id: 'o1',
      status,
      createdAt: new Date('2026-10-26T14:05:00Z'),
      createdAtRaw: '2026-10-26 14:05:00+00',
      zone: 'Huanchaco',
      itemCount: 3,
      totalWeightKg: 13.700000000000001,
      thumbnailPhotoId: 'p1',
      acceptanceCount: 2,
      chosenRecyclerName: 'Jorge Luis',
      alreadyAccepted: true,
      distanceKm: 3.449,
    });

    it('neighbor sees the count while PENDING and the recycler name while CONFIRMED', () => {
      expect(assembler.neighborSummary(row(OrderStatus.PENDING))).toMatchObject({
        acceptanceCount: 2,
        chosenRecyclerName: null,
        totalWeightKg: 13.7,
      });
      expect(assembler.neighborSummary(row(OrderStatus.CONFIRMED))).toMatchObject({
        acceptanceCount: null,
        chosenRecyclerName: 'Jorge Luis',
      });
      expect(assembler.neighborSummary(row(OrderStatus.PENDING)).thumbnailUrl).toBe(
        '/api/v1/servicing-order/o1/photo/p1/retrieval',
      );
    });

    it('recycler sees distance and alreadyAccepted but never names or counts', () => {
      const summary = assembler.recyclerSummary(row(OrderStatus.PENDING));
      expect(summary).toMatchObject({ approximateDistanceKm: 3.4, alreadyAccepted: true });
      expect(summary).not.toHaveProperty('chosenRecyclerName');
      expect(summary).not.toHaveProperty('acceptanceCount');
    });

    it('a missing photo gives a null thumbnail', () => {
      expect(
        assembler.neighborSummary({ ...row(OrderStatus.PENDING), thumbnailPhotoId: null }).thumbnailUrl,
      ).toBeNull();
    });
  });

  it('myAcceptance keeps only what the recycler list needs', () => {
    const view = assembler.myAcceptance({
      acceptanceId: 'a1',
      orderId: 'o1',
      label: AcceptanceLabel.CHOSEN,
      rank: 0,
      zone: 'Huanchaco',
      totalWeightKg: 11.5,
      acceptedAt: new Date('2026-10-23T18:40:00Z'),
      acceptedAtRaw: 'raw',
    });
    expect(view).toEqual({
      orderId: 'o1',
      acceptanceLabel: 'CHOSEN',
      zone: 'Huanchaco',
      totalWeightKg: 11.5,
      acceptedAt: '2026-10-23T18:40:00.000Z',
    });
  });
});
