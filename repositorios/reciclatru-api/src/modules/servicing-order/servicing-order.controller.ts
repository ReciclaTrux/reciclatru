import { Body, Controller, Get, HttpCode, HttpStatus, Param, Post, Put, Query } from '@nestjs/common';
import { CurrentUser, Roles } from '../../common/decorators/auth.decorators.js';
import { PartyRole } from '../../common/domain/enums.js';
import { toGeoPoint } from '../../common/geo/geo-point.js';
import type { AuthenticatedUser } from '../../common/utils/request-context.js';
import { uuidPipe } from '../../common/utils/self-reference.js';
import {
  AvailabilityQueryDto,
  ConfirmRecyclerDto,
  GeoQueryDto,
  OrderItemsDto,
  OrderListQueryDto,
} from './dto/order.dto.js';
import type { ServicingOrderRecord } from './domain/order.types.js';
import { OrderCommandService } from './services/order-command.service.js';
import { OrderQueryService } from './services/order-query.service.js';

const toOutput = (record: ServicingOrderRecord) => ({
  crReferenceId: record.id,
  servicingOrderInstanceRecord: record,
});

/**
 * Orders. Static routes (`retrieval`, `availability/retrieval`, `acceptance/retrieval`) are declared before
 * the `:crReferenceId` routes so they are never read as an order id.
 */
@Controller('servicing-order')
export class ServicingOrderController {
  constructor(
    private readonly commands: OrderCommandService,
    private readonly queries: OrderQueryService,
  ) {}

  @Roles(PartyRole.NEIGHBOR)
  @Post('initiation')
  async create(@Body() body: OrderItemsDto, @CurrentUser() user: AuthenticatedUser) {
    return toOutput(await this.commands.create(user, body.items));
  }

  @Roles(PartyRole.NEIGHBOR)
  @Get('retrieval')
  listMine(@Query() query: OrderListQueryDto, @CurrentUser() user: AuthenticatedUser) {
    return this.queries.listForNeighbor(user.id, query.tab, query.limit, query.cursor);
  }

  @Roles(PartyRole.RECYCLER)
  @Get('availability/retrieval')
  listAvailable(@Query() query: AvailabilityQueryDto, @CurrentUser() user: AuthenticatedUser) {
    return this.queries.listAvailable(user.id, toGeoPoint(query.lat, query.lng), query.limit, query.cursor);
  }

  @Roles(PartyRole.RECYCLER)
  @Get('acceptance/retrieval')
  listMyAcceptances(@Query() query: OrderListQueryDto, @CurrentUser() user: AuthenticatedUser) {
    return this.queries.listMyAcceptances(user.id, query.tab, query.limit, query.cursor);
  }

  @Get(':crReferenceId/retrieval')
  async retrieve(
    @Param('crReferenceId', uuidPipe) id: string,
    @Query() query: GeoQueryDto,
    @CurrentUser() user: AuthenticatedUser,
  ) {
    return toOutput(await this.queries.getDetail(user, id, toGeoPoint(query.lat, query.lng)));
  }

  @Roles(PartyRole.NEIGHBOR)
  @Put(':crReferenceId/update')
  async update(
    @Param('crReferenceId', uuidPipe) id: string,
    @Body() body: OrderItemsDto,
    @CurrentUser() user: AuthenticatedUser,
  ) {
    return toOutput(await this.commands.update(user, id, body.items));
  }

  @Roles(PartyRole.NEIGHBOR)
  @Put(':crReferenceId/termination')
  async cancel(@Param('crReferenceId', uuidPipe) id: string, @CurrentUser() user: AuthenticatedUser) {
    return toOutput(await this.commands.cancel(user, id));
  }

  @Roles(PartyRole.NEIGHBOR)
  @HttpCode(HttpStatus.OK)
  @Put(':crReferenceId/confirmation/execution')
  async choose(
    @Param('crReferenceId', uuidPipe) id: string,
    @Body() body: ConfirmRecyclerDto,
    @CurrentUser() user: AuthenticatedUser,
  ) {
    return toOutput(await this.commands.chooseRecycler(user, id, body.acceptanceId));
  }

  @Roles(PartyRole.NEIGHBOR)
  @Put(':crReferenceId/fulfillment/execution')
  async markCollected(@Param('crReferenceId', uuidPipe) id: string, @CurrentUser() user: AuthenticatedUser) {
    return toOutput(await this.commands.markCollected(user, id));
  }
}
