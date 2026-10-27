import { Body, Controller, HttpCode, HttpStatus, Post } from '@nestjs/common';
import { Public } from '../../common/decorators/auth.decorators.js';
import { AuthService, type LoginResult } from './auth.service.js';
import { LoginDto } from './dto/login.dto.js';

@Controller('party-authentication')
export class AuthController {
  constructor(private readonly auth: AuthService) {}

  @Public()
  @HttpCode(HttpStatus.OK)
  @Post('initiation')
  login(@Body() body: LoginDto): Promise<LoginResult> {
    return this.auth.login(body.email, body.password);
  }
}
