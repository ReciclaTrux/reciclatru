import { Logger } from '@nestjs/common';

/** Unit tests call services directly; their log lines would only add noise to the Jest output. */
Logger.overrideLogger(false);
