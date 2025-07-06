package com.cp.fishthebreak.viewModels.profile.email
sealed class ChangeEmailUIEvent{

    data class EmailChanged(val email:String): ChangeEmailUIEvent()
    data class Code1Changed(val code:String): ChangeEmailUIEvent()
    data class Code2Changed(val code: String) : ChangeEmailUIEvent()
    data class Code3Changed(val code: String) : ChangeEmailUIEvent()
    data class Code4Changed(val code: String) : ChangeEmailUIEvent()

    object SaveButtonClicked : ChangeEmailUIEvent()
    object VerifyOtpButtonClicked : ChangeEmailUIEvent()
}
