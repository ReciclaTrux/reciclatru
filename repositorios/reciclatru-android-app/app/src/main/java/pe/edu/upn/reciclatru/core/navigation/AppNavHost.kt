package pe.edu.upn.reciclatru.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pe.edu.upn.reciclatru.feature.profile.ui.LocationScreen
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Único `NavHost` de la app. Cada feature registra sus pantallas acá cuando su HU lo
 * necesita; las pantallas no se conocen entre sí.
 *
 * El destino inicial es un marcador mientras no exista el ingreso (HU-01/HU-02). Cuando
 * esas HU estén, el inicio pasa a `AppRoutes.Auth` o a la raíz según [SessionStore].
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Home,
        modifier = modifier,
    ) {
        composable(AppRoutes.Home) {
            HomePlaceholder(
                onOpenPickupLocation = { navController.navigate(AppRoutes.PickupLocation) },
            )
        }
        composable(AppRoutes.PickupLocation) {
            LocationScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
            )
        }
    }
}

/**
 * Pantalla temporal de arranque. No es una HU: existe para que la app compile y se pueda
 * abrir la única HU implementada (HU-04) mientras el equipo hace el registro y el ingreso.
 */
@Composable
private fun HomePlaceholder(onOpenPickupLocation: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(RtSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s4, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "ReciclaTru",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Conectamos a los vecinos de Trujillo con recicladores.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RtInlineAlert(
            tone = RtInlineAlertTone.Info,
            message = "Esta es la base de la app. El registro y el ingreso llegan con " +
                "HU-01 y HU-02; el punto de recojo ya está listo como ejemplo.",
        )
        RtButton(
            text = "Marcar punto de recojo",
            onClick = onOpenPickupLocation,
            fullWidth = true,
            modifier = Modifier.testTag("btn_abrir_ubicacion"),
        )
    }
}

@Preview(name = "Inicio", showBackground = true, heightDp = 700)
@Composable
private fun HomePlaceholderPreview() {
    RtTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            HomePlaceholder(onOpenPickupLocation = {})
        }
    }
}
