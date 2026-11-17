import { check, sleep } from 'k6';
import { call, json } from '../http.js';
import { LAT, LNG, LOGIN_RATE, PASSWORD, THINK } from '../config.js';
import { pick, rand } from '../data.js';

const think = (min, max) => THINK > 0 && sleep(rand(min, max) * THINK);

// Reciclador: busca pedidos cercanos y, a veces, acepta uno.
export function recyclerFlow(me) {
  if (Math.random() < LOGIN_RATE) {
    const res = call('POST', '/party-authentication/initiation', { name: 'login', body: { email: me.email, password: PASSWORD } });
    check(res, { 'login 200': (r) => r.status === 200 });
    think(0.5, 1);
  }

  const list = call('GET', `/servicing-order/availability/retrieval?lat=${LAT}&lng=${LNG}&limit=20`, { name: 'availability', token: me.token });
  const ok = check(list, { 'pedidos disponibles 200': (r) => r.status === 200 });
  think(1, 3);

  const items = ok ? json(list)?.items || [] : [];
  if (items.length > 0 && Math.random() < 0.3) {
    const order = pick(items);
    // 409 = otro reciclador/vecino cambió el pedido antes: es una respuesta válida, no un fallo.
    const res = call('POST', `/servicing-order/${order.id}/acceptance/initiation`, {
      name: 'order_accept', token: me.token, expect: [200, 201, 409],
    });
    check(res, { 'aceptación 2xx o 409': (r) => [200, 201, 409].includes(r.status) });
    think(1, 2);
  }

  const mine = call('GET', '/servicing-order/acceptance/retrieval?tab=ACTIVE&limit=20', { name: 'accepted_list', token: me.token });
  check(mine, { 'mis aceptados 200': (r) => r.status === 200 });
  think(1, 3);
}
