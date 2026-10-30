import type { CorsOptions } from '@nestjs/common/interfaces/external/cors-options.interface.js';
import { CORRELATION_HEADER } from '../utils/request-context.js';

const PREFLIGHT_CACHE_SECONDS = 600;

/** Splits `CORS_ORIGINS`; a bare `*` is rejected so the allowlist can never be opened by accident. */
export function parseCorsOrigins(raw: string | undefined): string[] {
  const origins = (raw ?? '')
    .split(',')
    .map((origin) => origin.trim().replace(/\/+$/, ''))
    .filter(Boolean);
  if (origins.includes('*')) {
    throw new Error('CORS_ORIGINS must list explicit origins; a bare "*" is not allowed');
  }
  return origins;
}

/** Turns `http://localhost:*` into a matcher where `*` stands for any run of characters inside one origin. */
function toMatcher(origin: string): RegExp {
  const pattern = origin
    .split('*')
    .map((part) => part.replace(/[.+?^${}()|[\]\\]/g, '\\$&'))
    .join('[^/]*');
  return new RegExp(`^${pattern}$`, 'i');
}

/**
 * CORS for browser clients (Flutter web, Angular...). Native mobile apps ignore CORS, and with no
 * configured origins no CORS header is ever sent, so browsers keep blocking cross-origin calls.
 */
export function createCorsOptions(origins: string[]): CorsOptions | false {
  if (origins.length === 0) return false;
  const matchers = origins.map(toMatcher);
  return {
    origin: (origin, callback) => {
      callback(null, origin === undefined || matchers.some((matcher) => matcher.test(origin)));
    },
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Authorization', 'Content-Type', CORRELATION_HEADER],
    exposedHeaders: [CORRELATION_HEADER],
    maxAge: PREFLIGHT_CACHE_SECONDS,
    credentials: false,
  };
}
