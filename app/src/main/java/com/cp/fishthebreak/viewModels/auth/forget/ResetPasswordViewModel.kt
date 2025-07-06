package com.cp.fishthebreak.viewModels.auth.forget

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.RegisterModel
import com.cp.fishthebreak.models.auth.ResetPasswordModel
import com.cp.fishthebreak.models.auth.SendCodeModel
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.getMyDeviceId
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.rules.Validator
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor
    (
    private val repository: Repository,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var forgetPasswordUIState = MutableStateFlow(ResetPasswordUIState())
    val forgetPasswordUIStates = forgetPasswordUIState.asStateFlow()

    private val _validationError: MutableStateFlow<String> =
        MutableStateFlow("")
    val validationError: StateFlow<String> = _validationError.asStateFlow()

    private val _forgetResponse: MutableStateFlow<NetworkResult<ResetPasswordModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val response: StateFlow<NetworkResult<ResetPasswordModel>> = _forgetResponse.asStateFlow()

    fun onPasswordChangeEvent(password: CharSequence){
        onEvent(ResetPasswordUIEvent.PasswordChanged(password.toString()))
    }

    fun onConfirmPasswordChangeEvent(password: CharSequence){
        onEvent(ResetPasswordUIEvent.ConfirmPasswordChanged(password.toString()))
    }

    fun onEmailChangeEvent(email: String){
        onEvent(ResetPasswordUIEvent.EmailChanged(email))
    }

    fun onOtpChangeEvent(otp: String){
        onEvent(ResetPasswordUIEvent.OtpChanged(otp))
    }

    fun onNextClickEvent(view: View?){
        onEvent(ResetPasswordUIEvent.NextButtonClicked, view)
    }

    private fun onEvent(event: ResetPasswordUIEvent, view: View? = null) {
        when (event) {
            is ResetPasswordUIEvent.EmailChanged -> {
                forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
                    email = event.email
                )
            }
            is ResetPasswordUIEvent.OtpChanged -> {
                forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
                    otp = event.otp
                )
            }
            is ResetPasswordUIEvent.PasswordChanged -> {
                forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
                    password = event.password
                )
                forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
                    passwordError = Validator.validatePassword(
                        password = forgetPasswordUIState.value.password
                    ).status
                )
            }
            is ResetPasswordUIEvent.ConfirmPasswordChanged -> {
                forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
                    confirmPassword = event.password
                )
                forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
                    confirmPasswordError = Validator.validateConfirmPassword(
                        password = forgetPasswordUIState.value.password,
                        confirmPassword = forgetPasswordUIState.value.confirmPassword,
                    ).status
                )
            }
            

            is ResetPasswordUIEvent.NextButtonClicked -> {
                resetPassword(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateLoginUIDataWithRules(): Boolean {
        val passwordResult = Validator.validatePassword(
            password = forgetPasswordUIState.value.password
        )
        val confirmPasswordResult = Validator.validateConfirmPassword(
            password = forgetPasswordUIState.value.password,
            confirmPassword = forgetPasswordUIState.value.confirmPassword,
        )

        val emailResult = Validator.validateEmail(
            email = forgetPasswordUIState.value.email
        )

        val otpResult = Validator.validateOTPCode(
            code = forgetPasswordUIState.value.otp,
        )

        forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
            passwordError = passwordResult.status,
            confirmPasswordError = confirmPasswordResult.status
        )
        var allValidationsPassed = true
        if(!emailResult.status){
            _validationError.value = applicationContext.resources.getString(R.string.error_invalid_email)
            allValidationsPassed = false
            return false
        }
        if(!otpResult.status){
            _validationError.value = applicationContext.resources.getString(R.string.error_otp_code)
            allValidationsPassed = false
            return false
        }
        if(!passwordResult.status){
            _validationError.value = applicationContext.resources.getString(R.string.error_invalid_password)
            allValidationsPassed = false
            return false
        }
        if(!confirmPasswordResult.status){
            _validationError.value = applicationContext.resources.getString(R.string.error_confirm_password_match)
            allValidationsPassed = false
            return false
        }
        return allValidationsPassed
        //allValidationsPassed.value = emailResult.status && passwordResult.status

    }

    private fun resetPassword(view: View?) = viewModelScope.launch {
        if(validateLoginUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _forgetResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
                jsonObject.addProperty("password", forgetPasswordUIState.value.password)
                jsonObject.addProperty("password_confirmation", forgetPasswordUIState.value.confirmPassword)
                jsonObject.addProperty("email", forgetPasswordUIState.value.email)
                jsonObject.addProperty("otp", forgetPasswordUIState.value.otp)

                repository.resetPassword(jsonObject).collect { values ->
                    _forgetResponse.value = values
                }
            } else {
                _forgetResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    fun resetError(){
        _validationError.value = ""
    }

    fun resetResponse(){
        _forgetResponse.value = NetworkResult.NoCallYet()
    }

}