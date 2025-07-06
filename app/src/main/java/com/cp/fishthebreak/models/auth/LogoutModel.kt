package com.cp.fishthebreak.models.auth

data class LogoutModel(
    val `data`: Any?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)