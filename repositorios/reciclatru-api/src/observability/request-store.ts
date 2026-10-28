import { AsyncLocalStorage } from 'node:async_hooks';
import type { PartyRole } from '../common/domain/enums.js';

/** Data attached to every log line written while a request is being handled. */
export interface LogContext {
  correlationId: string;
  userId?: string;
  role?: PartyRole;
}

const storage = new AsyncLocalStorage<LogContext>();

export const requestStore = {
  run: <T>(context: LogContext, callback: () => T): T => storage.run(context, callback),
  current: (): LogContext | undefined => storage.getStore(),
};
