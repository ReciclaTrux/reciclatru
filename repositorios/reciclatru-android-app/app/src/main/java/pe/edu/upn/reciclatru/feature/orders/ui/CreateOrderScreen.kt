package pe.edu.upn.reciclatru.feature.orders.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtConfirmDialog
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
import java.io.File

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

    val context = LocalContext.current
    var sourceSheetVisible by rememberSaveable { mutableStateOf(false) }
    var cameraPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onPhotoAdded(it.toString()) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) cameraPhoto?.let(viewModel::onPhotoAdded)
    }

    CreateOrderContent(
        state = state,
        onBack = onBack,
        onAddPhoto = { sourceSheetVisible = true },
        onRemovePhoto = viewModel::onPhotoRemoved,
        onDescriptionChange = viewModel::onDescriptionChange,
        onWeightChange = viewModel::onWeightChange,
        onRemoveItem = viewModel::onRemoveItem,
        onAddItem = viewModel::onAddItem,
        onPublish = viewModel::onPublish,
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

private fun Context.newCameraPhotoUri(): Uri {
    val directory = File(cacheDir, "photos").apply { mkdirs() }
    val file = File(directory, "foto_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoSourceSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = RtSpacing.s6).padding(bottom = RtSpacing.s5),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
        ) {
            Text(
                text = "Agregar foto",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = RtSpacing.s5),
            )
            RtButton(
                text = "Tomar foto",
                onClick = onCamera,
                variant = RtButtonVariant.Secondary,
                icon = Icons.Rounded.PhotoCamera,
                fullWidth = true,
                modifier = Modifier.testTag("btn_tomar_foto"),
            )
            RtButton(
                text = "Elegir de la galería",
                onClick = onGallery,
                variant = RtButtonVariant.Secondary,
                icon = Icons.Rounded.PhotoLibrary,
                fullWidth = true,
                modifier = Modifier.testTag("btn_elegir_galeria"),
            )
        }
    }
}

@Composable
private fun CreateOrderContent(
    state: CreateOrderUiState,
    onBack: () -> Unit,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
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
                        text = when {
                            state.uploadingPhotos -> "Subiendo fotos…"
                            state.publishing -> "Publicando…"
                            else -> "Publicar pedido"
                        },
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
            RtPhotoPicker(
                photos = state.photos,
                onAdd = onAddPhoto,
                onRemove = onRemovePhoto,
                enabled = !state.publishing,
                modifier = Modifier.testTag("selector_fotos_pedido"),
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
            onAddPhoto = {},
            onRemovePhoto = {},
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
            onAddPhoto = {},
            onRemovePhoto = {},
            onDescriptionChange = { _, _ -> },
            onWeightChange = { _, _ -> },
            onRemoveItem = {},
            onAddItem = {},
            onPublish = {},
        )
    }
}
