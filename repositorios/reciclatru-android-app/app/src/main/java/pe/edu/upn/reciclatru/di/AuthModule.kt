package pe.edu.upn.reciclatru.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upn.reciclatru.data.remote.api.auth.AuthApi
import pe.edu.upn.reciclatru.data.repository.auth.AuthRepositoryImpl
import pe.edu.upn.reciclatru.domain.repository.auth.AuthRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(implementation: AuthRepositoryImpl): AuthRepository

    companion object {

        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    }
}
