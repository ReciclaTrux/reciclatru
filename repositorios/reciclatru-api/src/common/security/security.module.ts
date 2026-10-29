import { Module } from '@nestjs/common';
import { PasswordHasher } from './password-hasher.service.js';
import { PhoneProtector } from './phone-protector.service.js';

@Module({ providers: [PasswordHasher, PhoneProtector], exports: [PasswordHasher, PhoneProtector] })
export class SecurityModule {}
