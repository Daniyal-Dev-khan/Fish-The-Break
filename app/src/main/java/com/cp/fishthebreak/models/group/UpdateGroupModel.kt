package com.cp.fishthebreak.models.group

data class UpdateGroupModel(
    val `data`: UpdateGroupData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class UpdateGroupData(
    val updated: Boolean
)