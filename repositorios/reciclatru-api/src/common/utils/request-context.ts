import type { Request } from 'express';
import type { PartyRole } from '../domain/enums.js';

export const CORRELATION_HEADER = 'x-correlation-id';

export interface AuthenticatedUser {
  id: string;
  role: PartyRole;
}

/** Express request enriched by the correlation middleware and the JWT guard. */
export type RequestContext = Request & { correlationId?: string; user?: AuthenticatedUser };
