import { Injectable } from '@nestjs/common';
import { Database } from '../../../database/database.service.js';
import type { AcceptanceRow } from '../domain/order.types.js';

const ACCEPTANCE_COLUMNS = `id, order_id AS "orderId", recycler_id AS "recyclerId", status, accepted_at AS "acceptedAt"`;

/** Data access for recycler acceptances. */
@Injectable()
export class AcceptanceRepository {
  constructor(private readonly db: Database) {}

  find(orderId: string, recyclerId: string): Promise<AcceptanceRow | null> {
    return this.db.queryOne<AcceptanceRow>(
      `SELECT ${ACCEPTANCE_COLUMNS} FROM order_acceptance WHERE order_id = $1 AND recycler_id = $2`,
      [orderId, recyclerId],
    );
  }

  /** Inserts the acceptance. The database trigger rejects it when the order is no longer PENDING. */
  async insert(orderId: string, recyclerId: string): Promise<boolean> {
    const affected = await this.db.execute(
      'INSERT INTO order_acceptance (order_id, recycler_id) VALUES ($1, $2) ON CONFLICT (order_id, recycler_id) DO NOTHING',
      [orderId, recyclerId],
    );
    return affected > 0;
  }

  count(orderId: string): Promise<number> {
    return this.db
      .queryOne<{ total: number }>(
        'SELECT count(*)::int AS total FROM order_acceptance WHERE order_id = $1',
        [orderId],
      )
      .then((row) => row?.total ?? 0);
  }

  listByOrder(orderId: string): Promise<AcceptanceRow[]> {
    return this.db.query<AcceptanceRow>(
      `SELECT ${ACCEPTANCE_COLUMNS} FROM order_acceptance WHERE order_id = $1 ORDER BY accepted_at, id`,
      [orderId],
    );
  }

  findChosen(orderId: string): Promise<AcceptanceRow | null> {
    return this.db.queryOne<AcceptanceRow>(
      `SELECT ${ACCEPTANCE_COLUMNS} FROM order_acceptance WHERE order_id = $1 AND status = 'CHOSEN'`,
      [orderId],
    );
  }

  /** Runs the atomic database function that confirms the order and picks the recycler (RN12). */
  async choose(orderId: string, acceptanceId: string, neighborId: string): Promise<void> {
    await this.db.query('SELECT confirm_pickup_order($1, $2, $3)', [orderId, acceptanceId, neighborId]);
  }
}
