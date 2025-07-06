package com.cp.fishthebreak.viewModels.routes

import com.cp.fishthebreak.models.routes.MeasureDistanceModel

sealed class RouteUIEvent{

    data class RouteIdChanged(val routeId:Int?): RouteUIEvent()
    data class NameChanged(val name:String): RouteUIEvent()
    data class DescriptionChanged(val description:String): RouteUIEvent()
    data class ImageChanged(val image:String): RouteUIEvent()
    data class LocationsChanged(val locations: ArrayList<MeasureDistanceModel>): RouteUIEvent()

    object SaveButtonClicked : RouteUIEvent()
}
