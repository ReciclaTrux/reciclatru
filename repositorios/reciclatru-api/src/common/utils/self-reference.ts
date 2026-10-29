import { ParseUUIDPipe } from '@nestjs/common';
import { NotFoundAppException } from '../errors/app.exception.js';
import type { AuthenticatedUser } from './request-context.js';

export const SELF_ALIAS = 'me';

/** Pipe for `:id` path params: a malformed id is simply a resource that does not exist. */
export const uuidPipe = new ParseUUIDPipe({ exceptionFactory: () => new NotFoundAppException() });

/** Resolves the BIAN `cr-reference-id` of the caller: `me` or the caller's own id; anything else is a 404. */
export function resolveSelfId(crReferenceId: string, user: AuthenticatedUser): string {
  if (crReferenceId === SELF_ALIAS || crReferenceId === user.id) return user.id;
  throw new NotFoundAppException();
}
