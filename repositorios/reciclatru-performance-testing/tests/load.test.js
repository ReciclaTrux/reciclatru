// Carga: el tráfico esperado, sostenido varios minutos. Mide la latencia "normal".
import { prepare } from '../src/setup.js';
import { THRESHOLDS } from '../src/config.js';
import { reporter } from '../src/report.js';
import { rampingScenarios } from '../src/scenarios.js';

export { neighbor, recycler } from '../src/scenarios.js';

export const options = {
  scenarios: rampingScenarios([
    { duration: '1m', target: 1 }, // sube hasta VUS
    { duration: '5m', target: 1 }, // se mantiene
    { duration: '1m', target: 0 }, // baja
  ]),
  setupTimeout: '180s',
  thresholds: THRESHOLDS,
};

export function setup() {
  return prepare();
}

// Reporte HTML en results/load.html y resumen en la terminal.
export const handleSummary = reporter('load');