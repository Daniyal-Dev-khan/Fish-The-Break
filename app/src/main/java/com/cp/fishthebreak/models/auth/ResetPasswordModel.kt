package com.cp.fishthebreak.models.auth

data class ResetPasswordModel(
    val `data`: User?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)