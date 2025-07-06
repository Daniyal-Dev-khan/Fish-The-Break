package com.cp.fishthebreak.viewModels.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.di.local.AppDatabase
import com.cp.fishthebreak.models.auth.DeleteAccountModel
import com.cp.fishthebreak.models.auth.LogoutModel
import com.cp.fishthebreak.models.auth.RegisterModel
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.deleteAppCache
import com.cp.fishthebreak.utils.deleteAppDownloadCache
import com.cp.fishthebreak.utils.getMyDeviceId
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor
    (
    private val repository: Repository,
    private val localDb: AppDatabase,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext){
    
    private val _profileResponse: MutableStateFlow<NetworkResult<RegisterModel>> =
        MutableStateFlow(initUserFromLocalStorage())
    val profileResponse: StateFlow<NetworkResult<RegisterModel>> = _profileResponse.asStateFlow()

    private val _logoutResponse: MutableStateFlow<NetworkResult<LogoutModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val logoutResponse: StateFlow<NetworkResult<LogoutModel>> = _logoutResponse.asStateFlow()

    private val _deleteResponse: MutableStateFlow<NetworkResult<DeleteAccountModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val deleteResponse: StateFlow<NetworkResult<DeleteAccountModel>> = _deleteResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()


    private fun initUserFromLocalStorage(): NetworkResult<RegisterModel>{
        val user = preference.getUser()
        return if(user != null) {
            NetworkResult.Success(RegisterModel(preference.getUser(), "", true, 200))
        }else{
            NetworkResult.NoCallYet()
        }
    }

    suspend fun getProfiles(
    ): Flow<NetworkResult<RegisterModel>> {
        return flow<NetworkResult<RegisterModel>>  {
            repository.getProfile().collect{values->
                if (values.data?.status == true && values.data.statusCode == 200) {
                    preference.saveUser(values.data.data)
                }
                emit(values)
            }
        }.flowOn(Dispatchers.IO)
    }


    fun logout() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            if (applicationContext.isNetworkAvailable()) {
                _logoutResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
                jsonObject.addProperty("device_id", applicationContext.getMyDeviceId())
                repository.logout(jsonObject).collect { values ->
                    if (values.data?.status == true && values.data.statusCode == 200) {
                        preference.clearAllPreferenceData()
                        preference.isFirstLaunch(false)
                        localDb.clearAllTables()
                        applicationContext.deleteAppCache()
                        applicationContext.deleteAppDownloadCache()
                    }
                    _logoutResponse.value = values
                }
            } else {
                _logoutResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    fun resetResponse(){
        _profileResponse.value = NetworkResult.NoCallYet()
        _logoutResponse.value = NetworkResult.NoCallYet()
    }

}