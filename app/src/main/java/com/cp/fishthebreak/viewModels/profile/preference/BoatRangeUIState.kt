package com.cp.fishthebreak.viewModels.profile.preference

data class BoatRangeUIState(
    var range  : Int = 0,
    var boatToggle :Boolean = false,

    var rangeError :Boolean = true,
    var boatToggleError :Boolean = false,
)
