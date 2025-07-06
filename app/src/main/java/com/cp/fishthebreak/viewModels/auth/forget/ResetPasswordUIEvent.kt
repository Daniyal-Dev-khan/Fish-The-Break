package com.cp.fishthebreak.viewModels.auth.forget

sealed class ResetPasswordUIEvent{

    data class EmailChanged(val email:String): ResetPasswordUIEvent()
    data class OtpChanged(val otp:String): ResetPasswordUIEvent()
    data class PasswordChanged(val password:String): ResetPasswordUIEvent()
    data class ConfirmPasswordChanged(val password:String): ResetPasswordUIEvent()

    object NextButtonClicked : ResetPasswordUIEvent()
}
