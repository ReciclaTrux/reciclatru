package pe.edu.upn.reciclatru.ui.designsystem.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Anillo de foco del sistema: 2 px en `primary-pressed` con 2 px de separación
 * respecto del control.
 */
internal fun Modifier.rtFocusRing(visible: Boolean, shape: Shape, color: Color): Modifier {
    if (!visible) return this
    return drawWithContent {
        drawContent()
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutlineRing(
            outline = outline.expanded(RtStroke.focusOffset.toPx()),
            color = color,
            width = RtStroke.focus.toPx(),
        )
    }
}

private fun DrawScope.drawOutlineRing(outline: Outline, color: Color, width: Float) {
    when (outline) {
        is Outline.Rectangle -> drawRect(
            color = color,
            topLeft = outline.rect.topLeft,
            size = outline.rect.size,
            style = Stroke(width),
        )

        is Outline.Rounded -> drawRoundRect(
            color = color,
            topLeft = Offset(outline.roundRect.left, outline.roundRect.top),
            size = Size(
                width = outline.roundRect.right - outline.roundRect.left,
                height = outline.roundRect.bottom - outline.roundRect.top,
            ),
            cornerRadius = outline.roundRect.topLeftCornerRadius,
            style = Stroke(width),
        )

        is Outline.Generic -> drawPath(
            path = outline.path,
            color = color,
            style = Stroke(width),
        )
    }
}

private fun Outline.expanded(delta: Float): Outline = when (this) {
    is Outline.Rectangle -> Outline.Rectangle(
        Rect(
            left = rect.left - delta,
            top = rect.top - delta,
            right = rect.right + delta,
            bottom = rect.bottom + delta,
        ),
    )

    is Outline.Rounded -> Outline.Rounded(
        RoundRect(
            left = roundRect.left - delta,
            top = roundRect.top - delta,
            right = roundRect.right + delta,
            bottom = roundRect.bottom + delta,
            topLeftCornerRadius = roundRect.topLeftCornerRadius.expanded(delta),
            topRightCornerRadius = roundRect.topRightCornerRadius.expanded(delta),
            bottomRightCornerRadius = roundRect.bottomRightCornerRadius.expanded(delta),
            bottomLeftCornerRadius = roundRect.bottomLeftCornerRadius.expanded(delta),
        ),
    )

    is Outline.Generic -> this
}

private fun CornerRadius.expanded(delta: Float) = CornerRadius(x + delta, y + delta)
