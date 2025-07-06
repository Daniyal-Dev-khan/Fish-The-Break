package com.cp.fishthebreak.viewModels.group.update

import android.app.Application
import android.view.View
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.models.group.AddMembersModel
import com.cp.fishthebreak.models.group.GroupMember
import com.cp.fishthebreak.models.group.GroupMemberModel
import com.cp.fishthebreak.models.group.LeaveGroupModel
import com.cp.fishthebreak.models.group.UpdateGroupModel
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
class GroupInfoViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val state: SavedStateHandle,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private val _groupMemberResponse: MutableStateFlow<NetworkResult<GroupMemberModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val groupMemberResponse: StateFlow<NetworkResult<GroupMemberModel>> = _groupMemberResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _list: MutableStateFlow<ArrayList<GroupMember>> = MutableStateFlow(ArrayList())
    val list: StateFlow<ArrayList<GroupMember>> = _list.asStateFlow()

    private val _nextPage: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val nextPage: StateFlow<Int?> = _nextPage.asStateFlow()

    private val _currentPage: MutableStateFlow<Int> = MutableStateFlow(1)

    private val _notifyAdapter: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val notifyAdapter: StateFlow<Boolean> = _notifyAdapter.asStateFlow()

    private val _selectedList: MutableStateFlow<List<User>> = MutableStateFlow(ArrayList())
    val selectedList: StateFlow<List<User>> = _selectedList.asStateFlow()

    private val _searchText: MutableStateFlow<String> =
        MutableStateFlow("")

    private var groupInfoUIState = MutableStateFlow(GroupInfoUIState())
    val groupInfoUIStates = groupInfoUIState.asStateFlow()

    private val _updateGroupResponse: MutableStateFlow<NetworkResult<UpdateGroupModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val updateGroupResponse: StateFlow<NetworkResult<UpdateGroupModel>> =
        _updateGroupResponse.asStateFlow()

    private val _leaveGroupResponse: MutableStateFlow<NetworkResult<LeaveGroupModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val leaveGroupResponse: StateFlow<NetworkResult<LeaveGroupModel>> =
        _leaveGroupResponse.asStateFlow()

    private val _addMembersResponse: MutableStateFlow<NetworkResult<AddMembersModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val addMembersResponse: StateFlow<NetworkResult<AddMembersModel>> =
        _addMembersResponse.asStateFlow()

    //Also you can do this using safe args
    val navArgs = GroupInfoFragmentArgs.fromSavedStateHandle(state)

    init {
        loadData()
    }

    fun loadData(){
        onNameChangeEvent(navArgs.data.name?:"")
        onImageChangeEvent(navArgs.data.profile_pic?:"")
        onGroupIdChangeEvent(navArgs.data.id)
        getAllUser(1)
    }

    fun onNameChangeEvent(name: CharSequence) {
        onEvent(GroupInfoUIEvent.NameChanged(name.toString()))
    }

    fun onImageChangeEvent(image: String) {
        onEvent(GroupInfoUIEvent.ImageChanged(image))
    }

    fun onGroupIdChangeEvent(id: Int) {
        onEvent(GroupInfoUIEvent.GroupIdChanged(id))
    }

    fun onSaveClickEvent(view: View?) {
        onEvent(GroupInfoUIEvent.SaveButtonClicked, view)
    }

    private fun onEvent(event: GroupInfoUIEvent, view: View? = null) {
        when (event) {
            is GroupInfoUIEvent.NameChanged -> {
                groupInfoUIState.value = groupInfoUIState.value.copy(
                    name = event.name
                )
                groupInfoUIState.value = groupInfoUIState.value.copy(
                    nameError = Validator.validateText(
                        text = groupInfoUIState.value.name
                    ).status
                )
            }

            is GroupInfoUIEvent.ImageChanged -> {
                groupInfoUIState.value = groupInfoUIState.value.copy(
                    image = event.image
                )
            }

            is GroupInfoUIEvent.GroupIdChanged -> {
                groupInfoUIState.value = groupInfoUIState.value.copy(
                    groupId = event.id
                )
            }

            is GroupInfoUIEvent.SaveButtonClicked -> {
                updateGroup(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateUIDataWithRules(): Boolean {
        val nameResult = Validator.validateText(
            text = groupInfoUIState.value.name
        )

        groupInfoUIState.value = groupInfoUIState.value.copy(
            nameError = nameResult.status
        )

        return nameResult.status
    }

    private fun updateGroup(view: View?) = viewModelScope.launch {
        if (validateUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _updateGroupResponse.value = NetworkResult.Loading()
                if(groupInfoUIState.value.image.isEmpty() || groupInfoUIState.value.image == (navArgs.data.profile_pic?:"")) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("name", groupInfoUIState.value.name)
                    jsonObject.addProperty("id", groupInfoUIState.value.groupId?:0)
                    repository.updateGroup(jsonObject).collect { values ->
                        _updateGroupResponse.value = values
                    }
                }else{
                    val multipartBody: MultipartBody.Part? =
                        if (!URLUtil.isValidUrl(groupInfoUIState.value.image)) {
                            val file: File? = File(groupInfoUIState.value.image)
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
                    repository.updateGroup(
                        groupInfoUIState.value.name.toRequestBody(),
                        (groupInfoUIState.value.groupId?:0).toString().toRequestBody(),
                        multipartBody
                    ).collect { values ->
                        _updateGroupResponse.value = values
                    }
                }
            } else {
                _updateGroupResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )
            }
        }
    }
    private fun leaveGroup(position: Int, data: GroupMember) = viewModelScope.launch {
        if (validateUIDataWithRules()) {
            if (applicationContext.isNetworkAvailable()) {
                _leaveGroupResponse.value = NetworkResult.Loading()
                val jsonObject = JsonObject()
                if(data.is_admin != 1) {
                    jsonObject.addProperty("user_id", data.user_id)
                }
                jsonObject.addProperty("id", groupInfoUIState.value.groupId?:0)
                repository.leaveGroup(jsonObject).collect { values ->
                    if(values.data?.status == true && values.data.statusCode == 200 && values.data.data?.leaved == true){
                        _notifyAdapter.value = true
                    }else{
                        _list.value.add(position,data)
                        _notifyAdapter.value = true
                    }
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
    }

    fun addMembers(users: List<User>) = viewModelScope.launch {
        if (validateUIDataWithRules()) {
            if (applicationContext.isNetworkAvailable()) {
                _addMembersResponse.value = NetworkResult.Loading()
                val list: ArrayList<Int> = ArrayList()
                users.forEach { user->
                    list.add(user.id)
                }
                val jsonObject = JsonObject()
                jsonObject.addProperty("id", groupInfoUIState.value.groupId?:0)
                jsonObject.add(
                    "user_id", Gson().toJsonTree(
                        Arrays.toString(list.toArray())
                    )
                )
                repository.addMembersInGroup(jsonObject).collect { values ->
                    if(values.data?.status == true && values.data.statusCode == 200 && values.data.data?.added_group_member== true){
                        getAllUser(1)
                    }
                    _addMembersResponse.value = values
                }
            } else {
                _addMembersResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )
            }
        }
    }

    fun getAllUser(page: Int? = null) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            if(page == null){
                _currentPage.value =  _currentPage.value + 1
            }else{
                _currentPage.value = 1
            }
            repository.getGroupUsers(10,_currentPage.value,groupInfoUIState.value.groupId?:0).collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200) {
                    if (page != 1){
                        values.data.data.addAll(0,_list.value)
                    }
                    if(_currentPage.value >= (values.data.last_page?:0)){
                        _nextPage.value = null
                    }else{
                        _nextPage.value = values.data.last_page
                    }
                    _list.value = values.data.data
                }
                _groupMemberResponse.value = values
                _loadingResponse.value = false
                _notifyAdapter.value = true
            }
        } else {
            _loadingResponse.value = false
            _groupMemberResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetResponse() {
        _groupMemberResponse.value = NetworkResult.NoCallYet()
        _addMembersResponse.value = NetworkResult.NoCallYet()
        _leaveGroupResponse.value = NetworkResult.NoCallYet()
        _updateGroupResponse.value = NetworkResult.NoCallYet()
    }
    fun resetList() {
        _list.value = ArrayList()
        _selectedList.value = ArrayList()
        _searchText.value = ""
    }

    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }

    fun removeUser(position: Int){
        val data = _list.value[position]
        _list.value.removeAt(position)
        leaveGroup(position, data)
    }

}