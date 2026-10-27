import {
  Controller,
  Get,
  Header,
  HttpCode,
  HttpStatus,
  Param,
  Post,
  Put,
  Redirect,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { CurrentUser, Roles } from '../../common/decorators/auth.decorators.js';
import { PartyRole } from '../../common/domain/enums.js';
import type { AuthenticatedUser } from '../../common/utils/request-context.js';
import { uuidPipe } from '../../common/utils/self-reference.js';
import { OrderPhotoService, type UploadedPhoto } from './services/order-photo.service.js';

@Controller('servicing-order/:crReferenceId/photo')
export class OrderPhotoController {
  constructor(private readonly photos: OrderPhotoService) {}

  @Roles(PartyRole.NEIGHBOR)
  @UseInterceptors(FileInterceptor('file'))
  @Post('initiation')
  upload(
    @Param('crReferenceId', uuidPipe) id: string,
    @UploadedFile() file: UploadedPhoto | undefined,
    @CurrentUser() user: AuthenticatedUser,
  ) {
    return this.photos.upload(user, id, file);
  }

  /** 302 to a signed URL of the private bucket; the token never reaches the storage provider. */
  @Get(':photoId/retrieval')
  @Redirect()
  @Header('Cache-Control', 'private, max-age=300')
  async retrieve(
    @Param('crReferenceId', uuidPipe) id: string,
    @Param('photoId', uuidPipe) photoId: string,
    @CurrentUser() user: AuthenticatedUser,
  ) {
    return { url: await this.photos.signedUrlFor(user, id, photoId), statusCode: HttpStatus.FOUND };
  }

  @Roles(PartyRole.NEIGHBOR)
  @HttpCode(HttpStatus.NO_CONTENT)
  @Put(':photoId/termination')
  remove(
    @Param('crReferenceId', uuidPipe) id: string,
    @Param('photoId', uuidPipe) photoId: string,
    @CurrentUser() user: AuthenticatedUser,
  ) {
    return this.photos.remove(user, id, photoId);
  }
}
