package pe.edu.upn.reciclatru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import pe.edu.upn.reciclatru.core.navigation.AppNavHost
import pe.edu.upn.reciclatru.core.navigation.AppSessionViewModel
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sessionViewModel: AppSessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            sessionViewModel.startDestination.value == null
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RtTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
