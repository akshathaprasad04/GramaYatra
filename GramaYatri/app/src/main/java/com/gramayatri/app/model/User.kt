package com.gramayatri.app.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val village: String = "",
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
