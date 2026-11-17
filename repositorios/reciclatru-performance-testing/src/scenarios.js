import { PEAK_VUS } from './config.js';
import { neighborFlow } from './flows/neighbor.js';
import { recyclerFlow } from './flows/recycler.js';

// 70 % de los usuarios son vecinos y 30 % recicladores.
const SHARE = { neighbor: 0.7, recycler: 0.3 };

function scale(stages, share) {
  return stages.map((s) => ({
    duration: s.duration,
    target: s.target === 0 ? 0 : Math.max(1, Math.round(s.target * PEAK_VUS * share)),
  }));
}

/**
 * Arma los escenarios de k6 a partir de etapas expresadas como fracción del máximo (VUS):
 * [{ duration: '1m', target: 0.5 }] = durante 1 min sube hasta el 50 % de VUS.
 */
export function rampingScenarios(stages) {
  const base = { executor: 'ramping-vus', startVUs: 0, gracefulRampDown: '30s' };
  return {
    neighbor: { ...base, exec: 'neighbor', stages: scale(stages, SHARE.neighbor) },
    recycler: { ...base, exec: 'recycler', stages: scale(stages, SHARE.recycler) },
  };
}

// Funciones que k6 ejecuta en cada iteración; reparten los usuarios de prueba entre los VUs.
export function neighbor(data) {
  neighborFlow(data.neighbors[(__VU - 1) % data.neighbors.length]);
}

export function recycler(data) {
  recyclerFlow(data.recyclers[(__VU - 1) % data.recyclers.length]);
}
