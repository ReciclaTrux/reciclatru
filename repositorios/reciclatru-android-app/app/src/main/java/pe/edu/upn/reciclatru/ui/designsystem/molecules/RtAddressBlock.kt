package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Dirección, distrito y referencia del vecino, más "Cómo llegar" y "Copiar" (HU-15). Solo
 * lo ve el reciclador elegido mientras el pedido está Confirmado.
 *
 * @param address puede ser nula: se muestra "Sin dirección escrita: usa «Cómo llegar» y la
 *   referencia".
 * @param reference opcional; sin ella esa parte no aparece.
 * @param lat latitud del punto de recogida. No se dibuja: la pantalla arma la URI `geo:`.
 * @param lng longitud del punto de recogida. No se dibuja.
 */
@Composable
fun RtAddressBlock(
    address: String?,
    district: String,
    lat: Double,
    lng: Double,
    onDirections: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    reference: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        KeyValue(
            label = "Dirección",
            value = address?.takeIf { it.isNotBlank() }
                ?: "Sin dirección escrita: usa «Cómo llegar» y la referencia",
            extra = district,
        )
        if (!reference.isNullOrBlank()) {
            KeyValue(label = "Referencia", value = reference)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
            RtButton(
                text = "Cómo llegar",
                onClick = onDirections,
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Directions,
                fullWidth = true,
            )
            RtButton(
                text = "Copiar",
                onClick = onCopy,
                variant = RtButtonVariant.Secondary,
                icon = Icons.Rounded.ContentCopy,
            )
        }
    }
}

@Composable
private fun KeyValue(label: String, value: String, extra: String? = null) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurface,
        )
        if (extra != null) {
            Text(
                text = extra,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Con dirección y referencia", showBackground = true)
@Composable
private fun RtAddressBlockPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtAddressBlock(
                address = "Av. Larco 1250",
                district = "Víctor Larco Herrera",
                reference = "Dpto. 302, puerta negra, frente al parque",
                lat = -8.1342,
                lng = -79.0267,
                onDirections = {},
                onCopy = {},
                modifier = Modifier.padding(RtSpacing.s4),
            )
        }
    }
}

@Preview(name = "Sin dirección escrita ni referencia", showBackground = true)
@Composable
private fun RtAddressBlockEmptyPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtAddressBlock(
                address = null,
                district = "Trujillo",
                lat = -8.1119,
                lng = -79.0288,
                onDirections = {},
                onCopy = {},
                modifier = Modifier.padding(RtSpacing.s4),
            )
        }
    }
}
