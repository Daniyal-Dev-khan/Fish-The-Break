package com.cp.fishthebreak.viewModels.profile.password

sealed class UpdatePasswordUIEvent{

    data class CurrentPasswordChanged(val password:String): UpdatePasswordUIEvent()
    data class PasswordChanged(val password:String): UpdatePasswordUIEvent()
    data class ConfirmPasswordChanged(val password:String): UpdatePasswordUIEvent()

    object SaveButtonClicked : UpdatePasswordUIEvent()
}
