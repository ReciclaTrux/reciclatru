import { EventEmitter } from 'node:events';
import { afterEach, describe, expect, it, jest } from '@jest/globals';
import { Logger } from '@nestjs/common';
import type { NextFunction, Response } from 'express';
import type { RequestContext } from '../common/utils/request-context.js';
import { RequestLogMiddleware } from './request-log.middleware.js';

interface Scenario {
  status: number;
  route?: string;
  user?: { id: string; role: 'NEIGHBOR' | 'RECYCLER' };
  errorCode?: string;
  finished?: boolean;
}

function run({ status, route, user, errorCode, finished = true }: Scenario) {
  const spies = {
    log: jest.spyOn(Logger.prototype, 'log').mockImplementation(() => undefined),
    warn: jest.spyOn(Logger.prototype, 'warn').mockImplementation(() => undefined),
    error: jest.spyOn(Logger.prototype, 'error').mockImplementation(() => undefined),
    debug: jest.spyOn(Logger.prototype, 'debug').mockImplementation(() => undefined),
  };
  const response = Object.assign(new EventEmitter(), {
    statusCode: status,
    writableFinished: finished,
    locals: errorCode ? { errorCode } : {},
  });
  const request = {
    method: 'GET',
    originalUrl: '/api/v1/servicing-order/123e4567/retrieval?cursor=SECRET',
    route: route ? { path: route } : undefined,
    correlationId: 'corr-1',
    user,
  } as unknown as RequestContext;
  const next = jest.fn() as unknown as NextFunction;

  new RequestLogMiddleware().use(request, response as unknown as Response, next);
  response.emit('close');
  return { spies, next };
}

describe('RequestLogMiddleware', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('continues the chain and logs when the response closes', () => {
    const { spies, next } = run({ status: 200, route: '/api/v1/servicing-order/:orderId/retrieval' });
    expect(next).toHaveBeenCalledTimes(1);
    expect(spies.log).toHaveBeenCalledWith(
      expect.objectContaining({
        event: 'http.request',
        method: 'GET',
        route: '/api/v1/servicing-order/:orderId/retrieval',
        status: 200,
        correlationId: 'corr-1',
        durationMs: expect.any(Number),
      }),
    );
  });

  it('never writes the real URL, ids or cursors', () => {
    const { spies } = run({ status: 200, route: '/api/v1/servicing-order/:orderId/retrieval' });
    expect(JSON.stringify(spies.log.mock.calls)).not.toMatch(/SECRET|123e4567/);
  });

  it('includes the user and role when the guard authenticated the request', () => {
    const { spies } = run({ status: 200, route: '/r', user: { id: 'u-1', role: 'RECYCLER' } });
    expect(spies.log).toHaveBeenCalledWith(expect.objectContaining({ userId: 'u-1', role: 'RECYCLER' }));
  });

  it('uses warn with the error code for 4xx, including rejected requests', () => {
    const { spies } = run({ status: 401, route: '/r', errorCode: 'UNAUTHENTICATED' });
    expect(spies.warn).toHaveBeenCalledWith(
      expect.objectContaining({ status: 401, errorCode: 'UNAUTHENTICATED' }),
    );
    expect(spies.log).not.toHaveBeenCalled();
  });

  it('uses error for 5xx', () => {
    const { spies } = run({ status: 500, route: '/r', errorCode: 'INTERNAL_ERROR' });
    expect(spies.error).toHaveBeenCalledWith(expect.objectContaining({ status: 500 }));
  });

  it.each([[undefined], ['*path']])('reports %p as an unmatched route', (route) => {
    const { spies } = run({ status: 404, route });
    expect(spies.warn).toHaveBeenCalledWith(expect.objectContaining({ route: 'unmatched' }));
  });

  it('keeps health checks out of the default level', () => {
    const { spies } = run({ status: 200, route: '/api/v1/health' });
    expect(spies.debug).toHaveBeenCalledTimes(1);
    expect(spies.log).not.toHaveBeenCalled();
  });

  it('flags requests the client aborted', () => {
    const { spies } = run({ status: 200, route: '/r', finished: false });
    expect(spies.log).toHaveBeenCalledWith(expect.objectContaining({ aborted: true }));
  });
});
