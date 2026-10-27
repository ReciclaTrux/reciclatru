import { describe, expect, it } from '@jest/globals';
import { BadRequestAppException } from '../errors/app.exception.js';
import { CursorCodec, toPage } from './cursor.codec.js';

const ID = '5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24';
const isKeyset = (v: Record<string, unknown>) => CursorCodec.isTimestamp(v.c) && CursorCodec.isUuid(v.i);

describe('CursorCodec', () => {
  it('round-trips a payload as an opaque base64url string', () => {
    const cursor = CursorCodec.encode({ c: '2026-10-23 14:10:00.123456+00', i: ID });
    expect(cursor).toMatch(/^[A-Za-z0-9_-]+$/);
    expect(CursorCodec.decode(cursor, isKeyset)).toEqual({ c: '2026-10-23 14:10:00.123456+00', i: ID });
  });

  it.each([
    ['not base64 json', 'basura'],
    ['an array', Buffer.from('[1,2]').toString('base64url')],
    ['missing fields', Buffer.from('{}').toString('base64url')],
    [
      'an injection attempt',
      Buffer.from(JSON.stringify({ c: "2026-01-01'; DROP TABLE x;--", i: ID })).toString('base64url'),
    ],
  ])('rejects %s with a 400 on the cursor field', (_name, cursor) => {
    try {
      CursorCodec.decode(cursor, isKeyset);
      throw new Error('should have thrown');
    } catch (error) {
      expect(error).toBeInstanceOf(BadRequestAppException);
      expect((error as BadRequestAppException).errors).toEqual([
        { field: 'cursor', message: 'Cursor inválido' },
      ]);
    }
  });

  it('validates timestamps, uuids and numbers', () => {
    expect(CursorCodec.isTimestamp('2026-10-23 14:10:00+00')).toBe(true);
    expect(CursorCodec.isTimestamp('2026-10-23T14:10:00.123456Z')).toBe(true);
    expect(CursorCodec.isTimestamp('yesterday')).toBe(false);
    expect(CursorCodec.isUuid(ID)).toBe(true);
    expect(CursorCodec.isUuid('123')).toBe(false);
    expect(CursorCodec.isFiniteNumber(3.4)).toBe(true);
    expect(CursorCodec.isFiniteNumber(Number.NaN)).toBe(false);
    expect(CursorCodec.isFiniteNumber('3')).toBe(false);
  });
});

describe('toPage', () => {
  const rows = [1, 2, 3];

  it('returns every row and no cursor when there is no extra row', () => {
    expect(toPage(rows, 3, (n) => ({ n }))).toEqual({ items: [1, 2, 3], nextCursor: null });
  });

  it('drops the extra row and builds the cursor from the last returned one', () => {
    const page = toPage(rows, 2, (n) => ({ n }));
    expect(page.items).toEqual([1, 2]);
    expect(CursorCodec.decode(page.nextCursor as string, (v) => v.n === 2)).toEqual({ n: 2 });
  });

  it('handles an empty result', () => {
    expect(toPage([], 20, () => ({}))).toEqual({ items: [], nextCursor: null });
  });
});
