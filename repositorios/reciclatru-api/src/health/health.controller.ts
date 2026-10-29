import { Controller, Get, Inject } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { appConfig } from '../config/app.config.js';
import { Public } from '../common/decorators/auth.decorators.js';
import { Database } from '../database/database.service.js';
import { ServiceUnavailableException } from '@nestjs/common';

export interface HealthResponse {
  status: 'UP';
  version: string;
}

/** Liveness and database readiness probe. */
@Controller('health')
export class HealthController {
  constructor(
    private readonly database: Database,
    @Inject(appConfig.KEY) private readonly config: ConfigType<typeof appConfig>,
  ) {}

  @Public()
  @Get()
  async check(): Promise<HealthResponse> {
    try {
      await this.database.ping();
    } catch {
      throw new ServiceUnavailableException('Database unavailable');
    }
    return { status: 'UP', version: this.config.version };
  }
}
