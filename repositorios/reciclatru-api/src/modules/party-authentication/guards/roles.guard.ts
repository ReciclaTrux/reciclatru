import { type CanActivate, type ExecutionContext, Injectable } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { ROLES_KEY } from '../../../common/decorators/auth.decorators.js';
import type { PartyRole } from '../../../common/domain/enums.js';
import { ForbiddenAppException } from '../../../common/errors/app.exception.js';
import type { RequestContext } from '../../../common/utils/request-context.js';

/** Global guard: enforces `@Roles(...)` after the JWT guard has identified the caller. */
@Injectable()
export class RolesGuard implements CanActivate {
  constructor(private readonly reflector: Reflector) {}

  canActivate(context: ExecutionContext): boolean {
    const roles = this.reflector.getAllAndOverride<PartyRole[] | undefined>(ROLES_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (!roles || roles.length === 0) return true;
    const user = context.switchToHttp().getRequest<RequestContext>().user;
    if (!user || !roles.includes(user.role)) throw new ForbiddenAppException();
    return true;
  }
}
