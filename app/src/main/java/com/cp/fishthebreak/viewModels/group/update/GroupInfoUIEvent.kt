package com.cp.fishthebreak.viewModels.group.update

import com.cp.fishthebreak.models.auth.User

sealed class GroupInfoUIEvent{

    data class NameChanged(val name:String): GroupInfoUIEvent()
    data class ImageChanged(val image:String): GroupInfoUIEvent()
    data class GroupIdChanged(val id: Int): GroupInfoUIEvent()

    object SaveButtonClicked : GroupInfoUIEvent()
}
