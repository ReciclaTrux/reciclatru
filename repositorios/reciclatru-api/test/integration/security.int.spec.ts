import { afterAll, beforeAll, describe, expect, it } from '@jest/globals';
import { createTestApp, type TestApp } from './test-app.js';

const LOGIN = '/api/v1/party-authentication/initiation';
const REGISTER = '/api/v1/party-data-management/initiation';
const PROTECTED = '/api/v1/servicing-order/retrieval';
const badLogin = { email: 'nadie@example.com', password: 'incorrecta1' };

describe('Rate limit, body limit and headers (PostgreSQL)', () => {
  let t: TestApp;

  beforeAll(async () => {
    t = await createTestApp({
      RATE_LIMIT_ENABLED: 'true',
      RATE_LIMIT_WINDOW_SECONDS: '60',
      RATE_LIMIT_AUTH_MAX: '3',
      RATE_LIMIT_MAX: '8',
      JSON_BODY_LIMIT: '1kb',
    });
  });

  afterAll(async () => {
    await t.app.close();
  });

  it('answers 429 problem+json with Retry-After once the login limit is exceeded', async () => {
    for (let attempt = 0; attempt < 3; attempt += 1) {
      expect((await t.http().post(LOGIN).send(badLogin)).status).toBe(401);
    }
    const blocked = await t.http().post(LOGIN).send(badLogin);

    expect(blocked.status).toBe(429);
    expect(blocked.headers['content-type']).toMatch(/application\/problem\+json/);
    expect(blocked.headers['retry-after']).toMatch(/^\d+$/);
    expect(blocked.body).toMatchObject({
      status: 429,
      code: 'TOO_MANY_REQUESTS',
      correlationId: expect.any(String),
    });
  });

  it('counts registration separately from login', async () => {
    const response = await t.http().post(REGISTER).send({});
    expect(response.status).toBe(400);
  });

  it('limits registration with the strict value', async () => {
    await t.http().post(REGISTER).send({});
    await t.http().post(REGISTER).send({});
    expect((await t.http().post(REGISTER).send({})).status).toBe(429);
  });

  it('applies the general limit before authentication, so token-less floods are cut too', async () => {
    const statuses: number[] = [];
    for (let attempt = 0; attempt < 10; attempt += 1) {
      statuses.push((await t.http().get(PROTECTED)).status);
    }
    expect(statuses.slice(0, 8).every((status) => status === 401)).toBe(true);
    expect(statuses.slice(8)).toEqual([429, 429]);
  });

  it('never limits the health probe', async () => {
    for (let attempt = 0; attempt < 12; attempt += 1) {
      expect((await t.http().get('/api/v1/health')).status).toBe(200);
    }
  });

  it('rejects a JSON body above the configured limit with 413', async () => {
    const response = await t
      .http()
      .post('/api/v1/health')
      .send({ filler: 'x'.repeat(2000) });
    expect(response.status).toBe(413);
    expect(response.body).toMatchObject({ code: 'PAYLOAD_TOO_LARGE' });
  });

  it('sends security headers and hides the framework', async () => {
    const response = await t.http().get('/api/v1/health');
    expect(response.headers['x-content-type-options']).toBe('nosniff');
    expect(response.headers['strict-transport-security']).toBeDefined();
    expect(response.headers['x-powered-by']).toBeUndefined();
  });
});
