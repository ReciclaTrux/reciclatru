import { Inject, Injectable } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { HttpException, HttpStatus } from '@nestjs/common';
import { QueryFailedError } from 'typeorm';
import { appConfig } from '../../config/app.config.js';
import { AppException, type FieldError } from './app.exception.js';
import { translateDatabaseError } from './database-error.translator.js';
import { ERROR_TITLES, ErrorCode } from './error-code.js';

/** RFC 7807 body returned for every error (`application/problem+json`). */
export interface ProblemDetails {
  type: string;
  title: string;
  status: number;
  code: ErrorCode;
  detail?: string;
  instance: string;
  errors?: FieldError[];
  correlationId?: string;
}

const MULTER_FILE_TOO_LARGE = 'File too large';

const STATUS_CODES: Partial<Record<number, ErrorCode>> = {
  [HttpStatus.BAD_REQUEST]: ErrorCode.VALIDATION_ERROR,
  [HttpStatus.UNAUTHORIZED]: ErrorCode.UNAUTHENTICATED,
  [HttpStatus.FORBIDDEN]: ErrorCode.FORBIDDEN_ROLE,
  [HttpStatus.NOT_FOUND]: ErrorCode.NOT_FOUND,
  [HttpStatus.PAYLOAD_TOO_LARGE]: ErrorCode.PAYLOAD_TOO_LARGE,
};

interface RawHttpError {
  status?: number;
  statusCode?: number;
  type?: string;
  message?: string;
}

/** Turns any thrown value into a ProblemDetails object. Unknown errors never leak internals. */
@Injectable()
export class ProblemDetailsFactory {
  constructor(@Inject(appConfig.KEY) private readonly config: ConfigType<typeof appConfig>) {}

  /** Builds the problem for an exception raised while serving `instance`. */
  create(exception: unknown, instance: string, correlationId?: string): ProblemDetails {
    const resolved = this.resolve(exception);
    return {
      type: `${this.config.errorTypeBaseUrl}/${resolved.code.toLowerCase().replaceAll('_', '-')}`,
      title: ERROR_TITLES[resolved.code],
      status: resolved.status,
      code: resolved.code,
      ...(resolved.detail ? { detail: resolved.detail } : {}),
      instance,
      ...(resolved.errors?.length ? { errors: resolved.errors } : {}),
      ...(correlationId ? { correlationId } : {}),
    };
  }

  private resolve(exception: unknown): {
    status: number;
    code: ErrorCode;
    detail?: string;
    errors?: FieldError[];
  } {
    if (exception instanceof AppException) {
      return {
        status: exception.getStatus(),
        code: exception.code,
        detail: exception.detail,
        errors: exception.errors,
      };
    }
    if (exception instanceof QueryFailedError) {
      const translated = translateDatabaseError(exception);
      if (translated) return this.resolve(translated);
      return this.internal();
    }
    if (exception instanceof HttpException) {
      return this.fromHttpException(exception);
    }
    return this.fromRawError(exception as RawHttpError);
  }

  private fromHttpException(exception: HttpException) {
    const status = exception.getStatus();
    const message = exception.message;
    if (status === HttpStatus.PAYLOAD_TOO_LARGE && message === MULTER_FILE_TOO_LARGE) {
      return { status, code: ErrorCode.PHOTO_TOO_LARGE };
    }
    return {
      status,
      code: STATUS_CODES[status] ?? ErrorCode.HTTP_ERROR,
      detail: this.safeDetail(status, message),
    };
  }

  private fromRawError(error: RawHttpError) {
    const status = error?.status ?? error?.statusCode;
    if (typeof status === 'number' && status >= 400 && status < 500) {
      return {
        status,
        code: STATUS_CODES[status] ?? ErrorCode.HTTP_ERROR,
        detail: this.safeDetail(status, error.message),
      };
    }
    return this.internal();
  }

  private safeDetail(status: number, message?: string): string | undefined {
    return status === HttpStatus.NOT_FOUND ? undefined : message;
  }

  private internal() {
    return { status: HttpStatus.INTERNAL_SERVER_ERROR, code: ErrorCode.INTERNAL_ERROR };
  }
}
