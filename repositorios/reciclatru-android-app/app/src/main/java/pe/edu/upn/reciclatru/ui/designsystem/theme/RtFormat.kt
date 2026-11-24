package pe.edu.upn.reciclatru.ui.designsystem.theme

import java.util.Locale

/** Formato de datos que el sistema muestra igual en varias pantallas. */
object RtFormat {
    /** Peso en kilogramos con punto decimal y un decimal como máximo: "14.5 kg", "6 kg". */
    fun kg(value: Double): String {
        val rounded = Math.round(value * 10) / 10.0
        val text = if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            String.format(Locale.US, "%.1f", rounded)
        }
        return "$text kg"
    }

    /** Distancia: "a 2.4 km" (un decimal) o "a 800 m" si es menor a 1 km. */
    fun distanceKm(value: Double): String =
        if (value < 1.0) {
            "a ${Math.round(value * 1000)} m"
        } else {
            "a ${String.format(Locale.US, "%.1f", value)} km"
        }

    /** Cantidad de ítems: "1 ítem", "3 ítems". */
    fun items(count: Int): String = if (count == 1) "1 ítem" else "$count ítems"

    /** Agrupa un celular de nueve dígitos: "987 654 321". Si no es un celular, lo deja igual. */
    fun phone(value: String): String {
        val digits = value.filter { it.isDigit() }
        val isPhone = digits.length == 9 && value.none { it.isLetter() }
        return if (isPhone) "${digits.take(3)} ${digits.substring(3, 6)} ${digits.substring(6)}" else value
    }
}
