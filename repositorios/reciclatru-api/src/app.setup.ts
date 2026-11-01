import type { INestApplication } from '@nestjs/common';
import helmet from 'helmet';
import type { ConfigType } from '@nestjs/config';
import type { NestExpressApplication } from '@nestjs/platform-express';
import { createCorsOptions } from './common/http/cors.js';
import { createValidationPipe } from './common/pipes/validation.js';
import { appConfig } from './config/app.config.js';

export const GLOBAL_PREFIX = 'api/v1';

/** Settings shared by `main.ts` and the tests, so tests exercise the same pipeline as production. */
export function configureApp(app: INestApplication): void {
  const settings = app.get<ConfigType<typeof appConfig>>(appConfig.KEY);
  // A JSON API: browsers of other origins (allowed by CORS) must still be able to read its responses.
  app.use(helmet({ crossOriginResourcePolicy: { policy: 'cross-origin' } }));
  (app as NestExpressApplication).useBodyParser('json', { limit: settings.bodyLimit });
  app.setGlobalPrefix(GLOBAL_PREFIX);
  app.useGlobalPipes(createValidationPipe());
  const cors = createCorsOptions(settings.corsOrigins);
  if (cors) app.enableCors(cors);
  app.enableShutdownHooks();
  (app as NestExpressApplication).disable('x-powered-by');
  (app as NestExpressApplication).set('trust proxy', settings.trustProxyHops);
}
