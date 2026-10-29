import { BadRequestAppException } from '../errors/app.exception.js';

export interface GeoPoint {
  lat: number;
  lng: number;
}

/** `lat` and `lng` travel together: both present or both absent. */
export function toGeoPoint(lat?: number, lng?: number): GeoPoint | null {
  if (lat === undefined && lng === undefined) return null;
  if (lat === undefined || lng === undefined) {
    throw new BadRequestAppException('Envía lat y lng juntos.', [
      { field: lat === undefined ? 'lat' : 'lng', message: 'lat y lng deben enviarse juntos' },
    ]);
  }
  return { lat, lng };
}
