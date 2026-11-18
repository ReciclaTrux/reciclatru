package pe.edu.upn.reciclatru.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upn.reciclatru.data.local.orders.InMemoryPendingPhotosStore
import pe.edu.upn.reciclatru.data.remote.api.orders.OrdersApi
import pe.edu.upn.reciclatru.data.repository.orders.OrdersRepositoryImpl
import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.domain.repository.orders.PendingPhotosStore
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrdersModule {

    @Binds
    @Singleton
    abstract fun bindOrdersRepository(implementation: OrdersRepositoryImpl): OrdersRepository

    @Binds
    @Singleton
    abstract fun bindPendingPhotosStore(implementation: InMemoryPendingPhotosStore): PendingPhotosStore

    companion object {

        @Provides
        @Singleton
        fun provideOrdersApi(retrofit: Retrofit): OrdersApi = retrofit.create(OrdersApi::class.java)
    }
}
