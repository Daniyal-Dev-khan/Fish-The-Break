package com.cp.fishthebreak.models.vessel

data class GetVesselModel(
    val `data`: List<VesselData>,
    val last_page: Int,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)