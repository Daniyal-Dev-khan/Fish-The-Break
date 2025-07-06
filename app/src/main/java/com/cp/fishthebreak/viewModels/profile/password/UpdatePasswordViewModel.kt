package com.cp.fishthebreak.viewModels.profile.password

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
import com.cp.fishthebreak.models.profile.ChangePasswordModel
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.getMyDeviceId
import com.cp.fishthebreak.utils.getPasswordValidationError
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
class UpdatePasswordViewModel @Inject constructor
    (
    private val repository: Repository,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var updatePasswordUIState = MutableStateFlow(UpdatePasswordUIState())
    val updatePasswordUIStates = updatePasswordUIState.asStateFlow()


    private val _updateResponse: MutableStateFlow<NetworkResult<ChangePasswordModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val response: StateFlow<NetworkResult<ChangePasswordModel>> = _updateResponse.asStateFlow()

    private val _passwordErrorResponse: MutableStateFlow<String> =
        MutableStateFlow("")
    val passwordErrorResponse: StateFlow<String> = _passwordErrorResponse.asStateFlow()

    fun onCurrentPasswordChangeEvent(password: CharSequence) {
        onEvent(UpdatePasswordUIEvent.CurrentPasswordChanged(password.toString()))
    }

    fun onPasswordChangeEvent(password: CharSequence) {
        onEvent(UpdatePasswordUIEvent.PasswordChanged(password.toString()))
    }

    fun onConfirmPasswordChangeEvent(password: CharSequence) {
        onEvent(UpdatePasswordUIEvent.ConfirmPasswordChanged(password.toString()))
    }


    fun onSaveClickEvent(view: View?) {
        onEvent(UpdatePasswordUIEvent.SaveButtonClicked, view)
    }

    private fun onEvent(event: UpdatePasswordUIEvent, view: View? = null) {
        when (event) {
            is UpdatePasswordUIEvent.CurrentPasswordChanged -> {
                updatePasswordUIState.value = updatePasswordUIState.value.copy(
                    currentPassword = event.password
                )
                updatePasswordUIState.value = updatePasswordUIState.value.copy(
                    currentPasswordError = Validator.validateLoginPassword(
                        password = updatePasswordUIState.value.currentPassword
                    ).status
                )
            }

            is UpdatePasswordUIEvent.PasswordChanged -> {
                updatePasswordUIState.value = updatePasswordUIState.value.copy(
                    password = event.password
                )
                _passwordErrorResponse.value = updatePasswordUIState.value.password.getPasswordValidationError()?:""
                updatePasswordUIState.value = updatePasswordUIState.value.copy(
                    passwordError = Validator.validatePassword(
                        password = updatePasswordUIState.value.password
                    ).status
                )
            }

            is UpdatePasswordUIEvent.ConfirmPasswordChanged -> {
                updatePasswordUIState.value = updatePasswordUIState.value.copy(
                    confirmPassword = event.password
                )
                updatePasswordUIState.value = updatePasswordUIState.value.copy(
                    confirmPasswordError = Validator.validateConfirmPassword(
                        password = updatePasswordUIState.value.password,
                        confirmPassword = updatePasswordUIState.value.confirmPassword,
                    ).status
                )
            }


            is UpdatePasswordUIEvent.SaveButtonClicked -> {
                resetPassword(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateLoginUIDataWithRules(): Boolean {
        val currentPasswordResult = Validator.validateLoginPassword(
            password = updatePasswordUIState.value.currentPassword
        )
        val passwordResult = Validator.validatePassword(
            password = updatePasswordUIState.value.password
        )
        val confirmPasswordResult = Validator.validateConfirmPassword(
            password = updatePasswordUIState.value.password,
            confirmPassword = updatePasswordUIState.value.confirmPassword,
        )
        _passwordErrorResponse.value = updatePasswordUIState.value.password.getPasswordValidationError()?:""
        updatePasswordUIState.value = updatePasswordUIState.value.copy(
            currentPasswordError = currentPasswordResult.status,
            passwordError = passwordResult.status,
            confirmPasswordError = confirmPasswordResult.status
        )

        return currentPasswordResult.status && passwordResult.status && confirmPasswordResult.status
    }

    private fun resetPassword(view: View?) = viewModelScope.launch {
        if (validateLoginUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _updateResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
                jsonObject.addProperty("old_password", updatePasswordUIState.value.currentPassword)
                jsonObject.addProperty("password", updatePasswordUIState.value.password)
                jsonObject.addProperty(
                    "password_confirmation",
                    updatePasswordUIState.value.confirmPassword
                )

                repository.updatePassword(jsonObject).collect { values ->
                    _updateResponse.value = values
                }
            } else {
                _updateResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    fun resetResponse() {
        _updateResponse.value = NetworkResult.NoCallYet()
    }

}