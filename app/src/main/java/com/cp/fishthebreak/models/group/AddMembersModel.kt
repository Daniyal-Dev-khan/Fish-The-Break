package com.cp.fishthebreak.models.group

data class AddMembersModel(
    val `data`: AddMembersData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class AddMembersData(
    val added_group_member: Boolean
)