/** Integration tests against a real PostgreSQL (migrations applied). Run with `npm run test:integration`. */
import base from './jest.config.mjs';

export default {
  ...base,
  roots: ['<rootDir>/test/integration'],
  testRegex: '\\.int\\.spec\\.ts$',
  testTimeout: 30000,
};
