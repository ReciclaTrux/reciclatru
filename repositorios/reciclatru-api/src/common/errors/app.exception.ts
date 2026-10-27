import { HttpException, HttpStatus } from '@nestjs/common';
import { ERROR_TITLES, ErrorCode } from './error-code.js';

export interface FieldError {
  field: string;
  message: string;
}

/** Base of every business error. The exception filter renders it as `application/problem+json`. */
export class AppException extends HttpException {
  constructor(
    readonly code: ErrorCode,
    status: HttpStatus,
    readonly detail?: string,
    readonly errors?: FieldError[],
  ) {
    super(ERROR_TITLES[code], status);
  }

  get title(): string {
    return ERROR_TITLES[this.code];
  }
}

/** HTTP 400: malformed or invalid input. */
export class BadRequestAppException extends AppException {
  constructor(detail?: string, errors?: FieldError[]) {
    super(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, detail, errors);
  }
}

/** HTTP 401: missing, invalid or expired credentials. */
export class UnauthorizedAppException extends AppException {
  constructor(detail?: string) {
    super(ErrorCode.UNAUTHENTICATED, HttpStatus.UNAUTHORIZED, detail);
  }
}

type ForbiddenCode = ErrorCode.FORBIDDEN_ROLE | ErrorCode.NOT_ORDER_OWNER | ErrorCode.NOT_CHOSEN_RECYCLER;

/** HTTP 403: authenticated but not allowed. */
export class ForbiddenAppException extends AppException {
  constructor(code: ForbiddenCode = ErrorCode.FORBIDDEN_ROLE, detail?: string) {
    super(code, HttpStatus.FORBIDDEN, detail);
  }
}

/** HTTP 404: resource does not exist (or is not visible to the caller). */
export class NotFoundAppException extends AppException {
  constructor(detail?: string) {
    super(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, detail);
  }
}

type ConflictCode =
  | ErrorCode.EMAIL_ALREADY_REGISTERED
  | ErrorCode.ORDER_NOT_AVAILABLE
  | ErrorCode.ORDER_NOT_EDITABLE
  | ErrorCode.ORDER_NOT_CANCELLABLE
  | ErrorCode.ORDER_NOT_PENDING
  | ErrorCode.ORDER_NOT_CONFIRMED
  | ErrorCode.CONCURRENT_UPDATE;

/** HTTP 409: the request conflicts with the current state of the resource. */
export class ConflictAppException extends AppException {
  constructor(code: ConflictCode, detail?: string) {
    super(code, HttpStatus.CONFLICT, detail);
  }
}

/** HTTP 413: uploaded photo exceeds the allowed size. */
export class PayloadTooLargeAppException extends AppException {
  constructor(
    code: ErrorCode.PHOTO_TOO_LARGE | ErrorCode.PAYLOAD_TOO_LARGE = ErrorCode.PHOTO_TOO_LARGE,
    detail?: string,
  ) {
    super(code, HttpStatus.PAYLOAD_TOO_LARGE, detail);
  }
}

type UnprocessableCode =
  | ErrorCode.PROFILE_INCOMPLETE
  | ErrorCode.INVALID_ITEMS
  | ErrorCode.INVALID_WEIGHT
  | ErrorCode.PHOTO_LIMIT_REACHED
  | ErrorCode.PHOTO_INVALID_TYPE;

/** HTTP 422: the request is well formed but breaks a business rule. */
export class UnprocessableAppException extends AppException {
  constructor(code: UnprocessableCode, detail?: string, errors?: FieldError[]) {
    super(code, HttpStatus.UNPROCESSABLE_ENTITY, detail, errors);
  }
}
