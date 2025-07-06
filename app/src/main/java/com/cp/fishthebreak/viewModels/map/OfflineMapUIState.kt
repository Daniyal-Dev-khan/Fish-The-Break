package com.cp.fishthebreak.viewModels.map

data class OfflineMapUIState(
    var name: String = "",
    var description: String = "",
    var image: String = "",
    var mapPath: String = "",
    var mapId: Int? = null,
    var mapDate: Long? = null,

    var nameError: Boolean = true,
    var descriptionError: Boolean = true,
)
