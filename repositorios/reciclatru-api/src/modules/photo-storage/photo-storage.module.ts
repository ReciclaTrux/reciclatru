import { Module } from '@nestjs/common';
import { PhotoStorage } from './photo-storage.js';
import { S3PhotoStorage } from './s3-photo-storage.js';

@Module({
  providers: [{ provide: PhotoStorage, useClass: S3PhotoStorage }],
  exports: [PhotoStorage],
})
export class PhotoStorageModule {}
