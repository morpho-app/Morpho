package com.morpho.butterfly.auth


data class AuthTokens(
    val auth: String,
    val refresh: String,
)
