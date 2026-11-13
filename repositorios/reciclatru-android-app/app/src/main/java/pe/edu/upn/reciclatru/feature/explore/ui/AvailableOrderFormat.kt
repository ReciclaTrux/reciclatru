package pe.edu.upn.reciclatru.feature.explore.ui

import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt

fun formatAvailableWeight(weightKg: Double): String {
    val tenths = (weightKg * 10).roundToInt()
    return if (tenths % 10 == 0) "${tenths / 10} kg" else "${tenths / 10}.${tenths % 10} kg"
}

fun formatAvailableItems(count: Int): String = if (count == 1) "1 ítem" else "$count ítems"

fun formatDistance(distanceKm: Double?): String? {
    if (distanceKm == null) return null
    val tenths = (distanceKm * 10).roundToInt()
    return if (tenths % 10 == 0) "a ${tenths / 10} km" else "a ${tenths / 10}.${tenths % 10} km"
}

fun formatTimeAgo(createdAt: Instant?, now: Instant = Instant.now()): String? {
    if (createdAt == null) return null
    val minutes = Duration.between(createdAt, now).toMinutes().coerceAtLeast(0)
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> "hace $minutes min"
        hours < 24 -> "hace $hours h"
        days < 2 -> "ayer"
        else -> "hace $days días"
    }
}
