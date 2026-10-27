import { Module } from '@nestjs/common';
import { SecurityModule } from '../../common/security/security.module.js';
import { LocationModule } from '../location-data-management/location.module.js';
import { PartyController } from './party.controller.js';
import { PartyRepository } from './party.repository.js';
import { PartyService } from './party.service.js';

@Module({
  imports: [SecurityModule, LocationModule],
  controllers: [PartyController],
  providers: [PartyRepository, PartyService],
  exports: [PartyService],
})
export class PartyModule {}
