import { Injectable } from '@nestjs/common';
import { AcceptanceLabel, OrderStatus, type ListTab } from '../../../common/domain/enums.js';
import type { GeoPoint } from '../../../common/geo/geo-point.js';
import { Database } from '../../../database/database.service.js';
import type { MyAcceptanceRow, SummaryRow } from '../domain/order.types.js';

/** Keyset position of the last row of a list: timestamp text, id and, for availability, the distance. */
export interface ListCursor {
  c?: string;
  i: string;
  d?: number;
  r?: number;
}

const SUMMARY_COLUMNS = `
  o.id, o.status, o.created_at AS "createdAt", o.created_at::text AS "createdAtRaw", l.district AS zone,
  (SELECT count(*)::int FROM order_item i WHERE i.order_id = o.id) AS "itemCount",
  (SELECT COALESCE(sum(i.weight_kg), 0)::float8 FROM order_item i WHERE i.order_id = o.id) AS "totalWeightKg",
  (SELECT p.id FROM order_photo p WHERE p.order_id = o.id ORDER BY p.sort_order LIMIT 1) AS "thumbnailPhotoId",
  (SELECT count(*)::int FROM order_acceptance a WHERE a.order_id = o.id) AS "acceptanceCount",
  (SELECT up.full_name FROM order_acceptance a JOIN user_profile up ON up.user_id = a.recycler_id
    WHERE a.order_id = o.id AND a.status = 'CHOSEN') AS "chosenRecyclerName"`;

const TAB_STATUSES: Record<ListTab, OrderStatus[]> = {
  ACTIVE: [OrderStatus.PENDING, OrderStatus.CONFIRMED],
  HISTORY: [OrderStatus.COLLECTED, OrderStatus.CANCELLED],
};

const TAB_LABELS: Record<ListTab, AcceptanceLabel[]> = {
  ACTIVE: [AcceptanceLabel.AWAITING_CONFIRMATION, AcceptanceLabel.CHOSEN],
  HISTORY: [AcceptanceLabel.NOT_CHOSEN, AcceptanceLabel.CANCELLED_BY_NEIGHBOR, AcceptanceLabel.COLLECTED],
};

/** Paginated read models. All lists use keyset pagination and fetch one extra row to know if there is a next page. */
@Injectable()
export class OrderListRepository {
  constructor(private readonly db: Database) {}

  /** HU-06: the neighbor's own orders, newest first. */
  findByNeighbor(
    neighborId: string,
    tab: ListTab,
    limit: number,
    cursor: ListCursor | null,
  ): Promise<SummaryRow[]> {
    return this.db.query<SummaryRow>(
      `SELECT * FROM (
         SELECT ${SUMMARY_COLUMNS}, false AS "alreadyAccepted", NULL::float8 AS "distanceKm"
           FROM pickup_order o JOIN location l ON l.id = o.pickup_location_id
          WHERE o.neighbor_id = $1 AND o.status = ANY($2::order_status_type[])
       ) t
       WHERE ($3::timestamptz IS NULL OR ("createdAt", id) < ($3::timestamptz, $4::uuid))
       ORDER BY "createdAt" DESC, id DESC
       LIMIT $5`,
      [neighborId, TAB_STATUSES[tab], cursor?.c ?? null, cursor?.i ?? null, limit + 1],
    );
  }

  /** HU-10: PENDING orders for a recycler, nearest first when a point is given, newest first otherwise. */
  findAvailable(
    recyclerId: string,
    point: GeoPoint | null,
    limit: number,
    cursor: ListCursor | null,
  ): Promise<SummaryRow[]> {
    return point
      ? this.findAvailableByDistance(recyclerId, point, limit, cursor)
      : this.findAvailableByDate(recyclerId, limit, cursor);
  }

  private findAvailableByDistance(
    recyclerId: string,
    point: GeoPoint,
    limit: number,
    cursor: ListCursor | null,
  ): Promise<SummaryRow[]> {
    return this.db.query<SummaryRow>(
      `SELECT * FROM (
         SELECT ${SUMMARY_COLUMNS},
                round(haversine_km(l.latitude::float8, l.longitude::float8, $2::float8, $3::float8)::numeric, 3)::float8 AS "distanceKm",
                EXISTS (SELECT 1 FROM order_acceptance x WHERE x.order_id = o.id AND x.recycler_id = $1) AS "alreadyAccepted"
           FROM pickup_order o JOIN location l ON l.id = o.pickup_location_id
          WHERE o.status = 'PENDING'
       ) t
       WHERE ($4::float8 IS NULL OR "distanceKm" > $4::float8 OR ("distanceKm" = $4::float8 AND id > $5::uuid))
       ORDER BY "distanceKm" ASC, id ASC
       LIMIT $6`,
      [recyclerId, point.lat, point.lng, cursor?.d ?? null, cursor?.i ?? null, limit + 1],
    );
  }

  private findAvailableByDate(
    recyclerId: string,
    limit: number,
    cursor: ListCursor | null,
  ): Promise<SummaryRow[]> {
    return this.db.query<SummaryRow>(
      `SELECT * FROM (
         SELECT ${SUMMARY_COLUMNS}, NULL::float8 AS "distanceKm",
                EXISTS (SELECT 1 FROM order_acceptance x WHERE x.order_id = o.id AND x.recycler_id = $1) AS "alreadyAccepted"
           FROM pickup_order o JOIN location l ON l.id = o.pickup_location_id
          WHERE o.status = 'PENDING'
       ) t
       WHERE ($2::timestamptz IS NULL OR ("createdAt", id) < ($2::timestamptz, $3::uuid))
       ORDER BY "createdAt" DESC, id DESC
       LIMIT $4`,
      [recyclerId, cursor?.c ?? null, cursor?.i ?? null, limit + 1],
    );
  }

  /** HU-14: the recycler's acceptances with the label that combines acceptance and order state. */
  findAcceptancesByRecycler(
    recyclerId: string,
    tab: ListTab,
    limit: number,
    cursor: ListCursor | null,
  ): Promise<MyAcceptanceRow[]> {
    return this.db.query<MyAcceptanceRow>(
      `SELECT * FROM (
         SELECT a.id AS "acceptanceId", o.id AS "orderId", l.district AS zone,
                a.accepted_at AS "acceptedAt", a.accepted_at::text AS "acceptedAtRaw",
                (SELECT COALESCE(sum(i.weight_kg), 0)::float8 FROM order_item i WHERE i.order_id = o.id) AS "totalWeightKg",
                CASE WHEN a.status = 'DISCARDED' THEN 'NOT_CHOSEN'
                     WHEN o.status = 'CANCELLED' THEN 'CANCELLED_BY_NEIGHBOR'
                     WHEN o.status = 'COLLECTED' THEN 'COLLECTED'
                     WHEN a.status = 'CHOSEN' THEN 'CHOSEN'
                     ELSE 'AWAITING_CONFIRMATION' END AS label
           FROM order_acceptance a
           JOIN pickup_order o ON o.id = a.order_id
           JOIN location l ON l.id = o.pickup_location_id
          WHERE a.recycler_id = $1
       ) t,
       LATERAL (SELECT CASE WHEN t.label = 'CHOSEN' THEN 0 ELSE 1 END AS rank) k
       WHERE t.label = ANY($2::text[])
         AND ($3::int IS NULL OR k.rank > $3::int
              OR (k.rank = $3::int AND (t."acceptedAt" < $4::timestamptz
                  OR (t."acceptedAt" = $4::timestamptz AND t."acceptanceId" < $5::uuid))))
       ORDER BY k.rank, t."acceptedAt" DESC, t."acceptanceId" DESC
       LIMIT $6`,
      [recyclerId, TAB_LABELS[tab], cursor?.r ?? null, cursor?.c ?? null, cursor?.i ?? null, limit + 1],
    );
  }
}
