import { describe, expect, it, jest } from '@jest/globals';
import type { ExecutionContext } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { JwtService } from '@nestjs/jwt';
import { PartyRole } from '../../../common/domain/enums.js';
import { ForbiddenAppException, UnauthorizedAppException } from '../../../common/errors/app.exception.js';
import type { RequestContext } from '../../../common/utils/request-context.js';
import { JwtAuthGuard } from './jwt-auth.guard.js';
import { RolesGuard } from './roles.guard.js';

const SECRET = 'a'.repeat(48);

function contextFor(request: Partial<RequestContext>): ExecutionContext {
  return {
    getHandler: () => () => undefined,
    getClass: () => class {},
    switchToHttp: () => ({ getRequest: () => request }),
  } as unknown as ExecutionContext;
}

function reflectorReturning(value: unknown): Reflector {
  return { getAllAndOverride: jest.fn().mockReturnValue(value) } as unknown as Reflector;
}

describe('JwtAuthGuard', () => {
  const jwt = new JwtService({ secret: SECRET, signOptions: { expiresIn: 60 } });

  it('lets public routes through without a token', async () => {
    const guard = new JwtAuthGuard(reflectorReturning(true), jwt);
    await expect(guard.canActivate(contextFor({ headers: {} }))).resolves.toBe(true);
  });

  it('accepts a valid Bearer token and exposes the user', async () => {
    const token = await jwt.signAsync({ sub: 'user-1', role: PartyRole.RECYCLER });
    const request: Partial<RequestContext> = { headers: { authorization: `Bearer ${token}` } };
    await expect(
      new JwtAuthGuard(reflectorReturning(false), jwt).canActivate(contextFor(request)),
    ).resolves.toBe(true);
    expect(request.user).toEqual({ id: 'user-1', role: PartyRole.RECYCLER });
  });

  it.each([
    ['no header', undefined],
    ['another scheme', 'Basic abc'],
    ['an empty bearer', 'Bearer '],
    ['garbage', 'Bearer not.a.token'],
  ])('rejects %s', async (_name, authorization) => {
    const guard = new JwtAuthGuard(reflectorReturning(false), jwt);
    await expect(guard.canActivate(contextFor({ headers: { authorization } }))).rejects.toBeInstanceOf(
      UnauthorizedAppException,
    );
  });

  it('rejects a token signed with another secret, an expired one and one with an unknown role', async () => {
    const guard = new JwtAuthGuard(reflectorReturning(false), jwt);
    const forged = await new JwtService({ secret: 'b'.repeat(48) }).signAsync({
      sub: 'u',
      role: PartyRole.NEIGHBOR,
    });
    const expired = await jwt.signAsync({ sub: 'u', role: PartyRole.NEIGHBOR }, { expiresIn: -10 });
    const unknownRole = await jwt.signAsync({ sub: 'u', role: 'ADMIN' });
    for (const token of [forged, expired, unknownRole]) {
      await expect(
        guard.canActivate(contextFor({ headers: { authorization: `Bearer ${token}` } })),
      ).rejects.toBeInstanceOf(UnauthorizedAppException);
    }
  });
});

describe('RolesGuard', () => {
  it('allows routes without role restrictions', () => {
    expect(new RolesGuard(reflectorReturning(undefined)).canActivate(contextFor({}))).toBe(true);
  });

  it('allows a caller whose role is listed', () => {
    const request = { user: { id: 'u', role: PartyRole.NEIGHBOR } };
    expect(new RolesGuard(reflectorReturning([PartyRole.NEIGHBOR])).canActivate(contextFor(request))).toBe(
      true,
    );
  });

  it('forbids another role', () => {
    const request = { user: { id: 'u', role: PartyRole.RECYCLER } };
    expect(() =>
      new RolesGuard(reflectorReturning([PartyRole.NEIGHBOR])).canActivate(contextFor(request)),
    ).toThrow(ForbiddenAppException);
  });
});
