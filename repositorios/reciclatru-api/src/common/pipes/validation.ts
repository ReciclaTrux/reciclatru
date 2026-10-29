import { ValidationPipe, type ValidationError } from '@nestjs/common';
import { BadRequestAppException, type FieldError } from '../errors/app.exception.js';

/** Flattens nested class-validator errors into `items[0].weightKg`-style field paths. */
export function flattenValidationErrors(errors: ValidationError[], parent = ''): FieldError[] {
  return errors.flatMap((error) => {
    const path = parent
      ? /^\d+$/.test(error.property)
        ? `${parent}[${error.property}]`
        : `${parent}.${error.property}`
      : error.property;
    const own = Object.values(error.constraints ?? {}).map((message) => ({ field: path, message }));
    return [...own, ...flattenValidationErrors(error.children ?? [], path)];
  });
}

/** Global pipe: strips unknown fields, rejects them, converts types and reports errors as VALIDATION_ERROR. */
export function createValidationPipe(): ValidationPipe {
  return new ValidationPipe({
    transform: true,
    whitelist: true,
    forbidNonWhitelisted: true,
    exceptionFactory: (errors) =>
      new BadRequestAppException('Revisa los campos indicados.', flattenValidationErrors(errors)),
  });
}
