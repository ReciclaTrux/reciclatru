import { describe, expect, it } from '@jest/globals';
import type { ExecutionContext } from '@nestjs/common';
import type { ThrottlerOptions } from '@nestjs/throttler';
import { createThrottlerOptions, StrictRateLimit, type RateLimitSettings } from './rate-limit.js';

class ExampleController {
  @StrictRateLimit()
  login(): void {}

  retrieve(): void {}
}

const settings: RateLimitSettings = { enabled: true, windowSeconds: 60, max: 100, authMax: 10 };

function contextOf(handler: () => void): ExecutionContext {
  return { getHandler: () => handler, getClass: () => ExampleController } as unknown as ExecutionContext;
}

function throttlerOf(overrides: Partial<RateLimitSettings> = {}) {
  const options = createThrottlerOptions({ ...settings, ...overrides });
  if (Array.isArray(options)) throw new Error('expected the object form');
  const [throttler] = options.throttlers as [ThrottlerOptions & { limit: (c: ExecutionContext) => number }];
  return { options, throttler };
}

const strict = contextOf(ExampleController.prototype.login);
const general = contextOf(ExampleController.prototype.retrieve);

describe('createThrottlerOptions', () => {
  it('uses one throttler named default, so Retry-After keeps its standard name', () => {
    const { options, throttler } = throttlerOf();
    expect(options.throttlers).toHaveLength(1);
    expect(throttler.name).toBe('default');
  });

  it('converts the window to milliseconds', () => {
    expect(throttlerOf({ windowSeconds: 30 }).throttler.ttl).toBe(30_000);
  });

  it('gives strict routes the tight limit and every other route the general one', () => {
    const { throttler } = throttlerOf();
    expect(throttler.limit(strict)).toBe(10);
    expect(throttler.limit(general)).toBe(100);
  });

  it('counts strict routes per route and client, and general routes per client only', () => {
    const { throttler } = throttlerOf();
    const key = throttler.generateKey as (c: ExecutionContext, client: string, name: string) => string;
    expect(key(strict, '1.2.3.4', 'default')).toBe('default:ExampleController:login:1.2.3.4');
    expect(key(general, '1.2.3.4', 'default')).toBe('default:1.2.3.4');
    expect(key(general, '5.6.7.8', 'default')).not.toBe(key(general, '1.2.3.4', 'default'));
  });

  it('skips every request when the limit is disabled', () => {
    const { options } = throttlerOf({ enabled: false });
    expect(options.skipIf?.(general)).toBe(true);
    expect(throttlerOf().options.skipIf?.(general)).toBe(false);
  });
});
