import { describe, expect, it, jest } from '@jest/globals';
import type { Request, Response } from 'express';
import { CorrelationIdMiddleware } from './correlation-id.middleware.js';

function run(header?: string) {
  const request = {
    header: (name: string) => (name === 'x-correlation-id' ? header : undefined),
  } as unknown as Request & { correlationId?: string };
  const setHeader = jest.fn();
  const next = jest.fn();
  new CorrelationIdMiddleware().use(request, { setHeader } as unknown as Response, next);
  return { request, setHeader, next };
}

describe('CorrelationIdMiddleware', () => {
  it('keeps a valid incoming id and echoes it', () => {
    const { request, setHeader, next } = run('trace-abc_1.2:3');
    expect(request.correlationId).toBe('trace-abc_1.2:3');
    expect(setHeader).toHaveBeenCalledWith('x-correlation-id', 'trace-abc_1.2:3');
    expect(next).toHaveBeenCalledTimes(1);
  });

  it.each([[undefined], [''], ['has spaces'], ['x'.repeat(101)], ['<script>']])(
    'generates a uuid for %p',
    (header) => {
      const { request } = run(header);
      expect(request.correlationId).toMatch(
        /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/,
      );
    },
  );
});
