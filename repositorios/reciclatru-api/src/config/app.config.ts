import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { registerAs } from '@nestjs/config';
import { parseCorsOrigins } from '../common/http/cors.js';

const SECONDS_PER_DAY = 86_400;

function readVersion(): string {
  try {
    return JSON.parse(readFileSync(join(process.cwd(), 'package.json'), 'utf8')).version ?? '0.0.0';
  } catch {
    return '0.0.0';
  }
}

/** Typed application settings built from the validated environment. Inject with `@Inject(appConfig.KEY)`. */
export const appConfig = registerAs('app', () => ({
  env: process.env.NODE_ENV ?? 'development',
  port: Number(process.env.PORT ?? 3000),
  version: readVersion(),
  corsOrigins: parseCorsOrigins(process.env.CORS_ORIGINS),
  errorTypeBaseUrl: process.env.ERROR_TYPE_BASE_URL ?? 'https://api.reciclatru.example.com/errors',
  database: {
    url: process.env.DATABASE_URL as string,
    ssl: (process.env.DATABASE_SSL ?? 'true') === 'true',
    poolMax: Number(process.env.DATABASE_POOL_MAX ?? 10),
  },
  security: {
    jwtSecret: process.env.JWT_SECRET as string,
    jwtExpiresInSeconds: Number(process.env.JWT_EXPIRES_IN_DAYS ?? 30) * SECONDS_PER_DAY,
    bcryptRounds: Number(process.env.BCRYPT_ROUNDS ?? 10),
    phoneEncryptionKey: process.env.PHONE_ENCRYPTION_KEY as string,
  },
  storage: {
    endpoint: process.env.S3_ENDPOINT as string,
    region: process.env.S3_REGION as string,
    bucket: process.env.S3_BUCKET as string,
    accessKeyId: process.env.S3_ACCESS_KEY_ID as string,
    secretAccessKey: process.env.S3_SECRET_ACCESS_KEY as string,
    forcePathStyle: (process.env.S3_FORCE_PATH_STYLE ?? 'false') === 'true',
  },
  photos: {
    urlTtlSeconds: Number(process.env.PHOTO_URL_TTL_SECONDS ?? 300),
    maxBytes: Number(process.env.PHOTO_MAX_BYTES ?? 2 * 1024 * 1024),
  },
}));

export type AppConfig = ReturnType<typeof appConfig>;
