package com.cp.fishthebreak.models.group

data class ShareInGroupModel(
    val `data`: ShareInGroupData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class ShareInGroupData(
    val sent: Boolean
)