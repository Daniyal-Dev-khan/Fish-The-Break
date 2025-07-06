package com.cp.fishthebreak.viewModels.profile.edit

import android.app.Application
import android.view.View
import android.webkit.URLUtil
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
import com.cp.fishthebreak.utils.rules.ValidationResult
import com.cp.fishthebreak.utils.rules.Validator
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var editProfileUIState = MutableStateFlow(EditProfileUIState())
    val editProfileUIStates = editProfileUIState.asStateFlow()


    private val _editProfileResponse: MutableStateFlow<NetworkResult<RegisterModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val editProfileResponse: StateFlow<NetworkResult<RegisterModel>> =
        _editProfileResponse.asStateFlow()

    private val _userResponse: MutableStateFlow<User?> =
        MutableStateFlow(loadUserFromLocalStorage())
    val userResponse: StateFlow<User?> = _userResponse.asStateFlow()

    private val _userNameError: MutableStateFlow<String> =
        MutableStateFlow(applicationContext.resources.getString(R.string.error_invalid_username))
    val userNameError: StateFlow<String> = _userNameError.asStateFlow()

    private fun loadUserFromLocalStorage(): User? {
        val user = preference.getUser()
        onFirstNameEvent(user?.first_name ?: "")
        onLastNameEvent(user?.last_name ?: "")
        onUsernameEvent(user?.username ?: "")
        onPhoneEvent(user?.mobile ?: "")
        return user
    }

    fun onFirstNameEvent(firstName: CharSequence) {
        onEvent(EditProfileUIEvent.FirstNameChanged(firstName.toString()))
    }

    fun onLastNameEvent(lastName: CharSequence) {
        onEvent(EditProfileUIEvent.LastNameChanged(lastName.toString()))
    }

    fun onUsernameEvent(username: CharSequence) {
        onEvent(EditProfileUIEvent.UserNameChanged(username.toString()))
    }

    fun onPhoneEvent(mobile: CharSequence) {
        onEvent(EditProfileUIEvent.MobileChanged(mobile.toString()))
    }

    fun onPhoneError(status: Boolean) {
        editProfileUIState.value = editProfileUIState.value.copy(
            mobileError = status
        )
    }

    fun onUserImageEvent(userImage: String) {
        onEvent(EditProfileUIEvent.UserImageChanged(userImage))
    }

    fun onSaveClickEvent(view: View?) {
        onEvent(EditProfileUIEvent.SaveButtonClicked, view)
    }

    private fun onEvent(event: EditProfileUIEvent, view: View? = null) {
        when (event) {
            is EditProfileUIEvent.FirstNameChanged -> {
                editProfileUIState.value = editProfileUIState.value.copy(
                    firstName = event.firstName
                )
                editProfileUIState.value = editProfileUIState.value.copy(
                    firstNameError = Validator.validateFirstName(
                        fName = editProfileUIState.value.firstName
                    ).status
                )
            }

            is EditProfileUIEvent.LastNameChanged -> {
                editProfileUIState.value = editProfileUIState.value.copy(
                    lastName = event.lastName
                )
                editProfileUIState.value = editProfileUIState.value.copy(
                    lastNameError = Validator.validateLastName(
                        lName = editProfileUIState.value.lastName
                    ).status
                )
            }

            is EditProfileUIEvent.UserNameChanged -> {
                editProfileUIState.value = editProfileUIState.value.copy(
                    userName = event.username
                )
                editProfileUIState.value = editProfileUIState.value.copy(
                    userNameError = Validator.validateUserName(
                        username = editProfileUIState.value.userName
                    ).status
                )
                if (!Validator.validateUserName(username = editProfileUIState.value.userName).status) {
                    _userNameError.value =
                        applicationContext.resources.getString(R.string.error_invalid_username)
                    editProfileUIState.value = editProfileUIState.value.copy(
                        userNameError = false
                    )
                }
                if (Validator.validateEmail(email = editProfileUIState.value.userName).status) {
                    _userNameError.value =
                        applicationContext.resources.getString(R.string.error_invalid_username_email)
                    editProfileUIState.value = editProfileUIState.value.copy(
                        userNameError = false
                    )
                } else if (editProfileUIState.value.userName.contains("@")) {
                    _userNameError.value =
                        applicationContext.resources.getString(R.string.error_invalid_username_at_the_rate)
                    editProfileUIState.value = editProfileUIState.value.copy(
                        userNameError = false
                    )
                }
            }

            is EditProfileUIEvent.MobileChanged -> {
                editProfileUIState.value = editProfileUIState.value.copy(
                    mobile = event.mobile
                )
//                editProfileUIState.value = editProfileUIState.value.copy(
//                    mobileError = Validator.validatePhone(
//                        phone = editProfileUIState.value.mobile
//                    ).status
//                )
            }

            is EditProfileUIEvent.UserImageChanged -> {
                editProfileUIState.value = editProfileUIState.value.copy(
                    userImage = event.userImage
                )
                _userResponse.value?.profile_pic = event.userImage
            }


            is EditProfileUIEvent.SaveButtonClicked -> {
                updateProfile(view)
            }
        }
        //validateUIDataWithRules()
    }

    private fun validateUIDataWithRules(): Boolean {
        val firstNameResult = Validator.validateFirstName(
            fName = editProfileUIState.value.firstName
        )
        val lastNameResult = Validator.validateLastName(
            lName = editProfileUIState.value.lastName
        )
        var userNameResult = Validator.validateUserName(
            username = editProfileUIState.value.userName
        )
        val mobileResult = Validator.validatePhone(
            phone = editProfileUIState.value.mobile
        )
        if (!Validator.validateUserName(username = editProfileUIState.value.userName).status) {
            _userNameError.value =
                applicationContext.resources.getString(R.string.error_invalid_username)
            userNameResult = ValidationResult(status = false)
        }
        if (Validator.validateEmail(email = editProfileUIState.value.userName).status) {
            _userNameError.value =
                applicationContext.resources.getString(R.string.error_invalid_username_email)
            userNameResult = ValidationResult(status = false)
        } else if (editProfileUIState.value.userName.contains("@")) {
            _userNameError.value =
                applicationContext.resources.getString(R.string.error_invalid_username_at_the_rate)
            userNameResult = ValidationResult(status = false)
        }
        editProfileUIState.value = editProfileUIState.value.copy(
            firstNameError = firstNameResult.status,
            lastNameError = lastNameResult.status,
            userNameError = userNameResult.status,
            mobileError = mobileResult.status
        )
        return firstNameResult.status && lastNameResult.status && userNameResult.status && mobileResult.status

        //allValidationsPassed.value = emailResult.status && passwordResult.status

    }

    private fun updateProfile(view: View?) = viewModelScope.launch {
        if (validateUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                if (editProfileUIState.value.userImage.isEmpty()) {
                    val jsonObject = JsonObject()
                    var isDataChanged = false
                    if ((preference.getUser()?.first_name
                            ?: "") != editProfileUIState.value.firstName
                    ) {
                        jsonObject.addProperty("first_name", editProfileUIState.value.firstName)
                        isDataChanged = true
                    }
                    if ((preference.getUser()?.last_name
                            ?: "") != editProfileUIState.value.lastName
                    ) {
                        jsonObject.addProperty("last_name", editProfileUIState.value.lastName)
                        isDataChanged = true
                    }
                    if ((preference.getUser()?.username
                            ?: "") != editProfileUIState.value.userName
                    ) {
                        jsonObject.addProperty("username", editProfileUIState.value.userName)
                        isDataChanged = true
                    }
                    if ((preference.getUser()?.mobile ?: "") != editProfileUIState.value.mobile) {
                        jsonObject.addProperty("mobile", editProfileUIState.value.mobile)
                        isDataChanged = true
                    }
                    if (isDataChanged) {
                        _editProfileResponse.value = NetworkResult.Loading()
                        repository.editProfile(jsonObject).collect { values ->
                            if (values.data?.status == true && values.data.statusCode == 200) {
                                preference.saveUser(values.data.data)
                            }
                            _editProfileResponse.value = values
                        }
                    }
                } else {
                    _editProfileResponse.value = NetworkResult.Loading()
                    val multipartBody: MultipartBody.Part? =
                        if (!URLUtil.isValidUrl(editProfileUIState.value.userImage)) {
//                        val file = if(isNeedToCompressImage(editProfileUIState.value.userImage)){
//                            val bitmap = BitmapFactory.decodeFile(editProfileUIState.value.userImage)
//                            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//                            val imageFileName = "JPEG_" + timeStamp + "_"
//                            val sd_main = File(Environment.getExternalStorageDirectory(), "/Pictures")
//                            val newFile = bitmap.compressMyImage(sd_main,imageFileName)
//                            val oldExif = ExifInterface(editProfileUIState.value.userImage)
//                            val exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION)
//                            if (exifOrientation != null) {
//                                val newExif = ExifInterface(newFile?.path?:"")
//                                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
//                                newExif.saveAttributes()
//                            }
//                            newFile
//                        }else{
//                            File(editProfileUIState.value.userImage)
//                        }
                            val file: File? = File(editProfileUIState.value.userImage)
                            if (file != null) {
                                val requestFile =
                                    file.asRequestBody("image/png".toMediaTypeOrNull())
                                MultipartBody.Part.createFormData(
                                    "profile_pic", file.name, requestFile
                                )
                            } else {
                                null
                            }
                        } else
                            null

                    if ((preference.getUser()?.username
                            ?: "") != editProfileUIState.value.userName
                    ) {
                        repository.editProfile(
                            editProfileUIState.value.firstName.toRequestBody(),
                            editProfileUIState.value.lastName.toRequestBody(),
                            editProfileUIState.value.userName.toRequestBody(),
                            editProfileUIState.value.mobile.toRequestBody(),
                            multipartBody
                        ).collect { values ->
                            if (values.data?.status == true && values.data.statusCode == 200) {
                                preference.saveUser(values.data.data)
                            }
                            _editProfileResponse.value = values
                        }
                    } else {
                        repository.editProfile(
                            editProfileUIState.value.firstName.toRequestBody(),
                            editProfileUIState.value.lastName.toRequestBody(),
                            editProfileUIState.value.mobile.toRequestBody(),
                            multipartBody
                        ).collect { values ->
                            if (values.data?.status == true && values.data.statusCode == 200) {
                                preference.saveUser(values.data.data)
                            }
                            _editProfileResponse.value = values
                        }
                    }
                }
            } else {
                _editProfileResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }

    fun resetResponse() {
        _editProfileResponse.value = NetworkResult.NoCallYet()
    }

}