package com.cp.fishthebreak.models.routes

data class GetAllRouteModel(
    val `data`: ArrayList<SaveRouteData>,
    val last_page: Int?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)