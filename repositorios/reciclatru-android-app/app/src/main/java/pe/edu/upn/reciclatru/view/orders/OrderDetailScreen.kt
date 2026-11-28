package pe.edu.upn.reciclatru.view.orders


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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderAcceptance
import pe.edu.upn.reciclatru.model.orders.OrderContact
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtStatusChip
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtConfirmDialog
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtContactRow
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbarHost
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtAcceptanceCard
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtActionBar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtPhotoGallery
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.viewmodel.orders.OrderDetailUiState
import pe.edu.upn.reciclatru.viewmodel.orders.OrderDetailViewModel
import java.time.Instant


@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    editResult: String? = null,
    onEditResultHandled: () -> Unit = {},
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(editResult) {
        editResult?.let { message ->
            viewModel.onEdited(message)
            onEditResultHandled()
        }
    }

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
        onMarkCollected = viewModel::onCollectClick,
        onEdit = { state.order?.let { order -> onEdit(order.id) } },
        onRetryPhotos = viewModel::onRetryPhotos,
        onRetryAcceptances = viewModel::onRetryAcceptances,
        onChoose = viewModel::onChooseClick,
        modifier = modifier,
    )

    state.recyclerToChoose?.let { acceptance ->
        RtConfirmDialog(
            title = "¿Elegir a ${acceptance.fullName}?",
            message = "Él recogerá tu pedido. Los demás recicladores quedarán descartados y no podrás cambiar tu elección.",
            confirmLabel = "Elegir",
            onConfirm = viewModel::onChooseConfirmed,
            onDismiss = viewModel::onChooseDismissed,
            loading = state.choosing,
            modifier = Modifier.testTag("dialog_elegir_reciclador"),
        )
    }

    val order = state.order
    if (state.collectDialogVisible && order != null) {
        val recyclerName = order.counterpart?.fullName ?: order.chosenRecyclerName
        RtConfirmDialog(
            title = "¿${recyclerName ?: "El reciclador"} ya recogió tu material?",
            message = "Esta acción no se puede deshacer.",
            confirmLabel = "Sí, recolectado",
            onConfirm = viewModel::onCollectConfirmed,
            onDismiss = viewModel::onCollectDismissed,
            loading = state.collecting,
            modifier = Modifier.testTag("dialog_marcar_recolectado"),
        )
    }
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
    onRetryPhotos: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onMarkCollected: () -> Unit = {},
    onRetryAcceptances: () -> Unit = {},
    onChoose: (OrderAcceptance) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RtTopAppBar(title = "Detalle del pedido", onBack = onBack) },
        snackbarHost = { RtSnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.loading && state.order?.canCancel == true) {
                val cancelButton: @Composable () -> Unit = {
                    RtButton(
                        text = "Cancelar pedido",
                        onClick = onCancel,
                        variant = RtButtonVariant.Secondary,
                        fullWidth = true,
                        modifier = Modifier.testTag("btn_cancelar_pedido"),
                    )
                }
                if (state.order?.canMarkCollected == true) {
                    RtActionBar(
                        primary = {
                            RtButton(
                                text = "Recolectado",
                                onClick = onMarkCollected,
                                fullWidth = true,
                                modifier = Modifier.testTag("btn_marcar_recolectado"),
                            )
                        },
                        secondary = cancelButton,
                    )
                } else if (state.order?.canEdit == true) {
                    RtActionBar(
                        primary = {
                            RtButton(
                                text = "Editar",
                                onClick = onEdit,
                                icon = Icons.Rounded.Edit,
                                fullWidth = true,
                                modifier = Modifier.testTag("btn_editar_pedido"),
                            )
                        },
                        secondary = cancelButton,
                    )
                } else {
                    RtActionBar(primary = cancelButton)
                }
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
                    if (order.status == OrderStatus.Pending) {
                        AcceptancesSection(
                            state = state,
                            canChoose = order.canChooseRecycler,
                            onChoose = onChoose,
                            onRetry = onRetryAcceptances,
                        )
                    }
                    MaterialsCard(items = order.items, totalWeightKg = order.totalWeightKg)
                    if (order.photos.isNotEmpty()) {
                        RtPhotoGallery(
                            photos = order.photos.map { it.url },
                            modifier = Modifier.testTag("galeria_fotos"),
                        )
                    }
                    if (state.pendingPhotoCount > 0) {
                        RtInlineAlert(
                            tone = RtInlineAlertTone.Warning,
                            title = if (state.pendingPhotoCount == 1) {
                                "No se subió 1 foto"
                            } else {
                                "No se subieron ${state.pendingPhotoCount} fotos"
                            },
                            message = "El pedido quedó publicado. Puedes intentar subirlas otra vez.",
                            actionLabel = if (state.uploadingPhotos) "Subiendo…" else "Reintentar",
                            onAction = onRetryPhotos,
                            modifier = Modifier.testTag("alert_fotos_pendientes"),
                        )
                    }
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
private fun AcceptancesSection(
    state: OrderDetailUiState,
    canChoose: Boolean,
    onChoose: (OrderAcceptance) -> Unit,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    when {
        state.acceptancesLoading -> Box(
            modifier = Modifier.fillMaxWidth().testTag("cargando_aceptaciones"),
            contentAlignment = Alignment.Center,
        ) {
            RtSpinner()
        }
        state.acceptancesError != null -> RtInlineAlert(
            tone = RtInlineAlertTone.Error,
            title = "No pudimos cargar a los recicladores",
            message = state.acceptancesError,
            actionLabel = "Reintentar",
            onAction = onRetry,
            modifier = Modifier.testTag("alert_error_aceptaciones"),
        )
        state.acceptances.isNotEmpty() -> Column(
            modifier = Modifier.testTag("lista_aceptaciones"),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
        ) {
            Text(
                text = "Recicladores que aceptaron (${state.acceptances.count { it.status == AcceptanceStatus.Accepted }})",
                style = RtTextStyles.bodyStrong,
                color = MaterialTheme.colorScheme.onSurface,
            )
            state.acceptances
                .filter { it.status == AcceptanceStatus.Accepted }
                .forEach { acceptance ->
                    RtAcceptanceCard(
                        name = acceptance.fullName,
                        phone = acceptance.phone,
                        acceptedAt = formatAcceptedDate(acceptance.acceptedAt),
                        onCall = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${acceptance.phone}")),
                                )
                            }
                        },
                        onChoose = if (canChoose) ({ onChoose(acceptance) }) else null,
                        modifier = Modifier.testTag("aceptacion_${acceptance.id}"),
                    )
                }
            if (canChoose) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("nota_no_editable"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(RtSize.iconSm),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Ya no puedes editar este pedido porque hay recicladores que aceptaron.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChosenRecyclerCard(recycler: OrderContact) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(RtRadius.lg)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(RtStroke.divider, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(RtSpacing.s3)
            .testTag("tarjeta_reciclador_elegido"),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s2),
    ) {
        Text(
            text = "Reciclador elegido",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RtContactRow(
            name = recycler.fullName,
            subtitle = recycler.phone,
            phone = recycler.phone,
            onCall = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${recycler.phone}")))
                }
            },
        )
    }
}

@Composable
private fun StatusNotice(order: Order) {
    when (order.status) {
        OrderStatus.Pending -> if ((order.acceptanceCount ?: 0) == 0) {
            RtInlineAlert(
                tone = RtInlineAlertTone.Info,
                title = "Aún ningún reciclador aceptó tu pedido",
                message = "Desliza hacia abajo para ver si alguien aceptó.",
                modifier = Modifier.testTag("alert_aceptaciones"),
            )
        }
        OrderStatus.Confirmed -> {
            val recycler = order.counterpart
            if (recycler != null) {
                ChosenRecyclerCard(recycler)
            } else {
                RtInlineAlert(
                    tone = RtInlineAlertTone.Info,
                    title = "Reciclador elegido",
                    message = order.chosenRecyclerName ?: "Ya elegiste a un reciclador para este pedido.",
                    modifier = Modifier.testTag("alert_reciclador_elegido"),
                )
            }
        }
        OrderStatus.Collected -> RtInlineAlert(
            tone = RtInlineAlertTone.Success,
            title = "Entregaste ${formatWeightKg(order.totalWeightKg)} para reciclar",
            message = "Gracias por darle una segunda vida a tu material.",
            modifier = Modifier.testTag("alert_recolectado"),
        )
        OrderStatus.Cancelled -> Unit
    }
}

@Preview(name = "Pendiente sin aceptaciones", showBackground = true, heightDp = 700)
@Composable
private fun OrderDetailPendingEmptyPreview() {
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
            onRetryPhotos = {},
        )
    }
}

private val PreviewItems = listOf(OrderItem("Cajas de cartón", 6.0), OrderItem("Latas de aluminio", 3.0))

private fun previewOrder(
    status: OrderStatus,
    acceptanceCount: Int = 0,
    counterpart: OrderContact? = null,
    canChooseRecycler: Boolean = false,
) = Order(
    id = "1",
    status = status,
    items = PreviewItems,
    totalWeightKg = 9.0,
    acceptanceCount = acceptanceCount,
    createdAt = Instant.now(),
    counterpart = counterpart,
    canCancel = status == OrderStatus.Pending || status == OrderStatus.Confirmed,
    canMarkCollected = status == OrderStatus.Confirmed,
    canChooseRecycler = canChooseRecycler,
)

@Composable
private fun OrderDetailPreviewHost(state: OrderDetailUiState) {
    RtTheme {
        OrderDetailContent(
            state = state,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onRefresh = {},
            onCancel = {},
            onRetryPhotos = {},
        )
    }
}

@Preview(name = "P18 Pendiente con 3 aceptaciones", showBackground = true, heightDp = 800)
@Composable
private fun OrderDetailWithAcceptancesPreview() {
    OrderDetailPreviewHost(
        OrderDetailUiState(
            loading = false,
            order = previewOrder(OrderStatus.Pending, acceptanceCount = 3, canChooseRecycler = true),
            acceptances = listOf(
                OrderAcceptance("a1", AcceptanceStatus.Accepted, "Juan Pérez", "987654321", Instant.now()),
                OrderAcceptance("a2", AcceptanceStatus.Accepted, "María Ríos", "956111204", Instant.now()),
                OrderAcceptance("a3", AcceptanceStatus.Accepted, "Carlos Vega", "912330518", Instant.now()),
            ),
        ),
    )
}

@Preview(name = "Cargando aceptaciones", showBackground = true, heightDp = 700)
@Composable
private fun OrderDetailLoadingAcceptancesPreview() {
    OrderDetailPreviewHost(
        OrderDetailUiState(
            loading = false,
            order = previewOrder(OrderStatus.Pending, acceptanceCount = 2),
            acceptancesLoading = true,
        ),
    )
}

@Preview(name = "Error en aceptaciones", showBackground = true, heightDp = 700)
@Composable
private fun OrderDetailAcceptancesErrorPreview() {
    OrderDetailPreviewHost(
        OrderDetailUiState(
            loading = false,
            order = previewOrder(OrderStatus.Pending, acceptanceCount = 2),
            acceptancesError = "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
        ),
    )
}

@Preview(name = "P20 Confirmado", showBackground = true, heightDp = 700)
@Composable
private fun OrderDetailConfirmedPreview() {
    OrderDetailPreviewHost(
        OrderDetailUiState(
            loading = false,
            order = previewOrder(
                OrderStatus.Confirmed,
                counterpart = OrderContact("Juan Pérez", "987654321"),
            ),
        ),
    )
}

@Preview(name = "Recolectado", showBackground = true, heightDp = 700)
@Composable
private fun OrderDetailCollectedPreview() {
    OrderDetailPreviewHost(
        OrderDetailUiState(loading = false, order = previewOrder(OrderStatus.Collected)),
    )
}

@Preview(name = "Cargando", showBackground = true, heightDp = 400)
@Composable
private fun OrderDetailLoadingPreview() {
    OrderDetailPreviewHost(OrderDetailUiState(loading = true))
}

@Preview(name = "Error al cargar", showBackground = true, heightDp = 400)
@Composable
private fun OrderDetailLoadErrorPreview() {
    OrderDetailPreviewHost(
        OrderDetailUiState(
            loading = false,
            loadError = "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
        ),
    )
}
