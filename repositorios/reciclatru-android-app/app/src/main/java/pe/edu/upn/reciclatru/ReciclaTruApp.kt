package pe.edu.upn.reciclatru

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada de Hilt. Todo lo que necesita inyección (ViewModel, repositorio,
 * interceptor) nace desde acá; no se crean instancias a mano en Compose.
 */
@HiltAndroidApp
class ReciclaTruApp : Application()
