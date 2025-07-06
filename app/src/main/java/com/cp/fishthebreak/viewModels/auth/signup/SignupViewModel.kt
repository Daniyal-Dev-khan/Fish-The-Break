package com.cp.fishthebreak.viewModels.auth.signup

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.RegisterModel
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.getMyDeviceId
import com.cp.fishthebreak.utils.getPasswordValidationError
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.rules.ValidationResult
import com.cp.fishthebreak.utils.rules.Validator
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private val TAG = SignupViewModel::class.simpleName


    private var registrationUIState = MutableStateFlow(RegistrationUIState())
    val registrationUIStates = registrationUIState.asStateFlow()

    private val _passwordErrorResponse: MutableStateFlow<String> =
        MutableStateFlow("")
    val passwordErrorResponse: StateFlow<String> = _passwordErrorResponse.asStateFlow()

    private val _signupResponse: MutableStateFlow<NetworkResult<RegisterModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val response: StateFlow<NetworkResult<RegisterModel>> = _signupResponse.asStateFlow()

    private val _userNameError: MutableStateFlow<String> =
        MutableStateFlow(applicationContext.resources.getString(R.string.error_invalid_username))
    val userNameError: StateFlow<String> = _userNameError.asStateFlow()

    fun onFirstNameEvent(firstName: CharSequence) {
        onEvent(SignupUIEvent.FirstNameChanged(firstName.toString()))
    }

    fun onLastNameEvent(lastName: CharSequence) {
        onEvent(SignupUIEvent.LastNameChanged(lastName.toString()))
    }

    fun onUsernameEvent(username: CharSequence) {
        onEvent(SignupUIEvent.UsernameChanged(username.toString()))
    }

    fun onEmailEvent(email: CharSequence) {
        onEvent(SignupUIEvent.EmailChanged(email.toString()))
    }

    fun onPhoneEvent(phone: CharSequence) {
        onEvent(SignupUIEvent.PhoneChanged(phone.toString()))
    }

    fun onPasswordEvent(password: CharSequence) {
        onEvent(SignupUIEvent.PasswordChanged(password.toString()))
    }

    fun onConfirmPasswordEvent(password: CharSequence) {
        onEvent(SignupUIEvent.ConfirmPasswordChanged(password.toString()))
    }

    fun onPrivacyPolicyEvent(policyAccepted: Boolean) {
        onEvent(SignupUIEvent.PrivacyPolicyCheckBoxClicked(policyAccepted))
    }

    fun onSignupEvent(view: View?) {
        onEvent(SignupUIEvent.RegisterButtonClicked, view)
    }

    private fun onEvent(event: SignupUIEvent, view: View? = null) {
        when (event) {
            is SignupUIEvent.FirstNameChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    firstName = event.firstName
                )
                registrationUIState.value = registrationUIState.value.copy(
                    firstNameError = Validator.validateFirstName(
                        fName = registrationUIState.value.firstName
                    ).status
                )
            }

            is SignupUIEvent.LastNameChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    lastName = event.lastName
                )
                registrationUIState.value = registrationUIState.value.copy(
                    lastNameError = Validator.validateLastName(
                        lName = registrationUIState.value.lastName
                    ).status
                )
            }

            is SignupUIEvent.UsernameChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    username = event.username
                )
                registrationUIState.value = registrationUIState.value.copy(
                    usernameError = Validator.validateUserName(
                        username = registrationUIState.value.username
                    ).status
                )
                if (!Validator.validateUserName(username = registrationUIState.value.username).status) {
                    _userNameError.value =
                        applicationContext.resources.getString(R.string.error_invalid_username)
                    registrationUIState.value = registrationUIState.value.copy(
                        usernameError = false
                    )
                }
                if (Validator.validateEmail(email = registrationUIState.value.username).status) {
                    _userNameError.value =
                        applicationContext.resources.getString(R.string.error_invalid_username_email)
                    registrationUIState.value = registrationUIState.value.copy(
                        usernameError = false
                    )
                } else if (registrationUIState.value.username.contains("@")) {
                    _userNameError.value =
                        applicationContext.resources.getString(R.string.error_invalid_username_at_the_rate)
                    registrationUIState.value = registrationUIState.value.copy(
                        usernameError = false
                    )
                }
            }

            is SignupUIEvent.EmailChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    email = event.email
                )
                registrationUIState.value = registrationUIState.value.copy(
                    emailError = Validator.validateEmail(
                        email = registrationUIState.value.email
                    ).status
                )
            }

            is SignupUIEvent.PhoneChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    phone = event.phone
                )
                registrationUIState.value = registrationUIState.value.copy(
                    phoneError = Validator.validatePhone(
                        phone = registrationUIState.value.phone
                    ).status
                )
            }


            is SignupUIEvent.PasswordChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    password = event.password
                )
                _passwordErrorResponse.value =
                    registrationUIState.value.password.getPasswordValidationError() ?: ""
                registrationUIState.value = registrationUIState.value.copy(
                    passwordError = Validator.validatePassword(
                        password = registrationUIState.value.password
                    ).status
                )
            }

            is SignupUIEvent.ConfirmPasswordChanged -> {
                registrationUIState.value = registrationUIState.value.copy(
                    confirmPassword = event.confirmPassword
                )
                registrationUIState.value = registrationUIState.value.copy(
                    confirmPasswordError = Validator.validateConfirmPassword(
                        password = registrationUIState.value.password,
                        confirmPassword = registrationUIState.value.confirmPassword
                    ).status
                )
            }

            is SignupUIEvent.RegisterButtonClicked -> {
                signUp(view)
            }

            is SignupUIEvent.PrivacyPolicyCheckBoxClicked -> {
                registrationUIState.value = registrationUIState.value.copy(
                    privacyPolicyAccepted = event.status
                )
                registrationUIState.value = registrationUIState.value.copy(
                    privacyPolicyError = Validator.validatePrivacyPolicyAcceptance(
                        statusValue = registrationUIState.value.privacyPolicyAccepted
                    ).status
                )
            }
        }
        //validateDataWithRules()
    }


    private fun signUp(view: View?) = viewModelScope.launch {
        if (validateDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _signupResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
                jsonObject.addProperty("email", registrationUIState.value.email)
                jsonObject.addProperty("password", registrationUIState.value.password)
                jsonObject.addProperty(
                    "password_confirmation",
                    registrationUIState.value.confirmPassword
                )
                jsonObject.addProperty("first_name", registrationUIState.value.firstName)
                jsonObject.addProperty("last_name", registrationUIState.value.lastName)
                jsonObject.addProperty("username", registrationUIState.value.username)
                jsonObject.addProperty("mobile", registrationUIState.value.phone.replace(" ", ""))
//                jsonObject.addProperty("device_token", "aaa")
//                jsonObject.addProperty("device_type", "2")// => 1 IOS, 2 ANDROID
//                jsonObject.addProperty("device_id", applicationContext.getMyDeviceId())

                repository.signup(jsonObject).collect { values ->
                    if (values.data?.status == true) {
                        preference.saveUser(values.data.data)
                    }
                    _signupResponse.value = values
                }
            } else {
                _signupResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    private fun validateDataWithRules(): Boolean {
        val fNameResult = Validator.validateFirstName(
            fName = registrationUIState.value.firstName
        )

        val lNameResult = Validator.validateLastName(
            lName = registrationUIState.value.lastName
        )
        var usernameResult = Validator.validateUserName(
            username = registrationUIState.value.username
        )

        val emailResult = Validator.validateEmail(
            email = registrationUIState.value.email
        )

        val phoneResult = Validator.validatePhone(
            phone = registrationUIState.value.phone
        )


        val passwordResult = Validator.validatePassword(
            password = registrationUIState.value.password
        )

        val confirmPasswordResult = Validator.validateConfirmPassword(
            password = registrationUIState.value.password,
            confirmPassword = registrationUIState.value.confirmPassword
        )

        val privacyPolicyResult = Validator.validatePrivacyPolicyAcceptance(
            statusValue = registrationUIState.value.privacyPolicyAccepted
        )

        _passwordErrorResponse.value =
            registrationUIState.value.password.getPasswordValidationError() ?: ""
        if (!Validator.validateUserName(username = registrationUIState.value.username).status) {
            _userNameError.value =
                applicationContext.resources.getString(R.string.error_invalid_username)
            usernameResult = ValidationResult(status = false)
        }
        if (Validator.validateEmail(email = registrationUIState.value.username).status) {
            _userNameError.value =
                applicationContext.resources.getString(R.string.error_invalid_username_email)
            usernameResult = ValidationResult(status = false)
        } else if (registrationUIState.value.username.contains("@")) {
            _userNameError.value =
                applicationContext.resources.getString(R.string.error_invalid_username_at_the_rate)
            usernameResult = ValidationResult(status = false)
        }
        registrationUIState.value = registrationUIState.value.copy(
            firstNameError = fNameResult.status,
            lastNameError = lNameResult.status,
            usernameError = usernameResult.status,
            emailError = emailResult.status,
            passwordError = passwordResult.status,
            confirmPasswordError = confirmPasswordResult.status,
            privacyPolicyError = privacyPolicyResult.status,
            phoneError = phoneResult.status
        )

        var allValidationsPassed = true
        if (!fNameResult.status) {
            //_validationError.value = applicationContext.resources.getString(R.string.error_invalid_first_name)
            allValidationsPassed = false
            return false
        }
        if (!lNameResult.status) {
            //_validationError.value = applicationContext.resources.getString(R.string.error_invalid_last_name)
            allValidationsPassed = false
            return false
        }
        if (!usernameResult.status) {
            //_validationError.value = applicationContext.resources.getString(R.string.error_invalid_username)
            allValidationsPassed = false
            return false
        }
        if (!emailResult.status) {
            //_validationError.value = applicationContext.resources.getString(R.string.error_invalid_email)
            allValidationsPassed = false
            return false
        }
        if (!passwordResult.status) {
            //_validationError.value = applicationContext.resources.getString(R.string.error_invalid_password)
            allValidationsPassed = false
            return false
        }
        if (!confirmPasswordResult.status) {
            //_validationError.value = applicationContext.resources.getString(R.string.error_confirm_password_match)
            allValidationsPassed = false
            return false
        }
        if (!privacyPolicyResult.status) {
            //_validationError.value = applicationContext.resources.getString(R.string.error_terms)
            allValidationsPassed = false
            return false
        }

        if (!phoneResult.status) {
            //_validationError.value = applicationContext.resources.getString(R.string.error_terms)
            allValidationsPassed = false
            return false
        }
//        allValidationsPassed.value = fNameResult.status && lNameResult.status &&
//                emailResult.status && passwordResult.status && privacyPolicyResult.status && confirmPasswordResult.status

        return allValidationsPassed
    }

    fun resetResponse() {
        _signupResponse.value = NetworkResult.NoCallYet()
    }

}
