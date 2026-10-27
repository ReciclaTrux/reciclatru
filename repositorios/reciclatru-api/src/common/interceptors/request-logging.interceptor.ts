import {
  type CallHandler,
  type ExecutionContext,
  Injectable,
  Logger,
  type NestInterceptor,
} from '@nestjs/common';
import type { Response } from 'express';
import { type Observable, tap } from 'rxjs';
import type { RequestContext } from '../utils/request-context.js';

/** Logs one line per request with method, path, status, duration and correlation id. */
@Injectable()
export class RequestLoggingInterceptor implements NestInterceptor {
  private readonly logger = new Logger('HTTP');

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const http = context.switchToHttp();
    const request = http.getRequest<RequestContext>();
    const response = http.getResponse<Response>();
    const startedAt = Date.now();
    return next.handle().pipe(
      tap({
        next: () => this.log(request, response.statusCode, startedAt),
        error: () => this.log(request, undefined, startedAt),
      }),
    );
  }

  private log(request: RequestContext, status: number | undefined, startedAt: number): void {
    const duration = Date.now() - startedAt;
    this.logger.log(
      `[${request.correlationId}] ${request.method} ${request.originalUrl} ${status ?? 'error'} ${duration}ms`,
    );
  }
}
