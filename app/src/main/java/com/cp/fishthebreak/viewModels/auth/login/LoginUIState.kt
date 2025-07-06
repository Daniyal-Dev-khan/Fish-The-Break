package com.cp.fishthebreak.viewModels.auth.login

data class LoginUIState(
    var email  :String = "",
    var password  :String = "",
    var fcmToken  :String = "",
    var rememberMe: Boolean = false,

    var emailError :Boolean = true,
    var passwordError : Boolean = true

)
