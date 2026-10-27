import type { PartyRole } from '../domain/enums.js';

/** Claims stored in the access token. */
export interface JwtPayload {
  sub: string;
  role: PartyRole;
  iat: number;
  exp: number;
}
