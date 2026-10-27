import type { INestApplication } from '@nestjs/common';
import type { NestExpressApplication } from '@nestjs/platform-express';
import { createValidationPipe } from './common/pipes/validation.js';

export const GLOBAL_PREFIX = 'api/v1';

/** Settings shared by `main.ts` and the tests, so tests exercise the same pipeline as production. */
export function configureApp(app: INestApplication): void {
  app.setGlobalPrefix(GLOBAL_PREFIX);
  app.useGlobalPipes(createValidationPipe());
  app.enableShutdownHooks();
  (app as NestExpressApplication).disable('x-powered-by');
  (app as NestExpressApplication).set('trust proxy', 1);
}
