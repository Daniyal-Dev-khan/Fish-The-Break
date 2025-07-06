package com.cp.fishthebreak.viewModels.map
sealed class OfflineMapUIEvent{

    data class NameChanged(val name:String): OfflineMapUIEvent()
    data class DescriptionChanged(val description:String): OfflineMapUIEvent()
    data class ImageChanged(val image: String) : OfflineMapUIEvent()
    data class PathChanged(val mapPath: String) : OfflineMapUIEvent()
    data class MapIdChanged(val id: Int?) : OfflineMapUIEvent()
    data class MapDateChanged(val date: Long?) : OfflineMapUIEvent()

    object SaveButtonClicked : OfflineMapUIEvent()
}
