package com.cp.fishthebreak.viewModels.routes

import com.cp.fishthebreak.models.routes.MeasureDistanceModel

data class RouteUIState(
    var routeId: Int? = null,
    var name: String = "",
    var description: String = "",
    var image: String = "",
    var locations: ArrayList<MeasureDistanceModel> = ArrayList(),

    var nameError: Boolean = true,
    var descriptionError: Boolean = true,
)
