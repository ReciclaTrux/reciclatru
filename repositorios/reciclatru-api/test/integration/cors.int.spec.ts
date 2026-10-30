import { afterAll, beforeAll, describe, expect, it } from '@jest/globals';
import { createTestApp, type TestApp } from './test-app.js';

const ALLOWED = 'http://localhost:51234';

describe('CORS (PostgreSQL)', () => {
  let t: TestApp;

  beforeAll(async () => {
    t = await createTestApp({ CORS_ORIGINS: 'http://localhost:*,https://app.reciclatru.com' });
  });

  afterAll(async () => {
    await t.app.close();
  });

  it('answers the preflight of an allowed origin without a token', async () => {
    const response = await t
      .http()
      .options('/api/v1/servicing-order/retrieval')
      .set('Origin', ALLOWED)
      .set('Access-Control-Request-Method', 'GET')
      .set('Access-Control-Request-Headers', 'authorization,content-type,x-correlation-id');

    expect(response.status).toBe(204);
    expect(response.headers['access-control-allow-origin']).toBe(ALLOWED);
    expect(response.headers['access-control-allow-headers']).toMatch(/authorization/i);
  });

  it('exposes the correlation id to the browser on real responses', async () => {
    const response = await t.http().get('/api/v1/health').set('Origin', ALLOWED);

    expect(response.status).toBe(200);
    expect(response.headers['access-control-allow-origin']).toBe(ALLOWED);
    expect(response.headers['access-control-expose-headers']).toMatch(/x-correlation-id/i);
  });

  it('does not send CORS headers to an origin that is not listed', async () => {
    const response = await t.http().get('/api/v1/health').set('Origin', 'https://evil.example.com');

    expect(response.headers['access-control-allow-origin']).toBeUndefined();
  });
});
