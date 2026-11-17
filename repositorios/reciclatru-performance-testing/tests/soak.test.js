// Resistencia: carga moderada durante mucho tiempo (por defecto 30 min, -e DURATION=1h).
// Busca fugas de memoria o conexiones que no se liberan. Usa una carga baja (-e VUS=10).
import { prepare } from '../src/setup.js';
import { THRESHOLDS } from '../src/config.js';
import { reporter } from '../src/report.js';
import { rampingScenarios } from '../src/scenarios.js';

export { neighbor, recycler } from '../src/scenarios.js';

export const options = {
  scenarios: rampingScenarios([
    { duration: '2m', target: 1 },
    { duration: __ENV.DURATION || '30m', target: 1 },
    { duration: '1m', target: 0 },
  ]),
  setupTimeout: '180s',
  thresholds: THRESHOLDS,
};

export function setup() {
  return prepare();
}

// Reporte HTML en results/soak.html y resumen en la terminal.
export const handleSummary = reporter('soak');