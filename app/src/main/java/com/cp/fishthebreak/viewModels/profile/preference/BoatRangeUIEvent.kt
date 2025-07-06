package com.cp.fishthebreak.viewModels.profile.preference
sealed class BoatRangeUIEvent{

    data class RangeChanged(val range:Int): BoatRangeUIEvent()
    data class BoatToggleChanged(val toggle:Boolean): BoatRangeUIEvent()

    object SaveButtonClicked : BoatRangeUIEvent()
}
