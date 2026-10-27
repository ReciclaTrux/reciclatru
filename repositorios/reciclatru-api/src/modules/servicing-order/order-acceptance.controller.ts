import { Controller, Get, HttpStatus, Param, Post, Query, Res } from '@nestjs/common';
import type { Response } from 'express';
import { CurrentUser, Roles } from '../../common/decorators/auth.decorators.js';
import { PartyRole } from '../../common/domain/enums.js';
import { toGeoPoint } from '../../common/geo/geo-point.js';
import type { AuthenticatedUser } from '../../common/utils/request-context.js';
import { uuidPipe } from '../../common/utils/self-reference.js';
import { GeoQueryDto } from './dto/order.dto.js';
import { AcceptanceService } from './services/acceptance.service.js';

@Controller('servicing-order/:crReferenceId/acceptance')
export class OrderAcceptanceController {
  constructor(private readonly acceptances: AcceptanceService) {}

  /** 201 when the acceptance is new, 200 when the recycler had already accepted (double tap). */
  @Roles(PartyRole.RECYCLER)
  @Post('initiation')
  async accept(
    @Param('crReferenceId', uuidPipe) id: string,
    @Query() query: GeoQueryDto,
    @CurrentUser() user: AuthenticatedUser,
    @Res({ passthrough: true }) response: Response,
  ) {
    const { created, record } = await this.acceptances.accept(user, id, toGeoPoint(query.lat, query.lng));
    response.status(created ? HttpStatus.CREATED : HttpStatus.OK);
    return { crReferenceId: record.id, servicingOrderInstanceRecord: record };
  }

  @Roles(PartyRole.NEIGHBOR)
  @Get('retrieval')
  list(@Param('crReferenceId', uuidPipe) id: string, @CurrentUser() user: AuthenticatedUser) {
    return this.acceptances.listForOwner(user, id);
  }
}
