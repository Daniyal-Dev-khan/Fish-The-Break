package com.cp.fishthebreak.viewModels.profile.locations

sealed class LocationsUIEvent{

    data class IdChanged(val id:Int): LocationsUIEvent()
    data class NameChanged(val name:String): LocationsUIEvent()
    data class LatChanged(val lat:String): LocationsUIEvent()
    data class LangChanged(val lang:String): LocationsUIEvent()
    data class LatFormatChanged(val lat:String): LocationsUIEvent()
    data class LangFormatChanged(val lang:String): LocationsUIEvent()
    data class DateChanged(val date:String): LocationsUIEvent()
    data class TimeChanged(val time:String): LocationsUIEvent()
    data class WeightChanged(val weight:String): LocationsUIEvent()
    data class LengthChanged(val length:String): LocationsUIEvent()
    data class LengthInchesChanged(val length:String): LocationsUIEvent()
    data class DescriptionChanged(val description:String): LocationsUIEvent()
    data class ImageChanged(val image:String): LocationsUIEvent()
    data class RadioButtonChanged(val id:Int): LocationsUIEvent()
    data class TrollingIdChanged(val id:Long): LocationsUIEvent()

    object SaveButtonClicked : LocationsUIEvent()
}
