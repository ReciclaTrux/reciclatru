import { Type } from 'class-transformer';
import {
  ArrayMaxSize,
  IsArray,
  IsEnum,
  IsLatitude,
  IsLongitude,
  IsNumber,
  IsOptional,
  IsString,
  IsUUID,
  MaxLength,
  MinLength,
  ValidateNested,
} from 'class-validator';
import { ListTab } from '../../../common/domain/enums.js';
import { PageQueryDto } from '../../../common/pagination/page-query.dto.js';

/** Upper bound only to protect the parser; the business range (1 to 10) is checked by the order rules (422). */
const PARSER_ITEMS_LIMIT = 100;

export class OrderItemDto {
  @IsString() @MinLength(1) @MaxLength(100) description!: string;
  @IsNumber({ allowNaN: false, allowInfinity: false }) weightKg!: number;
}

export class OrderItemsDto {
  @IsArray()
  @ArrayMaxSize(PARSER_ITEMS_LIMIT)
  @ValidateNested({ each: true })
  @Type(() => OrderItemDto)
  items!: OrderItemDto[];
}

export class OrderListQueryDto extends PageQueryDto {
  @IsEnum(ListTab) tab: ListTab = ListTab.ACTIVE;
}

export class GeoQueryDto {
  @IsOptional() @Type(() => Number) @IsNumber() @IsLatitude() lat?: number;
  @IsOptional() @Type(() => Number) @IsNumber() @IsLongitude() lng?: number;
}

export class AvailabilityQueryDto extends PageQueryDto {
  @IsOptional() @Type(() => Number) @IsNumber() @IsLatitude() lat?: number;
  @IsOptional() @Type(() => Number) @IsNumber() @IsLongitude() lng?: number;
}

export class ConfirmRecyclerDto {
  @IsUUID() acceptanceId!: string;
}
