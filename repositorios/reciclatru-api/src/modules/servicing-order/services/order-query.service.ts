import { Injectable } from '@nestjs/common';
import { AcceptanceStatus, ListTab, OrderStatus, PartyRole } from '../../../common/domain/enums.js';
import { ConflictAppException } from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import type { GeoPoint } from '../../../common/geo/geo-point.js';
import { CursorCodec, toPage, type Page } from '../../../common/pagination/cursor.codec.js';
import type { AuthenticatedUser } from '../../../common/utils/request-context.js';
import { PartyService } from '../../party-data-management/party.service.js';
import type { Counterpart, ServicingOrderRecord, ServicingOrderSummary } from '../domain/order.types.js';
import { AcceptanceRepository } from '../repositories/acceptance.repository.js';
import { OrderListRepository, type ListCursor } from '../repositories/order-list.repository.js';
import { OrderRepository } from '../repositories/order.repository.js';
import { PhotoRepository } from '../repositories/photo.repository.js';
import { OrderViewAssembler } from '../views/order-view.assembler.js';
import { OrderAccessService, type OrderAccess } from './order-access.service.js';

/** Read side of the orders: detail with role-based visibility, and the three paginated lists. */
@Injectable()
export class OrderQueryService {
  constructor(
    private readonly access: OrderAccessService,
    private readonly orders: OrderRepository,
    private readonly photos: PhotoRepository,
    private readonly acceptances: AcceptanceRepository,
    private readonly lists: OrderListRepository,
    private readonly parties: PartyService,
    private readonly assembler: OrderViewAssembler,
  ) {}

  /** HU-06, HU-11, HU-15: a recycler with no acceptance cannot open an order that is no longer PENDING. */
  async getDetail(
    user: AuthenticatedUser,
    orderId: string,
    point: GeoPoint | null,
  ): Promise<ServicingOrderRecord> {
    const loaded = await this.access.load(user, orderId);
    if (
      user.role === PartyRole.RECYCLER &&
      !loaded.acceptance &&
      loaded.order.status !== OrderStatus.PENDING
    ) {
      throw new ConflictAppException(ErrorCode.ORDER_NOT_AVAILABLE);
    }
    return this.present(user, loaded, point);
  }

  /** Builds the role-specific record of an already loaded order. */
  async present(
    user: AuthenticatedUser,
    loaded: OrderAccess,
    point: GeoPoint | null = null,
  ): Promise<ServicingOrderRecord> {
    const { order } = loaded;
    const [items, photos, acceptanceCount] = await Promise.all([
      this.orders.findItems(order.id),
      this.photos.findByOrder(order.id),
      this.acceptances.count(order.id),
    ]);
    if (user.role === PartyRole.NEIGHBOR) {
      const chosenRecycler =
        order.status === OrderStatus.CONFIRMED ? await this.chosenRecycler(order.id) : null;
      return this.assembler.neighborRecord({ order, items, photos, acceptanceCount, chosenRecycler });
    }
    const { acceptance } = loaded;
    const seesPickup =
      acceptance?.status === AcceptanceStatus.CHOSEN && order.status === OrderStatus.CONFIRMED;
    const [neighborContact, reference, distanceKm] = await Promise.all([
      acceptance ? this.parties.getContact(order.neighborId) : Promise.resolve(null),
      seesPickup ? this.parties.getReference(order.neighborId) : Promise.resolve(null),
      point ? this.orders.distanceKm(order.id, point.lat, point.lng) : Promise.resolve(null),
    ]);
    return this.assembler.recyclerRecord({
      order,
      items,
      photos,
      acceptanceCount,
      acceptance,
      distanceKm,
      neighborContact,
      reference,
    });
  }

  /** HU-06: the neighbor's orders. */
  async listForNeighbor(
    neighborId: string,
    tab: ListTab,
    limit: number,
    cursor?: string,
  ): Promise<Page<ServicingOrderSummary>> {
    const decoded = cursor ? CursorCodec.decode<ListCursor>(cursor, this.isTimeCursor) : null;
    const rows = await this.lists.findByNeighbor(neighborId, tab, limit, decoded);
    return this.pageOf(
      rows,
      limit,
      (row) => ({ c: row.createdAtRaw, i: row.id }),
      (row) => this.assembler.neighborSummary(row),
    );
  }

  /** HU-10: orders open for acceptance, by distance when the recycler sends a point. */
  async listAvailable(recyclerId: string, point: GeoPoint | null, limit: number, cursor?: string) {
    const decoded = cursor
      ? CursorCodec.decode<ListCursor>(cursor, point ? this.isDistanceCursor : this.isTimeCursor)
      : null;
    const rows = await this.lists.findAvailable(recyclerId, point, limit, decoded);
    const page = this.pageOf(
      rows,
      limit,
      (row) => (point ? { d: row.distanceKm, i: row.id } : { c: row.createdAtRaw, i: row.id }),
      (row) => this.assembler.recyclerSummary(row),
    );
    return { sortedBy: point ? ('DISTANCE' as const) : ('DATE' as const), ...page };
  }

  /** HU-14: the recycler's acceptances. */
  async listMyAcceptances(recyclerId: string, tab: ListTab, limit: number, cursor?: string) {
    const decoded = cursor ? CursorCodec.decode<ListCursor>(cursor, this.isRankedCursor) : null;
    const rows = await this.lists.findAcceptancesByRecycler(recyclerId, tab, limit, decoded);
    return this.pageOf(
      rows,
      limit,
      (row) => ({ r: row.rank, c: row.acceptedAtRaw, i: row.acceptanceId }),
      (row) => this.assembler.myAcceptance(row),
    );
  }

  private async chosenRecycler(orderId: string): Promise<Counterpart | null> {
    const chosen = await this.acceptances.findChosen(orderId);
    return chosen ? ((await this.parties.getContact(chosen.recyclerId)) ?? null) : null;
  }

  private pageOf<R, V>(rows: R[], limit: number, cursorOf: (row: R) => object, view: (row: R) => V): Page<V> {
    const page = toPage(rows, limit, cursorOf);
    return { items: page.items.map(view), nextCursor: page.nextCursor };
  }

  private readonly isTimeCursor = (v: Record<string, unknown>) =>
    CursorCodec.isTimestamp(v.c) && CursorCodec.isUuid(v.i);
  private readonly isDistanceCursor = (v: Record<string, unknown>) =>
    CursorCodec.isFiniteNumber(v.d) && CursorCodec.isUuid(v.i);
  private readonly isRankedCursor = (v: Record<string, unknown>) =>
    (v.r === 0 || v.r === 1) && CursorCodec.isTimestamp(v.c) && CursorCodec.isUuid(v.i);
}
