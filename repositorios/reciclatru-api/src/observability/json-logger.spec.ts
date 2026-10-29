import { describe, expect, it } from '@jest/globals';
import { JsonLogger, type JsonLoggerOptions, parseLogFormat, parseLogLevel } from './json-logger.js';
import { requestStore } from './request-store.js';

function createLogger(options: Partial<JsonLoggerOptions> = {}) {
  const lines: string[] = [];
  const logger = new JsonLogger({
    level: 'info',
    format: 'json',
    write: (line) => lines.push(line),
    ...options,
  });
  const parsed = () => lines.map((line) => JSON.parse(line) as Record<string, unknown>);
  return { logger, lines, parsed };
}

describe('JsonLogger', () => {
  it('writes one JSON object per line with level, context and message', () => {
    const { logger, parsed } = createLogger();
    logger.log('hello', 'Greeter');
    expect(parsed()).toEqual([
      expect.objectContaining({ level: 'info', context: 'Greeter', message: 'hello' }),
    ]);
    expect(typeof parsed()[0]?.timestamp).toBe('string');
  });

  it('spreads business event objects into fields', () => {
    const { logger, parsed } = createLogger();
    logger.log({ event: 'order.created', orderId: 'o-1', itemCount: 2 }, 'OrderCommandService');
    expect(parsed()[0]).toEqual(
      expect.objectContaining({
        event: 'order.created',
        orderId: 'o-1',
        itemCount: 2,
        message: 'order.created',
      }),
    );
  });

  it('uses the explicit message of an event when there is one', () => {
    const { logger, parsed } = createLogger();
    logger.warn({ event: 'x', message: 'custom text' }, 'Ctx');
    expect(parsed()[0]).toEqual(expect.objectContaining({ level: 'warn', message: 'custom text' }));
  });

  it('masks sensitive fields before writing', () => {
    const { logger, lines } = createLogger();
    logger.log({ event: 'x', email: 'a@b.com', password: '1234', phone: '987654321' }, 'Ctx');
    expect(lines[0]).not.toContain('a@b.com');
    expect(lines[0]).not.toContain('1234');
    expect(lines[0]).not.toContain('987654321');
    expect(lines[0]).toContain('[redacted]');
  });

  it('adds the correlation id and user of the current request', () => {
    const { logger, parsed } = createLogger();
    requestStore.run({ correlationId: 'corr-1', userId: 'u-1' }, () => logger.log('inside', 'Ctx'));
    logger.log('outside', 'Ctx');
    expect(parsed()[0]).toEqual(expect.objectContaining({ correlationId: 'corr-1', userId: 'u-1' }));
    expect(parsed()[1]).not.toHaveProperty('correlationId');
  });

  it('lets an explicit field override the request context', () => {
    const { logger, parsed } = createLogger();
    requestStore.run({ correlationId: 'from-store' }, () =>
      logger.log({ event: 'x', correlationId: 'explicit' }, 'Ctx'),
    );
    expect(parsed()[0]?.correlationId).toBe('explicit');
  });

  it('reads the stack that Nest passes before the context on errors', () => {
    const { logger, parsed } = createLogger();
    logger.error('boom', 'Error: boom\n at x', 'Ctx');
    expect(parsed()[0]).toEqual(
      expect.objectContaining({ level: 'error', context: 'Ctx', stack: 'Error: boom\n at x' }),
    );
  });

  it('logs Error instances with their stack', () => {
    const { logger, parsed } = createLogger();
    logger.error(new Error('bad'), 'Ctx');
    expect(parsed()[0]).toEqual(
      expect.objectContaining({ message: 'bad', stack: expect.stringContaining('Error: bad') }),
    );
  });

  it('drops lines below the configured level', () => {
    const { logger, lines } = createLogger({ level: 'warn' });
    logger.debug('d');
    logger.log('i');
    logger.warn('w');
    logger.error('e');
    expect(lines).toHaveLength(2);
  });

  it('keeps Nest boot noise out of the default level', () => {
    const { logger, lines } = createLogger();
    logger.log('AppModule dependencies initialized', 'InstanceLoader');
    logger.log('Mapped {/api/v1/health, GET} route', 'RouterExplorer');
    expect(lines).toHaveLength(0);
  });

  it('shows boot noise when the level is debug', () => {
    const { logger, lines } = createLogger({ level: 'debug' });
    logger.log('AppModule dependencies initialized', 'InstanceLoader');
    expect(lines).toHaveLength(1);
  });

  it('writes a readable single line in text format', () => {
    const { logger, lines } = createLogger({ format: 'text' });
    logger.log({ event: 'order.created', orderId: 'o-1' }, 'OrderCommandService');
    expect(lines[0]).toMatch(/INFO \[OrderCommandService\] order\.created .*orderId=o-1/);
  });
});

describe('log settings parsing', () => {
  it.each([
    ['debug', 'debug'],
    ['warn', 'warn'],
    ['error', 'error'],
    ['info', 'info'],
    ['verbose', 'info'],
    [undefined, 'info'],
  ])('level %p becomes %p', (input, expected) => {
    expect(parseLogLevel(input)).toBe(expected);
  });

  it.each([
    ['text', 'text'],
    ['json', 'json'],
    ['other', 'json'],
    [undefined, 'json'],
  ])('format %p becomes %p', (input, expected) => {
    expect(parseLogFormat(input)).toBe(expected);
  });
});
