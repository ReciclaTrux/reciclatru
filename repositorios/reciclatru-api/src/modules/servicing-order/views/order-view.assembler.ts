import { Injectable } from '@nestjs/common';
import { AcceptanceStatus, OrderAction, OrderStatus } from '../../../common/domain/enums.js';
import { OrderRules } from '../domain/order.rules.js';
import type {
  AcceptanceRow,
  Counterpart,
  MyAcceptanceRow,
  OrderItemData,
  OrderRow,
  PhotoRow,
  PickupDetail,
  ServicingOrderRecord,
  ServicingOrderSummary,
  SummaryRow,
} from '../domain/order.types.js';

export const API_PREFIX = '/api/v1';

export interface NeighborViewData {
  order: OrderRow;
  items: OrderItemData[];
  photos: PhotoRow[];
  acceptanceCount: number;
  chosenRecycler: Counterpart | null;
}

export interface RecyclerViewData {
  order: OrderRow;
  items: OrderItemData[];
  photos: PhotoRow[];
  acceptanceCount: number;
  acceptance: AcceptanceRow | null;
  distanceKm: number | null;
  neighborContact: Counterpart | null;
  reference: string | null;
}

/** Builds the API representations and decides which fields each role may see (RN13). */
@Injectable()
export class OrderViewAssembler {
  photoUrl(orderId: string, photoId: string): string {
    return `${API_PREFIX}/servicing-order/${orderId}/photo/${photoId}/retrieval`;
  }

  neighborRecord(data: NeighborViewData): ServicingOrderRecord {
    const { order, chosenRecycler } = data;
    return {
      ...this.base(order, data.items, data.photos),
      acceptanceCount: data.acceptanceCount,
      chosenRecyclerName: chosenRecycler?.fullName ?? null,
      counterpart: chosenRecycler,
      availableActions: OrderRules.neighborActions(order.status, data.acceptanceCount),
    };
  }

  /** Contact data needs an ACCEPTED/CHOSEN acceptance and a live order; the address needs CHOSEN and CONFIRMED. */
  recyclerRecord(data: RecyclerViewData): ServicingOrderRecord {
    const { order, acceptance } = data;
    const liveOrder = order.status === OrderStatus.PENDING || order.status === OrderStatus.CONFIRMED;
    const active =
      acceptance?.status === AcceptanceStatus.ACCEPTED || acceptance?.status === AcceptanceStatus.CHOSEN;
    const pickup = this.pickup(data);
    return {
      ...this.base(order, data.items, data.photos),
      approximateDistanceKm: OrderRules.approximateDistance(data.distanceKm),
      acceptanceCount: data.acceptanceCount,
      alreadyAccepted: acceptance !== null,
      acceptanceStatus: acceptance?.status ?? null,
      counterpart: active && liveOrder ? data.neighborContact : null,
      pickup,
      availableActions: this.recyclerActions(order, acceptance, pickup !== null),
    };
  }

  neighborSummary(row: SummaryRow): ServicingOrderSummary {
    return {
      ...this.summaryBase(row),
      acceptanceCount: row.status === OrderStatus.PENDING ? row.acceptanceCount : null,
      chosenRecyclerName: row.status === OrderStatus.CONFIRMED ? row.chosenRecyclerName : null,
    };
  }

  recyclerSummary(row: SummaryRow): ServicingOrderSummary {
    return {
      ...this.summaryBase(row),
      approximateDistanceKm: OrderRules.approximateDistance(row.distanceKm),
      alreadyAccepted: row.alreadyAccepted,
    };
  }

  myAcceptance(row: MyAcceptanceRow) {
    return {
      orderId: row.orderId,
      acceptanceLabel: row.label,
      zone: row.zone,
      totalWeightKg: row.totalWeightKg,
      acceptedAt: row.acceptedAt.toISOString(),
    };
  }

  private base(order: OrderRow, items: OrderItemData[], photos: PhotoRow[]) {
    return {
      id: order.id,
      status: order.status,
      items,
      totalWeightKg: OrderRules.totalWeight(items),
      photos: photos.map((photo) => ({ id: photo.id, url: this.photoUrl(order.id, photo.id) })),
      zone: order.zone,
      createdAt: order.createdAt.toISOString(),
    };
  }

  private summaryBase(row: SummaryRow) {
    return {
      id: row.id,
      status: row.status,
      itemCount: row.itemCount,
      totalWeightKg: Math.round(row.totalWeightKg * 10) / 10,
      thumbnailUrl: row.thumbnailPhotoId ? this.photoUrl(row.id, row.thumbnailPhotoId) : null,
      zone: row.zone,
      createdAt: row.createdAt.toISOString(),
    };
  }

  private pickup(data: RecyclerViewData): PickupDetail | null {
    const chosen = data.acceptance?.status === AcceptanceStatus.CHOSEN;
    if (!chosen || data.order.status !== OrderStatus.CONFIRMED) return null;
    return {
      address: data.order.address,
      reference: data.reference,
      latitude: data.order.latitude,
      longitude: data.order.longitude,
    };
  }

  private recyclerActions(
    order: OrderRow,
    acceptance: AcceptanceRow | null,
    hasPickup: boolean,
  ): OrderAction[] {
    if (!acceptance && order.status === OrderStatus.PENDING) return [OrderAction.ACCEPT];
    return hasPickup ? [OrderAction.GO_PICKUP] : [];
  }
}
