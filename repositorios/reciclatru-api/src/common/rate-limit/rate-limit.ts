import { type ExecutionContext, SetMetadata } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import type { ThrottlerModuleOptions } from '@nestjs/throttler';

export const STRICT_RATE_LIMIT_KEY = 'strictRateLimit';

/** Marks a route (login, registration) that gets the tighter limit, counted per route and per client. */
export const StrictRateLimit = () => SetMetadata(STRICT_RATE_LIMIT_KEY, true);

export interface RateLimitSettings {
  enabled: boolean;
  windowSeconds: number;
  max: number;
  authMax: number;
}

const MILLIS_PER_SECOND = 1000;
const reflector = new Reflector();

const isStrict = (context: ExecutionContext): boolean =>
  reflector.get<boolean>(STRICT_RATE_LIMIT_KEY, context.getHandler()) === true;

/**
 * One throttler (so `Retry-After` keeps its standard name):
 * - strict routes: `authMax` per window, counted separately for each route and client;
 * - everything else: `max` per window, shared by all routes of the same client.
 */
export function createThrottlerOptions(settings: RateLimitSettings): ThrottlerModuleOptions {
  return {
    skipIf: () => !settings.enabled,
    throttlers: [
      {
        name: 'default',
        ttl: settings.windowSeconds * MILLIS_PER_SECOND,
        limit: (context) => (isStrict(context) ? settings.authMax : settings.max),
        generateKey: (context, client, name) =>
          isStrict(context)
            ? `${name}:${context.getClass().name}:${context.getHandler().name}:${client}`
            : `${name}:${client}`,
      },
    ],
  };
}
