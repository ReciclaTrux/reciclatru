import { Module } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { APP_GUARD } from '@nestjs/core';
import { JwtModule } from '@nestjs/jwt';
import { SecurityModule } from '../../common/security/security.module.js';
import { appConfig } from '../../config/app.config.js';
import { PartyModule } from '../party-data-management/party.module.js';
import { AuthController } from './auth.controller.js';
import { AuthService } from './auth.service.js';
import { JwtAuthGuard } from './guards/jwt-auth.guard.js';
import { RolesGuard } from './guards/roles.guard.js';

@Module({
  imports: [
    PartyModule,
    SecurityModule,
    JwtModule.registerAsync({
      inject: [appConfig.KEY],
      useFactory: (config: ConfigType<typeof appConfig>) => ({
        secret: config.security.jwtSecret,
        signOptions: { expiresIn: config.security.jwtExpiresInSeconds },
      }),
    }),
  ],
  controllers: [AuthController],
  providers: [
    AuthService,
    { provide: APP_GUARD, useClass: JwtAuthGuard },
    { provide: APP_GUARD, useClass: RolesGuard },
  ],
})
export class PartyAuthenticationModule {}
