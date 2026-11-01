import { Module } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { APP_GUARD } from '@nestjs/core';
import { ThrottlerGuard, ThrottlerModule } from '@nestjs/throttler';
import { appConfig } from '../../config/app.config.js';
import { createThrottlerOptions } from './rate-limit.js';

/** Per-client request limits (in memory: enough for a single instance such as Render's free plan). */
@Module({
  imports: [
    ThrottlerModule.forRootAsync({
      inject: [appConfig.KEY],
      useFactory: (config: ConfigType<typeof appConfig>) => createThrottlerOptions(config.rateLimit),
    }),
  ],
  providers: [{ provide: APP_GUARD, useClass: ThrottlerGuard }],
})
export class RateLimitModule {}
