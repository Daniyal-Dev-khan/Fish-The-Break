package com.cp.fishthebreak.models.map

import com.cp.fishthebreak.adapters.base.ListAdapterItem
import java.io.Serializable

data class GetAllLayerModel(
    val `data`: List<MapLayer>,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class MapLayer(
    val base_url: String,
    val description: String,
    override val id: Int,
    val image: String?,
    val layer_calling_name: String,
    val base_url_webpage: String,
    val layer_calling_url: String,
    val layer_name: String,
    val layer_type: String,
    val portal_item_id: String?,
    val self_hosted: Int,
    val short_description: String,
    var isSelected: Boolean = false,
    var opacity: Int? = null
) : ListAdapterItem, Serializable