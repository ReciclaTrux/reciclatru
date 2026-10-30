import { describe, expect, it, jest } from '@jest/globals';
import { createCorsOptions, parseCorsOrigins } from './cors.js';

function isAllowed(origins: string[], origin: string | undefined): boolean {
  const options = createCorsOptions(origins);
  if (!options || typeof options.origin !== 'function') throw new Error('CORS should be enabled');
  const callback = jest.fn();
  options.origin(origin, callback as never);
  return callback.mock.calls[0]?.[1] as boolean;
}

describe('parseCorsOrigins', () => {
  it('returns nothing when the variable is empty or missing', () => {
    expect(parseCorsOrigins(undefined)).toEqual([]);
    expect(parseCorsOrigins('  ')).toEqual([]);
  });

  it('splits by comma, trims spaces and drops trailing slashes', () => {
    expect(parseCorsOrigins(' https://app.reciclatru.com/ , http://localhost:5000 ')).toEqual([
      'https://app.reciclatru.com',
      'http://localhost:5000',
    ]);
  });

  it('refuses a bare wildcard', () => {
    expect(() => parseCorsOrigins('https://a.com,*')).toThrow(/explicit origins/);
  });
});

describe('createCorsOptions', () => {
  it('stays off when no origin is configured', () => {
    expect(createCorsOptions([])).toBe(false);
  });

  it('allows only the listed origins', () => {
    const origins = ['https://app.reciclatru.com'];
    expect(isAllowed(origins, 'https://app.reciclatru.com')).toBe(true);
    expect(isAllowed(origins, 'https://evil.example.com')).toBe(false);
    expect(isAllowed(origins, 'https://app.reciclatru.com.evil.com')).toBe(false);
    expect(isAllowed(origins, 'http://app.reciclatru.com')).toBe(false);
  });

  it('lets a wildcard cover the port of localhost, and nothing else', () => {
    const origins = ['http://localhost:*'];
    expect(isAllowed(origins, 'http://localhost:58231')).toBe(true);
    expect(isAllowed(origins, 'http://localhost')).toBe(false);
    expect(isAllowed(origins, 'http://localhost.evil.com:80')).toBe(false);
    expect(isAllowed(origins, 'https://localhost:5000')).toBe(false);
  });

  it('treats dots in an origin literally', () => {
    expect(isAllowed(['https://app.reciclatru.com'], 'https://appxreciclatru.com')).toBe(false);
  });

  it('does not block requests without Origin (native apps, curl)', () => {
    expect(isAllowed(['https://app.reciclatru.com'], undefined)).toBe(true);
  });

  it('allows the headers the API needs and exposes the correlation id', () => {
    const options = createCorsOptions(['https://a.com']);
    expect(options).toMatchObject({
      allowedHeaders: expect.arrayContaining(['Authorization', 'Content-Type', 'x-correlation-id']),
      exposedHeaders: ['x-correlation-id'],
      credentials: false,
    });
  });
});
