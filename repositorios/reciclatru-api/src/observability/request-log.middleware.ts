import { Injectable, Logger, type NestMiddleware } from '@nestjs/common';
import type { NextFunction, Response } from 'express';
import type { RequestContext } from '../common/utils/request-context.js';

const NANOS_PER_MILLI = 1_000_000n;
const HEALTH_ROUTE = '/api/v1/health';
/** Express reports the catch-all of the middlewares when no controller route matched. */
const CATCH_ALL_ROUTE = '*path';

/**
 * Writes one line per request when the response ends: route template (never the real URL, so ids and
 * cursors stay out of the logs), status, duration, correlation id and user. It lives in a middleware, not
 * an interceptor, so rejected requests (401, 403, unknown routes) are logged too.
 */
@Injectable()
export class RequestLogMiddleware implements NestMiddleware {
  private readonly logger = new Logger('HTTP');

  use(request: RequestContext, response: Response, next: NextFunction): void {
    const startedAt = process.hrtime.bigint();
    response.on('close', () => this.log(request, response, startedAt));
    next();
  }

  private log(request: RequestContext, response: Response, startedAt: bigint): void {
    const durationMs = Number((process.hrtime.bigint() - startedAt) / NANOS_PER_MILLI);
    const matched = (request.route as { path?: string } | undefined)?.path;
    const route = matched && matched !== CATCH_ALL_ROUTE ? matched : 'unmatched';
    const status = response.statusCode;
    const event = {
      event: 'http.request',
      message: `${request.method} ${route} ${status} ${durationMs}ms`,
      method: request.method,
      route,
      status,
      durationMs,
      correlationId: request.correlationId,
      userId: request.user?.id,
      role: request.user?.role,
      errorCode: (response.locals as { errorCode?: string }).errorCode,
      aborted: response.writableFinished ? undefined : true,
    };
    if (status >= 500) this.logger.error(event);
    else if (status >= 400) this.logger.warn(event);
    else if (route === HEALTH_ROUTE) this.logger.debug(event);
    else this.logger.log(event);
  }
}
