import { createCipheriv, createDecipheriv, randomBytes } from 'node:crypto';
import { Inject, Injectable } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { appConfig } from '../../config/app.config.js';

const ALGORITHM = 'aes-256-gcm';
const IV_BYTES = 12;
const VISIBLE_PREFIX = 3;
const VISIBLE_SUFFIX = 2;

/** Protects phone numbers at rest: a masked copy for display and an AES-256-GCM encrypted copy to recover it. */
@Injectable()
export class PhoneProtector {
  private readonly key: Buffer;

  constructor(@Inject(appConfig.KEY) config: ConfigType<typeof appConfig>) {
    this.key = Buffer.from(config.security.phoneEncryptionKey, 'base64');
  }

  /** `987654321` becomes `987****21`. */
  mask(phone: string): string {
    return `${phone.slice(0, VISIBLE_PREFIX)}****${phone.slice(-VISIBLE_SUFFIX)}`;
  }

  /** Returns `iv.authTag.data` in base64, a different value on every call. */
  encrypt(phone: string): string {
    const iv = randomBytes(IV_BYTES);
    const cipher = createCipheriv(ALGORITHM, this.key, iv);
    const data = Buffer.concat([cipher.update(phone, 'utf8'), cipher.final()]);
    return [iv, cipher.getAuthTag(), data].map((part) => part.toString('base64')).join('.');
  }

  /** Recovers the phone; throws if the payload was altered or the key is wrong. */
  decrypt(payload: string): string {
    const [iv, authTag, data] = payload.split('.').map((part) => Buffer.from(part, 'base64'));
    const decipher = createDecipheriv(ALGORITHM, this.key, iv);
    decipher.setAuthTag(authTag);
    return Buffer.concat([decipher.update(data), decipher.final()]).toString('utf8');
  }
}
