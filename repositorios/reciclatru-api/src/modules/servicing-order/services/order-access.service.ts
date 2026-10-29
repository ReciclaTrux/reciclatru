import { Injectable } from '@nestjs/common';
import { PartyRole } from '../../../common/domain/enums.js';
import { ForbiddenAppException, NotFoundAppException } from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import type { AuthenticatedUser } from '../../../common/utils/request-context.js';
import type { AcceptanceRow, OrderRow } from '../domain/order.types.js';
import { AcceptanceRepository } from '../repositories/acceptance.repository.js';
import { OrderRepository } from '../repositories/order.repository.js';

export interface OrderAccess {
  order: OrderRow;
  acceptance: AcceptanceRow | null;
}

/** Loads an order for a caller and applies the ownership rules (RN05, RN13). */
@Injectable()
export class OrderAccessService {
  constructor(
    private readonly orders: OrderRepository,
    private readonly acceptances: AcceptanceRepository,
  ) {}

  /** The order plus the caller's acceptance. A neighbor must own the order. */
  async load(user: AuthenticatedUser, orderId: string): Promise<OrderAccess> {
    const order = await this.orders.findById(orderId);
    if (!order) throw new NotFoundAppException();
    if (user.role === PartyRole.NEIGHBOR) {
      this.assertOwner(order, user);
      return { order, acceptance: null };
    }
    return { order, acceptance: await this.acceptances.find(orderId, user.id) };
  }

  /** The order for its owner; `lock` keeps the row locked until the surrounding transaction ends. */
  async loadOwned(user: AuthenticatedUser, orderId: string, lock = false): Promise<OrderRow> {
    if (user.role !== PartyRole.NEIGHBOR) throw new ForbiddenAppException(ErrorCode.FORBIDDEN_ROLE);
    const order = lock ? await this.orders.findByIdForUpdate(orderId) : await this.orders.findById(orderId);
    if (!order) throw new NotFoundAppException();
    this.assertOwner(order, user);
    return order;
  }

  private assertOwner(order: OrderRow, user: AuthenticatedUser): void {
    if (order.neighborId !== user.id) throw new ForbiddenAppException(ErrorCode.NOT_ORDER_OWNER);
  }
}
