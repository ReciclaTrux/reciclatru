// Pico: subida brusca de usuarios y vuelta a la normalidad. Comprueba si el API se recupera.
import { prepare } from '../src/setup.js';
import { THRESHOLDS } from '../src/config.js';
import { reporter } from '../src/report.js';
import { rampingScenarios } from '../src/scenarios.js';

export { neighbor, recycler } from '../src/scenarios.js';

export const options = {
  scenarios: rampingScenarios([
    { duration: '1m', target: 0.1 },
    { duration: '10s', target: 1 }, // pico
    { duration: '1m', target: 1 },
    { duration: '10s', target: 0.1 }, // vuelta a lo normal
    { duration: '2m', target: 0.1 }, // ¿se recupera?
    { duration: '20s', target: 0 },
  ]),
  setupTimeout: '180s',
  thresholds: THRESHOLDS,
};

export function setup() {
  return prepare();
}

// Reporte HTML en results/spike.html y resumen en la terminal.
export const handleSummary = reporter('spike');