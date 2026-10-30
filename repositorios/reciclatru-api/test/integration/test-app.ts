import { randomBytes } from 'node:crypto';
import type { INestApplication } from '@nestjs/common';
import { Test } from '@nestjs/testing';
import request from 'supertest';

/** In-memory replacement of the bucket, so the integration tests need no network. */
export class FakePhotoStorage {
  readonly objects = new Map<string, Buffer>();

  async put(key: string, body: Buffer): Promise<void> {
    this.objects.set(key, body);
  }

  async remove(key: string): Promise<void> {
    this.objects.delete(key);
  }

  async signedUrl(key: string, ttlSeconds: number): Promise<string> {
    return `https://storage.example.com/photos/${key}?X-Expires=${ttlSeconds}`;
  }
}

export interface TestApp {
  app: INestApplication;
  storage: FakePhotoStorage;
  http: () => ReturnType<typeof request>;
}

/** Boots the real application (same pipeline as production) with a fake bucket. */
export async function createTestApp(extraEnv: Record<string, string> = {}): Promise<TestApp> {
  Object.assign(process.env, {
    NODE_ENV: 'test',
    DATABASE_URL: process.env.TEST_DATABASE_URL ?? 'postgres://postgres@localhost:5433/reciclatru_test',
    DATABASE_SSL: 'false',
    JWT_SECRET: randomBytes(48).toString('base64'),
    PHONE_ENCRYPTION_KEY: randomBytes(32).toString('base64'),
    S3_ENDPOINT: 'http://localhost:9',
    S3_REGION: 'us-east-2',
    S3_BUCKET: 'photos',
    S3_ACCESS_KEY_ID: 'test',
    S3_SECRET_ACCESS_KEY: 'test',
    ...extraEnv,
  });
  const { AppModule } = await import('../../src/app.module.js');
  const { configureApp } = await import('../../src/app.setup.js');
  const { PhotoStorage } = await import('../../src/modules/photo-storage/photo-storage.js');

  const storage = new FakePhotoStorage();
  const moduleRef = await Test.createTestingModule({ imports: [AppModule] })
    .overrideProvider(PhotoStorage)
    .useValue(storage)
    .compile();
  const app = moduleRef.createNestApplication();
  configureApp(app);
  await app.init();
  return { app, storage, http: () => request(app.getHttpServer()) };
}
