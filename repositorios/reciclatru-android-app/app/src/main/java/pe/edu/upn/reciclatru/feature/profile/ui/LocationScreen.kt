package pe.edu.upn.reciclatru.feature.profile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbarHost
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtTextField
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtActionBar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtLatLng
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtMapPicker
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * HU-04. Dos pasos: elegir el punto en el mapa y confirmar dirección y distrito. Al
 * guardar, la API devuelve la ubicación vigente.
 *
 * @param onBack sale de la pantalla (o vuelve al mapa si está en el paso de dirección).
 * @param onDone se llama al terminar, después de guardar.
 */
@Composable
fun LocationScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Atrás dentro de la HU vuelve al mapa, no sale de la pantalla.
    BackHandler(enabled = state.step != LocationStep.Map) {
        viewModel.onBackToMap()
    }

    LocationContent(
        state = state,
        onBack = if (state.step == LocationStep.Map) onBack else viewModel::onBackToMap,
        onSearch = viewModel::onSearch,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSuggestionSelected = { label ->
            viewModel.onSuggestionSelected(label)
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar("Dirección actualizada en el mapa")
            }
        },
        onMapConfirmed = viewModel::onMapConfirmed,
        onAddressChange = viewModel::onAddressChange,
        onDistrictChange = viewModel::onDistrictChange,
        onSave = viewModel::onSave,
        onDone = onDone,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun LocationContent(
    state: LocationUiState,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onMapConfirmed: (latitude: Double, longitude: Double) -> Unit,
    onAddressChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onSave: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { RtSnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            RtTopAppBar(
            title = when (state.step) {
                LocationStep.Map -> "Punto de recojo"
                LocationStep.Address -> "Confirma la dirección"
                LocationStep.Saved -> "Punto de recojo"
            },
            onBack = onBack,
        )

        when (state.step) {
            LocationStep.Map -> MapStep(
                state = state,
                onSearch = onSearch,
                onSearchQueryChanged = onSearchQueryChanged,
                onSuggestionSelected = onSuggestionSelected,
                onMapConfirmed = onMapConfirmed,
                modifier = Modifier.weight(1f),
            )

            LocationStep.Address -> AddressStep(
                state = state,
                onAddressChange = onAddressChange,
                onDistrictChange = onDistrictChange,
                onSave = onSave,
                modifier = Modifier.weight(1f),
            )

            LocationStep.Saved -> SavedStep(
                state = state,
                onDone = onDone,
                modifier = Modifier.weight(1f),
            )
        }
        }
    }
}

@Composable
private fun MapStep(
    state: LocationUiState,
    onSearch: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onMapConfirmed: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(RtSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
    ) {
        if (!state.geocodingAvailable) {
            RtInlineAlert(
                tone = RtInlineAlertTone.Info,
                message = "Tu teléfono no puede buscar direcciones. Mueve el mapa a mano.",
                modifier = Modifier.testTag("alert_sin_geocoder"),
            )
        }
        // El mapa es stateless: `center` mueve la cámara cuando la búsqueda resuelve un
        // punto; el pin y el callback siguen viviendo en el organismo.
        RtMapPicker(
            initialCenter = RtLatLng(
                latitude = state.center.latitude,
                longitude = state.center.longitude,
            ),
            onConfirm = onMapConfirmed,
            center = RtLatLng(
                latitude = state.center.latitude,
                longitude = state.center.longitude,
            ),
            searchEnabled = state.geocodingAvailable,
            onSearch = onSearch,
            onSearchQueryChanged = onSearchQueryChanged,
            searchSuggestions = state.suggestions.map { suggestion -> suggestion.label },
            searchLoading = state.searching,
            onSuggestionSelected = onSuggestionSelected,
            searchError = state.searchError,
            modifier = Modifier.testTag("map_punto_recojo"),
        )
    }
}

@Composable
private fun AddressStep(
    state: LocationUiState,
    onAddressChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(RtSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
        ) {
            if (state.resolvingAddress) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RtSpinner()
                    Text(
                        text = "Buscando la dirección del pin.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            RtTextField(
                label = "Dirección",
                value = state.address,
                onValueChange = onAddressChange,
                placeholder = "Av. Larco 1250",
                helper = "Puedes corregir el texto; el punto del mapa no cambia",
                error = state.addressError,
                modifier = Modifier.testTag("input_direccion"),
            )
            RtTextField(
                label = "Distrito",
                value = state.district,
                onValueChange = onDistrictChange,
                placeholder = "Víctor Larco Herrera",
                error = state.districtError,
                modifier = Modifier.testTag("input_distrito"),
            )
            RtInlineAlert(
                tone = RtInlineAlertTone.Info,
                message = "Este punto se copiará a los pedidos que publiques desde ahora.",
                modifier = Modifier.testTag("alert_ubicacion_info"),
            )
            state.saveError?.let { message ->
                RtInlineAlert(
                    tone = RtInlineAlertTone.Error,
                    message = message,
                    modifier = Modifier.testTag("alert_error_ubicacion"),
                )
            }
        }
        RtActionBar(
            primary = {
                RtButton(
                    text = "Guardar ubicación",
                    onClick = onSave,
                    loading = state.saving,
                    fullWidth = true,
                    modifier = Modifier.testTag("btn_guardar_ubicacion"),
                )
            },
        )
    }
}

@Composable
private fun SavedStep(
    state: LocationUiState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(RtSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
        ) {
            RtInlineAlert(
                tone = RtInlineAlertTone.Success,
                title = "Ubicación guardada",
                message = "Guardamos tu punto de recojo.",
                modifier = Modifier.testTag("alert_ubicacion_guardada"),
            )
            state.savedLocation?.let { location ->
                Text(
                    text = "${location.address}, ${location.district}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        RtActionBar(
            primary = {
                RtButton(
                    text = "Listo",
                    onClick = onDone,
                    fullWidth = true,
                    modifier = Modifier.testTag("btn_listo_ubicacion"),
                )
            },
        )
    }
}

private val PreviewPoint = GeoPoint(latitude = -8.1116, longitude = -79.0287)

@Preview(name = "Paso 1: mapa", showBackground = true, heightDp = 900)
@Composable
private fun LocationMapPreview() {
    RtTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            LocationContent(
                state = LocationUiState(geocodingAvailable = true),
                onBack = {},
                onSearch = {},
                onSearchQueryChanged = {},
                onSuggestionSelected = {},
                onMapConfirmed = { _, _ -> },
                onAddressChange = {},
                onDistrictChange = {},
                onSave = {},
                onDone = {},
            )
        }
    }
}

@Preview(name = "Paso 2: dirección", showBackground = true, heightDp = 900)
@Composable
private fun LocationAddressPreview() {
    RtTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            LocationContent(
                state = LocationUiState(
                    step = LocationStep.Address,
                    center = PreviewPoint,
                    address = "Av. Larco 1250",
                    district = "Víctor Larco Herrera",
                ),
                onBack = {},
                onSearch = {},
                onSearchQueryChanged = {},
                onSuggestionSelected = {},
                onMapConfirmed = { _, _ -> },
                onAddressChange = {},
                onDistrictChange = {},
                onSave = {},
                onDone = {},
            )
        }
    }
}

@Preview(name = "Guardada", showBackground = true, heightDp = 640)
@Composable
private fun LocationSavedPreview() {
    RtTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            LocationContent(
                state = LocationUiState(
                    step = LocationStep.Saved,
                    savedLocation = PickupLocation(
                        point = PreviewPoint,
                        address = "Av. Larco 1250",
                        district = "Víctor Larco Herrera",
                    ),
                ),
                onBack = {},
                onSearch = {},
                onSearchQueryChanged = {},
                onSuggestionSelected = {},
                onMapConfirmed = { _, _ -> },
                onAddressChange = {},
                onDistrictChange = {},
                onSave = {},
                onDone = {},
            )
        }
    }
}
