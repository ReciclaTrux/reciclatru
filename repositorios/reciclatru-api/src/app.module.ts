import { type MiddlewareConsumer, Module, type NestModule } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { APP_FILTER } from '@nestjs/core';
import { CorrelationIdMiddleware } from './common/correlation/correlation-id.middleware.js';
import { ProblemDetailsFilter } from './common/filters/problem-details.filter.js';
import { ProblemDetailsFactory } from './common/errors/problem-details.js';
import { appConfig } from './config/app.config.js';
import { validateEnv } from './config/env.validation.js';
import { DatabaseModule } from './database/database.module.js';
import { HealthModule } from './health/health.module.js';
import { LocationModule } from './modules/location-data-management/location.module.js';
import { PartyModule } from './modules/party-data-management/party.module.js';
import { PartyAuthenticationModule } from './modules/party-authentication/auth.module.js';
import { ServicingOrderModule } from './modules/servicing-order/servicing-order.module.js';
import { RequestLogMiddleware } from './observability/request-log.middleware.js';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true, load: [appConfig], validate: validateEnv }),
    DatabaseModule,
    HealthModule,
    LocationModule,
    PartyModule,
    PartyAuthenticationModule,
    ServicingOrderModule,
  ],
  providers: [ProblemDetailsFactory, { provide: APP_FILTER, useClass: ProblemDetailsFilter }],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer): void {
    consumer.apply(CorrelationIdMiddleware, RequestLogMiddleware).forRoutes('*path');
  }
}
