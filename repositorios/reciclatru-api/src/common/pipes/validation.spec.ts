import { describe, expect, it } from '@jest/globals';
import { Type } from 'class-transformer';
import { IsNumber, IsString, ValidateNested } from 'class-validator';
import { BadRequestAppException } from '../errors/app.exception.js';
import { createValidationPipe } from './validation.js';

class ItemDto {
  @IsString() description!: string;
  @IsNumber() weightKg!: number;
}
class OrderDto {
  @ValidateNested({ each: true }) @Type(() => ItemDto) items!: ItemDto[];
}

const pipe = createValidationPipe();
const meta = { type: 'body' as const, metatype: OrderDto };

describe('validation pipe', () => {
  it('accepts a valid body and returns a class instance', async () => {
    const result = await pipe.transform({ items: [{ description: 'Cartón', weightKg: 2 }] }, meta);
    expect(result).toBeInstanceOf(OrderDto);
  });

  it('reports nested errors with indexed field paths as a 400', async () => {
    try {
      await pipe.transform({ items: [{ description: 'ok', weightKg: 'pesado' }] }, meta);
      throw new Error('should have thrown');
    } catch (error) {
      expect(error).toBeInstanceOf(BadRequestAppException);
      expect((error as BadRequestAppException).errors?.map((e) => e.field)).toEqual(['items[0].weightKg']);
    }
  });

  it('rejects unknown properties', async () => {
    await expect(pipe.transform({ items: [], admin: true }, meta)).rejects.toBeInstanceOf(
      BadRequestAppException,
    );
  });
});
