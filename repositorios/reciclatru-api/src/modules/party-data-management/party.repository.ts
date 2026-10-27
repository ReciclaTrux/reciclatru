import { Injectable } from '@nestjs/common';
import type { PartyRole } from '../../common/domain/enums.js';
import { Database } from '../../database/database.service.js';
import type { AccountRow, ProfileRow, ProfileWrite } from './party.types.js';

const ACCOUNT_COLUMNS = `id, email, password_hash AS "passwordHash", role, created_at AS "createdAt"`;

/** Data access for accounts and profiles. */
@Injectable()
export class PartyRepository {
  constructor(private readonly db: Database) {}

  async insertAccount(email: string, passwordHash: string, role: PartyRole): Promise<AccountRow> {
    const rows = await this.db.query<AccountRow>(
      `INSERT INTO app_user (email, password_hash, role) VALUES ($1, $2, $3) RETURNING ${ACCOUNT_COLUMNS}`,
      [email, passwordHash, role],
    );
    return rows[0];
  }

  findAccountByEmail(email: string): Promise<AccountRow | null> {
    return this.db.queryOne<AccountRow>(`SELECT ${ACCOUNT_COLUMNS} FROM app_user WHERE lower(email) = $1`, [
      email,
    ]);
  }

  findAccountById(id: string): Promise<AccountRow | null> {
    return this.db.queryOne<AccountRow>(`SELECT ${ACCOUNT_COLUMNS} FROM app_user WHERE id = $1`, [id]);
  }

  findProfile(userId: string): Promise<ProfileRow | null> {
    return this.db.queryOne<ProfileRow>(
      `SELECT full_name AS "fullName", phone AS "phoneMasked", phone_encrypted AS "phoneEncrypted",
              reference_note AS reference
         FROM user_profile WHERE user_id = $1`,
      [userId],
    );
  }

  findProfiles(userIds: string[]): Promise<(ProfileRow & { userId: string })[]> {
    return this.db.query(
      `SELECT user_id AS "userId", full_name AS "fullName", phone AS "phoneMasked",
              phone_encrypted AS "phoneEncrypted", reference_note AS reference
         FROM user_profile WHERE user_id = ANY($1::uuid[])`,
      [userIds],
    );
  }

  async saveProfile(userId: string, profile: ProfileWrite): Promise<void> {
    await this.db.execute(
      `INSERT INTO user_profile (user_id, full_name, phone, phone_encrypted, reference_note)
            VALUES ($1, $2, $3, $4, $5)
       ON CONFLICT (user_id) DO UPDATE
               SET full_name = EXCLUDED.full_name, phone = EXCLUDED.phone,
                   phone_encrypted = EXCLUDED.phone_encrypted, reference_note = EXCLUDED.reference_note`,
      [userId, profile.fullName, profile.phoneMasked, profile.phoneEncrypted, profile.reference],
    );
  }
}
