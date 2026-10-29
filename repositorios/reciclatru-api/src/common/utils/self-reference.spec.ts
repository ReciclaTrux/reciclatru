import { describe, expect, it } from '@jest/globals';
import { NotFoundAppException } from '../errors/app.exception.js';
import { PartyRole } from '../domain/enums.js';
import { resolveSelfId, uuidPipe } from './self-reference.js';

const user = { id: '7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8', role: PartyRole.NEIGHBOR };

describe('resolveSelfId', () => {
  it('maps the `me` alias and the own id to the caller', () => {
    expect(resolveSelfId('me', user)).toBe(user.id);
    expect(resolveSelfId(user.id, user)).toBe(user.id);
  });

  it('treats any other id as not found', () => {
    expect(() => resolveSelfId('3f2b8a2e-5d7c-4a39-8b6e-1c9d0e4f7a21', user)).toThrow(NotFoundAppException);
  });
});

describe('uuidPipe', () => {
  it('passes a valid uuid and turns a malformed one into 404', async () => {
    await expect(uuidPipe.transform(user.id, { type: 'param' })).resolves.toBe(user.id);
    await expect(uuidPipe.transform('abc', { type: 'param' })).rejects.toBeInstanceOf(NotFoundAppException);
  });
});
