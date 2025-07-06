package com.cp.fishthebreak.viewModels.profile.email

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.RegisterModel
import com.cp.fishthebreak.models.profile.ChangeEmailModel
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
class ChangeEmailViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var changeEmailUIState = MutableStateFlow(EmailChangeUIState())
    val changeEmailUIStates = changeEmailUIState.asStateFlow()

    private val _validationError: MutableStateFlow<String> =
        MutableStateFlow("")
    val validationError: StateFlow<String> = _validationError.asStateFlow()

    private val _updateEmailResponse: MutableStateFlow<NetworkResult<ChangeEmailModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val updateEmailResponse: StateFlow<NetworkResult<ChangeEmailModel>> =
        _updateEmailResponse.asStateFlow()

    private val _emailString: MutableStateFlow<String> =
        MutableStateFlow(preference.getUser()?.email ?: "")
    val emailString: StateFlow<String> = _emailString.asStateFlow()

    private val _emailErrorString: MutableStateFlow<String> =
        MutableStateFlow("")
    val emailErrorString: StateFlow<String> = _emailErrorString.asStateFlow()

    private val _loginResponse: MutableStateFlow<NetworkResult<RegisterModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val response: StateFlow<NetworkResult<RegisterModel>> = _loginResponse.asStateFlow()
    
    init {
        onEmailChangeEvent(preference.getUser()?.email ?: "")
    }

    fun onEmailChangeEvent(email: CharSequence) {
        onEvent(ChangeEmailUIEvent.EmailChanged(email.toString()))
    }

    fun onCode1ChangeEvent(code: CharSequence) {
        onEvent(ChangeEmailUIEvent.Code1Changed(code.toString()))
    }

    fun onCode2ChangeEvent(code: CharSequence) {
        onEvent(ChangeEmailUIEvent.Code2Changed(code.toString()))
    }

    fun onCode3ChangeEvent(code: CharSequence) {
        onEvent(ChangeEmailUIEvent.Code3Changed(code.toString()))
    }

    fun onCode4ChangeEvent(code: CharSequence) {
        onEvent(ChangeEmailUIEvent.Code4Changed(code.toString()))
    }

    fun onSaveClickEvent(view: View?) {
        onEvent(ChangeEmailUIEvent.SaveButtonClicked, view)
    }

    fun onVerifyOtpClickEvent(view: View?) {
        onEvent(ChangeEmailUIEvent.VerifyOtpButtonClicked, view)
    }

    private fun onEvent(event: ChangeEmailUIEvent, view: View? = null) {
        when (event) {
            is ChangeEmailUIEvent.EmailChanged -> {
                changeEmailUIState.value = changeEmailUIState.value.copy(
                    email = event.email
                )
                validateUIDataWithRules()
            }

            is ChangeEmailUIEvent.Code1Changed -> {
                changeEmailUIState.value = changeEmailUIState.value.copy(
                    code1 = event.code
                )
            }

            is ChangeEmailUIEvent.Code2Changed -> {
                changeEmailUIState.value = changeEmailUIState.value.copy(
                    code2 = event.code
                )
            }

            is ChangeEmailUIEvent.Code3Changed -> {
                changeEmailUIState.value = changeEmailUIState.value.copy(
                    code3 = event.code
                )
            }

            is ChangeEmailUIEvent.Code4Changed -> {
                changeEmailUIState.value = changeEmailUIState.value.copy(
                    code4 = event.code
                )
            }

            is ChangeEmailUIEvent.SaveButtonClicked -> {
                changeEmail(view)
            }

            is ChangeEmailUIEvent.VerifyOtpButtonClicked -> {
                verifyOtp(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateUIDataWithRules(): Boolean {
        val emailResult = Validator.validateEmail(
            email = changeEmailUIState.value.email
        )
        val emailChangeResult = Validator.validateEmailChane(
            email = changeEmailUIState.value.email,
            oldEmail = preference.getUser()?.email ?: ""
        )

        if (!emailChangeResult.status) {
            _emailErrorString.value =
                applicationContext.resources.getString(R.string.error_invalid_change_email)
            changeEmailUIState.value = changeEmailUIState.value.copy(
                emailError = emailChangeResult.status
            )
        } else if (!emailResult.status) {
            _emailErrorString.value =
                applicationContext.resources.getString(R.string.error_invalid_email)
            changeEmailUIState.value = changeEmailUIState.value.copy(
                emailError = emailResult.status
            )
        } else {
            changeEmailUIState.value = changeEmailUIState.value.copy(
                emailError = true
            )
        }
        return emailResult.status && emailChangeResult.status

    }

    private fun changeEmail(view: View?) = viewModelScope.launch {
        if (validateUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _updateEmailResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
                jsonObject.addProperty("email", changeEmailUIState.value.email)
                repository.updateEmail(jsonObject).collect { values ->
                    _emailString.value = changeEmailUIState.value.email
                    _updateEmailResponse.value = values
                }
            } else {
                _updateEmailResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )
            }
        }
    }

    private fun validateLoginUIDataWithRules(): Boolean {
        val emailResult = Validator.validateEmail(
            email = changeEmailUIState.value.email
        )
        val code1Result = Validator.validateOTP(
            code = changeEmailUIState.value.code1
        )

        val code2Result = Validator.validateOTP(
            code = changeEmailUIState.value.code2
        )

        val code3Result = Validator.validateOTP(
            code = changeEmailUIState.value.code3
        )

        val code4Result = Validator.validateOTP(
            code = changeEmailUIState.value.code4
        )

        changeEmailUIState.value = changeEmailUIState.value.copy(
            code1Error = code1Result.status,
            code2Error = code2Result.status,
            code3Error = code3Result.status,
            code4Error = code4Result.status,
        )

        return if(code1Result.status && code2Result.status && code3Result.status && code4Result.status){
            if(emailResult.status){
                true
            }else{
                _validationError.value = applicationContext.resources.getString(R.string.error_invalid_email)
                false
            }

        }else{
            _validationError.value = applicationContext.resources.getString(R.string.error_otp_code)
            false
        }
        //allValidationsPassed.value = emailResult.status && passwordResult.status

    }

    private fun verifyOtp(view: View?) = viewModelScope.launch {
        if(validateLoginUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _loginResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
                jsonObject.addProperty("email", changeEmailUIState.value.email)
                jsonObject.addProperty("otp", "${changeEmailUIState.value.code1}${changeEmailUIState.value.code2}${changeEmailUIState.value.code3}${changeEmailUIState.value.code4}")
                repository.verifyEmailOtp(jsonObject).collect { values ->
                    if(values.data?.status == true){
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

    fun resetResponse() {
        _updateEmailResponse.value = NetworkResult.NoCallYet()
    }

    fun resetError(){
        _validationError.value = ""
    }

}