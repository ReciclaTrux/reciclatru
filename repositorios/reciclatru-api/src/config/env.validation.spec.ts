import { describe, expect, it } from '@jest/globals';
import { validateEnv } from './env.validation.js';

const valid = {
  DATABASE_URL: 'postgres://user:pass@host/db',
  JWT_SECRET: 'a'.repeat(32),
  PHONE_ENCRYPTION_KEY: Buffer.alloc(32, 1).toString('base64'),
  S3_ENDPOINT: 'https://storage.example.com',
  S3_REGION: 'us-east-2',
  S3_BUCKET: 'photos',
  S3_ACCESS_KEY_ID: 'id',
  S3_SECRET_ACCESS_KEY: 'secret',
};

describe('validateEnv', () => {
  it('accepts the minimum set and applies defaults', () => {
    const env = validateEnv(valid);
    expect(env).toMatchObject({
      PORT: 3000,
      JWT_EXPIRES_IN_DAYS: 30,
      BCRYPT_ROUNDS: 10,
      DATABASE_SSL: true,
      PHOTO_URL_TTL_SECONDS: 300,
    });
  });

  it('converts strings coming from the process environment', () => {
    const env = validateEnv({ ...valid, PORT: '8080', DATABASE_SSL: 'false', JWT_EXPIRES_IN_DAYS: '7' });
    expect(env).toMatchObject({ PORT: 8080, DATABASE_SSL: false, JWT_EXPIRES_IN_DAYS: 7 });
  });

  it('lists every missing or invalid variable', () => {
    expect(() => validateEnv({})).toThrow(/DATABASE_URL[\s\S]*JWT_SECRET|JWT_SECRET[\s\S]*DATABASE_URL/);
  });

  it('rejects a short JWT secret', () => {
    expect(() => validateEnv({ ...valid, JWT_SECRET: 'short' })).toThrow(/JWT_SECRET/);
  });

  it('rejects an encryption key that is not 32 bytes', () => {
    expect(() =>
      validateEnv({ ...valid, PHONE_ENCRYPTION_KEY: Buffer.alloc(16).toString('base64') }),
    ).toThrow(/PHONE_ENCRYPTION_KEY/);
  });
});
