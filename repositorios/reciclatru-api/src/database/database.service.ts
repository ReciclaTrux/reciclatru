import { AsyncLocalStorage } from 'node:async_hooks';
import { Injectable } from '@nestjs/common';
import { DataSource, type QueryRunner } from 'typeorm';

/** Runs SQL on the pooled connection, or on the current transaction when called inside `UnitOfWork.run`. */
@Injectable()
export class Database {
  private readonly transactions = new AsyncLocalStorage<QueryRunner>();

  constructor(private readonly dataSource: DataSource) {}

  /** Executes a statement and returns its rows. */
  async query<T>(sql: string, params: unknown[] = []): Promise<T[]> {
    const result = await this.withRunner((runner) => runner.query(sql, params, true));
    return result.records as T[];
  }

  /** Executes a statement and returns the number of affected rows. */
  async execute(sql: string, params: unknown[] = []): Promise<number> {
    const result = await this.withRunner((runner) => runner.query(sql, params, true));
    return result.affected ?? result.records.length;
  }

  /** Returns the first row or null. */
  async queryOne<T>(sql: string, params: unknown[] = []): Promise<T | null> {
    const rows = await this.query<T>(sql, params);
    return rows[0] ?? null;
  }

  /** Runs `work` inside a transaction; nested calls join the outer one. */
  async transaction<T>(work: () => Promise<T>): Promise<T> {
    if (this.transactions.getStore()) return work();
    const runner = this.dataSource.createQueryRunner();
    await runner.connect();
    await runner.startTransaction();
    try {
      const result = await this.transactions.run(runner, work);
      await runner.commitTransaction();
      return result;
    } catch (error) {
      await runner.rollbackTransaction();
      throw error;
    } finally {
      await runner.release();
    }
  }

  /** Cheap liveness probe used by `/health`. */
  async ping(): Promise<void> {
    await this.query('SELECT 1');
  }

  private async withRunner<T>(action: (runner: QueryRunner) => Promise<T>): Promise<T> {
    const current = this.transactions.getStore();
    if (current) return action(current);
    const runner = this.dataSource.createQueryRunner();
    try {
      return await action(runner);
    } finally {
      await runner.release();
    }
  }
}
