import { afterAll, beforeAll, describe, expect, it } from '@jest/globals';
import { createTestApp, type TestApp } from './test-app.js';

const JPEG = Buffer.from([0xff, 0xd8, 0xff, 0xe0, 0x00, 0x10, 0x4a, 0x46, 0x49, 0x46]);
const PNG = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x00]);
const PASSWORD = 'Reciclatru2026';

interface Session {
  id: string;
  token: string;
  email: string;
}

describe('ReciclaTru API (PostgreSQL)', () => {
  let t: TestApp;
  const run = Date.now().toString(36);
  let neighbor: Session;
  let recyclerA: Session;
  let recyclerB: Session;
  let orderId: string;
  let acceptanceA: string;
  let acceptanceB: string;

  const auth = (s: Session) => ({ Authorization: `Bearer ${s.token}` });

  async function signUp(label: string, role: 'NEIGHBOR' | 'RECYCLER'): Promise<Session> {
    const email = `${label}.${run}@example.com`;
    await t
      .http()
      .post('/api/v1/party-data-management/initiation')
      .send({ email, password: PASSWORD, role })
      .expect(201);
    const login = await t
      .http()
      .post('/api/v1/party-authentication/initiation')
      .send({ email, password: PASSWORD })
      .expect(200);
    return { id: login.body.crReferenceId, token: login.body.accessToken, email };
  }

  beforeAll(async () => {
    t = await createTestApp();
  });

  afterAll(async () => {
    await t.app.close();
  });

  describe('operations', () => {
    it('GET /health is public and reports UP', async () => {
      const res = await t.http().get('/api/v1/health').expect(200);
      expect(res.body).toEqual({ status: 'UP', version: '1.0.0' });
    });

    it('propagates a valid x-correlation-id and generates one otherwise', async () => {
      const sent = await t.http().get('/api/v1/health').set('x-correlation-id', 'trace-123').expect(200);
      expect(sent.headers['x-correlation-id']).toBe('trace-123');
      const generated = await t
        .http()
        .get('/api/v1/health')
        .set('x-correlation-id', 'bad id with spaces')
        .expect(200);
      expect(generated.headers['x-correlation-id']).toMatch(/^[0-9a-f-]{36}$/);
    });

    it('answers errors as problem+json with the correlation id', async () => {
      const res = await t
        .http()
        .get('/api/v1/servicing-order/retrieval')
        .set('x-correlation-id', 'trace-401')
        .expect(401);
      expect(res.headers['content-type']).toContain('application/problem+json');
      expect(res.body).toMatchObject({ status: 401, code: 'UNAUTHENTICATED', correlationId: 'trace-401' });
    });
  });

  describe('registration and login', () => {
    it('registers, rejects duplicates in any letter case and validates input', async () => {
      neighbor = await signUp('rosa', 'NEIGHBOR');
      const duplicate = await t
        .http()
        .post('/api/v1/party-data-management/initiation')
        .send({ email: neighbor.email.toUpperCase(), password: PASSWORD, role: 'RECYCLER' })
        .expect(409);
      expect(duplicate.body.code).toBe('EMAIL_ALREADY_REGISTERED');

      const invalid = await t
        .http()
        .post('/api/v1/party-data-management/initiation')
        .send({ email: 'not-an-email', password: '123', role: 'ADMIN', extra: true })
        .expect(400);
      expect(invalid.body.code).toBe('VALIDATION_ERROR');
      expect(invalid.body.errors.map((e: { field: string }) => e.field)).toEqual(
        expect.arrayContaining(['email', 'password', 'role', 'extra']),
      );
    });

    it('login returns a 30 day token and hides which credential failed', async () => {
      const res = await t
        .http()
        .post('/api/v1/party-authentication/initiation')
        .send({ email: neighbor.email, password: PASSWORD })
        .expect(200);
      const days = (new Date(res.body.expiresAt).getTime() - Date.now()) / 86_400_000;
      expect(days).toBeGreaterThan(29.99);
      expect(days).toBeLessThanOrEqual(30);
      expect(res.body.tokenType).toBe('Bearer');
      expect(res.body.partyDataManagementInstanceRecord.profileComplete).toBe(false);

      const wrongPassword = await t
        .http()
        .post('/api/v1/party-authentication/initiation')
        .send({ email: neighbor.email, password: 'Otra12345' })
        .expect(401);
      const unknownEmail = await t
        .http()
        .post('/api/v1/party-authentication/initiation')
        .send({ email: `nobody.${run}@example.com`, password: PASSWORD })
        .expect(401);
      expect(wrongPassword.body.detail).toBe(unknownEmail.body.detail);
    });

    it('rejects a tampered token', async () => {
      await t
        .http()
        .get('/api/v1/party-data-management/me/retrieval')
        .set('Authorization', `Bearer ${neighbor.token}x`)
        .expect(401);
    });
  });

  describe('profile and pickup point', () => {
    it('stores the phone obfuscated in the database and returns the real one to its owner', async () => {
      const res = await t
        .http()
        .put('/api/v1/party-data-management/me/update')
        .set(auth(neighbor))
        .send({
          fullName: 'Rosa Elena Carranza Vega',
          phone: '987654321',
          reference: 'Portón verde frente al parque',
        })
        .expect(200);
      expect(res.body.partyDataManagementInstanceRecord).toMatchObject({
        fullName: 'Rosa Elena Carranza Vega',
        phone: '987654321',
        profileComplete: false,
      });

      const { Client } = await import('pg');
      const client = new Client({ connectionString: process.env.DATABASE_URL });
      await client.connect();
      const row = (
        await client.query('SELECT phone, phone_encrypted FROM user_profile WHERE user_id = $1', [
          neighbor.id,
        ])
      ).rows[0];
      await client.end();
      expect(row.phone).toBe('987****21');
      expect(row.phone_encrypted).not.toContain('987654321');
    });

    it('requires name and phone together and a single reference for neighbors only', async () => {
      recyclerA = await signUp('jorge', 'RECYCLER');
      const onlyName = await t
        .http()
        .put('/api/v1/party-data-management/me/update')
        .set(auth(recyclerA))
        .send({ fullName: 'Jorge Luis Mendoza Ruiz' })
        .expect(400);
      expect(onlyName.body.errors[0].field).toBe('phone');
      const badPhone = await t
        .http()
        .put('/api/v1/party-data-management/me/update')
        .set(auth(recyclerA))
        .send({ phone: '123456789' })
        .expect(400);
      expect(badPhone.body.code).toBe('VALIDATION_ERROR');
      await t
        .http()
        .put('/api/v1/party-data-management/me/update')
        .set(auth(recyclerA))
        .send({ fullName: 'Jorge Luis Mendoza Ruiz', phone: '944123456', reference: 'x' })
        .expect(400);
      const ok = await t
        .http()
        .put('/api/v1/party-data-management/me/update')
        .set(auth(recyclerA))
        .send({ fullName: 'Jorge Luis Mendoza Ruiz', phone: '944123456' })
        .expect(200);
      expect(ok.body.partyDataManagementInstanceRecord.profileComplete).toBe(true);
    });

    it('saves the pickup point only for neighbors and completes the profile', async () => {
      await t
        .http()
        .put('/api/v1/location-data-management/me/pickup-point/update')
        .set(auth(recyclerA))
        .send({ latitude: -8.077806, longitude: -79.122183 })
        .expect(403);
      const res = await t
        .http()
        .put('/api/v1/location-data-management/me/pickup-point/update')
        .set(auth(neighbor))
        .send({
          latitude: -8.0778061234,
          longitude: -79.122183,
          address: 'Av. Víctor Larco 1280, Huanchaco',
          district: 'Huanchaco',
        })
        .expect(200);
      expect(res.body.partyDataManagementInstanceRecord.pickupPoint).toEqual({
        latitude: -8.077806,
        longitude: -79.122183,
        address: 'Av. Víctor Larco 1280, Huanchaco',
        district: 'Huanchaco',
      });
      expect(res.body.partyDataManagementInstanceRecord.profileComplete).toBe(true);
    });

    it('treats another user id as not found', async () => {
      await t
        .http()
        .get(`/api/v1/party-data-management/${recyclerA.id}/retrieval`)
        .set(auth(neighbor))
        .expect(404);
      await t
        .http()
        .get(`/api/v1/party-data-management/${neighbor.id}/retrieval`)
        .set(auth(neighbor))
        .expect(200);
    });
  });

  describe('order lifecycle', () => {
    it('refuses to publish without a complete profile, with a wrong role or with invalid items', async () => {
      const incomplete = await signUp('sin.perfil', 'NEIGHBOR');
      const noProfile = await t
        .http()
        .post('/api/v1/servicing-order/initiation')
        .set(auth(incomplete))
        .send({ items: [{ description: 'Cartón', weightKg: 2 }] })
        .expect(422);
      expect(noProfile.body.code).toBe('PROFILE_INCOMPLETE');
      await t
        .http()
        .post('/api/v1/servicing-order/initiation')
        .set(auth(recyclerA))
        .send({ items: [{ description: 'Cartón', weightKg: 2 }] })
        .expect(403);

      const empty = await t
        .http()
        .post('/api/v1/servicing-order/initiation')
        .set(auth(neighbor))
        .send({ items: [] })
        .expect(422);
      expect(empty.body.code).toBe('INVALID_ITEMS');
      const eleven = Array.from({ length: 11 }, () => ({ description: 'Lata', weightKg: 1 }));
      expect(
        (
          await t
            .http()
            .post('/api/v1/servicing-order/initiation')
            .set(auth(neighbor))
            .send({ items: eleven })
            .expect(422)
        ).body.code,
      ).toBe('INVALID_ITEMS');
      for (const weightKg of [0, -1, 500.1, 2.55]) {
        const res = await t
          .http()
          .post('/api/v1/servicing-order/initiation')
          .set(auth(neighbor))
          .send({ items: [{ description: 'Lata', weightKg }] })
          .expect(422);
        expect(res.body.code).toBe('INVALID_WEIGHT');
      }
      await t
        .http()
        .post('/api/v1/servicing-order/initiation')
        .set(auth(neighbor))
        .send({ items: [{ description: 'Lata', weightKg: 'pesado' }] })
        .expect(400);
    });

    it('publishes a PENDING order keeping the order of the items', async () => {
      const res = await t
        .http()
        .post('/api/v1/servicing-order/initiation')
        .set(auth(neighbor))
        .send({
          items: [
            { description: 'Botellas de plástico PET', weightKg: 4.5 },
            { description: 'Cartón corrugado', weightKg: 8 },
            { description: 'Latas de aluminio', weightKg: 1.2 },
          ],
        })
        .expect(201);
      const record = res.body.servicingOrderInstanceRecord;
      orderId = res.body.crReferenceId;
      expect(record).toMatchObject({
        status: 'PENDING',
        totalWeightKg: 13.7,
        zone: 'Huanchaco',
        acceptanceCount: 0,
      });
      expect(record.items.map((i: { description: string }) => i.description)).toEqual([
        'Botellas de plástico PET',
        'Cartón corrugado',
        'Latas de aluminio',
      ]);
      expect(record.availableActions).toEqual(['EDIT', 'CANCEL']);
    });

    it('accepts up to 3 photos checking the real file type and size', async () => {
      const upload = (buffer: Buffer, name = 'foto.jpg', type = 'image/jpeg') =>
        t
          .http()
          .post(`/api/v1/servicing-order/${orderId}/photo/initiation`)
          .set(auth(neighbor))
          .attach('file', buffer, { filename: name, contentType: type });

      const fake = await upload(Buffer.from('<html>not an image</html>'), 'foto.jpg', 'image/jpeg').expect(
        422,
      );
      expect(fake.body.code).toBe('PHOTO_INVALID_TYPE');
      const tooBig = await upload(Buffer.concat([JPEG, Buffer.alloc(2 * 1024 * 1024)])).expect(413);
      expect(tooBig.body.code).toBe('PHOTO_TOO_LARGE');
      await t
        .http()
        .post(`/api/v1/servicing-order/${orderId}/photo/initiation`)
        .set(auth(neighbor))
        .expect(400);

      const first = await upload(JPEG).expect(201);
      await upload(PNG, 'b.png', 'image/png').expect(201);
      await upload(JPEG).expect(201);
      expect((await upload(JPEG).expect(422)).body.code).toBe('PHOTO_LIMIT_REACHED');
      expect(first.body.url).toBe(`/api/v1/servicing-order/${orderId}/photo/${first.body.id}/retrieval`);
      expect(t.storage.objects.size).toBe(3);
      expect([...t.storage.objects.keys()].every((key) => key.startsWith(`${orderId}/`))).toBe(true);

      await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/photo/${first.body.id}/termination`)
        .set(auth(neighbor))
        .expect(204);
      expect(t.storage.objects.size).toBe(2);
      await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/photo/${first.body.id}/termination`)
        .set(auth(neighbor))
        .expect(404);
      await upload(JPEG).expect(201);
    });

    it('redirects photo reads to a signed URL for the owner and for recyclers that can see the order', async () => {
      const detail = await t
        .http()
        .get(`/api/v1/servicing-order/${orderId}/retrieval`)
        .set(auth(neighbor))
        .expect(200);
      const photo = detail.body.servicingOrderInstanceRecord.photos[0];
      const asOwner = await t.http().get(photo.url).set(auth(neighbor)).redirects(0).expect(302);
      expect(asOwner.headers.location).toContain('https://storage.example.com/photos/');
      expect(asOwner.headers.location).toContain('X-Expires=300');
      await t.http().get(photo.url).set(auth(recyclerA)).redirects(0).expect(302);
      await t.http().get(photo.url).redirects(0).expect(401);
      const stranger = await signUp('vecino.ajeno', 'NEIGHBOR');
      await t.http().get(photo.url).set(auth(stranger)).redirects(0).expect(403);
    });

    it('lets the owner edit while nobody accepted, and not afterwards', async () => {
      const edited = await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/update`)
        .set(auth(neighbor))
        .send({
          items: [
            { description: 'Botellas de plástico PET', weightKg: 4.5 },
            { description: 'Cartón corrugado', weightKg: 8 },
            { description: 'Latas de aluminio', weightKg: 1.2 },
            { description: 'Papel', weightKg: 0.3 },
          ],
        })
        .expect(200);
      expect(edited.body.servicingOrderInstanceRecord.totalWeightKg).toBe(14);
    });

    it('shows recyclers the available list by date and by distance without personal data', async () => {
      const byDate = await t
        .http()
        .get('/api/v1/servicing-order/availability/retrieval')
        .set(auth(recyclerA))
        .expect(200);
      expect(byDate.body.sortedBy).toBe('DATE');
      const mine = byDate.body.items.find((o: { id: string }) => o.id === orderId);
      expect(mine).toMatchObject({ itemCount: 4, zone: 'Huanchaco', alreadyAccepted: false });
      expect(JSON.stringify(mine)).not.toMatch(/Rosa|987|Larco/);
      expect(mine.thumbnailUrl).toContain(`/servicing-order/${orderId}/photo/`);

      const byDistance = await t
        .http()
        .get('/api/v1/servicing-order/availability/retrieval')
        .query({ lat: -8.1116, lng: -79.0288 })
        .set(auth(recyclerA))
        .expect(200);
      expect(byDistance.body.sortedBy).toBe('DISTANCE');
      const near = byDistance.body.items.find((o: { id: string }) => o.id === orderId);
      expect(near.approximateDistanceKm).toBeGreaterThan(9);
      expect(near.approximateDistanceKm).toBeLessThan(13);
      await t
        .http()
        .get('/api/v1/servicing-order/availability/retrieval')
        .query({ lat: -8.1 })
        .set(auth(recyclerA))
        .expect(400);
      await t.http().get('/api/v1/servicing-order/availability/retrieval').set(auth(neighbor)).expect(403);
    });

    it('detail for a recycler without acceptance hides the neighbor data', async () => {
      const res = await t
        .http()
        .get(`/api/v1/servicing-order/${orderId}/retrieval`)
        .query({ lat: -8.1116, lng: -79.0288 })
        .set(auth(recyclerA))
        .expect(200);
      const record = res.body.servicingOrderInstanceRecord;
      expect(record).toMatchObject({
        alreadyAccepted: false,
        acceptanceStatus: null,
        counterpart: null,
        pickup: null,
        availableActions: ['ACCEPT'],
      });
      expect(record.approximateDistanceKm).toBeGreaterThan(0);
    });

    it('registers acceptances once per recycler and reveals the neighbor contact but not the address', async () => {
      const first = await t
        .http()
        .post(`/api/v1/servicing-order/${orderId}/acceptance/initiation`)
        .set(auth(recyclerA))
        .expect(201);
      const record = first.body.servicingOrderInstanceRecord;
      expect(record).toMatchObject({
        alreadyAccepted: true,
        acceptanceStatus: 'ACCEPTED',
        acceptanceCount: 1,
      });
      expect(record.counterpart).toEqual({ fullName: 'Rosa Elena Carranza Vega', phone: '987654321' });
      expect(record.pickup).toBeNull();
      await t
        .http()
        .post(`/api/v1/servicing-order/${orderId}/acceptance/initiation`)
        .set(auth(recyclerA))
        .expect(200);

      recyclerB = await signUp('marleni', 'RECYCLER');
      const incomplete = await t
        .http()
        .post(`/api/v1/servicing-order/${orderId}/acceptance/initiation`)
        .set(auth(recyclerB))
        .expect(422);
      expect(incomplete.body.code).toBe('PROFILE_INCOMPLETE');
      await t
        .http()
        .put('/api/v1/party-data-management/me/update')
        .set(auth(recyclerB))
        .send({ fullName: 'Marleni Quispe Rojas', phone: '955876543' })
        .expect(200);
      await t
        .http()
        .post(`/api/v1/servicing-order/${orderId}/acceptance/initiation`)
        .set(auth(recyclerB))
        .expect(201);
      await t
        .http()
        .post(`/api/v1/servicing-order/${orderId}/acceptance/initiation`)
        .set(auth(neighbor))
        .expect(403);
    });

    it('a double tap in parallel creates a single acceptance', async () => {
      const recycler = await signUp('doble.toque', 'RECYCLER');
      await t
        .http()
        .put('/api/v1/party-data-management/me/update')
        .set(auth(recycler))
        .send({ fullName: 'Doble Toque Pérez', phone: '911222333' })
        .expect(200);
      const other = await t
        .http()
        .post('/api/v1/servicing-order/initiation')
        .set(auth(neighbor))
        .send({ items: [{ description: 'Papel', weightKg: 1 }] })
        .expect(201);
      const target = other.body.crReferenceId;
      const responses = await Promise.all(
        [1, 2, 3].map(() =>
          t.http().post(`/api/v1/servicing-order/${target}/acceptance/initiation`).set(auth(recycler)),
        ),
      );
      expect(responses.map((r) => r.status).sort((a, b) => a - b)).toEqual([200, 200, 201]);
      const list = await t
        .http()
        .get(`/api/v1/servicing-order/${target}/acceptance/retrieval`)
        .set(auth(neighbor))
        .expect(200);
      expect(list.body.items).toHaveLength(1);
      await t.http().put(`/api/v1/servicing-order/${target}/termination`).set(auth(neighbor)).expect(200);
    });

    it('is not editable once accepted', async () => {
      const res = await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/update`)
        .set(auth(neighbor))
        .send({ items: [{ description: 'Papel', weightKg: 1 }] })
        .expect(409);
      expect(res.body.code).toBe('ORDER_NOT_EDITABLE');
      await t
        .http()
        .post(`/api/v1/servicing-order/${orderId}/photo/initiation`)
        .set(auth(neighbor))
        .attach('file', JPEG, 'a.jpg')
        .expect(409);
    });

    it('lists acceptances to the owner with the real contact data, oldest first', async () => {
      const res = await t
        .http()
        .get(`/api/v1/servicing-order/${orderId}/acceptance/retrieval`)
        .set(auth(neighbor))
        .expect(200);
      expect(res.body.items.map((a: { fullName: string }) => a.fullName)).toEqual([
        'Jorge Luis Mendoza Ruiz',
        'Marleni Quispe Rojas',
      ]);
      expect(res.body.items[0]).toMatchObject({ status: 'ACCEPTED', phone: '944123456' });
      [acceptanceA, acceptanceB] = res.body.items.map((a: { id: string }) => a.id);
      await t
        .http()
        .get(`/api/v1/servicing-order/${orderId}/acceptance/retrieval`)
        .set(auth(recyclerA))
        .expect(403);
    });

    it('neighbor list shows the acceptance count of PENDING orders', async () => {
      const res = await t
        .http()
        .get('/api/v1/servicing-order/retrieval')
        .query({ tab: 'ACTIVE' })
        .set(auth(neighbor))
        .expect(200);
      const mine = res.body.items.find((o: { id: string }) => o.id === orderId);
      expect(mine).toMatchObject({ status: 'PENDING', acceptanceCount: 2, itemCount: 4 });
    });

    it('chooses one recycler atomically and discards the rest', async () => {
      await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/confirmation/execution`)
        .set(auth(neighbor))
        .send({ acceptanceId: 'not-a-uuid' })
        .expect(400);
      await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/confirmation/execution`)
        .set(auth(neighbor))
        .send({ acceptanceId: '3f2b8a2e-5d7c-4a39-8b6e-1c9d0e4f7a21' })
        .expect(404);
      const [ok, late] = await Promise.all([
        t
          .http()
          .put(`/api/v1/servicing-order/${orderId}/confirmation/execution`)
          .set(auth(neighbor))
          .send({ acceptanceId: acceptanceA }),
        t
          .http()
          .put(`/api/v1/servicing-order/${orderId}/confirmation/execution`)
          .set(auth(neighbor))
          .send({ acceptanceId: acceptanceB }),
      ]);
      expect([ok.status, late.status].sort((a, b) => a - b)).toEqual([200, 409]);
      const winner = ok.status === 200 ? ok : late;
      const loser = ok.status === 200 ? late : ok;
      expect(loser.body.code).toBe('ORDER_NOT_PENDING');
      expect(winner.body.servicingOrderInstanceRecord.status).toBe('CONFIRMED');
      expect(winner.body.servicingOrderInstanceRecord.availableActions).toEqual(['MARK_COLLECTED', 'CANCEL']);

      const chosenIsA =
        winner.body.servicingOrderInstanceRecord.chosenRecyclerName === 'Jorge Luis Mendoza Ruiz';
      const [chosen, discarded] = chosenIsA ? [recyclerA, recyclerB] : [recyclerB, recyclerA];

      const asChosen = await t
        .http()
        .get(`/api/v1/servicing-order/${orderId}/retrieval`)
        .set(auth(chosen))
        .expect(200);
      expect(asChosen.body.servicingOrderInstanceRecord.pickup).toEqual({
        address: 'Av. Víctor Larco 1280, Huanchaco',
        reference: 'Portón verde frente al parque',
        latitude: -8.077806,
        longitude: -79.122183,
      });
      expect(asChosen.body.servicingOrderInstanceRecord.availableActions).toEqual(['GO_PICKUP']);

      const asDiscarded = await t
        .http()
        .get(`/api/v1/servicing-order/${orderId}/retrieval`)
        .set(auth(discarded))
        .expect(200);
      expect(asDiscarded.body.servicingOrderInstanceRecord).toMatchObject({
        acceptanceStatus: 'DISCARDED',
        pickup: null,
      });

      const newcomer = await signUp('tardio', 'RECYCLER');
      const unavailable = await t
        .http()
        .get(`/api/v1/servicing-order/${orderId}/retrieval`)
        .set(auth(newcomer))
        .expect(409);
      expect(unavailable.body.code).toBe('ORDER_NOT_AVAILABLE');
    });

    it("lists each recycler's acceptances with the right label and paginates with opaque cursors", async () => {
      const active = await t
        .http()
        .get('/api/v1/servicing-order/acceptance/retrieval')
        .query({ tab: 'ACTIVE' })
        .set(auth(recyclerA))
        .expect(200);
      const history = await t
        .http()
        .get('/api/v1/servicing-order/acceptance/retrieval')
        .query({ tab: 'HISTORY' })
        .set(auth(recyclerA))
        .expect(200);
      const labelsA = [...active.body.items, ...history.body.items].map(
        (i: { acceptanceLabel: string }) => i.acceptanceLabel,
      );
      expect(labelsA).toEqual(expect.arrayContaining(['CHOSEN']));
      const labelsB = (
        await t
          .http()
          .get('/api/v1/servicing-order/acceptance/retrieval')
          .query({ tab: 'HISTORY' })
          .set(auth(recyclerB))
          .expect(200)
      ).body.items.map((i: { acceptanceLabel: string }) => i.acceptanceLabel);
      expect(labelsB).toContain('NOT_CHOSEN');

      for (let i = 0; i < 3; i += 1) {
        await t
          .http()
          .post('/api/v1/servicing-order/initiation')
          .set(auth(neighbor))
          .send({ items: [{ description: `Lote ${i}`, weightKg: 1 }] })
          .expect(201);
      }
      const seen: string[] = [];
      let cursor: string | undefined;
      let pages = 0;
      do {
        const page = await t
          .http()
          .get('/api/v1/servicing-order/retrieval')
          .query({ tab: 'ACTIVE', limit: 2, ...(cursor ? { cursor } : {}) })
          .set(auth(neighbor))
          .expect(200);
        expect(page.body.items.length).toBeLessThanOrEqual(2);
        seen.push(...page.body.items.map((o: { id: string }) => o.id));
        cursor = page.body.nextCursor ?? undefined;
        pages += 1;
      } while (cursor && pages < 20);
      expect(pages).toBeGreaterThanOrEqual(2);
      expect(new Set(seen).size).toBe(seen.length);

      const bad = await t
        .http()
        .get('/api/v1/servicing-order/retrieval')
        .query({ cursor: 'basura' })
        .set(auth(neighbor))
        .expect(400);
      expect(bad.body.errors[0].field).toBe('cursor');
      await t
        .http()
        .get('/api/v1/servicing-order/retrieval')
        .query({ limit: 500 })
        .set(auth(neighbor))
        .expect(400);
    });

    it('paginates the distance sorted list without repeating or skipping orders', async () => {
      const point = { lat: -8.1116, lng: -79.0288 };
      const seen: string[] = [];
      let cursor: string | undefined;
      let pages = 0;
      do {
        const page = await t
          .http()
          .get('/api/v1/servicing-order/availability/retrieval')
          .query({ ...point, limit: 2, ...(cursor ? { cursor } : {}) })
          .set(auth(recyclerA))
          .expect(200);
        seen.push(...page.body.items.map((o: { id: string }) => o.id));
        cursor = page.body.nextCursor ?? undefined;
        pages += 1;
      } while (cursor && pages < 20);
      const all = await t
        .http()
        .get('/api/v1/servicing-order/availability/retrieval')
        .query({ ...point, limit: 50 })
        .set(auth(recyclerA))
        .expect(200);
      expect(seen).toEqual(all.body.items.map((o: { id: string }) => o.id));
    });

    it('marks as collected once, hides contact data afterwards and keeps the history', async () => {
      await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/fulfillment/execution`)
        .set(auth(recyclerA))
        .expect(403);
      const done = await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/fulfillment/execution`)
        .set(auth(neighbor))
        .expect(200);
      expect(done.body.servicingOrderInstanceRecord.status).toBe('COLLECTED');
      const again = await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/fulfillment/execution`)
        .set(auth(neighbor))
        .expect(409);
      expect(again.body.code).toBe('ORDER_NOT_CONFIRMED');
      const cancel = await t
        .http()
        .put(`/api/v1/servicing-order/${orderId}/termination`)
        .set(auth(neighbor))
        .expect(409);
      expect(cancel.body.code).toBe('ORDER_NOT_CANCELLABLE');

      const history = await t
        .http()
        .get('/api/v1/servicing-order/retrieval')
        .query({ tab: 'HISTORY' })
        .set(auth(neighbor))
        .expect(200);
      expect(history.body.items.map((o: { id: string }) => o.id)).toContain(orderId);
    });

    it('cancelled orders disappear from the available list and are labeled for recyclers', async () => {
      const created = await t
        .http()
        .post('/api/v1/servicing-order/initiation')
        .set(auth(neighbor))
        .send({ items: [{ description: 'Vidrio', weightKg: 3 }] })
        .expect(201);
      const id = created.body.crReferenceId;
      await t
        .http()
        .post(`/api/v1/servicing-order/${id}/acceptance/initiation`)
        .set(auth(recyclerA))
        .expect(201);
      const cancelled = await t
        .http()
        .put(`/api/v1/servicing-order/${id}/termination`)
        .set(auth(neighbor))
        .expect(200);
      expect(cancelled.body.servicingOrderInstanceRecord.status).toBe('CANCELLED');

      const available = await t
        .http()
        .get('/api/v1/servicing-order/availability/retrieval')
        .set(auth(recyclerA))
        .expect(200);
      expect(available.body.items.map((o: { id: string }) => o.id)).not.toContain(id);
      const history = await t
        .http()
        .get('/api/v1/servicing-order/acceptance/retrieval')
        .query({ tab: 'HISTORY' })
        .set(auth(recyclerA))
        .expect(200);
      expect(history.body.items.find((i: { orderId: string }) => i.orderId === id).acceptanceLabel).toBe(
        'CANCELLED_BY_NEIGHBOR',
      );
      const detail = await t
        .http()
        .get(`/api/v1/servicing-order/${id}/retrieval`)
        .set(auth(recyclerA))
        .expect(200);
      expect(detail.body.servicingOrderInstanceRecord).toMatchObject({ counterpart: null, pickup: null });
    });

    it('answers 404 for unknown or malformed order ids and 403 for foreign orders', async () => {
      await t.http().get('/api/v1/servicing-order/not-a-uuid/retrieval').set(auth(neighbor)).expect(404);
      await t
        .http()
        .get('/api/v1/servicing-order/3f2b8a2e-5d7c-4a39-8b6e-1c9d0e4f7a21/retrieval')
        .set(auth(neighbor))
        .expect(404);
      const stranger = await signUp('otro.vecino', 'NEIGHBOR');
      const res = await t
        .http()
        .get(`/api/v1/servicing-order/${orderId}/retrieval`)
        .set(auth(stranger))
        .expect(403);
      expect(res.body.code).toBe('NOT_ORDER_OWNER');
    });
  });
});
