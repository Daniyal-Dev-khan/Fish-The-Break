package com.cp.fishthebreak.viewModels.profile.preference

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.RegisterModel
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.utils.SharePreferenceHelper
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
class PreferenceViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private val _boatRange : MutableStateFlow<String> =
        MutableStateFlow("")
    val boatRange: StateFlow<String> = _boatRange.asStateFlow()

    private var preferenceUIState = MutableStateFlow(BoatRangeUIState())
    val preferenceUIStates = preferenceUIState.asStateFlow()

    private val _profileResponse: MutableStateFlow<NetworkResult<RegisterModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val profileResponse: StateFlow<NetworkResult<RegisterModel>> = _profileResponse.asStateFlow()

    private val _toggleResponse: MutableStateFlow<NetworkResult<RegisterModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val toggleResponse: StateFlow<NetworkResult<RegisterModel>> = _toggleResponse.asStateFlow()

    private val _validationError: MutableStateFlow<String> =
        MutableStateFlow("")
    val validationError: StateFlow<String> = _validationError.asStateFlow()

    private val _userResponse: MutableStateFlow<User?> =
        MutableStateFlow(loadUserFromLocalStorage())
    val userResponse: StateFlow<User?> = _userResponse.asStateFlow()

    private fun loadUserFromLocalStorage(): User? {
        val user = preference.getUser()
        _boatRange.value = user?.user_configuration?.range?.toString()?:""
        onToggleChangeEvent((user?.user_configuration?.boat_range?:0) == 1)
        return user
    }

    fun onRangeChangeEvent(range: Int) {
        onEvent(BoatRangeUIEvent.RangeChanged(range))
    }

    fun onToggleChangeEvent(toggle: Boolean) {
        onEvent(BoatRangeUIEvent.BoatToggleChanged(toggle))
    }
    fun onSaveClickedEvent(view: View) {
        onEvent(BoatRangeUIEvent.SaveButtonClicked,view)
    }

    private fun onEvent(event: BoatRangeUIEvent, view: View? = null) {
        when (event) {
            is BoatRangeUIEvent.RangeChanged -> {
                preferenceUIState.value = preferenceUIState.value.copy(
                    range = event.range
                )
            }

            is BoatRangeUIEvent.BoatToggleChanged -> {
                preferenceUIState.value = preferenceUIState.value.copy(
                    boatToggle = event.toggle
                )
            }

            is BoatRangeUIEvent.SaveButtonClicked -> {
                setBoatRange(view)
            }

        }
    }

    fun toggleVessel() = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _toggleResponse.value = NetworkResult.Loading()
            repository.toggleVessel().collect { values ->
                if (values.data?.status == true && values.data.statusCode == 200) {
                    preference.saveUser(values.data.data)
                    _userResponse.value = values.data.data
                }
                _toggleResponse.value = values
            }
        } else {
            _toggleResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    private fun validateUIDataWithRules(): Boolean {
        val rangeResult = Validator.validateBoatRange(
            range = preferenceUIState.value.range
        )

        preferenceUIState.value = preferenceUIState.value.copy(
            rangeError = rangeResult.status
        )
        if(!rangeResult.status) {
            _validationError.value =
                applicationContext.resources.getString(R.string.error_boat_range)
        }

        return rangeResult.status
    }

    private fun setBoatRange(view: View?) = viewModelScope.launch {
        if (validateUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _profileResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
            jsonObject.addProperty("boat_range", (if(preferenceUIState.value.boatToggle){1}else{0}))
            jsonObject.addProperty("range", preferenceUIState.value.range)
                repository.boatRange(jsonObject).collect { values ->
                    if (values.data?.status == true && values.data.statusCode == 200) {
                        preference.saveUser(values.data.data)
                        _userResponse.value = values.data.data
                    }
                    _profileResponse.value = values
                }
            } else {
                _profileResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )
            }
        }
    }

    fun toggleBoatRange(view: View?, toggle: Boolean) = viewModelScope.launch {
        view?.let { applicationContext.hideKeyboardFrom(it) }
        if (applicationContext.isNetworkAvailable()) {
            _toggleResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("boat_range", (if(toggle){1}else{0}))
            repository.boatRange(jsonObject).collect { values ->
                if (values.data?.status == true && values.data.statusCode == 200) {
                    preference.saveUser(values.data.data)
                    _userResponse.value = values.data.data
                }
                _toggleResponse.value = values
            }
        } else {
            _toggleResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }

    fun resetResponse() {
        _profileResponse.value = NetworkResult.NoCallYet()
    }
    fun resetErrorResponse() {
        _validationError.value = ""
    }

}