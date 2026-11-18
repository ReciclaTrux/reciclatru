package pe.edu.upn.reciclatru.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upn.reciclatru.data.remote.api.health.HealthApi
import pe.edu.upn.reciclatru.data.repository.health.HealthRepositoryImpl
import pe.edu.upn.reciclatru.domain.repository.health.HealthRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthModule {

    @Binds
    @Singleton
    abstract fun bindHealthRepository(implementation: HealthRepositoryImpl): HealthRepository

    companion object {

        @Provides
        @Singleton
        fun provideHealthApi(retrofit: Retrofit): HealthApi =
            retrofit.create(HealthApi::class.java)
    }
}
