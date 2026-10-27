import { jest } from '@jest/globals';

/**
 * Builds a stub of a class where every listed method is a jest mock.
 * Tests configure only the methods they care about.
 */
export function stubOf<T>(
  ...methods: (keyof T & string)[]
): { [K in keyof T]: ReturnType<typeof jest.fn> } & T {
  const stub: Record<string, unknown> = {};
  for (const method of methods) stub[method] = jest.fn();
  return stub as { [K in keyof T]: ReturnType<typeof jest.fn> } & T;
}
