package com.cp.fishthebreak.utils.rules

import com.cp.fishthebreak.utils.isValidEmail
import com.cp.fishthebreak.utils.isValidLatNauticalFormat
import com.cp.fishthebreak.utils.isValidLngNauticalFormat
import com.cp.fishthebreak.utils.isValidPasswordFormat
import com.cp.fishthebreak.utils.isValidateUsername


object Validator {


    fun validateFirstName(fName: String): ValidationResult {
        return ValidationResult(
            (!fName.isNullOrEmpty() && fName.length >= 1)
        )

    }

    fun validateLastName(lName: String): ValidationResult {
        return ValidationResult(
            (!lName.isNullOrEmpty() && lName.length >= 1)
        )
    }

    fun validateUserName(username: String): ValidationResult {
        return ValidationResult(
            (!username.isNullOrEmpty() && username.isValidateUsername())
        )
    }

    fun validateEmail(email: String): ValidationResult {
        return ValidationResult(
            (!email.isNullOrEmpty() && email.isValidEmail())
        )
    }

    fun validatePhone(phone: String): ValidationResult {
        return ValidationResult(
            (!phone.isNullOrEmpty())
        )
    }

    fun validateBoatRange(range: Int): ValidationResult {
        return ValidationResult(
            (range > 0)
        )
    }

    fun validateEmailChane(email: String, oldEmail: String): ValidationResult {
        return ValidationResult(
            (email != oldEmail)
        )
    }

    fun validateOTP(code: String): ValidationResult {
        return ValidationResult(
            (!code.isNullOrEmpty() && code.length == 1)
        )
    }

    fun validateOTPCode(code: String): ValidationResult {
        return ValidationResult(
            (!code.isNullOrEmpty() && code.length == 4)
        )
    }

    fun validatePassword(password: String): ValidationResult {
        return ValidationResult(
            (!password.isNullOrEmpty() && password.isValidPasswordFormat())
        )
    }

    fun validateLoginPassword(password: String): ValidationResult {
        return ValidationResult(
            (!password.trim().isNullOrEmpty())
        )
    }

    fun validateLocationName(name: String): ValidationResult {
        return ValidationResult(
            (!name.trim().isNullOrEmpty())
        )
    }

    fun validateLatLang(point: String): ValidationResult {
        return ValidationResult(
            (!point.trim().isNullOrEmpty())
        )
    }

    fun validateLatFormat(point: String): ValidationResult {
        return ValidationResult(
            (point.trim().isNotEmpty() && point.trim().isValidLatNauticalFormat())
        )
    }

    fun validateLngFormat(point: String): ValidationResult {
        return ValidationResult(
            (point.trim().isNotEmpty() && point.trim().isValidLngNauticalFormat())
        )
    }

    fun validateDate(date: String): ValidationResult {
        return ValidationResult(
            (!date.trim().isNullOrEmpty())
        )
    }

    fun validateTime(time: String): ValidationResult {
        return ValidationResult(
            (!time.trim().isNullOrEmpty())
        )
    }

    fun validateText(text: String): ValidationResult {
        return ValidationResult(
            (!text.trim().isNullOrEmpty())
        )
    }

    fun validateList(list: List<Any>): ValidationResult {
        return ValidationResult(
            list.isNotEmpty()
        )
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return ValidationResult(
            (!confirmPassword.isNullOrEmpty() && confirmPassword.isValidPasswordFormat() && password == confirmPassword)
        )
    }

    fun validatePrivacyPolicyAcceptance(statusValue: Boolean): ValidationResult {
        return ValidationResult(
            statusValue
        )
    }

}

data class ValidationResult(
    val status: Boolean = false
)








