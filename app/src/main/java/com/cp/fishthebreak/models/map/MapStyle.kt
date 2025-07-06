package com.cp.fishthebreak.models.map

import com.cp.fishthebreak.adapters.base.ListAdapterItem
import java.io.Serializable

data class MapStyle(
    override val id: Int = 0,
    val name: String,
    val type: String,
    val image: Int,
    var isSelected: Boolean = false
): ListAdapterItem, Serializable