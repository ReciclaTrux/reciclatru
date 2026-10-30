package pe.edu.upn.reciclatru.core.health.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upn.reciclatru.core.health.domain.HealthRepository
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
