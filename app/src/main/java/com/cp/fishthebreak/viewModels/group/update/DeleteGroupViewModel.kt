package com.cp.fishthebreak.viewModels.group.update

import android.app.Application
import android.view.View
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.OnFindUserClickListeners
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.models.group.AddMembersModel
import com.cp.fishthebreak.models.group.CreateGroupModel
import com.cp.fishthebreak.models.group.FindUsersModel
import com.cp.fishthebreak.models.group.GroupMember
import com.cp.fishthebreak.models.group.GroupMemberModel
import com.cp.fishthebreak.models.group.LeaveGroupModel
import com.cp.fishthebreak.models.group.UpdateGroupModel
import com.cp.fishthebreak.screens.fragments.group.DeleteGroupFragmentArgs
import com.cp.fishthebreak.screens.fragments.group.GroupInfoFragmentArgs
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.rules.Validator
import com.google.gson.Gson
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
class DeleteGroupViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val state: SavedStateHandle,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private val _leaveGroupResponse: MutableStateFlow<NetworkResult<LeaveGroupModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val leaveGroupResponse: StateFlow<NetworkResult<LeaveGroupModel>> =
        _leaveGroupResponse.asStateFlow()


    //Also you can do this using safe args
    val navArgs = DeleteGroupFragmentArgs.fromSavedStateHandle(state)


    fun leaveGroup(roomId: Int) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _leaveGroupResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", roomId)
            repository.leaveGroup(jsonObject).collect { values ->
                _leaveGroupResponse.value = values
            }
        } else {
            _leaveGroupResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }


    fun resetResponse() {
        _leaveGroupResponse.value = NetworkResult.NoCallYet()
    }

}