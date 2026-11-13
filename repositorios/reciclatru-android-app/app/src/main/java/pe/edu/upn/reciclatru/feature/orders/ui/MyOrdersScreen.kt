package pe.edu.upn.reciclatru.feature.orders.ui

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
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus
import pe.edu.upn.reciclatru.feature.orders.domain.OrderSummary
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersTab
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtEmptyState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbarHost
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtTab
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtTabs
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtBottomNav
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtBottomNavItem
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtOrderCard
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtOrderCardFooter
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import java.time.Instant

private const val OrdersDestination = "orders"
private const val PublishDestination = "publish"
private const val ProfileDestination = "profile"

@Composable
fun MyOrdersScreen(
    publishedOrderId: String?,
    onOrderPublishedHandled: () -> Unit,
    onPublish: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyOrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    LaunchedEffect(publishedOrderId) {
        publishedOrderId?.let { orderId ->
            onOrderPublishedHandled()
            viewModel.onOrderPublished(orderId)
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            val orderId = state.messageOrderId
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (orderId != null) "Ver" else null,
                duration = if (orderId != null) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            viewModel.onMessageShown()
            if (orderId != null && result == SnackbarResult.ActionPerformed) onOpenOrder(orderId)
        }
    }

    MyOrdersContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onTabSelected = viewModel::onTabSelected,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onLoadMore = viewModel::onLoadMore,
        onPublish = onPublish,
        onOpenProfile = onOpenProfile,
        onOpenOrder = onOpenOrder,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyOrdersContent(
    state: MyOrdersUiState,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (OrdersTab) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPublish: () -> Unit,
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
                RtTopAppBar(title = "Mis pedidos")
                RtTabs(
                    tabs = listOf(
                        RtTab(label = "Activos", count = state.active.orders.size.takeIf { state.active.loaded }),
                        RtTab(label = "Historial"),
                    ),
                    selectedIndex = if (state.selectedTab == OrdersTab.Active) 0 else 1,
                    onSelect = { index -> onTabSelected(if (index == 0) OrdersTab.Active else OrdersTab.History) },
                    modifier = Modifier.testTag("tabs_mis_pedidos"),
                )
            }
        },
        bottomBar = {
            RtBottomNav(
                items = listOf(
                    RtBottomNavItem(Icons.AutoMirrored.Rounded.ReceiptLong, "Mis pedidos", OrdersDestination),
                    RtBottomNavItem(Icons.Rounded.AddCircle, "Publicar", PublishDestination),
                    RtBottomNavItem(Icons.Rounded.Person, "Perfil", ProfileDestination),
                ),
                selected = OrdersDestination,
                onSelect = { destination ->
                    when (destination) {
                        PublishDestination -> onPublish()
                        ProfileDestination -> onOpenProfile()
                    }
                },
            )
        },
        snackbarHost = { RtSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            current.loading -> Box(
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
                    title = "No pudimos cargar tus pedidos",
                    message = current.error,
                    onRetry = onRetry,
                )
            }
            else -> PullToRefreshBox(
                isRefreshing = current.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            ) {
                if (current.orders.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (state.selectedTab == OrdersTab.Active) {
                            RtEmptyState(
                                icon = Icons.Rounded.Recycling,
                                title = "Aún no publicaste pedidos",
                                message = "Publica el material que tienes y un reciclador de tu zona lo recogerá.",
                                actionLabel = "Publicar pedido",
                                actionIcon = Icons.Rounded.Add,
                                onAction = onPublish,
                                modifier = Modifier.testTag("empty_mis_pedidos"),
                            )
                        } else {
                            RtEmptyState(
                                icon = Icons.Rounded.History,
                                title = "Tu historial está vacío",
                                message = "Aquí verás tus pedidos recolectados y cancelados.",
                                modifier = Modifier.testTag("empty_historial"),
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("lista_mis_pedidos"),
                        contentPadding = PaddingValues(RtSpacing.s4),
                        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
                    ) {
                        items(items = current.orders, key = { it.id }) { order ->
                            RtOrderCard(
                                title = formatOrderDate(order.createdAt),
                                status = order.status.toRtStatus(),
                                items = formatItemCount(order.itemCount),
                                weight = formatWeightKg(order.totalWeightKg),
                                onClick = { onOpenOrder(order.id) },
                                footer = order.toFooter(),
                                thumbnail = order.thumbnailUrl,
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

private fun OrderSummary.toFooter(): RtOrderCardFooter? = when (status) {
    OrderStatus.Pending -> {
        val count = acceptanceCount ?: 0
        when (count) {
            0 -> RtOrderCardFooter(Icons.Rounded.Group, "Sin aceptaciones aún")
            1 -> RtOrderCardFooter(Icons.Rounded.Group, "1 aceptación")
            else -> RtOrderCardFooter(Icons.Rounded.Group, "$count aceptaciones")
        }
    }
    OrderStatus.Confirmed -> chosenRecyclerName?.let { RtOrderCardFooter(Icons.Rounded.Person, it) }
    OrderStatus.Collected, OrderStatus.Cancelled -> null
}

@Preview(name = "Mis pedidos", showBackground = true, heightDp = 760)
@Composable
private fun MyOrdersPreview() {
    RtTheme {
        MyOrdersContent(
            state = MyOrdersUiState(
                active = OrdersTabState(
                    loaded = true,
                    orders = listOf(
                        OrderSummary("1", OrderStatus.Pending, 3, 14.5, 3, null, Instant.now()),
                        OrderSummary("2", OrderStatus.Pending, 1, 5.0, 0, null, Instant.now().minusSeconds(86_400)),
                        OrderSummary("3", OrderStatus.Confirmed, 2, 9.0, null, "Juan Pérez", Instant.parse("2026-11-10T15:00:00Z")),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onLoadMore = {},
            onPublish = {},
            onOpenProfile = {},
            onOpenOrder = {},
        )
    }
}

@Preview(name = "Mis pedidos vacío", showBackground = true, heightDp = 760)
@Composable
private fun MyOrdersEmptyPreview() {
    RtTheme {
        MyOrdersContent(
            state = MyOrdersUiState(active = OrdersTabState(loaded = true)),
            snackbarHostState = remember { SnackbarHostState() },
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onLoadMore = {},
            onPublish = {},
            onOpenProfile = {},
            onOpenOrder = {},
        )
    }
}
