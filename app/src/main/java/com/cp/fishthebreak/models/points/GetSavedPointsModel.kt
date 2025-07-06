package com.cp.fishthebreak.models.points

data class GetSavedPointsModel(
    val `data`: ArrayList<SavePointsData>,
    val last_page: Int?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)
