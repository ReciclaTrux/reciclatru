import { Injectable } from '@nestjs/common';
import { Database } from '../../database/database.service.js';
import type { PickupPoint } from './pickup-point.js';

/** Data access for the immutable pickup-point snapshots (RN18). */
@Injectable()
export class LocationRepository {
  constructor(private readonly db: Database) {}

  findCurrent(userId: string): Promise<PickupPoint | null> {
    return this.db.queryOne<PickupPoint>(
      `SELECT latitude::float8 AS latitude, longitude::float8 AS longitude, address, district
         FROM location WHERE user_id = $1 AND is_current`,
      [userId],
    );
  }

  /** Retires the current snapshot and stores the new one as current. */
  async replaceCurrent(userId: string, point: PickupPoint): Promise<void> {
    await this.db.transaction(async () => {
      await this.db.execute('UPDATE location SET is_current = false WHERE user_id = $1 AND is_current', [
        userId,
      ]);
      await this.db.execute(
        `INSERT INTO location (user_id, latitude, longitude, address, district) VALUES ($1, $2, $3, $4, $5)`,
        [userId, point.latitude, point.longitude, point.address, point.district],
      );
    });
  }
}
