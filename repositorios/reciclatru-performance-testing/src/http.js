import http from 'k6/http';
import { API } from './config.js';

// Un 4xx/5xx cuenta como fallo, salvo que la petición declare lo contrario (ver `expect`).
http.setResponseCallback(http.expectedStatuses({ min: 200, max: 399 }));

/**
 * Llama a la API.
 * @param {string} method  GET | POST | PUT
 * @param {string} path    ruta sin prefijo, p. ej. '/servicing-order/retrieval?tab=ACTIVE'
 * @param {object} o       { body, token, name, phase, expect }
 *   name   → etiqueta de la métrica (sin ids, para no crear una serie por pedido)
 *   expect → códigos que NO son fallo (p. ej. [201, 409])
 */
export function call(method, path, o = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (o.token) headers.Authorization = `Bearer ${o.token}`;

  const params = {
    headers,
    tags: { name: o.name || path, phase: o.phase || 'run' },
  };
  if (o.expect) params.responseCallback = http.expectedStatuses(...o.expect);

  const body = o.body === undefined ? null : JSON.stringify(o.body);
  return http.request(method, `${API}${path}`, body, params);
}

export function json(res) {
  try {
    return res.json();
  } catch (_) {
    return null;
  }
}
