import { describe, expect, it } from '@jest/globals';
import { OrderAction, OrderStatus } from '../../../common/domain/enums.js';
import { UnprocessableAppException } from '../../../common/errors/app.exception.js';
import { ErrorCode } from '../../../common/errors/error-code.js';
import { OrderRules } from './order.rules.js';

const item = (weightKg: number) => ({ description: 'Material', weightKg });

describe('OrderRules', () => {
  describe('validateItems (RN06)', () => {
    it('accepts 1 to 10 items with valid weights', () => {
      expect(() => OrderRules.validateItems([item(0.1)])).not.toThrow();
      expect(() => OrderRules.validateItems(Array.from({ length: 10 }, () => item(500)))).not.toThrow();
    });

    it.each([[0], [11]])('rejects %i items with INVALID_ITEMS', (count) => {
      const run = () => OrderRules.validateItems(Array.from({ length: count }, () => item(1)));
      expect(run).toThrow(UnprocessableAppException);
      try {
        run();
      } catch (error) {
        expect((error as UnprocessableAppException).code).toBe(ErrorCode.INVALID_ITEMS);
      }
    });

    it('reports every invalid weight by index', () => {
      try {
        OrderRules.validateItems([item(1), item(0), item(501), item(2.55)]);
        throw new Error('should have thrown');
      } catch (error) {
        const problem = error as UnprocessableAppException;
        expect(problem.code).toBe(ErrorCode.INVALID_WEIGHT);
        expect(problem.errors?.map((e) => e.field)).toEqual([
          'items[1].weightKg',
          'items[2].weightKg',
          'items[3].weightKg',
        ]);
      }
    });
  });

  describe('isValidWeight', () => {
    it.each([
      [0.1, true],
      [2.5, true],
      [4.5, true],
      [0.3, true],
      [500, true],
      [0, false],
      [-1, false],
      [500.1, false],
      [0.05, false],
      [1.25, false],
      [Number.NaN, false],
      [Infinity, false],
    ])('%p -> %p', (value, expected) => expect(OrderRules.isValidWeight(value)).toBe(expected));
  });

  it('totalWeight sums without floating point noise', () => {
    expect(OrderRules.totalWeight([item(4.5), item(8), item(1.2)])).toBe(13.7);
    expect(OrderRules.totalWeight([item(0.1), item(0.2)])).toBe(0.3);
    expect(OrderRules.totalWeight([])).toBe(0);
  });

  it('isEditable only for PENDING orders without acceptances (RN11)', () => {
    expect(OrderRules.isEditable(OrderStatus.PENDING, 0)).toBe(true);
    expect(OrderRules.isEditable(OrderStatus.PENDING, 1)).toBe(false);
    expect(OrderRules.isEditable(OrderStatus.CONFIRMED, 0)).toBe(false);
  });

  it('isCancellable from PENDING or CONFIRMED only (RN16)', () => {
    expect([OrderStatus.PENDING, OrderStatus.CONFIRMED].every((s) => OrderRules.isCancellable(s))).toBe(true);
    expect([OrderStatus.COLLECTED, OrderStatus.CANCELLED].some((s) => OrderRules.isCancellable(s))).toBe(
      false,
    );
  });

  it('neighborActions follow the state machine', () => {
    expect(OrderRules.neighborActions(OrderStatus.PENDING, 0)).toEqual([
      OrderAction.EDIT,
      OrderAction.CANCEL,
    ]);
    expect(OrderRules.neighborActions(OrderStatus.PENDING, 2)).toEqual([
      OrderAction.CHOOSE_RECYCLER,
      OrderAction.CANCEL,
    ]);
    expect(OrderRules.neighborActions(OrderStatus.CONFIRMED, 2)).toEqual([
      OrderAction.MARK_COLLECTED,
      OrderAction.CANCEL,
    ]);
    expect(OrderRules.neighborActions(OrderStatus.COLLECTED, 1)).toEqual([]);
    expect(OrderRules.neighborActions(OrderStatus.CANCELLED, 1)).toEqual([]);
  });

  it('approximateDistance rounds to 0.1 km and keeps null', () => {
    expect(OrderRules.approximateDistance(3.449)).toBe(3.4);
    expect(OrderRules.approximateDistance(3.45)).toBe(3.5);
    expect(OrderRules.approximateDistance(null)).toBeNull();
  });
});
