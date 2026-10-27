import { QueryFailedError } from 'typeorm';
import {
  AppException,
  ConflictAppException,
  ForbiddenAppException,
  NotFoundAppException,
  UnprocessableAppException,
} from './app.exception.js';
import { ErrorCode } from './error-code.js';

interface DriverError {
  code?: string;
  message?: string;
  constraint?: string;
}

const SERIALIZATION_FAILURE = '40001';
const INSUFFICIENT_PRIVILEGE = '42501';
const NO_DATA_FOUND = 'P0002';
const UNIQUE_VIOLATION = '23505';
const CHECK_VIOLATION = '23514';

/** Converts the errors raised by the database triggers and constraints into business exceptions. */
export function translateDatabaseError(error: QueryFailedError): AppException | null {
  const driver = (error.driverError ?? {}) as DriverError;
  const message = driver.message ?? '';
  switch (driver.code) {
    case SERIALIZATION_FAILURE:
      if (message.includes(ErrorCode.ORDER_NOT_AVAILABLE))
        return new ConflictAppException(ErrorCode.ORDER_NOT_AVAILABLE);
      if (message.includes(ErrorCode.ORDER_NOT_PENDING))
        return new ConflictAppException(ErrorCode.ORDER_NOT_PENDING);
      return new ConflictAppException(ErrorCode.CONCURRENT_UPDATE);
    case INSUFFICIENT_PRIVILEGE:
      if (message.includes(ErrorCode.NOT_ORDER_OWNER))
        return new ForbiddenAppException(ErrorCode.NOT_ORDER_OWNER);
      return new ForbiddenAppException(ErrorCode.FORBIDDEN_ROLE);
    case NO_DATA_FOUND:
      return new NotFoundAppException();
    case UNIQUE_VIOLATION:
      if (driver.constraint === 'uq_app_user_email')
        return new ConflictAppException(ErrorCode.EMAIL_ALREADY_REGISTERED);
      return new ConflictAppException(ErrorCode.CONCURRENT_UPDATE);
    case CHECK_VIOLATION:
      if (message.includes('ORDER_ITEM_LIMIT_REACHED'))
        return new UnprocessableAppException(ErrorCode.INVALID_ITEMS);
      if (driver.constraint === 'ck_order_item_weight')
        return new UnprocessableAppException(ErrorCode.INVALID_WEIGHT);
      return null;
    default:
      return null;
  }
}

/** True when the failure is a unique violation on the given constraint. */
export function isUniqueViolation(error: unknown, constraint: string): boolean {
  if (!(error instanceof QueryFailedError)) return false;
  const driver = (error.driverError ?? {}) as DriverError;
  return driver.code === UNIQUE_VIOLATION && driver.constraint === constraint;
}
