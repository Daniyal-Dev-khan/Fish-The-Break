package com.cp.fishthebreak.models.profile

import com.cp.fishthebreak.adapters.base.ListAdapterItem
import java.io.Serializable

data class OfflineMapModel (
    override val id: Int = 0,
    val name: String,
    val type: String,
    val image: Int
): ListAdapterItem, Serializable