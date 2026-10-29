package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.maps.UiSettings
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSearchField
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtElevation
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Punto geográfico del pedido; el mapa siempre parte de este centro. */
data class RtLatLng(val latitude: Double, val longitude: Double)

/** Estilo fijo del Release 1: `positron` de OpenFreeMap. Sin API key ni cuenta. */
private const val MapStyleUrl = "https://tiles.openfreemap.org/styles/positron"

/** Zoom inicial: una calle de Trujillo se reconoce sin perder el barrio. */
private const val InitialZoom = 16.0

/** Duración de la animación cuando la pantalla mueve la cámara con [RtMapPicker.center]. */
private const val CameraAnimationMillis = 500

/** Créditos de los datos del mapa, obligatorios con OpenStreetMap y OpenFreeMap. */
private const val MapAttribution = "© OpenStreetMap · OpenFreeMap"

/**
 * Mapa con pin fijo al centro, buscador arriba y botón Confirmar ubicación abajo
 * (HU-04). El pin no se mueve: el mapa se mueve debajo. Sin GPS ni permiso de ubicación.
 *
 * El mapa lo pinta MapLibre Native con el estilo `positron` de OpenFreeMap; las vistas
 * previas dibujan un relleno de calles porque no cargan el mapa real.
 *
 * @param initialCenter ubicación guardada o centro de Trujillo.
 * @param onConfirm entrega la latitud y la longitud del pin.
 * @param center centro controlado: si cambia (por ejemplo, al resolver una búsqueda), la
 *   cámara se anima hasta ahí. El pin sigue fijo en el centro.
 * @param searchEnabled muestra el buscador; la pantalla lo apaga si el teléfono no tiene
 *   el servicio de búsqueda de direcciones.
 * @param onSearch avisa el texto buscado al confirmar desde el teclado, para que la
 *   pantalla mueva el mapa; el estado del texto vive en este componente.
 * @param onSearchQueryChanged avisa cada cambio del texto para que la feature haga el
 *   debounce, consulte al geocoder y entregue [searchSuggestions]. El organismo no
 *   debounce ni conoce el geocoder.
 * @param searchSuggestions direcciones sugeridas por la feature; el buscador muestra hasta
 *   cuatro.
 * @param searchLoading muestra el spinner del buscador mientras la feature consulta.
 * @param onSuggestionSelected avisa cuál sugerencia se eligió; la feature resuelve y
 *   actualiza [center].
 * @param searchError error del buscador, por ejemplo "No encontramos esa dirección.
 *   Mueve el mapa a mano.".
 */
@Composable
fun RtMapPicker(
    initialCenter: RtLatLng,
    onConfirm: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier,
    center: RtLatLng? = null,
    searchEnabled: Boolean = true,
    onSearch: ((String) -> Unit)? = null,
    onSearchQueryChanged: ((String) -> Unit)? = null,
    searchSuggestions: List<String> = emptyList(),
    searchLoading: Boolean = false,
    onSuggestionSelected: ((String) -> Unit)? = null,
    searchError: String? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.lg)
    val sheetShape = RoundedCornerShape(
        topStart = RtRadius.lg,
        topEnd = RtRadius.lg,
        bottomStart = RtRadius.lg,
        bottomEnd = RtRadius.lg,
    )
    var query by rememberSaveable { mutableStateOf("") }
    // En modo vista previa (Android Studio Preview), evitamos instanciar RtMapHandle
    // o cargar clases de MapLibre (MapView) para no causar NoClassDefFoundError.
    val isPreview = LocalInspectionMode.current
    val mapHandle = if (isPreview) null else remember { RtMapHandle() }
    val focusManager = LocalFocusManager.current

    // `mapHandle.map` entra en las claves para animar cuando el mapa ya cargó.
    LaunchedEffect(center, mapHandle?.map) {
        if (isPreview) return@LaunchedEffect
        val map = mapHandle?.map ?: return@LaunchedEffect
        val target = center ?: return@LaunchedEffect
        map.animateCamera(
            CameraUpdateFactory.newLatLng(LatLng(target.latitude, target.longitude)),
            CameraAnimationMillis,
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (searchEnabled) {
                // El buscador vive fuera de la vista de plataforma del mapa; su lista
                // flota sobre el mapa sin empujarlo (ver el overlay de abajo).
                Spacer(modifier = Modifier.height(RtSize.inputHeight + RtSpacing.s3))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RtSize.mapHeight)
                    .clip(shape)
                    .background(colorScheme.surfaceVariant)
                    .border(RtStroke.control, colorScheme.outline, shape),
            ) {
                if (isPreview) {
                    MapPlaceholder(
                        streetColor = colorScheme.surface,
                        blockColor = colorScheme.secondaryContainer,
                    )
                } else {
                    mapHandle?.let { handle ->
                        RtMapView(
                            initialCenter = initialCenter,
                            handle = handle,
                            onMapTap = {
                                focusManager.clearFocus()
                                if (query.isNotEmpty()) {
                                    query = ""
                                    onSearchQueryChanged?.invoke("")
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "Punto de recojo",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = -RtSize.iconMd)
                        .size(RtSize.mapPin),
                    tint = colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(RtSpacing.s3))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = RtElevation.level3, shape = sheetShape, clip = false)
                    .clip(sheetShape)
                    .background(colorScheme.surface)
                    .padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                Text(
                    text = "Mueve el mapa hasta tu puerta",
                    style = RtTextStyles.bodyStrong,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = "El pin marca el punto donde el reciclador recogerá tu material. " +
                        "No usamos tu GPS.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                RtButton(
                    text = "Confirmar ubicación",
                    onClick = {
                        val selectedCenter = mapHandle?.centerOr(initialCenter) ?: initialCenter
                        onConfirm(selectedCenter.latitude, selectedCenter.longitude)
                    },
                    fullWidth = true,
                )
                Text(
                    text = MapAttribution,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
        if (searchEnabled) {
            RtSearchField(
                query = query,
                onQueryChange = { value ->
                    query = value
                    onSearchQueryChanged?.invoke(value)
                },
                onSearch = { onSearch?.invoke(query) },
                error = searchError,
                suggestions = searchSuggestions,
                suggestionsLoading = searchLoading,
                onSuggestionSelected = { suggestion ->
                    // El buscador es un atajo: se limpia y el pin manda.
                    query = ""
                    onSuggestionSelected?.invoke(suggestion)
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

/** Guarda el mapa ya cargado para leer su centro al confirmar, sin recomponer. */
private class RtMapHandle {
    var map: MapLibreMap? by mutableStateOf(null)

    fun centerOr(fallback: RtLatLng): RtLatLng {
        val target = map?.cameraPosition?.target ?: return fallback
        return RtLatLng(latitude = target.latitude, longitude = target.longitude)
    }
}

/**
 * Mapa real de MapLibre dentro de Compose. Sigue el ciclo de vida de la pantalla y deja
 * la atribución de datos a la hoja inferior del componente.
 */
@Composable
private fun RtMapView(
    initialCenter: RtLatLng,
    handle: RtMapHandle,
    onMapTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnMapTap by rememberUpdatedState(onMapTap)
    val mapView: MapView = remember(context) {
        MapLibre.getInstance(context)
        val view = MapView(context)
        view.onCreate(null)
        view
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        // El observador solo recibe eventos nuevos: si la pantalla ya está en primer
        // plano, el mapa arranca acá.
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView<MapView>(
        modifier = modifier,
        factory = {
            mapView.getMapAsync { map: MapLibreMap ->
                val settings: UiSettings = map.uiSettings
                settings.isRotateGesturesEnabled = false
                settings.isTiltGesturesEnabled = false
                settings.isCompassEnabled = false
                // Los créditos están en la hoja inferior, siempre visibles.
                settings.isAttributionEnabled = false

                map.setStyle(Style.Builder().fromUri(MapStyleUrl))
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(initialCenter.latitude, initialCenter.longitude))
                    .zoom(InitialZoom)
                    .build()
                // Tocar el mapa cierra el buscador y sus sugerencias, como en Flutter.
                map.addOnMapClickListener {
                    currentOnMapTap()
                    false
                }
                handle.map = map
            }
            mapView
        },
    )
}

/** Calles de relleno para las vistas previas: no se descarga ningún mapa. */
@Composable
private fun MapPlaceholder(streetColor: Color, blockColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val main = RtStroke.mapStreetMain.toPx()
        val secondary = RtStroke.mapStreetSecondary.toPx()
        drawLine(streetColor, Offset(0f, size.height * 0.24f), Offset(size.width, size.height * 0.19f), main)
        drawLine(streetColor, Offset(0f, size.height * 0.62f), Offset(size.width, size.height * 0.68f), main)
        drawLine(streetColor, Offset(size.width * 0.26f, 0f), Offset(size.width * 0.32f, size.height), main)
        drawLine(streetColor, Offset(size.width * 0.70f, 0f), Offset(size.width * 0.64f, size.height), main)
        drawLine(streetColor, Offset(0f, size.height * 0.45f), Offset(size.width, size.height * 0.44f), secondary)
        drawLine(streetColor, Offset(size.width * 0.48f, 0f), Offset(size.width * 0.49f, size.height), secondary)
        drawRect(
            color = blockColor,
            topLeft = Offset(size.width * 0.72f, size.height * 0.28f),
            size = Size(size.width * 0.22f, size.height * 0.16f),
        )
    }
}

@Preview(name = "Con buscador", showBackground = true, heightDp = 780)
@Composable
private fun RtMapPickerPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(RtSpacing.s4)) {
                RtMapPicker(
                    initialCenter = RtLatLng(latitude = -8.1116, longitude = -79.0287),
                    onConfirm = { _, _ -> },
                )
            }
        }
    }
}

@Preview(name = "Sin buscador y con error", showBackground = true, heightDp = 1450)
@Composable
private fun RtMapPickerStatesPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
            ) {
                RtMapPicker(
                    initialCenter = RtLatLng(latitude = -8.1116, longitude = -79.0287),
                    onConfirm = { _, _ -> },
                    searchEnabled = false,
                )
                RtMapPicker(
                    initialCenter = RtLatLng(latitude = -8.1116, longitude = -79.0287),
                    onConfirm = { _, _ -> },
                    searchError = "No encontramos esa dirección. Mueve el mapa a mano.",
                )
            }
        }
    }
}
