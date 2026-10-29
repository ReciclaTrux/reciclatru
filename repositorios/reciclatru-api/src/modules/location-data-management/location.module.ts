import { Module } from '@nestjs/common';
import { LocationRepository } from './location.repository.js';
import { LocationService } from './location.service.js';

@Module({ providers: [LocationRepository, LocationService], exports: [LocationService] })
export class LocationModule {}
