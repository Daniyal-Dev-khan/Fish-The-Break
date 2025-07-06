package com.cp.fishthebreak.models.group

import com.cp.fishthebreak.adapters.base.ListAdapterItem
import java.io.Serializable

data class LeaveReasonModel(
    val `data`: List<LeaveReasonData>,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class LeaveReasonData(
    val reason: String,
    var isSelected: Boolean = false
): ListAdapterItem, Serializable {
    override val id: Int
        get() = 0
}