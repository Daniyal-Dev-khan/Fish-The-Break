package com.cp.fishthebreak.viewModels.profile.edit

data class EditProfileUIState(
    var firstName  :String = "",
    var lastName  :String = "",
    var userName  :String = "",
    var mobile  :String = "",
    var userImage  :String = "",

    var firstNameError :Boolean = true,
    var lastNameError :Boolean = true,
    var userNameError :Boolean = true,
    var mobileError :Boolean = true,
    var userImageError :Boolean = true,
)
