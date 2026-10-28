import { describe, expect, it } from '@jest/globals';
import { REDACTED, redact } from './redaction.js';

describe('redact', () => {
  it.each(['password', 'newPassword', 'accessToken', 'authorization', 'phone', 'email', 'secretKey'])(
    'masks the key %s',
    (key) => {
      expect(redact({ [key]: 'value', orderId: 'o-1' })).toEqual({ [key]: REDACTED, orderId: 'o-1' });
    },
  );

  it('masks nested objects and arrays', () => {
    expect(redact({ user: { email: 'a@b.com', id: 'u-1' }, list: [{ token: 't' }] })).toEqual({
      user: { email: REDACTED, id: 'u-1' },
      list: [{ token: REDACTED }],
    });
  });

  it('keeps primitives, dates and null untouched', () => {
    const date = new Date();
    expect(redact({ n: 1, ok: true, none: null, at: date })).toEqual({
      n: 1,
      ok: true,
      none: null,
      at: date,
    });
  });

  it('stops at a maximum depth instead of walking huge objects', () => {
    const deep = { a: { b: { c: { d: 1 } } } };
    expect(redact(deep)).toEqual({ a: { b: { c: '[truncated]' } } });
  });
});
