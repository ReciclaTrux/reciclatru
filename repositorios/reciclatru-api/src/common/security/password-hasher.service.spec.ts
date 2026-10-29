import { describe, expect, it } from '@jest/globals';
import { testConfig } from '../../../test/support/config.js';
import { PasswordHasher } from './password-hasher.service.js';

describe('PasswordHasher', () => {
  const hasher = new PasswordHasher(testConfig());

  it('hashes with bcrypt and verifies the right password', async () => {
    const hash = await hasher.hash('Reciclatru2026');
    expect(hash).toMatch(/^\$2[aby]\$04\$/);
    expect(hash.length).toBeLessThanOrEqual(72);
    await expect(hasher.verify('Reciclatru2026', hash)).resolves.toBe(true);
  });

  it('rejects a wrong password', async () => {
    const hash = await hasher.hash('Reciclatru2026');
    await expect(hasher.verify('Otra12345', hash)).resolves.toBe(false);
  });

  it('returns false when there is no stored hash, still doing the comparison work', async () => {
    await expect(hasher.verify('Reciclatru2026', null)).resolves.toBe(false);
  });

  it('salts every hash', async () => {
    expect(await hasher.hash('Reciclatru2026')).not.toBe(await hasher.hash('Reciclatru2026'));
  });
});
