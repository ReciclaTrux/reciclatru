package pe.edu.upn.reciclatru.feature.orders.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtConfirmDialog
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtActionBar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtItemListEditor
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtItemListEntry
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

@Composable
fun CreateOrderScreen(
    onBack: () -> Unit,
    onPublished: (String) -> Unit,
    onChoosePickupPoint: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateOrderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.published) {
        if (state.published) onPublished(state.publishedOrderId.orEmpty())
    }
    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }

    CreateOrderContent(
        state = state,
        onBack = onBack,
        onDescriptionChange = viewModel::onDescriptionChange,
        onWeightChange = viewModel::onWeightChange,
        onRemoveItem = viewModel::onRemoveItem,
        onAddItem = viewModel::onAddItem,
        onPublish = viewModel::onPublish,
        modifier = modifier,
    )

    if (state.profileIncomplete) {
        RtConfirmDialog(
            title = "Completa tu perfil para publicar",
            message = "Falta marcar tu punto de recojo. Sin él, el reciclador no sabrá a dónde ir.",
            confirmLabel = "Marcar ubicación",
            onConfirm = {
                viewModel.onProfileDialogDismissed()
                onChoosePickupPoint()
            },
            onDismiss = {
                viewModel.onProfileDialogDismissed()
                onBack()
            },
            dismissLabel = "Ahora no",
            modifier = Modifier.testTag("dialog_perfil_incompleto"),
        )
    }
}

@Composable
private fun CreateOrderContent(
    state: CreateOrderUiState,
    onBack: () -> Unit,
    onDescriptionChange: (Long, String) -> Unit,
    onWeightChange: (Long, String) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onAddItem: () -> Unit,
    onPublish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RtTopAppBar(title = "Publicar pedido", onBack = onBack) },
        bottomBar = {
            RtActionBar(
                primary = {
                    RtButton(
                        text = if (state.publishing) "Publicando…" else "Publicar pedido",
                        onClick = onPublish,
                        loading = state.publishing,
                        fullWidth = true,
                        modifier = Modifier.testTag("btn_publicar_pedido"),
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(RtSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
        ) {
            state.publishError?.let { message ->
                RtInlineAlert(
                    tone = RtInlineAlertTone.Error,
                    message = message,
                    modifier = Modifier.testTag("alert_publicar_error"),
                )
            }
            RtItemListEditor(
                items = state.items.map { item ->
                    RtItemListEntry(
                        description = item.description,
                        weight = item.weight,
                        error = item.error,
                        descriptionInvalid = item.descriptionInvalid,
                        weightInvalid = item.weightInvalid,
                    )
                },
                totalWeight = state.totalWeightLabel,
                onDescriptionChange = { index, value -> onDescriptionChange(state.items[index].id, value) },
                onWeightChange = { index, value -> onWeightChange(state.items[index].id, value) },
                onRemove = { index -> onRemoveItem(state.items[index].id) },
                onAdd = onAddItem,
                error = state.itemsError,
                modifier = Modifier.testTag("editor_items_pedido"),
            )
            state.pickupAddress?.let { address ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("text_punto_recojo"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Place,
                        contentDescription = null,
                        modifier = Modifier.size(RtSize.iconCaption),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Se recogerá en $address",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(name = "Publicar pedido", showBackground = true, heightDp = 760)
@Composable
private fun CreateOrderPreview() {
    RtTheme {
        CreateOrderContent(
            state = CreateOrderUiState(
                items = listOf(
                    OrderItemInput(id = 0, description = "Botellas de plástico", weight = "2.5"),
                    OrderItemInput(id = 1, description = "Cartón", weight = "12"),
                ),
                totalWeightLabel = "14.5 kg",
                pickupAddress = "Av. Larco 1250, Víctor Larco Herrera",
            ),
            onBack = {},
            onDescriptionChange = { _, _ -> },
            onWeightChange = { _, _ -> },
            onRemoveItem = {},
            onAddItem = {},
            onPublish = {},
        )
    }
}

@Preview(name = "Con errores", showBackground = true, heightDp = 760)
@Composable
private fun CreateOrderErrorPreview() {
    RtTheme {
        CreateOrderContent(
            state = CreateOrderUiState(
                items = listOf(
                    OrderItemInput(id = 0, description = "Periódicos y revistas", weight = "5"),
                    OrderItemInput(
                        id = 1,
                        weight = "600",
                        error = "Escribe el material y un peso mayor a 0 y hasta 500 kg",
                        descriptionInvalid = true,
                        weightInvalid = true,
                    ),
                ),
                totalWeightLabel = "5 kg",
                publishError = "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
            ),
            onBack = {},
            onDescriptionChange = { _, _ -> },
            onWeightChange = { _, _ -> },
            onRemoveItem = {},
            onAddItem = {},
            onPublish = {},
        )
    }
}
