package com.cp.fishthebreak.viewModels.profile.vessel

data class VesselUIState(
    var name  :String = "",
    var make  :String = "",
    var model  :String = "",
    var year  :String = "",
    var boatRange  :String = "0",
    var boatRangeNM  :String = "0",
    var dockLocation  :String = "",

    var nameError :Boolean = true,
    var makeError :Boolean = true,
    var modelError :Boolean = true,
    var yearError :Boolean = true,
    var boatRangeError :Boolean = true,
    var dockLocationError :Boolean = true,
)
