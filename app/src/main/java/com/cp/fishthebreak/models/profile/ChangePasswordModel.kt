package com.cp.fishthebreak.models.profile

data class ChangePasswordModel(
    val `data`: ChangePasswordData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class ChangePasswordData(
    val change_password: Boolean
)