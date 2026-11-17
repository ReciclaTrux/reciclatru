import { check, sleep } from 'k6';
import { call, json } from '../http.js';
import { LOGIN_RATE, PASSWORD, THINK } from '../config.js';
import { randomOrder, rand } from '../data.js';

const think = (min, max) => THINK > 0 && sleep(rand(min, max) * THINK);

// Vecino: revisa sus pedidos y, de vez en cuando, publica uno nuevo.
export function neighborFlow(me) {
  if (Math.random() < LOGIN_RATE) {
    const res = call('POST', '/party-authentication/initiation', { name: 'login', body: { email: me.email, password: PASSWORD } });
    check(res, { 'login 200': (r) => r.status === 200 });
    think(0.5, 1);
  }

  const active = call('GET', '/servicing-order/retrieval?tab=ACTIVE&limit=20', { name: 'orders_list_active', token: me.token });
  check(active, { 'mis pedidos activos 200': (r) => r.status === 200 });
  think(1, 3);

  if (Math.random() < 0.3) {
    const created = call('POST', '/servicing-order/initiation', { name: 'order_create', token: me.token, body: randomOrder() });
    const ok = check(created, { 'pedido creado 201': (r) => r.status === 201 });
    if (ok) {
      const id = json(created)?.crReferenceId;
      think(0.5, 1.5);
      const detail = call('GET', `/servicing-order/${id}/retrieval`, { name: 'order_get', token: me.token });
      check(detail, { 'detalle de pedido 200': (r) => r.status === 200 });
    }
    think(1, 2);
  }

  if (Math.random() < 0.2) {
    const history = call('GET', '/servicing-order/retrieval?tab=HISTORY&limit=20', { name: 'orders_list_history', token: me.token });
    check(history, { 'historial 200': (r) => r.status === 200 });
  }
  think(1, 3);
}
