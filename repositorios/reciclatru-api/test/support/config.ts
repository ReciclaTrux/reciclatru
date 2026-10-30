import type { ConfigType } from '@nestjs/config';
import type { appConfig } from '../../src/config/app.config.js';

export type TestConfig = ConfigType<typeof appConfig>;

/** Minimal valid settings for unit tests; override only what a test needs. */
export function testConfig(overrides: Partial<TestConfig> = {}): TestConfig {
  return {
    env: 'test',
    port: 3000,
    version: '1.0.0',
    corsOrigins: [],
    errorTypeBaseUrl: 'https://api.test/errors',
    database: { url: 'postgres://localhost/test', ssl: false, poolMax: 2 },
    security: {
      jwtSecret: 'x'.repeat(48),
      jwtExpiresInSeconds: 30 * 86_400,
      bcryptRounds: 4,
      phoneEncryptionKey: Buffer.alloc(32, 7).toString('base64'),
    },
    storage: {
      endpoint: 'http://localhost',
      region: 'us-east-2',
      bucket: 'photos',
      accessKeyId: 'a',
      secretAccessKey: 'b',
      forcePathStyle: false,
    },
    photos: { urlTtlSeconds: 300, maxBytes: 2 * 1024 * 1024 },
    ...overrides,
  };
}
