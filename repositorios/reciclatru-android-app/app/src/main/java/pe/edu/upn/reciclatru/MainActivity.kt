package pe.edu.upn.reciclatru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RtTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // Las pantallas de las HU llegan con sus features; por ahora solo el
                    // nombre de la app con la tipografía y el color del sistema.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(RtSpacing.s4),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "ReciclaTru",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.rtColors.ods12,
                        )
                    }
                }
            }
        }
    }
}
