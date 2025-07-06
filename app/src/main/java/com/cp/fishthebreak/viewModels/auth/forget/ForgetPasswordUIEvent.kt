package com.cp.fishthebreak.viewModels.auth.forget

sealed class ForgetPasswordUIEvent{

    data class EmailChanged(val email:String): ForgetPasswordUIEvent()

    object NextButtonClicked : ForgetPasswordUIEvent()
}
