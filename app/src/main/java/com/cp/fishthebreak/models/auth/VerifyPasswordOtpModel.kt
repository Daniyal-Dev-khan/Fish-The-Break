package com.cp.fishthebreak.models.auth

data class VerifyPasswordOtpModel(
    val `data`: VerifyPasswordOtpData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class VerifyPasswordOtpData(
    val verify: Boolean
)