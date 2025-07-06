package com.cp.fishthebreak.viewModels

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
class SyncViewModel @Inject constructor
    (
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private val _syncResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val syncResponse: StateFlow<Boolean> = _syncResponse.asStateFlow()

    fun updateSync(syncStatus: Boolean){
        _syncResponse.value = syncStatus
    }

    fun resetResponse() {
        _syncResponse.value = false
    }

}