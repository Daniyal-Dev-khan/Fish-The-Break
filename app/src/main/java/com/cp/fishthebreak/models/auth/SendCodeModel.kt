package com.cp.fishthebreak.models.auth

data class SendCodeModel(
    val `data`: Boolean,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)