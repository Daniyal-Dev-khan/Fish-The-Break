package com.cp.fishthebreak.models.auth

data class DeleteAccountModel(
    val `data`: DeleteAccountModelData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class DeleteAccountModelData(
    val account_delete: Boolean
)