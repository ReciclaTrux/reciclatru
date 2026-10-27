import type { PartyRole } from '../../common/domain/enums.js';
import type { PickupPoint } from '../location-data-management/pickup-point.js';

export interface AccountRow {
  id: string;
  email: string;
  passwordHash: string;
  role: PartyRole;
  createdAt: Date;
}

export interface ProfileRow {
  fullName: string;
  phoneMasked: string;
  phoneEncrypted: string | null;
  reference: string | null;
}

export interface ProfileWrite {
  fullName: string;
  phoneMasked: string;
  phoneEncrypted: string;
  reference: string | null;
}

/** Contact data revealed to an authorized counterpart (HU-12, HU-13). */
export interface Contact {
  fullName: string;
  phone: string;
}

/** Shape of `partyDataManagementInstanceRecord` in the API. */
export interface PartyRecord {
  id: string;
  email: string;
  role: PartyRole;
  fullName: string | null;
  phone: string | null;
  reference: string | null;
  pickupPoint: PickupPoint | null;
  profileComplete: boolean;
  createdAt: string;
}
