package com.cp.fishthebreak.viewModels.auth.login

sealed class LoginUIEvent{

    data class EmailChanged(val email:String): LoginUIEvent()
    data class PasswordChanged(val password: String) : LoginUIEvent()
    data class FcmTokenChanged(val fcmToken: String) : LoginUIEvent()
    data class RememberMeChanged(val rememberMe: Boolean) : LoginUIEvent()

    object LoginButtonClicked : LoginUIEvent()
}
