package com.cp.fishthebreak.viewModels.profile.email

data class EmailChangeUIState(
    var email  :String = "",
    var code1 :String = "",
    var code2  :String = "",
    var code3  :String = "",
    var code4  :String = "",

    var emailError :Boolean = true,
    var code1Error :Boolean = false,
    var code2Error : Boolean = false,
    var code3Error : Boolean = false,
    var code4Error :Boolean = false,
)
