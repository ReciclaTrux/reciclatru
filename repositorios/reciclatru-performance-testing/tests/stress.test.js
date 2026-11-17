// Estrés: sube por escalones hasta superar lo esperado, para encontrar el punto donde el API se degrada.
// Conviene subir VUS (p. ej. -e VUS=150) y vigilar el dashboard de Grafana mientras corre.
import { prepare } from '../src/setup.js';
import { THRESHOLDS } from '../src/config.js';
import { reporter } from '../src/report.js';
import { rampingScenarios } from '../src/scenarios.js';

export { neighbor, recycler } from '../src/scenarios.js';

export const options = {
  scenarios: rampingScenarios([
    { duration: '2m', target: 0.2 },
    { duration: '2m', target: 0.4 },
    { duration: '2m', target: 0.6 },
    { duration: '2m', target: 0.8 },
    { duration: '2m', target: 1 },
    { duration: '1m', target: 0 },
  ]),
  setupTimeout: '180s',
  thresholds: {
    ...THRESHOLDS,
    // Corta la prueba si más del 20 % de las peticiones fallan: ya se encontró el límite.
    'http_req_failed{phase:run}': [{ threshold: 'rate<0.2', abortOnFail: true, delayAbortEval: '30s' }],
  },
};

export function setup() {
  return prepare();
}

// Reporte HTML en results/stress.html y resumen en la terminal.
export const handleSummary = reporter('stress');