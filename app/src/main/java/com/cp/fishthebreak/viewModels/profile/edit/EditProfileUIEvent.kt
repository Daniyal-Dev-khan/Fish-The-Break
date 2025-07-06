package com.cp.fishthebreak.viewModels.profile.edit

sealed class EditProfileUIEvent{

    data class FirstNameChanged(val firstName:String): EditProfileUIEvent()
    data class LastNameChanged(val lastName:String): EditProfileUIEvent()
    data class UserNameChanged(val username:String): EditProfileUIEvent()
    data class MobileChanged(val mobile:String): EditProfileUIEvent()
    data class UserImageChanged(val userImage:String): EditProfileUIEvent()

    object SaveButtonClicked : EditProfileUIEvent()
}
