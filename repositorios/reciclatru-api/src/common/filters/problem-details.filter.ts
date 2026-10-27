import { type ArgumentsHost, Catch, type ExceptionFilter, HttpStatus, Logger } from '@nestjs/common';
import type { Response } from 'express';
import { ProblemDetailsFactory } from '../errors/problem-details.js';
import type { RequestContext } from '../utils/request-context.js';

/** Global filter: every error leaves the API as `application/problem+json` with the correlation id. */
@Catch()
export class ProblemDetailsFilter implements ExceptionFilter {
  private readonly logger = new Logger('ExceptionFilter');

  constructor(private readonly problems: ProblemDetailsFactory) {}

  catch(exception: unknown, host: ArgumentsHost): void {
    const http = host.switchToHttp();
    const request = http.getRequest<RequestContext>();
    const response = http.getResponse<Response>();
    const problem = this.problems.create(
      exception,
      request.originalUrl ?? request.url,
      request.correlationId,
    );

    if (problem.status >= HttpStatus.INTERNAL_SERVER_ERROR) {
      this.logger.error(
        `[${request.correlationId}] ${request.method} ${problem.instance} -> ${problem.status}`,
        (exception as Error)?.stack,
      );
    }
    response.status(problem.status).type('application/problem+json').json(problem);
  }
}
