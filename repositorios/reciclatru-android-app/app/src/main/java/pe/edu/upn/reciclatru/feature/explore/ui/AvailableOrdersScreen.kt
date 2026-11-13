package pe.edu.upn.reciclatru.feature.explore.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.feature.explore.domain.AvailableOrder
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtEmptyState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbarHost
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtAvailableOrderCard
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtBottomNav
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtBottomNavItem
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtLocationPrompt
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import java.time.Instant

private const val LocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
private const val AvailableDestination = "available"
private const val AcceptedDestination = "accepted"
private const val ProfileDestination = "profile"

@Composable
fun AvailableOrdersScreen(
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AvailableOrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val activity = context.findActivity()
        val permanentlyDenied = !granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, LocationPermission)
        viewModel.onPermissionResult(granted, permanentlyDenied)
    }

    LaunchedEffect(Unit) {
        viewModel.onStart(context.hasLocationPermission())
    }
    LifecycleResumeEffect(Unit) {
        viewModel.onResume(context.hasLocationPermission())
        onPauseOrDispose { }
    }
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    AvailableOrdersContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onPromptContinue = {
            viewModel.onPromptAccepted()
            permissionLauncher.launch(LocationPermission)
        },
        onPromptDismiss = viewModel::onPromptDismissed,
        onLocationNoticeAction = { notice ->
            when (notice) {
                LocationNotice.PermissionDenied -> permissionLauncher.launch(LocationPermission)
                LocationNotice.PermissionDeniedPermanently -> context.openSettings(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ),
                )
                LocationNotice.Unavailable -> context.openSettings(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        },
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onLoadMore = viewModel::onLoadMore,
        onOpenProfile = onOpenProfile,
        modifier = modifier,
    )
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, LocationPermission) == PackageManager.PERMISSION_GRANTED

private fun Context.openSettings(intent: Intent) {
    runCatching { startActivity(intent) }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailableOrdersContent(
    state: AvailableOrdersUiState,
    snackbarHostState: SnackbarHostState,
    onPromptContinue: () -> Unit,
    onPromptDismiss: () -> Unit,
    onLocationNoticeAction: (LocationNotice) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RtTopAppBar(title = "Pedidos disponibles") },
        bottomBar = {
            RtBottomNav(
                items = listOf(
                    RtBottomNavItem(Icons.Rounded.TravelExplore, "Disponibles", AvailableDestination),
                    RtBottomNavItem(Icons.Rounded.AssignmentTurnedIn, "Aceptados", AcceptedDestination),
                    RtBottomNavItem(Icons.Rounded.Person, "Perfil", ProfileDestination),
                ),
                selected = AvailableDestination,
                onSelect = { destination -> if (destination == ProfileDestination) onOpenProfile() },
            )
        },
        snackbarHost = { RtSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.showLocationPrompt -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(RtSpacing.s4),
                contentAlignment = Alignment.Center,
            ) {
                RtLocationPrompt(
                    onContinue = onPromptContinue,
                    onDismiss = onPromptDismiss,
                    modifier = Modifier.testTag("prompt_ubicacion"),
                )
            }
            state.loading || (!state.loaded && state.loadError == null) -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                RtSpinner(size = RtSpinnerSize.Large)
            }
            state.loadError != null -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                RtErrorState(
                    title = "No pudimos cargar los pedidos",
                    message = state.loadError,
                    onRetry = onRetry,
                )
            }
            else -> PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("lista_pedidos_disponibles"),
                    contentPadding = PaddingValues(RtSpacing.s4),
                    verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
                ) {
                    state.locationNotice?.let { notice ->
                        item(key = "location_notice") {
                            RtInlineAlert(
                                tone = RtInlineAlertTone.Info,
                                title = "Activa tu ubicación para ver la distancia",
                                message = "Mientras tanto, los pedidos se ordenan del más reciente al más antiguo.",
                                actionLabel = if (notice == LocationNotice.PermissionDenied) {
                                    "Activar ubicación"
                                } else {
                                    "Abrir ajustes"
                                },
                                onAction = { onLocationNoticeAction(notice) },
                                modifier = Modifier.testTag("alert_activar_ubicacion"),
                            )
                        }
                    }
                    if (state.orders.isEmpty()) {
                        item(key = "empty") {
                            RtEmptyState(
                                icon = Icons.Rounded.TravelExplore,
                                title = "No hay pedidos disponibles por ahora",
                                message = "Cuando un vecino publique, aparecerá aquí. Desliza hacia abajo para actualizar.",
                                modifier = Modifier.testTag("empty_pedidos_disponibles"),
                            )
                        }
                    } else if (state.sortedByDistance) {
                        item(key = "sorted_by") {
                            Text(
                                text = "Ordenados del más cercano al más lejano",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(items = state.orders, key = { it.id }) { order ->
                        RtAvailableOrderCard(
                            zone = order.zone.ifBlank { "Trujillo" },
                            weight = formatAvailableWeight(order.totalWeightKg),
                            items = formatAvailableItems(order.itemCount),
                            distance = formatDistance(order.distanceKm),
                            timeAgo = formatTimeAgo(order.createdAt),
                            alreadyAccepted = order.alreadyAccepted,
                        )
                    }
                    if (state.nextCursor != null) {
                        item(key = "load_more") {
                            LaunchedEffect(state.nextCursor) { onLoadMore() }
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(RtSpacing.s3),
                                contentAlignment = Alignment.Center,
                            ) {
                                RtSpinner()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Pedidos disponibles", showBackground = true, heightDp = 760)
@Composable
private fun AvailableOrdersPreview() {
    RtTheme {
        AvailableOrdersContent(
            state = AvailableOrdersUiState(
                loaded = true,
                sortedByDistance = true,
                orders = listOf(
                    AvailableOrder("1", "Víctor Larco Herrera", 3, 14.5, 1.2, false, Instant.now().minusSeconds(7_200)),
                    AvailableOrder("2", "Trujillo", 1, 4.0, 2.6, true, Instant.now().minusSeconds(1_200)),
                    AvailableOrder("3", "La Esperanza", 4, 22.0, 5.1, false, Instant.now().minusSeconds(90_000)),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onPromptContinue = {},
            onPromptDismiss = {},
            onLocationNoticeAction = {},
            onRefresh = {},
            onRetry = {},
            onLoadMore = {},
            onOpenProfile = {},
        )
    }
}

@Preview(name = "Permiso de ubicación", showBackground = true, heightDp = 760)
@Composable
private fun AvailableOrdersPromptPreview() {
    RtTheme {
        AvailableOrdersContent(
            state = AvailableOrdersUiState(showLocationPrompt = true),
            snackbarHostState = remember { SnackbarHostState() },
            onPromptContinue = {},
            onPromptDismiss = {},
            onLocationNoticeAction = {},
            onRefresh = {},
            onRetry = {},
            onLoadMore = {},
            onOpenProfile = {},
        )
    }
}
