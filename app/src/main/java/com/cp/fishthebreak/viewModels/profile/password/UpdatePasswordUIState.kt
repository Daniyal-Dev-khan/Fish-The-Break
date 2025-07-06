package com.cp.fishthebreak.viewModels.profile.password

data class UpdatePasswordUIState(
    var currentPassword  :String = "",
    var password  :String = "",
    var confirmPassword  :String = "",

    var currentPasswordError :Boolean = true,
    var passwordError :Boolean = true,
    var confirmPasswordError :Boolean = true,
)
