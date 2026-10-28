import type { LoggerService } from '@nestjs/common';
import { logs, SeverityNumber, trace } from '@opentelemetry/api';
import { redact } from './redaction.js';
import { requestStore } from './request-store.js';

export type LogLevel = 'debug' | 'info' | 'warn' | 'error';
export type LogFormat = 'json' | 'text';

export interface JsonLoggerOptions {
  level: LogLevel;
  format: LogFormat;
  /** Where each line goes; stdout by default. */
  write?: (line: string) => void;
}

const WEIGHT: Record<LogLevel, number> = { debug: 10, info: 20, warn: 30, error: 40 };
const SEVERITY: Record<LogLevel, SeverityNumber> = {
  debug: SeverityNumber.DEBUG,
  info: SeverityNumber.INFO,
  warn: SeverityNumber.WARN,
  error: SeverityNumber.ERROR,
};
const SCOPE = 'reciclatru-api';
/** Nest prints one line per module and route at boot; they are useful only when debugging. */
const BOOT_NOISE = new Set(['InstanceLoader', 'RoutesResolver', 'RouterExplorer', 'NestFactory']);

export function parseLogLevel(value: string | undefined): LogLevel {
  return value === 'debug' || value === 'warn' || value === 'error' ? value : 'info';
}

export function parseLogFormat(value: string | undefined): LogFormat {
  return value === 'text' ? 'text' : 'json';
}

/**
 * Nest logger that writes one JSON object per line and, when OpenTelemetry is enabled,
 * forwards the same record to the OTLP exporter. Every line carries the request's
 * correlation id and user, plus trace and span ids when a trace is active.
 */
export class JsonLogger implements LoggerService {
  private readonly write: (line: string) => void;

  constructor(private readonly options: JsonLoggerOptions) {
    this.write = options.write ?? ((line) => process.stdout.write(`${line}\n`));
  }

  log(message: unknown, ...params: unknown[]): void {
    this.emit('info', message, params);
  }

  warn(message: unknown, ...params: unknown[]): void {
    this.emit('warn', message, params);
  }

  error(message: unknown, ...params: unknown[]): void {
    this.emit('error', message, params);
  }

  fatal(message: unknown, ...params: unknown[]): void {
    this.emit('error', message, params);
  }

  debug(message: unknown, ...params: unknown[]): void {
    this.emit('debug', message, params);
  }

  verbose(message: unknown, ...params: unknown[]): void {
    this.emit('debug', message, params);
  }

  private emit(requested: LogLevel, message: unknown, params: unknown[]): void {
    const rest = [...params];
    const context = typeof rest.at(-1) === 'string' ? (rest.pop() as string) : undefined;
    const level = requested === 'info' && context && BOOT_NOISE.has(context) ? 'debug' : requested;
    if (WEIGHT[level] < WEIGHT[this.options.level]) return;

    const stack = level === 'error' && typeof rest[0] === 'string' ? rest[0] : undefined;
    const { text, fields } = this.split(message);

    const span = trace.getActiveSpan()?.spanContext();
    const record: Record<string, unknown> = {
      ...requestStore.current(),
      ...fields,
      timestamp: new Date().toISOString(),
      level,
      context,
      message: text,
      stack: stack ?? fields.stack,
      traceId: span?.traceId,
      spanId: span?.spanId,
    };
    const clean = Object.fromEntries(Object.entries(record).filter(([, value]) => value !== undefined));

    this.write(this.options.format === 'json' ? JSON.stringify(clean) : this.toText(clean));
    this.forward(level, text, clean);
  }

  private split(message: unknown): { text: string; fields: Record<string, unknown> } {
    if (message instanceof Error) return { text: message.message, fields: { stack: message.stack } };
    if (message !== null && typeof message === 'object') {
      const { message: text, event, ...fields } = redact(message) as Record<string, unknown>;
      const label = text ?? event;
      return {
        text: typeof label === 'string' ? label : 'event',
        fields: event === undefined ? fields : { event, ...fields },
      };
    }
    return { text: String(message), fields: {} };
  }

  private toText(record: Record<string, unknown>): string {
    const { timestamp, level, context, message, ...rest } = record;
    const extra = Object.entries(rest)
      .map(([key, value]) => `${key}=${typeof value === 'string' ? value : JSON.stringify(value)}`)
      .join(' ');
    const scope = typeof context === 'string' ? context : 'App';
    return `${String(timestamp)} ${String(level).toUpperCase()} [${scope}] ${String(message)}${extra ? ` ${extra}` : ''}`;
  }

  private forward(level: LogLevel, text: string, record: Record<string, unknown>): void {
    const {
      timestamp: _timestamp,
      level: _level,
      message: _message,
      traceId: _t,
      spanId: _s,
      ...attributes
    } = record;
    logs.getLogger(SCOPE).emit({
      severityNumber: SEVERITY[level],
      severityText: level.toUpperCase(),
      body: text,
      attributes: Object.fromEntries(
        Object.entries(attributes).map(([key, value]) => [
          key,
          typeof value === 'object' ? JSON.stringify(value) : (value as string | number | boolean),
        ]),
      ),
    });
  }
}
