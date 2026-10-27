import { describe, expect, it } from '@jest/globals';
import { BadRequestException, HttpException, NotFoundException } from '@nestjs/common';
import { QueryFailedError } from 'typeorm';
import { testConfig } from '../../../test/support/config.js';
import {
  BadRequestAppException,
  ConflictAppException,
  ForbiddenAppException,
  NotFoundAppException,
  PayloadTooLargeAppException,
  UnauthorizedAppException,
  UnprocessableAppException,
} from './app.exception.js';
import { ErrorCode } from './error-code.js';
import { ProblemDetailsFactory } from './problem-details.js';

const factory = new ProblemDetailsFactory(testConfig());

function dbError(code: string, message = '', constraint?: string): QueryFailedError {
  return new QueryFailedError('SELECT', [], Object.assign(new Error(message), { code, constraint }));
}

describe('ProblemDetailsFactory', () => {
  it.each([
    [new BadRequestAppException('x', [{ field: 'email', message: 'bad' }]), 400, ErrorCode.VALIDATION_ERROR],
    [new UnauthorizedAppException(), 401, ErrorCode.UNAUTHENTICATED],
    [new ForbiddenAppException(ErrorCode.NOT_ORDER_OWNER), 403, ErrorCode.NOT_ORDER_OWNER],
    [new NotFoundAppException(), 404, ErrorCode.NOT_FOUND],
    [new ConflictAppException(ErrorCode.ORDER_NOT_AVAILABLE), 409, ErrorCode.ORDER_NOT_AVAILABLE],
    [new PayloadTooLargeAppException(), 413, ErrorCode.PHOTO_TOO_LARGE],
    [new UnprocessableAppException(ErrorCode.INVALID_ITEMS), 422, ErrorCode.INVALID_ITEMS],
  ])('maps %s to its status and code', (exception, status, code) => {
    const problem = factory.create(exception, '/api/v1/x', 'trace-1');
    expect(problem).toMatchObject({ status, code, instance: '/api/v1/x', correlationId: 'trace-1' });
    expect(problem.type).toBe(`https://api.test/errors/${code.toLowerCase().replaceAll('_', '-')}`);
  });

  it('includes field errors and detail only when present', () => {
    const withErrors = factory.create(
      new BadRequestAppException('Revisa', [{ field: 'a', message: 'b' }]),
      '/x',
    );
    expect(withErrors).toMatchObject({ detail: 'Revisa', errors: [{ field: 'a', message: 'b' }] });
    const bare = factory.create(new NotFoundAppException(), '/x');
    expect(bare).not.toHaveProperty('errors');
    expect(bare).not.toHaveProperty('detail');
    expect(bare).not.toHaveProperty('correlationId');
  });

  it('translates database errors raised by triggers', () => {
    expect(factory.create(dbError('40001', 'ORDER_NOT_AVAILABLE'), '/x')).toMatchObject({
      status: 409,
      code: 'ORDER_NOT_AVAILABLE',
    });
    expect(factory.create(dbError('40001', 'ORDER_NOT_PENDING'), '/x')).toMatchObject({
      status: 409,
      code: 'ORDER_NOT_PENDING',
    });
    expect(factory.create(dbError('40001', 'other'), '/x')).toMatchObject({
      status: 409,
      code: 'CONCURRENT_UPDATE',
    });
    expect(factory.create(dbError('42501', 'NOT_ORDER_OWNER'), '/x')).toMatchObject({
      status: 403,
      code: 'NOT_ORDER_OWNER',
    });
    expect(factory.create(dbError('42501', 'FORBIDDEN_ROLE'), '/x')).toMatchObject({
      status: 403,
      code: 'FORBIDDEN_ROLE',
    });
    expect(factory.create(dbError('P0002'), '/x')).toMatchObject({ status: 404 });
    expect(factory.create(dbError('23505', '', 'uq_app_user_email'), '/x')).toMatchObject({
      status: 409,
      code: 'EMAIL_ALREADY_REGISTERED',
    });
    expect(factory.create(dbError('23514', 'ORDER_ITEM_LIMIT_REACHED'), '/x')).toMatchObject({
      status: 422,
      code: 'INVALID_ITEMS',
    });
    expect(factory.create(dbError('23514', '', 'ck_order_item_weight'), '/x')).toMatchObject({
      status: 422,
      code: 'INVALID_WEIGHT',
    });
  });

  it('hides unknown database errors behind a generic 500', () => {
    const problem = factory.create(dbError('XX000', 'relation "secret_table" does not exist'), '/x');
    expect(problem).toMatchObject({ status: 500, code: 'INTERNAL_ERROR' });
    expect(JSON.stringify(problem)).not.toContain('secret_table');
  });

  it('maps framework HTTP exceptions', () => {
    expect(factory.create(new BadRequestException('Bad JSON'), '/x')).toMatchObject({
      status: 400,
      code: 'VALIDATION_ERROR',
      detail: 'Bad JSON',
    });
    expect(factory.create(new NotFoundException('Cannot GET /x'), '/x')).toMatchObject({
      status: 404,
      code: 'NOT_FOUND',
    });
    expect(factory.create(new NotFoundException('Cannot GET /x'), '/x')).not.toHaveProperty('detail');
    expect(factory.create(new HttpException('Teapot', 418), '/x')).toMatchObject({
      status: 418,
      code: 'HTTP_ERROR',
    });
  });

  it('maps the multer size error to PHOTO_TOO_LARGE', () => {
    expect(factory.create(new HttpException('File too large', 413), '/x')).toMatchObject({
      status: 413,
      code: 'PHOTO_TOO_LARGE',
    });
  });

  it('maps body-parser style errors by status and hides everything else', () => {
    expect(factory.create({ status: 400, message: 'Unexpected token' }, '/x')).toMatchObject({
      status: 400,
      code: 'VALIDATION_ERROR',
    });
    expect(factory.create(new Error('boom: password=123'), '/x')).toMatchObject({
      status: 500,
      code: 'INTERNAL_ERROR',
    });
    expect(JSON.stringify(factory.create(new Error('boom: password=123'), '/x'))).not.toContain('password');
    expect(factory.create(undefined, '/x')).toMatchObject({ status: 500 });
  });
});
