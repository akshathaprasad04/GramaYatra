package com.gramayatri.app.model

data class StopEta(
    val stopName: String = "",
    val etaMillis: Long = 0L,
    val isCurrentStop: Boolean = false
) {
    fun etaFormatted(): String {
        if (etaMillis <= 0L) return "No data"
        val diff = etaMillis - System.currentTimeMillis()
        if (diff <= 0) return "Arriving now"
        val minutes = diff / 60000
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 -> "${hours}h ${mins}m"
            minutes == 0L -> "< 1 min"
            else -> "${minutes} min"
        }
    }
}
