package com.cp.fishthebreak.viewModels.profile.vessel

sealed class VesselUIEvent{

    data class NameChanged(val name:String): VesselUIEvent()
    data class MakeChanged(val make:String): VesselUIEvent()
    data class ModelChanged(val model:String): VesselUIEvent()
    data class YearChanged(val year:String): VesselUIEvent()
    data class BoatRangeChanged(val boatRange:String): VesselUIEvent()
    data class BoatRangeNMChanged(val boatRangeNM:String): VesselUIEvent()
    data class DockLocationChanged(val dockLocation:String): VesselUIEvent()

    object SaveButtonClicked : VesselUIEvent()
}
