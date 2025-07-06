package com.cp.fishthebreak.models.group

data class RequestRejectModel(
    val `data`: RequestRejectData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class RequestRejectData(
    val rejected: Boolean
)