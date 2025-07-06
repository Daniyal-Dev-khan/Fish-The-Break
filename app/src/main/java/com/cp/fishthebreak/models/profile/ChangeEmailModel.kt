package com.cp.fishthebreak.models.profile

data class ChangeEmailModel(
    val `data`: Boolean,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)