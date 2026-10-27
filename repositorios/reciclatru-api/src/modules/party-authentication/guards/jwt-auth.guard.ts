import { type CanActivate, type ExecutionContext, Injectable } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { JwtService } from '@nestjs/jwt';
import { IS_PUBLIC_KEY } from '../../../common/decorators/auth.decorators.js';
import { PartyRole } from '../../../common/domain/enums.js';
import { UnauthorizedAppException } from '../../../common/errors/app.exception.js';
import type { JwtPayload } from '../../../common/security/jwt-payload.js';
import type { RequestContext } from '../../../common/utils/request-context.js';

/** Global guard: requires a valid Bearer token unless the route is marked `@Public()`. */
@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly jwt: JwtService,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (isPublic) return true;

    const request = context.switchToHttp().getRequest<RequestContext>();
    const token = this.extractToken(request.headers.authorization);
    if (!token) throw new UnauthorizedAppException();

    try {
      const payload = await this.jwt.verifyAsync<JwtPayload>(token);
      if (!payload.sub || !Object.values(PartyRole).includes(payload.role))
        throw new UnauthorizedAppException();
      request.user = { id: payload.sub, role: payload.role };
      return true;
    } catch {
      throw new UnauthorizedAppException();
    }
  }

  private extractToken(header?: string): string | null {
    const [scheme, token] = header?.split(' ') ?? [];
    return scheme === 'Bearer' && token ? token : null;
  }
}
