package pe.edu.upn.reciclatru.ui.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Formas del sistema: `radius-sm` (miniaturas), `radius-md` (botones, campos,
 * alertas), `radius-lg` (tarjetas, diálogos) y `radius-full` (etiquetas, avatar,
 * botón de ícono, buscador).
 */
val RtShapes = Shapes(
    extraSmall = RoundedCornerShape(RtRadius.sm),
    small = RoundedCornerShape(RtRadius.sm),
    medium = RoundedCornerShape(RtRadius.md),
    large = RoundedCornerShape(RtRadius.lg),
    extraLarge = RoundedCornerShape(RtRadius.full),
)
