package com.cp.fishthebreak.viewModels.group.create

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
import com.cp.fishthebreak.models.group.CreateGroupModel
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.profile.ChangeEmailModel
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.getMyDeviceId
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.rules.Validator
import com.google.gson.Gson
import com.google.gson.JsonArray
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
import java.util.Arrays
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var createGroupUIState = MutableStateFlow(CreateGroupUIState())
    val createGroupUIStates = createGroupUIState.asStateFlow()

    private val _createGroupResponse: MutableStateFlow<NetworkResult<CreateGroupModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val createGroupResponse: StateFlow<NetworkResult<CreateGroupModel>> =
        _createGroupResponse.asStateFlow()
    

    fun onNameChangeEvent(name: CharSequence) {
        onEvent(CreateGroupUIEvent.NameChanged(name.toString()))
    }

    fun onImageChangeEvent(image: String) {
        onEvent(CreateGroupUIEvent.ImageChanged(image))
    }

    fun onUserListChangeEvent(users: List<User>) {
        onEvent(CreateGroupUIEvent.UserListChanged(users))
    }

    fun onSaveClickEvent(view: View?) {
        onEvent(CreateGroupUIEvent.SaveButtonClicked, view)
    }

    private fun onEvent(event: CreateGroupUIEvent, view: View? = null) {
        when (event) {
            is CreateGroupUIEvent.NameChanged -> {
                createGroupUIState.value = createGroupUIState.value.copy(
                    name = event.name
                )
                createGroupUIState.value = createGroupUIState.value.copy(
                    nameError = Validator.validateText(
                        text = createGroupUIState.value.name
                    ).status
                )
            }
            
            is CreateGroupUIEvent.ImageChanged -> {
                createGroupUIState.value = createGroupUIState.value.copy(
                    image = event.image
                )
            }

            is CreateGroupUIEvent.UserListChanged -> {
                createGroupUIState.value = createGroupUIState.value.copy(
                    users = event.users
                )
                createGroupUIState.value = createGroupUIState.value.copy(
                    usersError = Validator.validateList(
                        list = createGroupUIState.value.users
                    ).status
                )
            }

            is CreateGroupUIEvent.SaveButtonClicked -> {
                createGroup(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateUIDataWithRules(): Boolean {
        val nameResult = Validator.validateText(
            text = createGroupUIState.value.name
        )
        val usersResult = Validator.validateList(
            list = createGroupUIState.value.users
        )

        createGroupUIState.value = createGroupUIState.value.copy(
            nameError = nameResult.status,
            usersError = usersResult.status
        )
        
        return nameResult.status && usersResult.status
    }

    private fun createGroup(view: View?) = viewModelScope.launch {
        if (validateUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _createGroupResponse.value = NetworkResult.Loading()
                val list: ArrayList<Int> = ArrayList()
                createGroupUIState.value.users.forEach { user ->
                    list.add(user.id)
                }
                if(createGroupUIState.value.image.isEmpty()) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("name", createGroupUIState.value.name)
                    jsonObject.add(
                        "user_id", Gson().toJsonTree(
                            Arrays.toString(list.toArray())
                        )
                    )
                    repository.createGroup(jsonObject).collect { values ->
                        _createGroupResponse.value = values
                    }
                }else{
                    val multipartBody: MultipartBody.Part? =
                        if (!URLUtil.isValidUrl(createGroupUIState.value.image)) {
                            val file: File? = File(createGroupUIState.value.image)
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
                    repository.createGroup(
                        createGroupUIState.value.name.toRequestBody(),
                        Arrays.toString(list.toArray()).toRequestBody(),
                        multipartBody
                    ).collect { values ->
                        _createGroupResponse.value = values
                    }
                }
            } else {
                _createGroupResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )
            }
        }
    }
    

    fun resetResponse() {
        _createGroupResponse.value = NetworkResult.NoCallYet()
    }

}