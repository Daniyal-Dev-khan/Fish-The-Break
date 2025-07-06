package com.cp.fishthebreak.models.points

data class GetFishLogsModel(
    val `data`: ArrayList<SaveFishLogData>,
    val last_page: Int?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)