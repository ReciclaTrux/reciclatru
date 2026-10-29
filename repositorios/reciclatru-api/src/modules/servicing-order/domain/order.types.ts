import type {
  AcceptanceLabel,
  AcceptanceStatus,
  OrderAction,
  OrderStatus,
} from '../../../common/domain/enums.js';

export interface OrderItemData {
  description: string;
  weightKg: number;
}

/** A pickup order joined with the snapshot of its pickup location. */
export interface OrderRow {
  id: string;
  neighborId: string;
  status: OrderStatus;
  createdAt: Date;
  zone: string | null;
  address: string | null;
  latitude: number;
  longitude: number;
}

export interface AcceptanceRow {
  id: string;
  orderId: string;
  recyclerId: string;
  status: AcceptanceStatus;
  acceptedAt: Date;
}

export interface PhotoRow {
  id: string;
  orderId: string;
  fileName: string;
  contentType: string;
}

/** Row of any order list: the SQL already computes counters, distance and the keyset values. */
export interface SummaryRow {
  id: string;
  status: OrderStatus;
  createdAt: Date;
  createdAtRaw: string;
  zone: string | null;
  itemCount: number;
  totalWeightKg: number;
  thumbnailPhotoId: string | null;
  acceptanceCount: number;
  chosenRecyclerName: string | null;
  alreadyAccepted: boolean;
  distanceKm: number | null;
}

export interface MyAcceptanceRow {
  acceptanceId: string;
  orderId: string;
  label: AcceptanceLabel;
  rank: number;
  zone: string | null;
  totalWeightKg: number;
  acceptedAt: Date;
  acceptedAtRaw: string;
}

export interface Counterpart {
  fullName: string;
  phone: string;
}

export interface PickupDetail {
  address: string | null;
  reference: string | null;
  latitude: number;
  longitude: number;
}

/** `servicingOrderInstanceRecord` of the API. */
export interface ServicingOrderRecord {
  id: string;
  status: OrderStatus;
  items: OrderItemData[];
  totalWeightKg: number;
  photos: { id: string; url: string }[];
  zone: string | null;
  approximateDistanceKm?: number | null;
  acceptanceCount?: number;
  alreadyAccepted?: boolean;
  acceptanceStatus?: AcceptanceStatus | null;
  chosenRecyclerName?: string | null;
  counterpart?: Counterpart | null;
  pickup?: PickupDetail | null;
  availableActions: OrderAction[];
  createdAt: string;
}

export interface ServicingOrderSummary {
  id: string;
  status: OrderStatus;
  itemCount: number;
  totalWeightKg: number;
  thumbnailUrl: string | null;
  zone: string | null;
  approximateDistanceKm?: number | null;
  acceptanceCount?: number | null;
  chosenRecyclerName?: string | null;
  alreadyAccepted?: boolean | null;
  createdAt: string;
}
