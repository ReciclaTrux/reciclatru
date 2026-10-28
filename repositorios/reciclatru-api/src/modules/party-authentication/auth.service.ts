import { Injectable, Logger } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { UnauthorizedAppException } from '../../common/errors/app.exception.js';
import type { JwtPayload } from '../../common/security/jwt-payload.js';
import { PasswordHasher } from '../../common/security/password-hasher.service.js';
import { PartyService } from '../party-data-management/party.service.js';
import type { PartyRecord } from '../party-data-management/party.types.js';

export interface LoginResult {
  crReferenceId: string;
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  partyDataManagementInstanceRecord: PartyRecord;
}

const MILLIS_PER_SECOND = 1000;

/** HU-02: credential check and JWT issuing. */
@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);

  constructor(
    private readonly parties: PartyService,
    private readonly hasher: PasswordHasher,
    private readonly jwt: JwtService,
  ) {}

  /** The error never says whether the email or the password was wrong. */
  async login(email: string, password: string): Promise<LoginResult> {
    const account = await this.parties.findAccountByEmail(email);
    const valid = await this.hasher.verify(password, account?.passwordHash ?? null);
    if (!account || !valid) {
      this.logger.warn({
        event: 'auth.login.failed',
        reason: account ? 'wrong-password' : 'unknown-account',
      });
      throw new UnauthorizedAppException('Correo o contraseña incorrectos.');
    }

    const accessToken = await this.jwt.signAsync({ sub: account.id, role: account.role });
    const { exp } = this.jwt.decode<JwtPayload>(accessToken);
    this.logger.log({ event: 'auth.login.succeeded', userId: account.id, role: account.role });
    return {
      crReferenceId: account.id,
      accessToken,
      tokenType: 'Bearer',
      expiresAt: new Date(exp * MILLIS_PER_SECOND).toISOString(),
      partyDataManagementInstanceRecord: await this.parties.retrieve(account.id),
    };
  }
}
