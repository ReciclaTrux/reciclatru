package pe.edu.upn.reciclatru.feature.profile.data

import android.content.Context
import android.location.Geocoder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pe.edu.upn.reciclatru.feature.profile.domain.GeocodingRepository
import pe.edu.upn.reciclatru.feature.profile.domain.ProfileRepository
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Singleton

/**
 * Une los contratos de `domain` con sus implementaciones y provee lo que depende de
 * Android (Geocoder, Retrofit). El resto de la app solo conoce las interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(implementation: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(implementation: GeocodingRepositoryImpl): GeocodingRepository

    companion object {

        @Provides
        @Singleton
        fun provideProfileApi(retrofit: Retrofit): ProfileApi =
            retrofit.create(ProfileApi::class.java)

        @Provides
        @Singleton
        fun provideGeocoder(@ApplicationContext context: Context): Geocoder =
            Geocoder(context, Locale.getDefault())
    }
}
