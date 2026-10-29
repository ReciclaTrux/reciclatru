import { Body, Controller, Get, Param, Post, Put } from '@nestjs/common';
import { Public, CurrentUser, Roles } from '../../common/decorators/auth.decorators.js';
import { PartyRole } from '../../common/domain/enums.js';
import type { AuthenticatedUser } from '../../common/utils/request-context.js';
import { resolveSelfId } from '../../common/utils/self-reference.js';
import { PickupPointDto, RegisterPartyDto, UpdateProfileDto } from './dto/party.dto.js';
import { PartyService } from './party.service.js';
import type { PartyRecord } from './party.types.js';

export interface PartyOutput {
  crReferenceId: string;
  partyDataManagementInstanceRecord: PartyRecord;
}

const toOutput = (record: PartyRecord): PartyOutput => ({
  crReferenceId: record.id,
  partyDataManagementInstanceRecord: record,
});

/** Registration, profile and pickup point endpoints. */
@Controller()
export class PartyController {
  constructor(private readonly parties: PartyService) {}

  @Public()
  @Post('party-data-management/initiation')
  async initiate(@Body() body: RegisterPartyDto): Promise<PartyOutput> {
    return toOutput(await this.parties.register(body));
  }

  @Get('party-data-management/:crReferenceId/retrieval')
  async retrieve(
    @Param('crReferenceId') id: string,
    @CurrentUser() user: AuthenticatedUser,
  ): Promise<PartyOutput> {
    return toOutput(await this.parties.retrieve(resolveSelfId(id, user)));
  }

  @Put('party-data-management/:crReferenceId/update')
  async update(
    @Param('crReferenceId') id: string,
    @Body() body: UpdateProfileDto,
    @CurrentUser() user: AuthenticatedUser,
  ): Promise<PartyOutput> {
    return toOutput(await this.parties.updateProfile(resolveSelfId(id, user), body));
  }

  @Roles(PartyRole.NEIGHBOR)
  @Put('location-data-management/:crReferenceId/pickup-point/update')
  async updatePickupPoint(
    @Param('crReferenceId') id: string,
    @Body() body: PickupPointDto,
    @CurrentUser() user: AuthenticatedUser,
  ): Promise<PartyOutput> {
    return toOutput(await this.parties.savePickupPoint(resolveSelfId(id, user), body));
  }
}
