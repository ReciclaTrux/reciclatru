import { Injectable } from '@nestjs/common';
import { LocationRepository } from './location.repository.js';
import type { PickupPoint } from './pickup-point.js';

const COORDINATE_DECIMALS = 6;

/** Business logic for the neighbor's pickup point. */
@Injectable()
export class LocationService {
  constructor(private readonly locations: LocationRepository) {}

  getCurrent(userId: string): Promise<PickupPoint | null> {
    return this.locations.findCurrent(userId);
  }

  /** Stores the point with coordinates rounded to 6 decimals and blank text turned into null. */
  async saveCurrent(userId: string, input: PickupPoint): Promise<PickupPoint> {
    const point: PickupPoint = {
      latitude: this.round(input.latitude),
      longitude: this.round(input.longitude),
      address: this.clean(input.address),
      district: this.clean(input.district),
    };
    await this.locations.replaceCurrent(userId, point);
    return point;
  }

  private round(value: number): number {
    return Number(value.toFixed(COORDINATE_DECIMALS));
  }

  private clean(value: string | null | undefined): string | null {
    const trimmed = value?.trim();
    return trimmed ? trimmed : null;
  }
}
