package com.gramayatri.app.model

data class BusPing(
    val pingId: String = "",
    val routeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val currentStop: String = "",
    val pingType: String = "",   // "ARRIVED", "PASSED", "ON_BUS"
    val timestamp: Long = 0L,
    val etaMap: Map<String, Long> = emptyMap()  // stopName -> ETA millis
)
