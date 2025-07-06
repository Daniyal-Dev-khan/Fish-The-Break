package com.cp.fishthebreak.viewModels.auth.login

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.RegisterModel
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
class LoginViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var loginUIState = MutableStateFlow<LoginUIState>(LoginUIState())
    val loginUIStates = loginUIState.asStateFlow()

    private val _loginResponse: MutableStateFlow<NetworkResult<RegisterModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val response: StateFlow<NetworkResult<RegisterModel>> = _loginResponse.asStateFlow()

    fun onEmailChangeEvent(email: CharSequence) {
        onEvent(LoginUIEvent.EmailChanged(email.toString()))
    }

    fun onFcmTokenChangeEvent(fcmToken: String) {
        onEvent(LoginUIEvent.FcmTokenChanged(fcmToken))
    }

    fun onPasswordChangeEvent(password: CharSequence) {
        onEvent(LoginUIEvent.PasswordChanged(password.toString()))
    }

    fun onRememberMeChangeEvent(rememberMe: Boolean) {
        onEvent(LoginUIEvent.RememberMeChanged(rememberMe))
    }

    fun onLoginEvent(view: View) {
        onEvent(LoginUIEvent.LoginButtonClicked, view)
    }

    init {
        val email = preference.getEmail()
        if (!email.isNullOrEmpty()) {
            onEmailChangeEvent(email)
            onRememberMeChangeEvent(true)
        }
    }

    private fun onEvent(event: LoginUIEvent, view: View? = null) {
        when (event) {
            is LoginUIEvent.EmailChanged -> {
                loginUIState.value = loginUIState.value.copy(
                    email = event.email
                )
                loginUIState.value = loginUIState.value.copy(
                    emailError = Validator.validateEmail(
                        email = loginUIState.value.email
                    ).status
                )
            }

            is LoginUIEvent.FcmTokenChanged -> {
                loginUIState.value = loginUIState.value.copy(
                    fcmToken = event.fcmToken
                )
            }

            is LoginUIEvent.RememberMeChanged -> {
                loginUIState.value = loginUIState.value.copy(
                    rememberMe = event.rememberMe
                )
            }

            is LoginUIEvent.PasswordChanged -> {
                loginUIState.value = loginUIState.value.copy(
                    password = event.password
                )
                loginUIState.value = loginUIState.value.copy(
                    passwordError = Validator.validateLoginPassword(
                        password = loginUIState.value.password
                    ).status
                )
            }

            is LoginUIEvent.LoginButtonClicked -> {
                login(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateLoginUIDataWithRules(): Boolean {
        val emailResult = Validator.validateEmail(
            email = loginUIState.value.email
        )


        val passwordResult = Validator.validateLoginPassword(
            password = loginUIState.value.password
        )

        loginUIState.value = loginUIState.value.copy(
            emailError = emailResult.status,
            passwordError = passwordResult.status
        )
        var allValidationsPassed = true
        if (!emailResult.status) {
            allValidationsPassed = false
            return false
        }
        if (!passwordResult.status) {
            allValidationsPassed = false
            return false
        }
        return allValidationsPassed
        //allValidationsPassed.value = emailResult.status && passwordResult.status

    }

    private fun login(view: View?) = viewModelScope.launch {
        if (validateLoginUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _loginResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
                jsonObject.addProperty("email", loginUIState.value.email)
                jsonObject.addProperty("password", loginUIState.value.password)
                jsonObject.addProperty("fcm_token", loginUIState.value.fcmToken)
                //jsonObject.addProperty("device_type", "2")// => 1 IOS, 2 ANDROID
                jsonObject.addProperty("device_id", applicationContext.getMyDeviceId())

                repository.login(jsonObject).collect { values ->
                    if (values.data?.status == true && !values.data.data?.token.isNullOrEmpty()) {
                        preference.saveUser(values.data.data)
                        if (loginUIState.value.rememberMe) {
                            preference.saveEmail(values.data.data?.email)
                        } else {
                            preference.saveEmail("")
                        }
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

    fun socialLogin(
        socialId: String,
        firstName: String,
        lastName: String,
        userName: String,
        socialPlatform: String,
        email: String,
        view: View
    ) = viewModelScope.launch {
        view.let { applicationContext.hideKeyboardFrom(it) }
        if (applicationContext.isNetworkAvailable()) {
            _loginResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
            jsonObject.addProperty("email", email)
            jsonObject.addProperty("fcm_token", loginUIState.value.fcmToken)
            //jsonObject.addProperty("device_type", "2")// => 1 IOS, 2 ANDROID
            jsonObject.addProperty("device_id", applicationContext.getMyDeviceId())
            jsonObject.addProperty("social_platform", socialPlatform)
            jsonObject.addProperty("social_login_id", socialId)
            jsonObject.addProperty("username", userName)
            jsonObject.addProperty("first_name", firstName)
            jsonObject.addProperty("last_name", lastName)

            repository.socialLogin(jsonObject).collect { values ->
                if (values.data?.status == true && !values.data.data?.token.isNullOrEmpty()) {
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


    fun resetResponse() {
        _loginResponse.value = NetworkResult.NoCallYet()
    }

}