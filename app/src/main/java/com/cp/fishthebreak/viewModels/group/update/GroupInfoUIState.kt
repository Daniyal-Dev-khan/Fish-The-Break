package com.cp.fishthebreak.viewModels.group.update

data class GroupInfoUIState(
    var name  :String = "",
    var image  :String = "",
    var groupId :Int? = null,

    var nameError :Boolean = true,
    var usersError :Boolean = true

)
