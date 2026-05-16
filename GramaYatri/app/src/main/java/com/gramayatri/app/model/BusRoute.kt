package com.gramayatri.app.model

data class BusRoute(
    val routeId: String = "",
    val routeName: String = "",
    val fromVillage: String = "",
    val toVillage: String = "",
    val stops: List<String> = emptyList(),
    val departureTime: String = "",
    val isActive: Boolean = true
)
