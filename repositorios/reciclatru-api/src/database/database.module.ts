import { Global, Module } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { appConfig } from '../config/app.config.js';
import { Database } from './database.service.js';

/** PostgreSQL connection pool (Neon pooled URL). Repositories use `Database` with plain SQL. */
@Global()
@Module({
  imports: [
    TypeOrmModule.forRootAsync({
      inject: [appConfig.KEY],
      useFactory: (config: ConfigType<typeof appConfig>) => ({
        type: 'postgres' as const,
        url: config.database.url,
        ssl: config.database.ssl ? { rejectUnauthorized: true } : false,
        extra: { max: config.database.poolMax },
        entities: [],
        synchronize: false,
        migrationsRun: false,
      }),
    }),
  ],
  providers: [Database],
  exports: [Database],
})
export class DatabaseModule {}
