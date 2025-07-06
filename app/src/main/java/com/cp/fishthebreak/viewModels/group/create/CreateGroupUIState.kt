package com.cp.fishthebreak.viewModels.group.create

import com.cp.fishthebreak.models.auth.User

data class CreateGroupUIState(
    var name  :String = "",
    var image  :String = "",
    var users  :List<User> = ArrayList(),

    var nameError :Boolean = true,
    var usersError :Boolean = true

)
