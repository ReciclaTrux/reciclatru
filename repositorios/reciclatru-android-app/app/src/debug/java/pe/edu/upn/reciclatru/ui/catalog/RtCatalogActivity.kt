package pe.edu.upn.reciclatru.ui.catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Pantalla de catálogo del sistema de diseño. Solo existe en builds de debug y no forma
 * parte de la app entregada: sirve para comparar los componentes con las vistas previas
 * del sistema.
 */
class RtCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RtTheme {
                RtCatalogScreen()
            }
        }
    }
}
