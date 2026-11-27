package pe.edu.upn.reciclatru.view.explore

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
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import pe.edu.upn.reciclatru.model.explore.AvailableOrderDetail
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.OrderContact
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAcceptanceChip
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAcceptanceState
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtConfirmDialog
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtContactRow
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtMeta
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtMetaRow
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbarHost
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtActionBar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtPhotoGallery
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.view.orders.MaterialsCard
import pe.edu.upn.reciclatru.view.orders.formatOrderDate
import pe.edu.upn.reciclatru.view.orders.formatPublishedDate
import pe.edu.upn.reciclatru.view.orders.formatWeightKg
import pe.edu.upn.reciclatru.viewmodel.explore.AvailableOrderDetailUiState
import pe.edu.upn.reciclatru.viewmodel.explore.AvailableOrderDetailViewModel
import java.time.Instant

/**
 * Detalle de un pedido para el reciclador: lo que ve antes de aceptar (HU-11) y después de
 * aceptar (HU-12). Sin aceptar nunca muestra dirección ni contacto del vecino.
 *
 * @param onUnavailable el pedido dejó de estar Pendiente: la pantalla que navega regresa a
 *   la lista y avisa "Este pedido ya no está disponible".
 * @param onCompleteProfile lleva a completar el perfil cuando falta para poder aceptar.
 */
@Composable
fun AvailableOrderDetailScreen(
    onBack: () -> Unit,
    onUnavailable: () -> Unit,
    onCompleteProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AvailableOrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.unavailable) {
        if (state.unavailable) onUnavailable()
    }
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    AvailableOrderDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onAccept = viewModel::onAcceptClick,
        modifier = modifier,
    )

    if (state.confirmDialogVisible) {
        RtConfirmDialog(
            title = "¿Aceptar este pedido?",
            message = "Al aceptar, el vecino verá tu nombre y celular. Si te elige, verás su dirección para ir a recoger.",
            confirmLabel = "Aceptar",
            onConfirm = viewModel::onAcceptConfirmed,
            onDismiss = viewModel::onAcceptDismissed,
            loading = state.accepting,
            modifier = Modifier.testTag("dialog_aceptar_pedido"),
        )
    }
    if (state.profileIncomplete) {
        RtConfirmDialog(
            title = "Completa tu perfil para aceptar",
            message = "El vecino necesita tu nombre y celular para coordinar el recojo.",
            confirmLabel = "Completar perfil",
            onConfirm = {
                viewModel.onProfileDialogDismissed()
                onCompleteProfile()
            },
            onDismiss = viewModel::onProfileDialogDismissed,
            dismissLabel = "Ahora no",
            modifier = Modifier.testTag("dialog_perfil_incompleto"),
        )
    }
}

private val ClosedStates = setOf(
    RtAcceptanceState.NotChosen,
    RtAcceptanceState.CancelledByNeighbor,
    RtAcceptanceState.Collected,
)

@Composable
private fun AvailableOrderDetailContent(
    state: AvailableOrderDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val order = state.order
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RtTopAppBar(title = "Detalle del pedido", onBack = onBack) },
        snackbarHost = { RtSnackbarHost(snackbarHostState) },
        bottomBar = {
            val closed = order?.alreadyAccepted == true && order.acceptanceState() in ClosedStates
            if (!state.loading && order != null && !closed) {
                RtActionBar(
                    primary = {
                        if (order.alreadyAccepted) {
                            RtButton(
                                text = "Ya aceptaste este pedido",
                                onClick = {},
                                variant = RtButtonVariant.Secondary,
                                icon = Icons.Rounded.Check,
                                enabled = false,
                                fullWidth = true,
                                modifier = Modifier.testTag("btn_ya_aceptaste"),
                            )
                        } else {
                            RtButton(
                                text = "Aceptar pedido",
                                onClick = onAccept,
                                fullWidth = true,
                                modifier = Modifier.testTag("btn_aceptar_pedido"),
                            )
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
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
            else -> Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
            ) {
                if (order.alreadyAccepted) AcceptedBody(order) else AvailableBody(order)
            }
        }
    }
}

@Composable
private fun AvailableBody(order: AvailableOrderDetail) {
    if (order.photos.isNotEmpty()) {
        RtPhotoGallery(
            photos = order.photos.map { it.url },
            modifier = Modifier.testTag("galeria_fotos"),
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        Text(
            text = order.zone.ifBlank { "Trujillo" },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("zona_pedido"),
        )
        RtMetaRow(items = orderMeta(order))
    }
    MaterialsCard(
        items = order.items,
        totalWeightKg = order.totalWeightKg,
        modifier = Modifier.testTag("materiales_pedido"),
    )
    RtInlineAlert(
        tone = RtInlineAlertTone.Info,
        message = "Verás el nombre y celular del vecino al aceptar. La dirección exacta solo se muestra si te elige.",
        modifier = Modifier.testTag("aviso_privacidad"),
    )
}

@Composable
private fun AcceptedBody(order: AvailableOrderDetail) {
    val context = LocalContext.current
    val neighbor = order.neighbor
    val acceptanceState = order.acceptanceState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = order.zone.ifBlank { "Trujillo" },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).testTag("zona_pedido"),
        )
        RtAcceptanceChip(
            state = acceptanceState,
            modifier = Modifier.testTag("chip_aceptacion"),
        )
    }
    AcceptanceAlert(state = acceptanceState)
    if (neighbor != null) {
        val shape = RoundedCornerShape(RtRadius.lg)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(RtStroke.divider, MaterialTheme.colorScheme.outlineVariant, shape)
                .padding(RtSpacing.s4)
                .testTag("tarjeta_vecino"),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
        ) {
            Text(
                text = "Vecino",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RtContactRow(
                name = neighbor.fullName,
                subtitle = neighbor.phone,
                phone = neighbor.phone,
                onCall = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${neighbor.phone}")))
                    }
                },
            )
            if (acceptanceState == RtAcceptanceState.Waiting) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(RtSize.iconCaption),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "La dirección se muestra solo si te elige.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    val shape = RoundedCornerShape(RtRadius.lg)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(RtStroke.divider, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(RtSpacing.s4)
            .testTag("resumen_pedido"),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Text(
            text = "Materiales",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
            Text(
                text = formatItems(order.items.size),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatWeightKg(order.totalWeightKg),
                style = RtTextStyles.bodyStrong,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        RtMetaRow(
            items = listOfNotNull(
                formatDistance(order.distanceKm)?.let { RtMeta(Icons.Rounded.NearMe, it) },
                RtMeta(Icons.Rounded.CalendarToday, formatPublishedDate(order.createdAt)),
            ),
        )
    }
}

@Composable
private fun AcceptanceAlert(state: RtAcceptanceState) {
    val (tone, title, message) = when (state) {
        RtAcceptanceState.Waiting -> Triple(
            RtInlineAlertTone.Success,
            "Aceptaste el pedido",
            "El vecino elegirá a un reciclador. Desliza hacia abajo en Aceptados para saber si te eligió.",
        )
        RtAcceptanceState.Chosen -> Triple(
            RtInlineAlertTone.Success,
            "El vecino te eligió",
            "Coordina con el vecino y ve a recoger el pedido.",
        )
        RtAcceptanceState.NotChosen -> Triple(
            RtInlineAlertTone.Info,
            "El vecino eligió a otro reciclador",
            "Este pedido ya no está disponible para ti.",
        )
        RtAcceptanceState.CancelledByNeighbor -> Triple(
            RtInlineAlertTone.Warning,
            "El vecino canceló el pedido",
            "Este pedido ya no se recogerá.",
        )
        RtAcceptanceState.Collected -> Triple(
            RtInlineAlertTone.Success,
            "Pedido recolectado",
            "Gracias por ayudar a reciclar en Trujillo.",
        )
    }
    RtInlineAlert(
        tone = tone,
        title = title,
        message = message,
        modifier = Modifier.testTag("alert_aceptado"),
    )
}

private fun orderMeta(order: AvailableOrderDetail): List<RtMeta> = listOfNotNull(
    formatDistance(order.distanceKm)?.let { RtMeta(Icons.Rounded.NearMe, it) },
    RtMeta(Icons.Rounded.CalendarToday, formatOrderDate(order.createdAt)),
    RtMeta(Icons.Rounded.Groups, formatAcceptanceCount(order.acceptanceCount)),
)

private fun formatItems(count: Int): String = if (count == 1) "1 ítem" else "$count ítems"

private fun formatAcceptanceCount(count: Int): String = when (count) {
    0 -> "Sin aceptaciones"
    1 -> "1 reciclador aceptó"
    else -> "$count recicladores aceptaron"
}

private val PreviewItems = listOf(
    OrderItem("Botellas de plástico", 2.5),
    OrderItem("Cartón", 10.0),
    OrderItem("Latas", 2.0),
)

private fun previewDetail(
    acceptanceStatus: AcceptanceStatus? = null,
    neighbor: OrderContact? = null,
) = AvailableOrderDetail(
    id = "1",
    status = OrderStatus.Pending,
    zone = "Víctor Larco Herrera",
    items = PreviewItems,
    totalWeightKg = 14.5,
    distanceKm = 1.2,
    createdAt = Instant.now(),
    acceptanceCount = 2,
    acceptanceStatus = acceptanceStatus,
    photos = emptyList(),
    neighbor = neighbor,
)

@Composable
private fun AvailableOrderDetailPreviewHost(state: AvailableOrderDetailUiState) {
    RtTheme {
        AvailableOrderDetailContent(
            state = state,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onAccept = {},
        )
    }
}

@Preview(name = "P29 Detalle antes de aceptar", showBackground = true, heightDp = 760)
@Composable
private fun AvailableOrderDetailPreview() {
    AvailableOrderDetailPreviewHost(AvailableOrderDetailUiState(loading = false, order = previewDetail()))
}

@Preview(name = "P31 Pedido aceptado", showBackground = true, heightDp = 760)
@Composable
private fun AvailableOrderDetailAcceptedPreview() {
    AvailableOrderDetailPreviewHost(
        AvailableOrderDetailUiState(
            loading = false,
            order = previewDetail(
                acceptanceStatus = AcceptanceStatus.Accepted,
                neighbor = OrderContact("Ana Torres", "948120337"),
            ),
        ),
    )
}

@Preview(name = "Cargando", showBackground = true, heightDp = 400)
@Composable
private fun AvailableOrderDetailLoadingPreview() {
    AvailableOrderDetailPreviewHost(AvailableOrderDetailUiState(loading = true))
}

@Preview(name = "Error al cargar", showBackground = true, heightDp = 400)
@Composable
private fun AvailableOrderDetailErrorPreview() {
    AvailableOrderDetailPreviewHost(
        AvailableOrderDetailUiState(
            loading = false,
            loadError = "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
        ),
    )
}
