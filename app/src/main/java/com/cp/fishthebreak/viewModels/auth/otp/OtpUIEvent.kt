package com.cp.fishthebreak.viewModels.auth.otp

sealed class OtpUIEvent{

    data class EmailChanged(val email:String): OtpUIEvent()
    data class Code1Changed(val code:String): OtpUIEvent()
    data class Code2Changed(val code: String) : OtpUIEvent()
    data class Code3Changed(val code: String) : OtpUIEvent()
    data class Code4Changed(val code: String) : OtpUIEvent()

    object NextButtonClicked : OtpUIEvent()
    object onNextPasswordResetEvent : OtpUIEvent()
    object onResendEvent : OtpUIEvent()
}
