import { OrderAction, OrderStatus } from '../../../common/domain/enums.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import { UnprocessableAppException } from '../../../common/errors/app.exception.js';
import type { OrderItemData } from './order.types.js';

export const MIN_ITEMS = 1;
export const MAX_ITEMS = 10;
export const MAX_WEIGHT_KG = 500;
export const MAX_PHOTOS = 3;

const DISTANCE_DECIMALS = 10;

/** Pure business rules of the pickup order (RN06, RN11, RN16, RN15). */
export const OrderRules = {
  /** RN06: 1 to 10 items, each weighing more than 0 and at most 500 kg with one decimal. */
  validateItems(items: OrderItemData[]): void {
    if (items.length < MIN_ITEMS || items.length > MAX_ITEMS) {
      throw new UnprocessableAppException(
        ErrorCode.INVALID_ITEMS,
        `Un pedido lleva de ${MIN_ITEMS} a ${MAX_ITEMS} ítems.`,
      );
    }
    const errors = items.flatMap((item, index) =>
      OrderRules.isValidWeight(item.weightKg)
        ? []
        : [
            {
              field: `items[${index}].weightKg`,
              message: `El peso debe ser mayor a 0, hasta ${MAX_WEIGHT_KG} kg y con un decimal`,
            },
          ],
    );
    if (errors.length > 0) throw new UnprocessableAppException(ErrorCode.INVALID_WEIGHT, undefined, errors);
  },

  isValidWeight(weightKg: number): boolean {
    const tenths = Math.round(weightKg * 10);
    return weightKg > 0 && weightKg <= MAX_WEIGHT_KG && Math.abs(weightKg * 10 - tenths) < 1e-9;
  },

  /** Sum with one decimal, free of floating point noise. */
  totalWeight(items: OrderItemData[]): number {
    return items.reduce((sum, item) => sum + Math.round(item.weightKg * 10), 0) / 10;
  },

  /** RN11: editable only while PENDING and nobody has accepted it. */
  isEditable(status: OrderStatus, acceptanceCount: number): boolean {
    return status === OrderStatus.PENDING && acceptanceCount === 0;
  },

  isCancellable(status: OrderStatus): boolean {
    return status === OrderStatus.PENDING || status === OrderStatus.CONFIRMED;
  },

  /** Actions the neighbor owner can take in the current state. */
  neighborActions(status: OrderStatus, acceptanceCount: number): OrderAction[] {
    if (status === OrderStatus.PENDING) {
      return [
        ...(acceptanceCount === 0 ? [OrderAction.EDIT] : [OrderAction.CHOOSE_RECYCLER]),
        OrderAction.CANCEL,
      ];
    }
    if (status === OrderStatus.CONFIRMED) return [OrderAction.MARK_COLLECTED, OrderAction.CANCEL];
    return [];
  },

  /** Distance shown to the recycler, rounded to 0.1 km. */
  approximateDistance(distanceKm: number | null): number | null {
    return distanceKm === null ? null : Math.round(distanceKm * DISTANCE_DECIMALS) / DISTANCE_DECIMALS;
  },
};
