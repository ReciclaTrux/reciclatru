package pe.edu.upn.reciclatru.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pe.edu.upn.reciclatru.data.local.explore.FusedDeviceLocationProvider
import pe.edu.upn.reciclatru.data.local.explore.SharedPrefsLocationPromptStore
import pe.edu.upn.reciclatru.data.remote.api.explore.ExploreApi
import pe.edu.upn.reciclatru.data.repository.explore.ExploreRepositoryImpl
import pe.edu.upn.reciclatru.data.repository.explore.MyAcceptancesRepositoryImpl
import pe.edu.upn.reciclatru.domain.repository.explore.DeviceLocationProvider
import pe.edu.upn.reciclatru.domain.repository.explore.ExploreRepository
import pe.edu.upn.reciclatru.domain.repository.explore.LocationPromptStore
import pe.edu.upn.reciclatru.domain.repository.explore.MyAcceptancesRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExploreModule {

    @Binds
    @Singleton
    abstract fun bindExploreRepository(implementation: ExploreRepositoryImpl): ExploreRepository

    @Binds
    @Singleton
    abstract fun bindMyAcceptancesRepository(implementation: MyAcceptancesRepositoryImpl): MyAcceptancesRepository

    @Binds
    @Singleton
    abstract fun bindDeviceLocationProvider(implementation: FusedDeviceLocationProvider): DeviceLocationProvider

    @Binds
    @Singleton
    abstract fun bindLocationPromptStore(implementation: SharedPrefsLocationPromptStore): LocationPromptStore

    companion object {

        @Provides
        @Singleton
        fun provideExploreApi(retrofit: Retrofit): ExploreApi = retrofit.create(ExploreApi::class.java)

        @Provides
        @Singleton
        fun provideFusedLocationClient(
            @ApplicationContext context: Context,
        ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }
}
