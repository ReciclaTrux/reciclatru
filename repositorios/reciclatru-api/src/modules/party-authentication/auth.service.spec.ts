import { beforeEach, describe, expect, it } from '@jest/globals';
import { JwtService } from '@nestjs/jwt';
import { stubOf } from '../../../test/support/stubs.js';
import { PartyRole } from '../../common/domain/enums.js';
import { UnauthorizedAppException } from '../../common/errors/app.exception.js';
import { PasswordHasher } from '../../common/security/password-hasher.service.js';
import { PartyService } from '../party-data-management/party.service.js';
import { AuthService } from './auth.service.js';

const THIRTY_DAYS = 30 * 86_400;

describe('AuthService', () => {
  const jwt = new JwtService({ secret: 'k'.repeat(48), signOptions: { expiresIn: THIRTY_DAYS } });
  let parties: ReturnType<typeof stubOf<PartyService>>;
  let hasher: ReturnType<typeof stubOf<PasswordHasher>>;
  let service: AuthService;

  beforeEach(() => {
    parties = stubOf<PartyService>('findAccountByEmail', 'retrieve');
    hasher = stubOf<PasswordHasher>('verify');
    service = new AuthService(parties, hasher, jwt);
  });

  it('issues a token valid for 30 days with the account id and role', async () => {
    parties.findAccountByEmail.mockResolvedValue({
      id: 'u1',
      email: 'a@b.com',
      passwordHash: 'h',
      role: PartyRole.RECYCLER,
      createdAt: new Date(),
    });
    parties.retrieve.mockResolvedValue({ id: 'u1' });
    hasher.verify.mockResolvedValue(true);

    const result = await service.login('a@b.com', 'Reciclatru2026');
    const payload = jwt.decode<{ sub: string; role: string; iat: number; exp: number }>(result.accessToken);

    expect(payload).toMatchObject({ sub: 'u1', role: PartyRole.RECYCLER });
    expect(payload.exp - payload.iat).toBe(THIRTY_DAYS);
    expect(result).toMatchObject({
      crReferenceId: 'u1',
      tokenType: 'Bearer',
      partyDataManagementInstanceRecord: { id: 'u1' },
    });
    expect(new Date(result.expiresAt).getTime()).toBe(payload.exp * 1000);
  });

  it('gives the same error for a wrong password and for an unknown email', async () => {
    parties.findAccountByEmail.mockResolvedValueOnce({
      id: 'u1',
      email: 'a@b.com',
      passwordHash: 'h',
      role: PartyRole.NEIGHBOR,
      createdAt: new Date(),
    });
    hasher.verify.mockResolvedValue(false);
    const wrongPassword = await service.login('a@b.com', 'bad').catch((e: unknown) => e);

    parties.findAccountByEmail.mockResolvedValueOnce(null);
    const unknownEmail = await service.login('none@b.com', 'bad').catch((e: unknown) => e);

    expect(wrongPassword).toBeInstanceOf(UnauthorizedAppException);
    expect((wrongPassword as UnauthorizedAppException).detail).toBe(
      (unknownEmail as UnauthorizedAppException).detail,
    );
    expect(hasher.verify).toHaveBeenLastCalledWith('bad', null);
  });
});
