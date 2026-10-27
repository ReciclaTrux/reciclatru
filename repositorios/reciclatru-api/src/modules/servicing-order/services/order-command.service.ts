import { Injectable } from '@nestjs/common';
import { OrderStatus } from '../../../common/domain/enums.js';
import { ConflictAppException, UnprocessableAppException } from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import type { AuthenticatedUser } from '../../../common/utils/request-context.js';
import { Database } from '../../../database/database.service.js';
import { PartyService } from '../../party-data-management/party.service.js';
import { OrderRules } from '../domain/order.rules.js';
import type { OrderItemData, ServicingOrderRecord } from '../domain/order.types.js';
import { AcceptanceRepository } from '../repositories/acceptance.repository.js';
import { OrderRepository } from '../repositories/order.repository.js';
import { OrderAccessService } from './order-access.service.js';
import { OrderQueryService } from './order-query.service.js';

/** Write side of the neighbor's orders: publish, edit, cancel, choose a recycler and mark as collected. */
@Injectable()
export class OrderCommandService {
  constructor(
    private readonly db: Database,
    private readonly access: OrderAccessService,
    private readonly orders: OrderRepository,
    private readonly acceptances: AcceptanceRepository,
    private readonly parties: PartyService,
    private readonly query: OrderQueryService,
  ) {}

  /** HU-05: publishes a PENDING order that copies the neighbor's current location. */
  async create(user: AuthenticatedUser, items: OrderItemData[]): Promise<ServicingOrderRecord> {
    OrderRules.validateItems(items);
    if (!(await this.parties.isProfileComplete(user.id))) {
      throw new UnprocessableAppException(
        ErrorCode.PROFILE_INCOMPLETE,
        'Completa tu perfil y ubicación antes de publicar.',
      );
    }
    const orderId = await this.db.transaction(async () => {
      const id = await this.orders.insert(user.id);
      if (!id) throw new UnprocessableAppException(ErrorCode.PROFILE_INCOMPLETE);
      await this.orders.insertItems(id, items);
      await this.orders.addHistory(id, null, OrderStatus.PENDING, user.id);
      return id;
    });
    return this.present(user, orderId);
  }

  /** HU-07 (RN11): replaces the items while the order is PENDING and has no acceptances. */
  async update(
    user: AuthenticatedUser,
    orderId: string,
    items: OrderItemData[],
  ): Promise<ServicingOrderRecord> {
    OrderRules.validateItems(items);
    await this.db.transaction(async () => {
      const order = await this.access.loadOwned(user, orderId, true);
      if (!OrderRules.isEditable(order.status, await this.acceptances.count(orderId))) {
        throw new ConflictAppException(ErrorCode.ORDER_NOT_EDITABLE);
      }
      await this.orders.replaceItems(orderId, items);
    });
    return this.present(user, orderId);
  }

  /** HU-08 (RN16, RN17): logical cancellation from PENDING or CONFIRMED. */
  async cancel(user: AuthenticatedUser, orderId: string): Promise<ServicingOrderRecord> {
    await this.db.transaction(async () => {
      const order = await this.access.loadOwned(user, orderId, true);
      if (!OrderRules.isCancellable(order.status))
        throw new ConflictAppException(ErrorCode.ORDER_NOT_CANCELLABLE);
      await this.orders.transition(orderId, [order.status], OrderStatus.CANCELLED, 'cancelled_at');
      await this.orders.addHistory(orderId, order.status, OrderStatus.CANCELLED, user.id);
    });
    return this.present(user, orderId);
  }

  /** HU-13 (RN12): confirms the order and chooses the recycler in one database transaction. */
  async chooseRecycler(
    user: AuthenticatedUser,
    orderId: string,
    acceptanceId: string,
  ): Promise<ServicingOrderRecord> {
    await this.access.loadOwned(user, orderId);
    await this.acceptances.choose(orderId, acceptanceId, user.id);
    return this.present(user, orderId);
  }

  /** HU-16 (RN15): CONFIRMED to COLLECTED, final. */
  async markCollected(user: AuthenticatedUser, orderId: string): Promise<ServicingOrderRecord> {
    await this.db.transaction(async () => {
      const order = await this.access.loadOwned(user, orderId, true);
      if (order.status !== OrderStatus.CONFIRMED)
        throw new ConflictAppException(ErrorCode.ORDER_NOT_CONFIRMED);
      await this.orders.transition(orderId, [OrderStatus.CONFIRMED], OrderStatus.COLLECTED, 'collected_at');
      await this.orders.addHistory(orderId, OrderStatus.CONFIRMED, OrderStatus.COLLECTED, user.id);
    });
    return this.present(user, orderId);
  }

  private async present(user: AuthenticatedUser, orderId: string): Promise<ServicingOrderRecord> {
    return this.query.present(user, await this.access.load(user, orderId));
  }
}
