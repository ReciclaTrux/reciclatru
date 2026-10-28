/**
 * OpenTelemetry bootstrap. It is loaded before the application (`node --import ./dist/src/instrumentation.js`)
 * and does nothing unless `OTEL_ENABLED=true`, so local runs and tests never send data anywhere.
 *
 * Endpoint, protocol and headers come from the standard `OTEL_EXPORTER_OTLP_*` variables.
 */
import { register } from 'node:module';

const HEALTH_PATH = '/api/v1/health';
const SHUTDOWN_SIGNALS = ['SIGTERM', 'SIGINT'] as const;

async function startOpenTelemetry(): Promise<void> {
  // Lets the instrumentations patch ESM modules; must run before the application imports them.
  register('@opentelemetry/instrumentation/hook.mjs', import.meta.url);

  const [
    { diag, DiagConsoleLogger, DiagLogLevel },
    { NodeSDK },
    traces,
    logs,
    processors,
    http,
    express,
    pg,
  ] = await Promise.all([
    import('@opentelemetry/api'),
    import('@opentelemetry/sdk-node'),
    import('@opentelemetry/exporter-trace-otlp-proto'),
    import('@opentelemetry/exporter-logs-otlp-proto'),
    import('@opentelemetry/sdk-logs'),
    import('@opentelemetry/instrumentation-http'),
    import('@opentelemetry/instrumentation-express'),
    import('@opentelemetry/instrumentation-pg'),
  ]);

  // Without this, a wrong token or endpoint fails silently.
  diag.setLogger(new DiagConsoleLogger(), DiagLogLevel.WARN);

  const sdk = new NodeSDK({
    serviceName: process.env.OTEL_SERVICE_NAME ?? 'reciclatru-api',
    traceExporter: new traces.OTLPTraceExporter(),
    logRecordProcessors: [new processors.BatchLogRecordProcessor({ exporter: new logs.OTLPLogExporter() })],
    instrumentations: [
      new http.HttpInstrumentation({
        ignoreIncomingRequestHook: (request) => request.url?.startsWith(HEALTH_PATH) ?? false,
      }),
      new express.ExpressInstrumentation(),
      new pg.PgInstrumentation(),
    ],
  });
  sdk.start();

  for (const signal of SHUTDOWN_SIGNALS) {
    // Flushes the pending batch: Render stops idle free services, and unsent telemetry would be lost.
    process.once(signal, () => void sdk.shutdown().catch(() => undefined));
  }
}

if (process.env.OTEL_ENABLED === 'true') await startOpenTelemetry();
