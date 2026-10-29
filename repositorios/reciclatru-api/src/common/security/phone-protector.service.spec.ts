import { describe, expect, it } from '@jest/globals';
import { testConfig } from '../../../test/support/config.js';
import { PhoneProtector } from './phone-protector.service.js';

describe('PhoneProtector', () => {
  const protector = new PhoneProtector(testConfig());

  it('masks a phone keeping the first three and last two digits', () => {
    expect(protector.mask('987654321')).toBe('987****21');
    expect(protector.mask('912345678')).toBe('912****78');
  });

  it('encrypts and decrypts back to the same phone', () => {
    expect(protector.decrypt(protector.encrypt('987654321'))).toBe('987654321');
  });

  it('never repeats the ciphertext and never contains the plain phone', () => {
    const first = protector.encrypt('987654321');
    const second = protector.encrypt('987654321');
    expect(first).not.toBe(second);
    expect(first).not.toContain('987654321');
  });

  it('rejects a tampered payload', () => {
    const [iv, tag, data] = protector.encrypt('987654321').split('.');
    const flipped = Buffer.from(data, 'base64');
    flipped[0] ^= 0xff;
    expect(() => protector.decrypt([iv, tag, flipped.toString('base64')].join('.'))).toThrow();
  });

  it('rejects a payload encrypted with another key', () => {
    const other = new PhoneProtector(
      testConfig({
        security: { ...testConfig().security, phoneEncryptionKey: Buffer.alloc(32, 9).toString('base64') },
      }),
    );
    expect(() => protector.decrypt(other.encrypt('987654321'))).toThrow();
  });
});
