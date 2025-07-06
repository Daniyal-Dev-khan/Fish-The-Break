package com.cp.fishthebreak.viewModels.group.create

import com.cp.fishthebreak.models.auth.User

sealed class CreateGroupUIEvent{

    data class NameChanged(val name:String): CreateGroupUIEvent()
    data class ImageChanged(val image:String): CreateGroupUIEvent()
    data class UserListChanged(val users: List<User>): CreateGroupUIEvent()

    object SaveButtonClicked : CreateGroupUIEvent()
}
