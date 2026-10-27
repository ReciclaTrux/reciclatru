import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import { stubOf } from '../../../test/support/stubs.js';
import { testConfig } from '../../../test/support/config.js';
import { PartyRole } from '../../common/domain/enums.js';
import {
  BadRequestAppException,
  ConflictAppException,
  NotFoundAppException,
} from '../../common/errors/app.exception.js';
import { PasswordHasher } from '../../common/security/password-hasher.service.js';
import { PhoneProtector } from '../../common/security/phone-protector.service.js';
import { LocationService } from '../location-data-management/location.service.js';
import { PartyRepository } from './party.repository.js';
import { PartyService } from './party.service.js';
import type { AccountRow } from './party.types.js';
import { QueryFailedError } from 'typeorm';

const phones = new PhoneProtector(testConfig());
const account = (role: PartyRole): AccountRow => ({
  id: 'u1',
  email: 'rosa@example.com',
  passwordHash: 'hash',
  role,
  createdAt: new Date('2026-10-26T14:05:00Z'),
});

describe('PartyService', () => {
  let repo: ReturnType<typeof stubOf<PartyRepository>>;
  let locations: ReturnType<typeof stubOf<LocationService>>;
  let hasher: ReturnType<typeof stubOf<PasswordHasher>>;
  let service: PartyService;

  beforeEach(() => {
    repo = stubOf<PartyRepository>(
      'insertAccount',
      'findAccountByEmail',
      'findAccountById',
      'findProfile',
      'findProfiles',
      'saveProfile',
    );
    locations = stubOf<LocationService>('getCurrent', 'saveCurrent');
    hasher = stubOf<PasswordHasher>('hash', 'verify');
    service = new PartyService(repo, locations, hasher, phones);
  });

  describe('register', () => {
    it('hashes the password and creates an incomplete record', async () => {
      hasher.hash.mockResolvedValue('bcrypt-hash');
      repo.insertAccount.mockResolvedValue(account(PartyRole.NEIGHBOR));
      const record = await service.register({
        email: 'rosa@example.com',
        password: 'Reciclatru2026',
        role: PartyRole.NEIGHBOR,
      });
      expect(repo.insertAccount).toHaveBeenCalledWith('rosa@example.com', 'bcrypt-hash', PartyRole.NEIGHBOR);
      expect(record).toMatchObject({
        id: 'u1',
        profileComplete: false,
        fullName: null,
        phone: null,
        pickupPoint: null,
      });
    });

    it('turns a duplicated email into 409 EMAIL_ALREADY_REGISTERED', async () => {
      hasher.hash.mockResolvedValue('h');
      const duplicate = new QueryFailedError(
        'INSERT',
        [],
        Object.assign(new Error('dup'), { code: '23505', constraint: 'uq_app_user_email' }),
      );
      repo.insertAccount.mockRejectedValue(duplicate);
      await expect(
        service.register({ email: 'a@b.com', password: 'x'.repeat(8), role: PartyRole.RECYCLER }),
      ).rejects.toBeInstanceOf(ConflictAppException);
    });

    it('does not hide unexpected database errors', async () => {
      hasher.hash.mockResolvedValue('h');
      repo.insertAccount.mockRejectedValue(new Error('connection lost'));
      await expect(
        service.register({ email: 'a@b.com', password: 'x'.repeat(8), role: PartyRole.RECYCLER }),
      ).rejects.toThrow('connection lost');
    });
  });

  describe('updateProfile', () => {
    it('stores the phone masked and encrypted, never in clear', async () => {
      repo.findAccountById.mockResolvedValue(account(PartyRole.NEIGHBOR));
      repo.findProfile.mockResolvedValueOnce(null);
      await service.updateProfile('u1', {
        fullName: 'Rosa Elena',
        phone: '987654321',
        reference: 'Portón verde',
      });
      const [, written] = repo.saveProfile.mock.calls[0] as [
        string,
        { phoneMasked: string; phoneEncrypted: string; reference: string },
      ];
      expect(written.phoneMasked).toBe('987****21');
      expect(written.phoneEncrypted).not.toContain('987654321');
      expect(phones.decrypt(written.phoneEncrypted)).toBe('987654321');
      expect(written.reference).toBe('Portón verde');
    });

    it('returns the real phone and a complete profile once name, phone and point exist', async () => {
      const encrypted = phones.encrypt('987654321');
      repo.findAccountById.mockResolvedValue(account(PartyRole.NEIGHBOR));
      repo.findProfile
        .mockResolvedValueOnce(null)
        .mockResolvedValue({
          fullName: 'Rosa Elena',
          phoneMasked: '987****21',
          phoneEncrypted: encrypted,
          reference: null,
        });
      locations.getCurrent.mockResolvedValue({
        latitude: -8.07,
        longitude: -79.12,
        address: null,
        district: 'Huanchaco',
      });
      const record = await service.updateProfile('u1', { fullName: 'Rosa Elena', phone: '987654321' });
      expect(record).toMatchObject({ phone: '987654321', profileComplete: true });
    });

    it('merges a partial update with the stored profile', async () => {
      const encrypted = phones.encrypt('987654321');
      repo.findAccountById.mockResolvedValue(account(PartyRole.NEIGHBOR));
      repo.findProfile.mockResolvedValue({
        fullName: 'Rosa Elena',
        phoneMasked: '987****21',
        phoneEncrypted: encrypted,
        reference: 'Antes',
      });
      locations.getCurrent.mockResolvedValue(null);
      await service.updateProfile('u1', { fullName: 'Rosa E. Carranza' });
      expect(repo.saveProfile).toHaveBeenCalledWith('u1', {
        fullName: 'Rosa E. Carranza',
        phoneMasked: '987****21',
        phoneEncrypted: encrypted,
        reference: 'Antes',
      });
    });

    it('clears the reference when an empty one is sent', async () => {
      repo.findAccountById.mockResolvedValue(account(PartyRole.NEIGHBOR));
      repo.findProfile.mockResolvedValue({
        fullName: 'Rosa',
        phoneMasked: '987****21',
        phoneEncrypted: phones.encrypt('987654321'),
        reference: 'Antes',
      });
      locations.getCurrent.mockResolvedValue(null);
      await service.updateProfile('u1', { reference: '' });
      expect((repo.saveProfile.mock.calls[0] as unknown[])[1]).toMatchObject({ reference: null });
    });

    it('requires name and phone to exist after the merge', async () => {
      repo.findAccountById.mockResolvedValue(account(PartyRole.RECYCLER));
      repo.findProfile.mockResolvedValue(null);
      const error = await service.updateProfile('u1', { fullName: 'Jorge Mendoza' }).catch((e: unknown) => e);
      expect(error).toBeInstanceOf(BadRequestAppException);
      expect((error as BadRequestAppException).errors).toEqual([
        { field: 'phone', message: 'El celular es obligatorio' },
      ]);
      expect(repo.saveProfile).not.toHaveBeenCalled();
    });

    it('treats a legacy profile without encrypted phone as missing the phone', async () => {
      repo.findAccountById.mockResolvedValue(account(PartyRole.RECYCLER));
      repo.findProfile.mockResolvedValue({
        fullName: 'Jorge',
        phoneMasked: '944****56',
        phoneEncrypted: null,
        reference: null,
      });
      await expect(service.updateProfile('u1', { fullName: 'Jorge Mendoza' })).rejects.toBeInstanceOf(
        BadRequestAppException,
      );
    });

    it('rejects an empty body, a reference for recyclers and an unknown user', async () => {
      await expect(service.updateProfile('u1', {})).rejects.toBeInstanceOf(BadRequestAppException);
      repo.findAccountById.mockResolvedValue(account(PartyRole.RECYCLER));
      await expect(service.updateProfile('u1', { reference: 'x' })).rejects.toBeInstanceOf(
        BadRequestAppException,
      );
      repo.findAccountById.mockResolvedValue(null);
      await expect(service.updateProfile('u1', { fullName: 'Ana' })).rejects.toBeInstanceOf(
        NotFoundAppException,
      );
    });
  });

  describe('retrieve and profile completeness (RN20)', () => {
    const profile = {
      fullName: 'Rosa',
      phoneMasked: '987****21',
      phoneEncrypted: phones.encrypt('987654321'),
      reference: null,
    };

    it('a neighbor needs a pickup point, a recycler does not', async () => {
      repo.findProfile.mockResolvedValue(profile);
      locations.getCurrent.mockResolvedValue(null);
      repo.findAccountById.mockResolvedValue(account(PartyRole.NEIGHBOR));
      expect(await service.isProfileComplete('u1')).toBe(false);
      repo.findAccountById.mockResolvedValue(account(PartyRole.RECYCLER));
      expect(await service.isProfileComplete('u1')).toBe(true);
    });

    it('does not look up the pickup point for recyclers', async () => {
      repo.findAccountById.mockResolvedValue(account(PartyRole.RECYCLER));
      repo.findProfile.mockResolvedValue(profile);
      await service.retrieve('u1');
      expect(locations.getCurrent).not.toHaveBeenCalled();
    });

    it('throws 404 for an unknown user', async () => {
      repo.findAccountById.mockResolvedValue(null);
      await expect(service.retrieve('nobody')).rejects.toBeInstanceOf(NotFoundAppException);
    });
  });

  describe('contacts', () => {
    it('decrypts the real phone for authorized lookups', async () => {
      repo.findProfile.mockResolvedValue({
        fullName: 'Jorge',
        phoneMasked: '944****56',
        phoneEncrypted: phones.encrypt('944123456'),
        reference: 'Portón',
      });
      expect(await service.getContact('u2')).toEqual({ fullName: 'Jorge', phone: '944123456' });
      expect(await service.getReference('u2')).toBe('Portón');
    });

    it('returns null for missing profiles and skips them in bulk lookups', async () => {
      repo.findProfile.mockResolvedValue(null);
      expect(await service.getContact('u3')).toBeNull();
      repo.findProfiles.mockResolvedValue([
        {
          userId: 'a',
          fullName: 'A',
          phoneMasked: '911****33',
          phoneEncrypted: phones.encrypt('911222333'),
          reference: null,
        },
        { userId: 'b', fullName: 'B', phoneMasked: '922****44', phoneEncrypted: null, reference: null },
      ]);
      const contacts = await service.getContacts(['a', 'b']);
      expect([...contacts.keys()]).toEqual(['a']);
      expect(await service.getContacts([])).toEqual(new Map());
    });
  });

  it('savePickupPoint stores the point and returns the refreshed record', async () => {
    repo.findAccountById.mockResolvedValue(account(PartyRole.NEIGHBOR));
    repo.findProfile.mockResolvedValue(null);
    locations.saveCurrent.mockResolvedValue({});
    locations.getCurrent.mockResolvedValue({
      latitude: -8.07,
      longitude: -79.12,
      address: null,
      district: null,
    });
    const record = await service.savePickupPoint('u1', { latitude: -8.07, longitude: -79.12 });
    expect(locations.saveCurrent).toHaveBeenCalledWith('u1', {
      latitude: -8.07,
      longitude: -79.12,
      address: null,
      district: null,
    });
    expect(record.pickupPoint).not.toBeNull();
    expect(jest.isMockFunction(locations.saveCurrent)).toBe(true);
  });
});
