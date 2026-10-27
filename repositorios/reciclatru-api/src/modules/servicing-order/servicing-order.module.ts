import { Module } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { MulterModule } from '@nestjs/platform-express';
import { appConfig } from '../../config/app.config.js';
import { PartyModule } from '../party-data-management/party.module.js';
import { PhotoStorageModule } from '../photo-storage/photo-storage.module.js';
import { OrderAcceptanceController } from './order-acceptance.controller.js';
import { OrderPhotoController } from './order-photo.controller.js';
import { AcceptanceRepository } from './repositories/acceptance.repository.js';
import { OrderListRepository } from './repositories/order-list.repository.js';
import { OrderRepository } from './repositories/order.repository.js';
import { PhotoRepository } from './repositories/photo.repository.js';
import { ServicingOrderController } from './servicing-order.controller.js';
import { AcceptanceService } from './services/acceptance.service.js';
import { OrderAccessService } from './services/order-access.service.js';
import { OrderCommandService } from './services/order-command.service.js';
import { OrderPhotoService } from './services/order-photo.service.js';
import { OrderQueryService } from './services/order-query.service.js';
import { OrderViewAssembler } from './views/order-view.assembler.js';

@Module({
  imports: [
    PartyModule,
    PhotoStorageModule,
    MulterModule.registerAsync({
      inject: [appConfig.KEY],
      useFactory: (config: ConfigType<typeof appConfig>) => ({
        limits: { fileSize: config.photos.maxBytes, files: 1 },
      }),
    }),
  ],
  controllers: [ServicingOrderController, OrderAcceptanceController, OrderPhotoController],
  providers: [
    OrderRepository,
    AcceptanceRepository,
    PhotoRepository,
    OrderListRepository,
    OrderViewAssembler,
    OrderAccessService,
    OrderQueryService,
    OrderCommandService,
    AcceptanceService,
    OrderPhotoService,
  ],
})
export class ServicingOrderModule {}
