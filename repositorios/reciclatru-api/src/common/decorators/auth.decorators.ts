import { type ExecutionContext, SetMetadata, createParamDecorator } from '@nestjs/common';
import type { PartyRole } from '../domain/enums.js';
import type { AuthenticatedUser, RequestContext } from '../utils/request-context.js';

export const IS_PUBLIC_KEY = 'isPublic';
export const ROLES_KEY = 'roles';

/** Skips JWT authentication for the route. */
export const Public = () => SetMetadata(IS_PUBLIC_KEY, true);

/** Restricts the route to the given roles. */
export const Roles = (...roles: PartyRole[]) => SetMetadata(ROLES_KEY, roles);

/** Injects the authenticated user taken from the JWT. */
export const CurrentUser = createParamDecorator(
  (_data: unknown, context: ExecutionContext): AuthenticatedUser => {
    return context.switchToHttp().getRequest<RequestContext>().user as AuthenticatedUser;
  },
);
