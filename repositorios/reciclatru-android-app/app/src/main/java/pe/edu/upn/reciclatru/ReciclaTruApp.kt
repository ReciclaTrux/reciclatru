package pe.edu.upn.reciclatru

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Punto de entrada de Hilt. Todo lo que necesita inyección (ViewModel, repositorio,
 * interceptor) nace desde acá; no se crean instancias a mano en Compose.
 *
 * Las imágenes se cargan con el mismo cliente HTTP de la app, para que las fotos de los
 * pedidos viajen con el token.
 */
@HiltAndroidApp
class ReciclaTruApp : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var okHttpClient: Lazy<OkHttpClient>

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient.get() })) }
            .build()
}
