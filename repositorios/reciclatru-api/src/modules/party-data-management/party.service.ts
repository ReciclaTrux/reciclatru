import { Injectable, Logger } from '@nestjs/common';
import { PartyRole } from '../../common/domain/enums.js';
import {
  BadRequestAppException,
  ConflictAppException,
  NotFoundAppException,
} from '../../common/errors/app.exception.js';
import { ErrorCode } from '../../common/errors/error-code.js';
import { isUniqueViolation } from '../../common/errors/database-error.translator.js';
import { PasswordHasher } from '../../common/security/password-hasher.service.js';
import { PhoneProtector } from '../../common/security/phone-protector.service.js';
import { LocationService } from '../location-data-management/location.service.js';
import type { PickupPoint } from '../location-data-management/pickup-point.js';
import type { PickupPointDto, RegisterPartyDto, UpdateProfileDto } from './dto/party.dto.js';
import { PartyRepository } from './party.repository.js';
import type { AccountRow, Contact, PartyRecord, ProfileRow } from './party.types.js';

/** Accounts, profiles and the contact data shared between counterparts. */
@Injectable()
export class PartyService {
  private readonly logger = new Logger(PartyService.name);

  constructor(
    private readonly parties: PartyRepository,
    private readonly locations: LocationService,
    private readonly hasher: PasswordHasher,
    private readonly phones: PhoneProtector,
  ) {}

  /** HU-01: creates the account (email lowercase, password hashed with bcrypt). */
  async register(input: RegisterPartyDto): Promise<PartyRecord> {
    const passwordHash = await this.hasher.hash(input.password);
    try {
      const account = await this.parties.insertAccount(input.email, passwordHash, input.role);
      this.logger.log({ event: 'user.registered', userId: account.id, role: account.role });
      return this.toRecord(account, null, null);
    } catch (error) {
      if (isUniqueViolation(error, 'uq_app_user_email'))
        throw new ConflictAppException(ErrorCode.EMAIL_ALREADY_REGISTERED);
      throw error;
    }
  }

  findAccountByEmail(email: string): Promise<AccountRow | null> {
    return this.parties.findAccountByEmail(email);
  }

  /** Builds the full record of one user, with the real phone (only call it for the owner). */
  async retrieve(userId: string): Promise<PartyRecord> {
    const account = await this.parties.findAccountById(userId);
    if (!account) throw new NotFoundAppException();
    const [profile, pickupPoint] = await Promise.all([
      this.parties.findProfile(userId),
      account.role === PartyRole.NEIGHBOR ? this.locations.getCurrent(userId) : Promise.resolve(null),
    ]);
    return this.toRecord(account, profile, pickupPoint);
  }

  /** HU-03: completes or edits the profile; name and phone must both exist after the merge. */
  async updateProfile(userId: string, input: UpdateProfileDto): Promise<PartyRecord> {
    if (input.fullName === undefined && input.phone === undefined && input.reference === undefined) {
      throw new BadRequestAppException('Envía al menos un campo.', [
        { field: 'fullName', message: 'Nada que actualizar' },
      ]);
    }
    const account = await this.parties.findAccountById(userId);
    if (!account) throw new NotFoundAppException();
    if (input.reference !== undefined && account.role !== PartyRole.NEIGHBOR) {
      throw new BadRequestAppException('La referencia es solo para vecinos.', [
        { field: 'reference', message: 'Solo los vecinos pueden enviar una referencia' },
      ]);
    }

    const current = await this.parties.findProfile(userId);
    const fullName = input.fullName ?? current?.fullName;
    const phoneEncrypted = input.phone
      ? this.phones.encrypt(input.phone)
      : (current?.phoneEncrypted ?? undefined);
    const phoneMasked = input.phone ? this.phones.mask(input.phone) : current?.phoneMasked;
    this.assertRequired(fullName, phoneEncrypted);

    await this.parties.saveProfile(userId, {
      fullName: fullName as string,
      phoneMasked: phoneMasked as string,
      phoneEncrypted: phoneEncrypted as string,
      reference: input.reference === undefined ? (current?.reference ?? null) : input.reference || null,
    });
    this.logger.log({ event: 'profile.updated', userId, changed: this.changedFields(input) });
    return this.retrieve(userId);
  }

  /** HU-04: stores the pickup point and returns the refreshed profile. */
  async savePickupPoint(userId: string, input: PickupPointDto): Promise<PartyRecord> {
    await this.locations.saveCurrent(userId, {
      latitude: input.latitude,
      longitude: input.longitude,
      address: input.address ?? null,
      district: input.district ?? null,
    });
    this.logger.log({ event: 'location.updated', userId });
    return this.retrieve(userId);
  }

  /** Real contact data of a user, or null when the profile is incomplete. */
  async getContact(userId: string): Promise<Contact | null> {
    const profile = await this.parties.findProfile(userId);
    return profile ? this.toContact(profile) : null;
  }

  /** Same as `getContact` for several users at once, keyed by user id. */
  async getContacts(userIds: string[]): Promise<Map<string, Contact>> {
    const contacts = new Map<string, Contact>();
    if (userIds.length === 0) return contacts;
    for (const profile of await this.parties.findProfiles(userIds)) {
      const contact = this.toContact(profile);
      if (contact) contacts.set(profile.userId, contact);
    }
    return contacts;
  }

  /** The neighbor's live reference note, shown only to the chosen recycler. */
  async getReference(userId: string): Promise<string | null> {
    return (await this.parties.findProfile(userId))?.reference ?? null;
  }

  /** RN20: neighbor needs name, phone and pickup point; recycler needs name and phone. */
  async isProfileComplete(userId: string): Promise<boolean> {
    const record = await this.retrieve(userId);
    return record.profileComplete;
  }

  private changedFields(input: UpdateProfileDto): string[] {
    return (['fullName', 'phone', 'reference'] as const).filter((field) => input[field] !== undefined);
  }

  private assertRequired(fullName: string | undefined, phoneEncrypted: string | undefined): void {
    const errors = [
      ...(fullName ? [] : [{ field: 'fullName', message: 'El nombre es obligatorio' }]),
      ...(phoneEncrypted ? [] : [{ field: 'phone', message: 'El celular es obligatorio' }]),
    ];
    if (errors.length > 0) throw new BadRequestAppException('Completa nombre y celular.', errors);
  }

  private toContact(profile: ProfileRow): Contact | null {
    if (!profile.phoneEncrypted) return null;
    return { fullName: profile.fullName, phone: this.phones.decrypt(profile.phoneEncrypted) };
  }

  private toRecord(
    account: AccountRow,
    profile: ProfileRow | null,
    pickupPoint: PickupPoint | null,
  ): PartyRecord {
    const hasContact = Boolean(profile?.phoneEncrypted);
    const needsPoint = account.role === PartyRole.NEIGHBOR;
    return {
      id: account.id,
      email: account.email,
      role: account.role,
      fullName: profile?.fullName ?? null,
      phone: profile?.phoneEncrypted ? this.phones.decrypt(profile.phoneEncrypted) : null,
      reference: profile?.reference ?? null,
      pickupPoint,
      profileComplete: hasContact && (!needsPoint || pickupPoint !== null),
      createdAt: account.createdAt.toISOString(),
    };
  }
}
