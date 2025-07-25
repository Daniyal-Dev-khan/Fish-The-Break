package com.cp.fishthebreak.viewModels.profile.locations

data class LocationUIState(
    var name: String = "",
    var lat: String = "",
    var lang: String = "",
    var latFormat: String = "",
    var langFormat: String = "",
    var date: String = "",
    var time: String = "",
    var weight: String = "",
    var length: String = "",
    var lengthInches: String = "",
    var description: String = "",
    var image: String = "",
    var radioButton: Int = 0,
    var trollingId: Long? = null,
    var pointId: Int? = null,

    var nameError: Boolean = true,
    var latError: Boolean = true,
    var langError: Boolean = true,
    var dateError: Boolean = true,
    var timeError: Boolean = true,
    var weightError: Boolean = true,
    var lengthError: Boolean = true,
    var lengthInchesError: Boolean = true,
    var descriptionError: Boolean = true,
    var radioButtonError: Boolean = true,
)
