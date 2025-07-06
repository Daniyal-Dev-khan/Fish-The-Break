package com.cp.fishthebreak.models.group

data class CreateGroupModel(
    val `data`: CreateGroupData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class CreateGroupData(
    val created_group: Boolean
)