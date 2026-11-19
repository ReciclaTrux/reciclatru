package pe.edu.upn.reciclatru.view.orders

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.model.orders.OrderPhoto
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtConfirmDialog
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtPhotoPicker
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtActionBar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtItemListEditor
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtItemListEntry
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.viewmodel.orders.EditOrderUiState
import pe.edu.upn.reciclatru.viewmodel.orders.EditOrderViewModel
import pe.edu.upn.reciclatru.viewmodel.orders.OrderItemInput

/**
 * Edición de un pedido (HU-07). Mismos campos y validaciones que al publicar; la ubicación
 * no se edita.
 *
 * @param onFinished terminó el guardado o el pedido ya no era editable; recibe el mensaje que
 *   el detalle muestra al volver.
 */
@Composable
fun EditOrderScreen(
    onBack: () -> Unit,
    onFinished: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditOrderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished(state.finishMessage)
    }
    val requestBack: () -> Unit = {
        if (!viewModel.onBackRequested()) onBack()
    }
    BackHandler(onBack = requestBack)

    val context = LocalContext.current
    var sourceSheetVisible by rememberSaveable { mutableStateOf(false) }
    var cameraPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onPhotoAdded(it.toString()) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) cameraPhoto?.let(viewModel::onPhotoAdded)
    }

    EditOrderContent(
        state = state,
        onBack = requestBack,
        onRetry = viewModel::onRetry,
        onAddPhoto = { sourceSheetVisible = true },
        onRemovePhoto = viewModel::onPhotoRemoved,
        onDescriptionChange = viewModel::onDescriptionChange,
        onWeightChange = viewModel::onWeightChange,
        onRemoveItem = viewModel::onRemoveItem,
        onAddItem = viewModel::onAddItem,
        onSave = viewModel::onSave,
        modifier = modifier,
    )

    if (sourceSheetVisible) {
        PhotoSourceSheet(
            onDismiss = { sourceSheetVisible = false },
            onCamera = {
                sourceSheetVisible = false
                runCatching {
                    val uri = context.newCameraPhotoUri()
                    cameraPhoto = uri.toString()
                    cameraLauncher.launch(uri)
                }
            },
            onGallery = {
                sourceSheetVisible = false
                runCatching {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                }
            },
        )
    }

    if (state.discardDialogVisible) {
        RtConfirmDialog(
            title = "¿Descartar los cambios?",
            message = "Saldrás sin guardar lo que cambiaste en este pedido.",
            confirmLabel = "Descartar",
            onConfirm = {
                viewModel.onDiscardDismissed()
                onBack()
            },
            onDismiss = viewModel::onDiscardDismissed,
            dismissLabel = "Seguir editando",
            destructive = true,
            modifier = Modifier.testTag("dialog_descartar_cambios"),
        )
    }
}

@Composable
private fun EditOrderContent(
    state: EditOrderUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onDescriptionChange: (Long, String) -> Unit,
    onWeightChange: (Long, String) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onAddItem: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RtTopAppBar(title = "Editar pedido", onBack = onBack) },
        bottomBar = {
            if (!state.loading && state.loadError == null) {
                RtActionBar(
                    primary = {
                        RtButton(
                            text = when {
                                state.uploadingPhotos -> "Subiendo fotos…"
                                state.saving -> "Guardando…"
                                else -> "Guardar cambios"
                            },
                            onClick = onSave,
                            loading = state.saving,
                            fullWidth = true,
                            modifier = Modifier.testTag("btn_guardar_cambios"),
                        )
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
            state.loadError != null -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                RtErrorState(
                    title = "No pudimos cargar el pedido",
                    message = state.loadError,
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
                state.saveError?.let { message ->
                    RtInlineAlert(
                        tone = RtInlineAlertTone.Error,
                        message = message,
                        modifier = Modifier.testTag("alert_guardar_error"),
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
                RtPhotoPicker(
                    photos = state.keptPhotos.map { it.url } + state.newPhotos,
                    onAdd = onAddPhoto,
                    onRemove = onRemovePhoto,
                    enabled = !state.saving,
                    modifier = Modifier.testTag("selector_fotos_pedido"),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("text_ubicacion_no_editable"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Place,
                        contentDescription = null,
                        modifier = Modifier.size(RtSize.iconCaption),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "La ubicación no se edita. Si cambió, cancela este pedido y publica uno nuevo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditOrderPreviewHost(state: EditOrderUiState) {
    RtTheme {
        EditOrderContent(
            state = state,
            onBack = {},
            onRetry = {},
            onAddPhoto = {},
            onRemovePhoto = {},
            onDescriptionChange = { _, _ -> },
            onWeightChange = { _, _ -> },
            onRemoveItem = {},
            onAddItem = {},
            onSave = {},
        )
    }
}

@Preview(name = "P16 Editar pedido", showBackground = true, heightDp = 760)
@Composable
private fun EditOrderPreview() {
    EditOrderPreviewHost(
        EditOrderUiState(
            loading = false,
            items = listOf(
                OrderItemInput(id = 0, description = "Periódicos y revistas", weight = "5"),
                OrderItemInput(
                    id = 1,
                    description = "",
                    weight = "600",
                    error = "Escribe el material y un peso mayor a 0 y hasta 500 kg",
                    descriptionInvalid = true,
                    weightInvalid = true,
                ),
            ),
            totalWeightLabel = "5 kg",
            keptPhotos = listOf(OrderPhoto("a", "a"), OrderPhoto("b", "b")),
            newPhotos = listOf("c"),
            dirty = true,
        ),
    )
}

@Preview(name = "Guardando", showBackground = true, heightDp = 760)
@Composable
private fun EditOrderSavingPreview() {
    EditOrderPreviewHost(
        EditOrderUiState(
            loading = false,
            items = listOf(OrderItemInput(id = 0, description = "Cartón", weight = "7.5")),
            totalWeightLabel = "7.5 kg",
            saving = true,
            dirty = true,
        ),
    )
}

@Preview(name = "Error al guardar", showBackground = true, heightDp = 760)
@Composable
private fun EditOrderSaveErrorPreview() {
    EditOrderPreviewHost(
        EditOrderUiState(
            loading = false,
            items = listOf(OrderItemInput(id = 0, description = "Cartón", weight = "7.5")),
            totalWeightLabel = "7.5 kg",
            saveError = "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
            dirty = true,
        ),
    )
}

@Preview(name = "Cargando", showBackground = true, heightDp = 400)
@Composable
private fun EditOrderLoadingPreview() {
    EditOrderPreviewHost(EditOrderUiState(loading = true))
}

@Preview(name = "Error al cargar", showBackground = true, heightDp = 400)
@Composable
private fun EditOrderLoadErrorPreview() {
    EditOrderPreviewHost(
        EditOrderUiState(
            loading = false,
            loadError = "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
        ),
    )
}
