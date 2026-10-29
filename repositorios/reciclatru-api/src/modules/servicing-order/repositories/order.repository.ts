import { Injectable } from '@nestjs/common';
import type { OrderStatus } from '../../../common/domain/enums.js';
import { Database } from '../../../database/database.service.js';
import type { OrderItemData, OrderRow } from '../domain/order.types.js';

const ORDER_SELECT = `
  SELECT o.id, o.neighbor_id AS "neighborId", o.status, o.created_at AS "createdAt",
         l.district AS zone, l.address, l.latitude::float8 AS latitude, l.longitude::float8 AS longitude
    FROM pickup_order o
    JOIN location l ON l.id = o.pickup_location_id`;

/** Data access for orders, their items and their status history. */
@Injectable()
export class OrderRepository {
  constructor(private readonly db: Database) {}

  findById(orderId: string): Promise<OrderRow | null> {
    return this.db.queryOne<OrderRow>(`${ORDER_SELECT} WHERE o.id = $1`, [orderId]);
  }

  /** Same as `findById` but locks the order row until the transaction ends. */
  findByIdForUpdate(orderId: string): Promise<OrderRow | null> {
    return this.db.queryOne<OrderRow>(`${ORDER_SELECT} WHERE o.id = $1 FOR UPDATE OF o`, [orderId]);
  }

  /** Creates a PENDING order that copies the neighbor's current location. Returns null without a location. */
  async insert(neighborId: string): Promise<string | null> {
    const row = await this.db.queryOne<{ id: string }>(
      `INSERT INTO pickup_order (neighbor_id, pickup_location_id)
       SELECT $1, id FROM location WHERE user_id = $1 AND is_current
       RETURNING id`,
      [neighborId],
    );
    return row?.id ?? null;
  }

  async insertItems(orderId: string, items: OrderItemData[]): Promise<void> {
    for (const [index, item] of items.entries()) {
      await this.db.execute(
        'INSERT INTO order_item (order_id, description, weight_kg, position) VALUES ($1, $2, $3, $4)',
        [orderId, item.description, item.weightKg, index + 1],
      );
    }
  }

  async replaceItems(orderId: string, items: OrderItemData[]): Promise<void> {
    await this.db.execute('DELETE FROM order_item WHERE order_id = $1', [orderId]);
    await this.insertItems(orderId, items);
  }

  findItems(orderId: string): Promise<OrderItemData[]> {
    return this.db.query<OrderItemData>(
      `SELECT description, weight_kg::float8 AS "weightKg" FROM order_item WHERE order_id = $1 ORDER BY position, created_at, id`,
      [orderId],
    );
  }

  /** Moves the order to a final state; returns false when it was no longer in one of the allowed states. */
  async transition(
    orderId: string,
    from: OrderStatus[],
    to: OrderStatus,
    timestampColumn: 'collected_at' | 'cancelled_at',
  ): Promise<boolean> {
    const affected = await this.db.execute(
      `UPDATE pickup_order SET status = $3, ${timestampColumn} = now() WHERE id = $1 AND status = ANY($2::order_status_type[])`,
      [orderId, from, to],
    );
    return affected > 0;
  }

  async addHistory(
    orderId: string,
    from: OrderStatus | null,
    to: OrderStatus,
    changedBy: string,
  ): Promise<void> {
    await this.db.execute(
      'INSERT INTO order_status_history (order_id, from_status, to_status, changed_by) VALUES ($1, $2, $3, $4)',
      [orderId, from, to, changedBy],
    );
  }

  /** Distance in km between the order's pickup point and the given point. */
  async distanceKm(orderId: string, lat: number, lng: number): Promise<number | null> {
    const row = await this.db.queryOne<{ distance: number }>(
      `SELECT haversine_km(l.latitude::float8, l.longitude::float8, $2, $3) AS distance
         FROM pickup_order o JOIN location l ON l.id = o.pickup_location_id WHERE o.id = $1`,
      [orderId, lat, lng],
    );
    return row?.distance ?? null;
  }
}
