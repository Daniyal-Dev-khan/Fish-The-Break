package com.cp.fishthebreak.models.group

data class RequestAcceptModel(
    val `data`: RequestAcceptData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class RequestAcceptData(
    val accepted: Boolean
)