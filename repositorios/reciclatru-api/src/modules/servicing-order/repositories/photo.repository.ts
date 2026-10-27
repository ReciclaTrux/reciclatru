import { Injectable } from '@nestjs/common';
import { Database } from '../../../database/database.service.js';
import type { PhotoRow } from '../domain/order.types.js';

const PHOTO_COLUMNS = `id, order_id AS "orderId", file_name AS "fileName", content_type AS "contentType"`;

/** Data access for `order_photo`; `file_name` holds the object key in the bucket. */
@Injectable()
export class PhotoRepository {
  constructor(private readonly db: Database) {}

  findByOrder(orderId: string): Promise<PhotoRow[]> {
    return this.db.query<PhotoRow>(
      `SELECT ${PHOTO_COLUMNS} FROM order_photo WHERE order_id = $1 ORDER BY sort_order`,
      [orderId],
    );
  }

  findOne(orderId: string, photoId: string): Promise<PhotoRow | null> {
    return this.db.queryOne<PhotoRow>(
      `SELECT ${PHOTO_COLUMNS} FROM order_photo WHERE order_id = $1 AND id = $2`,
      [orderId, photoId],
    );
  }

  /** Inserts using the lowest free position (1 to 3); returns null when the three are taken. */
  insert(
    photoId: string,
    orderId: string,
    key: string,
    contentType: string,
    sizeBytes: number,
  ): Promise<PhotoRow | null> {
    return this.db.queryOne<PhotoRow>(
      `INSERT INTO order_photo (id, order_id, file_name, sort_order, content_type, size_bytes)
       SELECT $1, $2, $3, slot, $4, $5
         FROM generate_series(1, 3) AS slot
        WHERE NOT EXISTS (SELECT 1 FROM order_photo WHERE order_id = $2 AND sort_order = slot)
        ORDER BY slot LIMIT 1
       RETURNING ${PHOTO_COLUMNS}`,
      [photoId, orderId, key, contentType, sizeBytes],
    );
  }

  async delete(photoId: string): Promise<void> {
    await this.db.execute('DELETE FROM order_photo WHERE id = $1', [photoId]);
  }
}
