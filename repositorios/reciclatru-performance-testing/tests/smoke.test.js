// Humo: 1 usuario de cada rol durante 30 s. Comprueba que los escenarios y la API funcionan.
import { prepare } from '../src/setup.js';
import { THRESHOLDS } from '../src/config.js';
import { reporter } from '../src/report.js';

export { neighbor, recycler } from '../src/scenarios.js';

export const options = {
  scenarios: {
    neighbor: { executor: 'constant-vus', exec: 'neighbor', vus: 1, duration: '30s' },
    recycler: { executor: 'constant-vus', exec: 'recycler', vus: 1, duration: '30s' },
  },
  setupTimeout: '180s',
  thresholds: THRESHOLDS,
};

export function setup() {
  return prepare();
}

// Reporte HTML en results/smoke.html y resumen en la terminal.
export const handleSummary = reporter('smoke');