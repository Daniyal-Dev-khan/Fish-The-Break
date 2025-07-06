package com.cp.fishthebreak.models.profile

data class ResourceListModel(
    val `data`: ResourceData,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)