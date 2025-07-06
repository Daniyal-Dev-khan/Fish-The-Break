package com.cp.fishthebreak.viewModels.auth.signup

data class RegistrationUIState(
    var firstName :String = "",
    var lastName  :String = "",
    var username  :String = "",
    var email  :String = "",
    var phone  :String = "",
    var password  :String = "",
    var confirmPassword  :String = "",
    var privacyPolicyAccepted :Boolean = false,


    var firstNameError :Boolean = true,
    var lastNameError : Boolean = true,
    var usernameError : Boolean = true,
    var emailError :Boolean = true,
    var phoneError :Boolean = true,
    var passwordError : Boolean = true,
    var confirmPasswordError : Boolean = true,
    var privacyPolicyError:Boolean = true


)
