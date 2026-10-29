import { Injectable, Logger } from '@nestjs/common';
import {
  ConflictAppException,
  NotFoundAppException,
  UnprocessableAppException,
} from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import { OrderStatus } from '../../../common/domain/enums.js';
import type { GeoPoint } from '../../../common/geo/geo-point.js';
import type { AuthenticatedUser } from '../../../common/utils/request-context.js';
import { PartyService } from '../../party-data-management/party.service.js';
import type { ServicingOrderRecord } from '../domain/order.types.js';
import { AcceptanceRepository } from '../repositories/acceptance.repository.js';
import { OrderRepository } from '../repositories/order.repository.js';
import { OrderAccessService } from './order-access.service.js';
import { OrderQueryService } from './order-query.service.js';

export interface AcceptanceResult {
  created: boolean;
  record: ServicingOrderRecord;
}

export interface AcceptanceView {
  id: string;
  status: string;
  fullName: string;
  phone: string;
  acceptedAt: string;
}

/** Recycler acceptances (HU-12) and the neighbor's view of them (HU-13). */
@Injectable()
export class AcceptanceService {
  private readonly logger = new Logger(AcceptanceService.name);

  constructor(
    private readonly access: OrderAccessService,
    private readonly orders: OrderRepository,
    private readonly acceptances: AcceptanceRepository,
    private readonly parties: PartyService,
    private readonly query: OrderQueryService,
  ) {}

  /** A second tap returns the existing acceptance instead of failing or duplicating it. */
  async accept(user: AuthenticatedUser, orderId: string, point: GeoPoint | null): Promise<AcceptanceResult> {
    const order = await this.orders.findById(orderId);
    if (!order) throw new NotFoundAppException();

    let created = false;
    if (!(await this.acceptances.find(orderId, user.id))) {
      if (!(await this.parties.isProfileComplete(user.id))) {
        throw new UnprocessableAppException(
          ErrorCode.PROFILE_INCOMPLETE,
          'Completa tu nombre y celular antes de aceptar.',
        );
      }
      if (order.status !== OrderStatus.PENDING) throw new ConflictAppException(ErrorCode.ORDER_NOT_AVAILABLE);
      created = await this.acceptances.insert(orderId, user.id);
      if (created) this.logger.log({ event: 'order.accepted', orderId });
    }
    const record = await this.query.present(user, await this.access.load(user, orderId), point);
    return { created, record };
  }

  /** The neighbor sees who accepted, oldest first, with real contact data. */
  async listForOwner(user: AuthenticatedUser, orderId: string): Promise<{ items: AcceptanceView[] }> {
    await this.access.loadOwned(user, orderId);
    const rows = await this.acceptances.listByOrder(orderId);
    const contacts = await this.parties.getContacts(rows.map((row) => row.recyclerId));
    const items = rows.flatMap((row) => {
      const contact = contacts.get(row.recyclerId);
      return contact
        ? [
            {
              id: row.id,
              status: row.status,
              fullName: contact.fullName,
              phone: contact.phone,
              acceptedAt: row.acceptedAt.toISOString(),
            },
          ]
        : [];
    });
    return { items };
  }
}
