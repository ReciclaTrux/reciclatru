package pe.edu.upn.reciclatru.view.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.model.explore.AcceptanceLabel
import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import pe.edu.upn.reciclatru.model.explore.MyAcceptance
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtEmptyState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbarHost
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtTab
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtTabs
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtAcceptedOrderCard
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtBottomNav
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtBottomNavItem
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.view.orders.formatWeightKg
import pe.edu.upn.reciclatru.viewmodel.explore.AcceptancesTabState
import pe.edu.upn.reciclatru.viewmodel.explore.MyAcceptancesUiState
import pe.edu.upn.reciclatru.viewmodel.explore.MyAcceptancesViewModel
import java.time.Instant

private const val AvailableDestination = "available"
private const val AcceptedDestination = "accepted"
private const val ProfileDestination = "profile"

@Composable
fun MyAcceptancesScreen(
    onOpenAvailable: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyAcceptancesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    MyAcceptancesContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onTabSelected = viewModel::onTabSelected,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onLoadMore = viewModel::onLoadMore,
        onOpenAvailable = onOpenAvailable,
        onOpenProfile = onOpenProfile,
        onOpenOrder = onOpenOrder,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyAcceptancesContent(
    state: MyAcceptancesUiState,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (AcceptancesTab) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenAvailable: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = state.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                RtTopAppBar(title = "Pedidos aceptados")
                RtTabs(
                    tabs = listOf(
                        RtTab(label = "Activos", count = state.active.acceptances.size.takeIf { state.active.loaded }),
                        RtTab(label = "Historial"),
                    ),
                    selectedIndex = if (state.selectedTab == AcceptancesTab.Active) 0 else 1,
                    onSelect = { index ->
                        onTabSelected(if (index == 0) AcceptancesTab.Active else AcceptancesTab.History)
                    },
                    modifier = Modifier.testTag("tabs_mis_aceptados"),
                )
            }
        },
        bottomBar = {
            RtBottomNav(
                items = listOf(
                    RtBottomNavItem(Icons.Rounded.TravelExplore, "Disponibles", AvailableDestination),
                    RtBottomNavItem(Icons.Rounded.AssignmentTurnedIn, "Aceptados", AcceptedDestination),
                    RtBottomNavItem(Icons.Rounded.Person, "Perfil", ProfileDestination),
                ),
                selected = AcceptedDestination,
                onSelect = { destination ->
                    when (destination) {
                        AvailableDestination -> onOpenAvailable()
                        ProfileDestination -> onOpenProfile()
                    }
                },
            )
        },
        snackbarHost = { RtSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            current.loading || (!current.loaded && current.error == null) -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                RtSpinner(size = RtSpinnerSize.Large)
            }
            current.error != null -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                RtErrorState(
                    title = "No pudimos cargar tus pedidos aceptados",
                    message = current.error,
                    onRetry = onRetry,
                    modifier = Modifier.testTag("error_mis_aceptados"),
                )
            }
            else -> PullToRefreshBox(
                isRefreshing = current.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            ) {
                if (current.acceptances.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (state.selectedTab == AcceptancesTab.Active) {
                            RtEmptyState(
                                icon = Icons.Rounded.AssignmentTurnedIn,
                                title = "Aún no aceptaste pedidos",
                                message = "Revisa los pedidos disponibles de tu zona y acepta los que puedas recoger.",
                                actionLabel = "Ver pedidos disponibles",
                                actionIcon = Icons.Rounded.TravelExplore,
                                onAction = onOpenAvailable,
                                modifier = Modifier.testTag("empty_mis_aceptados"),
                            )
                        } else {
                            RtEmptyState(
                                icon = Icons.Rounded.History,
                                title = "Tu historial está vacío",
                                message = "Aquí verás los pedidos recolectados, cancelados o en los que no te eligieron.",
                                modifier = Modifier.testTag("empty_historial_aceptados"),
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("lista_mis_aceptados"),
                        contentPadding = PaddingValues(RtSpacing.s4),
                        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
                    ) {
                        items(items = current.acceptances, key = { it.orderId }) { acceptance ->
                            RtAcceptedOrderCard(
                                zone = acceptance.zone.ifBlank { "Trujillo" },
                                weight = formatWeightKg(acceptance.totalWeightKg),
                                acceptedAt = formatAcceptedOn(acceptance.acceptedAt),
                                state = acceptance.label.toRtAcceptanceState(),
                                onClick = { onOpenOrder(acceptance.orderId) },
                            )
                        }
                        if (current.nextCursor != null) {
                            item(key = "load_more") {
                                LaunchedEffect(current.nextCursor) { onLoadMore() }
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
}

@Preview(name = "Mis aceptados", showBackground = true, heightDp = 760)
@Composable
private fun MyAcceptancesPreview() {
    RtTheme {
        MyAcceptancesContent(
            state = MyAcceptancesUiState(
                active = AcceptancesTabState(
                    loaded = true,
                    acceptances = listOf(
                        MyAcceptance("1", AcceptanceLabel.Chosen, "Víctor Larco Herrera", 14.5, Instant.now()),
                        MyAcceptance("2", AcceptanceLabel.AwaitingConfirmation, "Trujillo", 4.0, Instant.now()),
                        MyAcceptance(
                            "3",
                            AcceptanceLabel.AwaitingConfirmation,
                            "La Esperanza",
                            22.0,
                            Instant.now().minusSeconds(86_400),
                        ),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onLoadMore = {},
            onOpenAvailable = {},
            onOpenProfile = {},
            onOpenOrder = {},
        )
    }
}

@Preview(name = "Mis aceptados vacío", showBackground = true, heightDp = 760)
@Composable
private fun MyAcceptancesEmptyPreview() {
    RtTheme {
        MyAcceptancesContent(
            state = MyAcceptancesUiState(active = AcceptancesTabState(loaded = true)),
            snackbarHostState = remember { SnackbarHostState() },
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onLoadMore = {},
            onOpenAvailable = {},
            onOpenProfile = {},
            onOpenOrder = {},
        )
    }
}
