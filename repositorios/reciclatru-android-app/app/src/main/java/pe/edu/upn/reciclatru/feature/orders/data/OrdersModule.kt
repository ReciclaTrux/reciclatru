package pe.edu.upn.reciclatru.feature.orders.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersRepository
import pe.edu.upn.reciclatru.feature.orders.domain.PendingPhotosStore
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
