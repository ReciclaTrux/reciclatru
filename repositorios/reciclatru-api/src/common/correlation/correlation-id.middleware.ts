import { randomUUID } from 'node:crypto';
import { Injectable, type NestMiddleware } from '@nestjs/common';
import type { NextFunction, Request, Response } from 'express';
import { trace } from '@opentelemetry/api';
import { requestStore } from '../../observability/request-store.js';
import { CORRELATION_HEADER, type RequestContext } from '../utils/request-context.js';

const VALID_CORRELATION_ID = /^[A-Za-z0-9._:-]{1,100}$/;

/** Reads or creates `x-correlation-id`, exposes it on the request, echoes it in the response and tags logs and the active trace with it. */
@Injectable()
export class CorrelationIdMiddleware implements NestMiddleware {
  use(request: Request, response: Response, next: NextFunction): void {
    const incoming = request.header(CORRELATION_HEADER);
    const correlationId = incoming && VALID_CORRELATION_ID.test(incoming) ? incoming : randomUUID();
    (request as RequestContext).correlationId = correlationId;
    response.setHeader(CORRELATION_HEADER, correlationId);
    trace.getActiveSpan()?.setAttribute('app.correlation_id', correlationId);
    requestStore.run({ correlationId }, next);
  }
}
