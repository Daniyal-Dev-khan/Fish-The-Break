package com.cp.fishthebreak.viewModels.auth.forget

data class ResetPasswordUIState(
    var password  :String = "",
    var confirmPassword  :String = "",
    var email  :String = "",
    var otp  :String = "",

    var passwordError :Boolean = true,
    var confirmPasswordError :Boolean = true,
    var emailError :Boolean = false,
    var otpError :Boolean = false,
)
