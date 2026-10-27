import { Inject, Injectable } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import * as bcrypt from 'bcrypt';
import { appConfig } from '../../config/app.config.js';

/** Hashes and verifies passwords with bcrypt. */
@Injectable()
export class PasswordHasher {
  /** Valid hash of a random value, used to keep login timing equal when the email does not exist. */
  private readonly decoyHash: string;

  constructor(@Inject(appConfig.KEY) private readonly config: ConfigType<typeof appConfig>) {
    this.decoyHash = bcrypt.hashSync(`decoy-${Math.random()}`, this.config.security.bcryptRounds);
  }

  hash(plainPassword: string): Promise<string> {
    return bcrypt.hash(plainPassword, this.config.security.bcryptRounds);
  }

  /** Compares against the stored hash; with `null` it still spends the same time and returns false. */
  async verify(plainPassword: string, hash: string | null): Promise<boolean> {
    const matches = await bcrypt.compare(plainPassword, hash ?? this.decoyHash);
    return hash !== null && matches;
  }
}
