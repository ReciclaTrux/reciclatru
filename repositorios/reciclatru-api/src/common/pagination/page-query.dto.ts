import { Type } from 'class-transformer';
import { IsInt, IsOptional, IsString, Max, MaxLength, Min } from 'class-validator';
import { DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE } from './cursor.codec.js';

/** Common `limit` and `cursor` query parameters. */
export class PageQueryDto {
  @IsOptional() @Type(() => Number) @IsInt() @Min(1) @Max(MAX_PAGE_SIZE) limit: number = DEFAULT_PAGE_SIZE;
  @IsOptional() @IsString() @MaxLength(512) cursor?: string;
}
