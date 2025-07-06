package com.cp.fishthebreak.viewModels.profile

import android.app.Application
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.di.local.AppDatabase
import com.cp.fishthebreak.di.local.LocalRepository
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor
    (
    private val repository: Repository,
    private val localDb: AppDatabase,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

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


    init {
        getProfile()
    }

    private fun initUserFromLocalStorage(): NetworkResult<RegisterModel> {
        val user = preference.getUser()
        return if (user != null) {
            NetworkResult.Success(RegisterModel(preference.getUser(), "", true, 200))
        } else {
            NetworkResult.NoCallYet()
        }
    }

    fun loadUserFromLocalStorage() {
        val user = preference.getUser()
        _profileResponse.value = if (user != null) {
            NetworkResult.Success(RegisterModel(preference.getUser(), "", true, 200))
        } else {
            NetworkResult.NoCallYet()
        }
    }

    fun getProfile() = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            repository.getProfile().collect { values ->
                if (values.data?.status == true && values.data.statusCode == 200) {
                    preference.saveUser(values.data.data)
                }
                _profileResponse.value = values
                _loadingResponse.value = false
            }
        } else {
            _loadingResponse.value = false
            _profileResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }

    fun logout() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            if (applicationContext.isNetworkAvailable()) {
                _loadingResponse.value = true
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
                    _loadingResponse.value = false
                }
            } else {
                _loadingResponse.value = false
                _logoutResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    fun deleteAccount() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            if (applicationContext.isNetworkAvailable()) {
                _deleteResponse.value = NetworkResult.Loading()
                repository.deleteAccount().collect { values ->
                    if (values.data?.status == true && values.data.statusCode == 200) {
                        preference.clearAllPreferenceData()
                        preference.isFirstLaunch(false)
                        localDb.clearAllTables()
                        applicationContext.deleteAppCache()
                        applicationContext.deleteAppDownloadCache()
                    }
                    _deleteResponse.value = values
                }
            } else {
                _deleteResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    fun updateProfilePic(imagePath: String) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            val multipartBody: MultipartBody.Part? = if (!URLUtil.isValidUrl(imagePath)) {
//                val file = if(isNeedToCompressImage(imagePath)){
//                    val bitmap = BitmapFactory.decodeFile(imagePath)
//                    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//                    val imageFileName = "JPEG_" + timeStamp + "_"
//                    val sd_main = File(Environment.getExternalStorageDirectory(), "/Pictures")
//                    val newFile = bitmap.compressMyImage(sd_main,imageFileName)
//                    val oldExif = ExifInterface(imagePath)
//                    val exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION)
//                    if (exifOrientation != null) {
//                        val newExif = ExifInterface(newFile?.path?:"")
//                        newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
//                        newExif.saveAttributes()
//                    }
//                    newFile
//                }else{
//                    File(imagePath)
//                }
                val file: File? = File(imagePath)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        "profile_pic", file.name, requestFile
                    )
                } else {
                    null
                }
            } else
                null

            repository.editProfile(
                multipartBody
            ).collect { values ->
                if (values.data?.status == true && values.data.statusCode == 200) {
                    preference.saveUser(values.data.data)
                }
                _profileResponse.value = values
                _loadingResponse.value = false
            }
        } else {
            _profileResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
            _loadingResponse.value = false
        }
    }

    fun resetResponse() {
//        loadUserFromLocalStorage()
        _profileResponse.value = NetworkResult.NoCallYet()
    }

}