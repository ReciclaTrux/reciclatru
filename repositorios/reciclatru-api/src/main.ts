import { Logger } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module.js';
import { configureApp } from './app.setup.js';
import { appConfig } from './config/app.config.js';
import { JsonLogger, parseLogFormat, parseLogLevel } from './observability/json-logger.js';

async function bootstrap(): Promise<void> {
  const app = await NestFactory.create(AppModule, {
    logger: new JsonLogger({
      level: parseLogLevel(process.env.LOG_LEVEL),
      format: parseLogFormat(process.env.LOG_FORMAT),
    }),
  });
  configureApp(app);
  const config = app.get<ConfigType<typeof appConfig>>(appConfig.KEY);
  await app.listen(config.port, '0.0.0.0');
  new Logger('Bootstrap').log(`ReciclaTru API ${config.version} listening on port ${config.port}`);
}

await bootstrap();
