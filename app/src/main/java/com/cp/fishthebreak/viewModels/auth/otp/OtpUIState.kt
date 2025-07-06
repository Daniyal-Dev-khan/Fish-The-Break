package com.cp.fishthebreak.viewModels.auth.otp

data class OtpUIState(
    var email :String = "",
    var code1 :String = "",
    var code2  :String = "",
    var code3  :String = "",
    var code4  :String = "",


    var emailError :Boolean = false,
    var code1Error :Boolean = false,
    var code2Error : Boolean = false,
    var code3Error : Boolean = false,
    var code4Error :Boolean = false,

)
