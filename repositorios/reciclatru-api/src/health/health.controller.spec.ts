import { describe, expect, it } from '@jest/globals';
import { ServiceUnavailableException } from '@nestjs/common';
import { stubOf } from '../../test/support/stubs.js';
import { testConfig } from '../../test/support/config.js';
import { Database } from '../database/database.service.js';
import { HealthController } from './health.controller.js';

describe('HealthController', () => {
  it('reports UP with the version when the database answers', async () => {
    const db = stubOf<Database>('ping');
    db.ping.mockResolvedValue(undefined);
    await expect(new HealthController(db, testConfig()).check()).resolves.toEqual({
      status: 'UP',
      version: '1.0.0',
    });
  });

  it('answers 503 when the database is unreachable', async () => {
    const db = stubOf<Database>('ping');
    db.ping.mockRejectedValue(new Error('ECONNREFUSED'));
    await expect(new HealthController(db, testConfig()).check()).rejects.toBeInstanceOf(
      ServiceUnavailableException,
    );
  });
});
