const SENSITIVE_KEY = /pass(word)?|token|secret|authorization|cookie|phone|email|signature|credential/i;
const MAX_DEPTH = 3;
export const REDACTED = '[redacted]';

/** Returns a copy of `value` where sensitive keys (passwords, tokens, phones, emails...) are masked. */
export function redact(value: unknown, depth = 0): unknown {
  if (Array.isArray(value))
    return depth >= MAX_DEPTH ? '[truncated]' : value.map((item) => redact(item, depth + 1));
  if (value === null || typeof value !== 'object' || value instanceof Date) return value;
  if (depth >= MAX_DEPTH) return '[truncated]';
  return Object.fromEntries(
    Object.entries(value).map(([key, item]) => [
      key,
      SENSITIVE_KEY.test(key) ? REDACTED : redact(item, depth + 1),
    ]),
  );
}
