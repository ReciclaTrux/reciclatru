import { Transform } from 'class-transformer';
import {
  IsEmail,
  IsEnum,
  IsLatitude,
  IsLongitude,
  IsNumber,
  IsOptional,
  IsString,
  Matches,
  MaxLength,
  MinLength,
} from 'class-validator';
import { PartyRole } from '../../../common/domain/enums.js';

const normalizeEmail = ({ value }: { value: unknown }) =>
  typeof value === 'string' ? value.trim().toLowerCase() : value;
const trim = ({ value }: { value: unknown }) => (typeof value === 'string' ? value.trim() : value);

export class RegisterPartyDto {
  @Transform(normalizeEmail) @IsEmail() @MaxLength(254) email!: string;
  @IsString() @MinLength(8) @MaxLength(72) password!: string;
  @IsEnum(PartyRole) role!: PartyRole;
}

export class UpdateProfileDto {
  @IsOptional() @Transform(trim) @IsString() @MinLength(2) @MaxLength(80) fullName?: string;
  @IsOptional()
  @IsString()
  @Matches(/^9\d{8}$/, { message: 'phone debe tener 9 dígitos y empezar con 9' })
  phone?: string;
  @IsOptional() @Transform(trim) @IsString() @MaxLength(150) reference?: string;
}

export class PickupPointDto {
  @IsNumber({ maxDecimalPlaces: 15 }) @IsLatitude() latitude!: number;
  @IsNumber({ maxDecimalPlaces: 15 }) @IsLongitude() longitude!: number;
  @IsOptional() @IsString() @MaxLength(200) address?: string | null;
  @IsOptional() @IsString() @MaxLength(80) district?: string | null;
}
