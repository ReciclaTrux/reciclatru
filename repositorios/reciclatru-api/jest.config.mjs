/** Unit tests: ESM + ts-jest. Run with `npm test`. */
export default {
  testEnvironment: 'node',
  setupFilesAfterEnv: ['<rootDir>/test/support/silence-logger.ts'],
  extensionsToTreatAsEsm: ['.ts'],
  moduleNameMapper: { '^(\\.{1,2}/.*)\\.js$': '$1' },
  transform: { '^.+\\.ts$': ['ts-jest', { useESM: true, tsconfig: 'tsconfig.json' }] },
  roots: ['<rootDir>/src', '<rootDir>/test'],
  testRegex: '(?<!\\.int)\\.spec\\.ts$',
  collectCoverageFrom: ['src/**/*.ts', '!src/main.ts', '!src/**/*.module.ts', '!src/**/entities/*.ts', '!src/**/dto/*.ts'],
  coverageDirectory: 'coverage',
};
