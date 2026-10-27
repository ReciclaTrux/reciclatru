import { describe, expect, it } from '@jest/globals';
import { BadRequestAppException } from '../errors/app.exception.js';
import { toGeoPoint } from './geo-point.js';

describe('toGeoPoint', () => {
  it('returns null when neither coordinate is sent', () => {
    expect(toGeoPoint()).toBeNull();
  });

  it('returns the point when both are sent, including zero', () => {
    expect(toGeoPoint(-8.1, -79.0)).toEqual({ lat: -8.1, lng: -79.0 });
    expect(toGeoPoint(0, 0)).toEqual({ lat: 0, lng: 0 });
  });

  it('rejects a single coordinate and names the missing one', () => {
    expect(() => toGeoPoint(-8.1, undefined)).toThrow(BadRequestAppException);
    try {
      toGeoPoint(undefined, -79);
    } catch (error) {
      expect((error as BadRequestAppException).errors?.[0].field).toBe('lat');
    }
  });
});
