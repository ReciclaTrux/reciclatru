import { BadRequestAppException } from '../errors/app.exception.js';

export const DEFAULT_PAGE_SIZE = 20;
export const MAX_PAGE_SIZE = 50;

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const PG_TIMESTAMP = /^\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}:\d{2}(\.\d{1,6})?(Z|[+-]\d{2}(:?\d{2})?)$/;

export interface Page<T> {
  items: T[];
  nextCursor: string | null;
}

/** Opaque cursor: base64url of a small JSON object with the keyset values of the last row. */
export class CursorCodec {
  static encode(payload: object): string {
    return Buffer.from(JSON.stringify(payload), 'utf8').toString('base64url');
  }

  /** Decodes and validates the cursor; any problem is reported as a 400 on the `cursor` field. */
  static decode<T extends object>(cursor: string, isValid: (value: Record<string, unknown>) => boolean): T {
    try {
      const value = JSON.parse(Buffer.from(cursor, 'base64url').toString('utf8'));
      if (value && typeof value === 'object' && isValid(value as Record<string, unknown>)) return value as T;
    } catch {
      // handled below
    }
    throw new BadRequestAppException('El cursor no es válido.', [
      { field: 'cursor', message: 'Cursor inválido' },
    ]);
  }

  static isUuid(value: unknown): boolean {
    return typeof value === 'string' && UUID.test(value);
  }

  static isTimestamp(value: unknown): boolean {
    return typeof value === 'string' && PG_TIMESTAMP.test(value);
  }

  static isFiniteNumber(value: unknown): boolean {
    return typeof value === 'number' && Number.isFinite(value);
  }
}

/** Splits `limit + 1` fetched rows into a page and builds the next cursor from the last returned row. */
export function toPage<T>(rows: T[], limit: number, cursorOf: (lastRow: T) => object): Page<T> {
  const hasMore = rows.length > limit;
  const items = hasMore ? rows.slice(0, limit) : rows;
  return { items, nextCursor: hasMore ? CursorCodec.encode(cursorOf(items[items.length - 1])) : null };
}
