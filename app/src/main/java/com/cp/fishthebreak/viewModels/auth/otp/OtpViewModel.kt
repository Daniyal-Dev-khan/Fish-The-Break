package com.cp.fishthebreak.viewModels.auth.otp

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.RegisterModel
import com.cp.fishthebreak.models.auth.SendCodeModel
import com.cp.fishthebreak.models.auth.VerifyPasswordOtpModel
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
class OtpViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var otpUIState = MutableStateFlow(OtpUIState())
    val otpUIStates = otpUIState.asStateFlow()

    //var allValidationsPassed = MutableStateFlow(false)

    private val _validationError: MutableStateFlow<String> =
        MutableStateFlow("")
    val validationError: StateFlow<String> = _validationError.asStateFlow()

    private val _loginResponse: MutableStateFlow<NetworkResult<RegisterModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val response: StateFlow<NetworkResult<RegisterModel>> = _loginResponse.asStateFlow()

    private val _resetResponse: MutableStateFlow<NetworkResult<VerifyPasswordOtpModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val resetResponse: StateFlow<NetworkResult<VerifyPasswordOtpModel>> =
        _resetResponse.asStateFlow()

    private val _resendResponse: MutableStateFlow<NetworkResult<SendCodeModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val resendResponse: StateFlow<NetworkResult<SendCodeModel>> = _resendResponse.asStateFlow()
    fun onEmailChangeEvent(email: String) {
        onEvent(OtpUIEvent.EmailChanged(email))
    }

    fun onCode1ChangeEvent(code: CharSequence) {
        onEvent(OtpUIEvent.Code1Changed(code.toString()))
    }

    fun onCode2ChangeEvent(code: CharSequence) {
        onEvent(OtpUIEvent.Code2Changed(code.toString()))
    }

    fun onCode3ChangeEvent(code: CharSequence) {
        onEvent(OtpUIEvent.Code3Changed(code.toString()))
    }

    fun onCode4ChangeEvent(code: CharSequence) {
        onEvent(OtpUIEvent.Code4Changed(code.toString()))
    }

    fun onNextEvent(view: View?) {
        onEvent(OtpUIEvent.NextButtonClicked, view)
    }

    fun onNextPasswordResetEvent(view: View?) {
        onEvent(OtpUIEvent.onNextPasswordResetEvent, view)
    }

    fun onResendEvent(view: View?) {
        onEvent(OtpUIEvent.onResendEvent, view)
    }

    private fun onEvent(event: OtpUIEvent, view: View? = null) {
        when (event) {
            is OtpUIEvent.EmailChanged -> {
                otpUIState.value = otpUIState.value.copy(
                    email = event.email
                )
            }

            is OtpUIEvent.Code1Changed -> {
                otpUIState.value = otpUIState.value.copy(
                    code1 = event.code
                )
            }

            is OtpUIEvent.Code2Changed -> {
                otpUIState.value = otpUIState.value.copy(
                    code2 = event.code
                )
            }

            is OtpUIEvent.Code3Changed -> {
                otpUIState.value = otpUIState.value.copy(
                    code3 = event.code
                )
            }

            is OtpUIEvent.Code4Changed -> {
                otpUIState.value = otpUIState.value.copy(
                    code4 = event.code
                )
            }

            is OtpUIEvent.NextButtonClicked -> {
                verifyOtp(view)
            }

            is OtpUIEvent.onNextPasswordResetEvent -> {
                verifyResetPasswordOtp(view)
            }

            is OtpUIEvent.onResendEvent -> {
                resendCode(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateLoginUIDataWithRules(): Boolean {
        val emailResult = Validator.validateEmail(
            email = otpUIState.value.email
        )
        val code1Result = Validator.validateOTP(
            code = otpUIState.value.code1
        )

        val code2Result = Validator.validateOTP(
            code = otpUIState.value.code2
        )

        val code3Result = Validator.validateOTP(
            code = otpUIState.value.code3
        )

        val code4Result = Validator.validateOTP(
            code = otpUIState.value.code4
        )

        otpUIState.value = otpUIState.value.copy(
            code1Error = code1Result.status,
            code2Error = code2Result.status,
            code3Error = code3Result.status,
            code4Error = code4Result.status,
        )

        return if (code1Result.status && code2Result.status && code3Result.status && code4Result.status) {
            if (emailResult.status) {
                true
            } else {
                _validationError.value =
                    applicationContext.resources.getString(R.string.error_invalid_email)
                false
            }

        } else {
            _validationError.value = applicationContext.resources.getString(R.string.error_otp_code)
            false
        }
        //allValidationsPassed.value = emailResult.status && passwordResult.status

    }

    private fun verifyOtp(view: View?) = viewModelScope.launch {
        if (validateLoginUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _loginResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
                jsonObject.addProperty("email", otpUIState.value.email)
                jsonObject.addProperty(
                    "otp",
                    "${otpUIState.value.code1}${otpUIState.value.code2}${otpUIState.value.code3}${otpUIState.value.code4}"
                )
                jsonObject.addProperty("fcm_token", "aaa")
                //jsonObject.addProperty("device_type", "2")// => 1 IOS, 2 ANDROID
                jsonObject.addProperty("device_id", applicationContext.getMyDeviceId())
                repository.verifyOtp(jsonObject).collect { values ->
                    if (values.data?.status == true) {
                        preference.saveUser(values.data.data)
                        values.data.data?.token?.let { preference.saveToken(it) }
                        preference.saveUserLogIn()
                    }
                    _loginResponse.value = values
                }
            } else {
                _loginResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    private fun verifyResetPasswordOtp(view: View?) = viewModelScope.launch {
        if (validateLoginUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _resetResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
                jsonObject.addProperty("email", otpUIState.value.email)
                jsonObject.addProperty(
                    "otp",
                    "${otpUIState.value.code1}${otpUIState.value.code2}${otpUIState.value.code3}${otpUIState.value.code4}"
                )
                repository.verifyPasswordResetOtp(jsonObject).collect { values ->
                    _resetResponse.value = values
                }
            } else {
                _resetResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    private fun resendCode(view: View?) = viewModelScope.launch {
        view?.let { applicationContext.hideKeyboardFrom(it) }
        if (applicationContext.isNetworkAvailable()) {
            _resendResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
            jsonObject.addProperty("email", otpUIState.value.email)
            repository.sendOtp(jsonObject).collect { values ->
                _resendResponse.value = values
            }
        } else {
            _resendResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetError() {
        _validationError.value = ""
    }

    fun resetResponse() {
        _loginResponse.value = NetworkResult.NoCallYet()
        _resetResponse.value = NetworkResult.NoCallYet()
        _resendResponse.value = NetworkResult.NoCallYet()
    }

}