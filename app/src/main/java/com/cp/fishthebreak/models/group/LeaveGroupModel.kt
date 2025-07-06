package com.cp.fishthebreak.models.group

data class LeaveGroupModel(
    val `data`: LeaveGroupData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class LeaveGroupData(
    val leaved: Boolean
)