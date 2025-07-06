package com.cp.fishthebreak.models.profile

import com.cp.fishthebreak.adapters.base.ListAdapterItem
import java.io.Serializable

data class AllResourcesModel(
    val `data`: AllResourcesData,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class AllResourcesData(
    val featured_resource: ResourceDetails?,
    val resources: List<ResourceData>
)

data class ResourceData(
    override val id: Int,
    val resources: List<ResourceDetails>,
    val type_name: String
): ListAdapterItem, Serializable

data class ResourceDetails(
    val base_url: String,
    val base_url_webpage: String,
    val category: String,
    val description: String,
    val file_name: String,
    val file_type: String,
    override val id: Int,
    val resource_type_id: Int,
    val resource_type_name: String,
    val short_description: String,
    val slug: String,
    val title: String
): ListAdapterItem, Serializable