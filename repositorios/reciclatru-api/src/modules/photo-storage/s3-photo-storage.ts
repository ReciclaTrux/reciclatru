import { DeleteObjectCommand, GetObjectCommand, PutObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { Inject, Injectable } from '@nestjs/common';
import { type ConfigType } from '@nestjs/config';
import { appConfig } from '../../config/app.config.js';
import { PhotoStorage } from './photo-storage.js';

/** S3-compatible implementation (Neon Object Storage, Cloudflare R2, AWS S3). */
@Injectable()
export class S3PhotoStorage extends PhotoStorage {
  private readonly client: S3Client;
  private readonly bucket: string;

  constructor(@Inject(appConfig.KEY) config: ConfigType<typeof appConfig>) {
    super();
    const { endpoint, region, bucket, accessKeyId, secretAccessKey, forcePathStyle } = config.storage;
    this.bucket = bucket;
    this.client = new S3Client({
      endpoint,
      region,
      forcePathStyle,
      credentials: { accessKeyId, secretAccessKey },
      requestChecksumCalculation: 'WHEN_REQUIRED',
      responseChecksumValidation: 'WHEN_REQUIRED',
      maxAttempts: 4,
    });
  }

  async put(key: string, body: Buffer, contentType: string): Promise<void> {
    await this.client.send(
      new PutObjectCommand({ Bucket: this.bucket, Key: key, Body: body, ContentType: contentType }),
    );
  }

  async remove(key: string): Promise<void> {
    await this.client.send(new DeleteObjectCommand({ Bucket: this.bucket, Key: key }));
  }

  signedUrl(key: string, ttlSeconds: number): Promise<string> {
    return getSignedUrl(this.client, new GetObjectCommand({ Bucket: this.bucket, Key: key }), {
      expiresIn: ttlSeconds,
    });
  }
}
