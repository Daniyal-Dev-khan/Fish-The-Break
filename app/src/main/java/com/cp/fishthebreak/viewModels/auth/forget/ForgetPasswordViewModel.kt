package com.cp.fishthebreak.viewModels.auth.forget

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.SendCodeModel
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
class ForgetPasswordViewModel @Inject constructor
    (
    private val repository: Repository,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var forgetPasswordUIState = MutableStateFlow(ForgetPasswordUIState())
    val forgetPasswordUIStates = forgetPasswordUIState.asStateFlow()


    private val _forgetResponse: MutableStateFlow<NetworkResult<SendCodeModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val forgetResponse: StateFlow<NetworkResult<SendCodeModel>> = _forgetResponse.asStateFlow()

    fun onEmailChangeEvent(email: CharSequence) {
        onEvent(ForgetPasswordUIEvent.EmailChanged(email.toString()))
    }

    fun onNextClickEvent(view: View?) {
        onEvent(ForgetPasswordUIEvent.NextButtonClicked, view)
    }

    private fun onEvent(event: ForgetPasswordUIEvent, view: View? = null) {
        when (event) {
            is ForgetPasswordUIEvent.EmailChanged -> {
                forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
                    email = event.email
                )
                validateLoginUIDataWithRules()
            }


            is ForgetPasswordUIEvent.NextButtonClicked -> {
                sendOtp(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateLoginUIDataWithRules(): Boolean {
        val emailResult = Validator.validateEmail(
            email = forgetPasswordUIState.value.email
        )

        forgetPasswordUIState.value = forgetPasswordUIState.value.copy(
            emailError = emailResult.status
        )
        return emailResult.status

        //allValidationsPassed.value = emailResult.status && passwordResult.status

    }

    private fun sendOtp(view: View?) = viewModelScope.launch {
        if (validateLoginUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _forgetResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
                jsonObject.addProperty("email", forgetPasswordUIState.value.email)

                repository.sendOtp(jsonObject).collect { values ->
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

    fun resetServerResponse() {
        _forgetResponse.value = NetworkResult.NoCallYet()
    }

}