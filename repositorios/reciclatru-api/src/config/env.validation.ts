import 'reflect-metadata';
import { plainToInstance, Transform } from 'class-transformer';
import {
  IsBoolean,
  IsInt,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsUrl,
  Max,
  Min,
  MinLength,
  validateSync,
} from 'class-validator';

/** Reads the original string: implicit conversion would turn 'false' into true before this runs. */
const toBoolean = ({ obj, key, value }: { obj: Record<string, unknown>; key: string; value: unknown }) => {
  const original = obj[key];
  return typeof original === 'string' ? original.toLowerCase() === 'true' : value;
};

/** Environment variables accepted by the API. Missing required values stop the boot with a clear message. */
export class EnvironmentVariables {
  @IsOptional() @IsString() NODE_ENV?: string = 'development';
  @IsInt() @Min(1) @Max(65535) PORT: number = 3000;

  @IsString() @IsNotEmpty() DATABASE_URL!: string;
  @Transform(toBoolean) @IsBoolean() DATABASE_SSL: boolean = true;
  @IsInt() @Min(1) @Max(50) DATABASE_POOL_MAX: number = 10;

  @IsString() @MinLength(32) JWT_SECRET!: string;
  @IsInt() @Min(1) @Max(365) JWT_EXPIRES_IN_DAYS: number = 30;
  @IsInt() @Min(4) @Max(15) BCRYPT_ROUNDS: number = 10;
  @IsString() @IsNotEmpty() PHONE_ENCRYPTION_KEY!: string;

  @IsUrl({ require_tld: false }) S3_ENDPOINT!: string;
  @IsString() @IsNotEmpty() S3_REGION!: string;
  @IsString() @IsNotEmpty() S3_BUCKET!: string;
  @IsString() @IsNotEmpty() S3_ACCESS_KEY_ID!: string;
  @IsString() @IsNotEmpty() S3_SECRET_ACCESS_KEY!: string;
  @Transform(toBoolean) @IsBoolean() S3_FORCE_PATH_STYLE: boolean = false;

  @IsInt() @Min(30) @Max(3600) PHOTO_URL_TTL_SECONDS: number = 300;
  @IsInt() @Min(1024) PHOTO_MAX_BYTES: number = 2 * 1024 * 1024;

  /** Comma-separated browser origins allowed to call the API; empty keeps CORS off. `*` may appear inside an origin (http://localhost:*). */
  @IsOptional() @IsString() CORS_ORIGINS?: string;

  @IsUrl({ require_tld: false }) ERROR_TYPE_BASE_URL: string = 'https://api.reciclatru.example.com/errors';
}

const AES_KEY_BYTES = 32;

/** Validates and normalizes `process.env`; used by `ConfigModule.forRoot({ validate })`. */
export function validateEnv(raw: Record<string, unknown>): EnvironmentVariables {
  const env = plainToInstance(EnvironmentVariables, raw, {
    enableImplicitConversion: true,
    exposeDefaultValues: true,
  });
  const errors = validateSync(env, { skipMissingProperties: false });
  const problems = errors.flatMap((error) => Object.values(error.constraints ?? {}));
  if (Buffer.from(env.PHONE_ENCRYPTION_KEY ?? '', 'base64').length !== AES_KEY_BYTES) {
    problems.push('PHONE_ENCRYPTION_KEY must be a base64-encoded 32-byte key');
  }
  if (problems.length > 0) {
    throw new Error(`Invalid environment configuration:\n - ${problems.join('\n - ')}`);
  }
  return env;
}
