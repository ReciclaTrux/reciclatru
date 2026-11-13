package pe.edu.upn.reciclatru.feature.orders.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.feature.orders.domain.Order
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtStatusChip
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtConfirmDialog
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbarHost
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtActionBar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import java.time.Instant

@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    OrderDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onRefresh = viewModel::onRefresh,
        onCancel = viewModel::onCancelClick,
        modifier = modifier,
    )

    val order = state.order
    if (state.cancelDialogVisible && order != null) {
        CancelOrderDialog(
            order = order,
            cancelling = state.cancelling,
            onConfirm = viewModel::onCancelConfirmed,
            onDismiss = viewModel::onCancelDismissed,
        )
    }
}

@Composable
private fun CancelOrderDialog(
    order: Order,
    cancelling: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val recyclerName = order.counterpart?.fullName ?: order.chosenRecyclerName
    val phone = order.counterpart?.phone

    RtConfirmDialog(
        title = "¿Seguro que deseas cancelar?",
        message = "El pedido pasará a tu historial y no se podrá reactivar.",
        confirmLabel = "Sí, cancelar",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true,
        loading = cancelling,
        modifier = Modifier.testTag("dialog_cancelar_pedido"),
        notice = if (order.status == OrderStatus.Confirmed) {
            {
                val firstName = recyclerName?.substringBefore(' ') ?: "El reciclador"
                RtInlineAlert(
                    tone = RtInlineAlertTone.Warning,
                    title = "${recyclerName ?: "El reciclador"} podría estar en camino",
                    message = if (phone != null) {
                        "Avísale antes de cancelar: ${phone.chunked(3).joinToString(" ")}"
                    } else {
                        "Avísale antes de cancelar."
                    },
                    actionLabel = if (phone != null) "Llamar a $firstName" else null,
                    onAction = if (phone != null) {
                        {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                            }
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.testTag("alert_reciclador_en_camino"),
                )
            }
        } else {
            null
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailContent(
    state: OrderDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RtTopAppBar(title = "Detalle del pedido", onBack = onBack) },
        snackbarHost = { RtSnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.loading && state.order?.canCancel == true) {
                RtActionBar(
                    primary = {
                        RtButton(
                            text = "Cancelar pedido",
                            onClick = onCancel,
                            variant = RtButtonVariant.Secondary,
                            fullWidth = true,
                            modifier = Modifier.testTag("btn_cancelar_pedido"),
                        )
                    },
                )
            }
        },
    ) { innerPadding ->
        val order = state.order
        when {
            state.loading -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                RtSpinner(size = RtSpinnerSize.Large)
            }
            state.loadError != null || order == null -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                RtErrorState(
                    title = "No pudimos cargar el pedido",
                    message = state.loadError.orEmpty(),
                    onRetry = onRetry,
                )
            }
            else -> PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(RtSpacing.s4),
                    verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = formatPublishedDate(order.createdAt),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        RtStatusChip(status = order.status.toRtStatus())
                    }
                    StatusNotice(order = order)
                    MaterialsCard(order = order)
                    if (order.status == OrderStatus.Collected || order.status == OrderStatus.Cancelled) {
                        Text(
                            text = "Este pedido ya no se puede editar ni cancelar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusNotice(order: Order) {
    when (order.status) {
        OrderStatus.Pending -> {
            val count = order.acceptanceCount ?: 0
            RtInlineAlert(
                tone = RtInlineAlertTone.Info,
                title = when (count) {
                    0 -> "Aún ningún reciclador aceptó tu pedido"
                    1 -> "1 reciclador aceptó tu pedido"
                    else -> "$count recicladores aceptaron tu pedido"
                },
                message = "Desliza hacia abajo para ver si alguien aceptó.",
                modifier = Modifier.testTag("alert_aceptaciones"),
            )
        }
        OrderStatus.Confirmed -> RtInlineAlert(
            tone = RtInlineAlertTone.Info,
            title = "Reciclador elegido",
            message = order.chosenRecyclerName ?: "Ya elegiste a un reciclador para este pedido.",
            modifier = Modifier.testTag("alert_reciclador_elegido"),
        )
        OrderStatus.Collected -> RtInlineAlert(
            tone = RtInlineAlertTone.Success,
            title = "Entregaste ${formatWeightKg(order.totalWeightKg)} para reciclar",
            message = "Gracias por darle una segunda vida a tu material.",
            modifier = Modifier.testTag("alert_recolectado"),
        )
        OrderStatus.Cancelled -> Unit
    }
}

@Composable
private fun MaterialsCard(order: Order) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.lg)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface)
            .border(RtStroke.divider, colorScheme.outlineVariant, shape)
            .padding(RtSpacing.s4)
            .testTag("card_materiales"),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Text(
            text = "Materiales",
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
        )
        order.items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatWeightKg(item.weightKg),
                    style = RtTextStyles.bodyStrong,
                    color = colorScheme.onSurface,
                )
            }
        }
        HorizontalDivider(thickness = RtStroke.divider, color = colorScheme.outlineVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
            Text(
                text = "Peso total",
                style = RtTextStyles.bodyStrong,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatWeightKg(order.totalWeightKg),
                style = RtTextStyles.bodyStrong,
                color = colorScheme.onSurface,
            )
        }
    }
}

@Preview(name = "Detalle pendiente", showBackground = true, heightDp = 700)
@Composable
private fun OrderDetailPreview() {
    RtTheme {
        OrderDetailContent(
            state = OrderDetailUiState(
                loading = false,
                order = Order(
                    id = "1",
                    status = OrderStatus.Pending,
                    items = listOf(OrderItem("Periódicos y revistas", 5.0), OrderItem("Cartón", 7.5)),
                    totalWeightKg = 12.5,
                    acceptanceCount = 0,
                    createdAt = Instant.now(),
                    canCancel = true,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onRefresh = {},
            onCancel = {},
        )
    }
}
