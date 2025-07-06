package com.cp.fishthebreak.viewModels.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.OnChatClickListeners
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.models.group.ChatListModel
import com.cp.fishthebreak.models.group.GroupMember
import com.cp.fishthebreak.models.group.RequestAcceptModel
import com.cp.fishthebreak.models.group.RequestRejectModel
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), OnChatClickListeners {

    private val _chatListResponse: MutableStateFlow<NetworkResult<ChatListModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val chatListResponse: StateFlow<NetworkResult<ChatListModel>> = _chatListResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _list: MutableStateFlow<ArrayList<ChatListData>> = MutableStateFlow(ArrayList())
    val list: StateFlow<ArrayList<ChatListData>> = _list.asStateFlow()

    private val _nextPage: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val nextPage: StateFlow<Int?> = _nextPage.asStateFlow()

    private val _currentPage: MutableStateFlow<Int> = MutableStateFlow(1)

    private val _notifyAdapter: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val notifyAdapter: StateFlow<Boolean> = _notifyAdapter.asStateFlow()

    private val _navigationResponse: MutableStateFlow<NavigationDirections?> =
        MutableStateFlow(null)
    val navigationResponse: StateFlow<NavigationDirections?> = _navigationResponse.asStateFlow()


    private val _acceptResponse: MutableStateFlow<NetworkResult<RequestAcceptModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val acceptResponse: StateFlow<NetworkResult<RequestAcceptModel>> = _acceptResponse.asStateFlow()

    private val _rejectResponse: MutableStateFlow<NetworkResult<RequestRejectModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val rejectResponse: StateFlow<NetworkResult<RequestRejectModel>> = _rejectResponse.asStateFlow()


    init {
        getAllGroups(1)
    }

    fun getAllGroups(page: Int? = null) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            if(page == null){
                _currentPage.value =  _currentPage.value + 1
            }else{
                _currentPage.value = 1
            }
            repository.getGroupList(20,_currentPage.value).collect { values ->
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
                _chatListResponse.value = values
                _loadingResponse.value = false
            }
        } else {
            _loadingResponse.value = false
            _chatListResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetResponse() {
        _chatListResponse.value = NetworkResult.NoCallYet()
        _acceptResponse.value = NetworkResult.NoCallYet()
        _rejectResponse.value = NetworkResult.NoCallYet()
    }

    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }
    fun resetNavigationResponse() {
        _navigationResponse.value = null
    }

    override fun onChatClick(data: ChatListData) {
        _navigationResponse.value = NavigationDirections.OpenGroup(data)
    }

    private fun acceptRequest(data: ChatListData) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _acceptResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("room_id", data.id)
            repository.acceptRequest(jsonObject).collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200 && values.data.data?.accepted == true){
                    data.status = 1
                    _notifyAdapter.value = true
                }
                _acceptResponse.value = values
            }
        } else {
            _acceptResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }

    fun rejectRequest(data: ChatListData, reason: String) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _rejectResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("room_id", data.id)
            jsonObject.addProperty("reason", reason)
            repository.rejectRequest(jsonObject).collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200 && values.data.data?.rejected == true){
                    _list.value.remove(data)
                    _notifyAdapter.value = true
                }
                _rejectResponse.value = values
            }
        } else {
            _rejectResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }


    override fun onAcceptClick(data: ChatListData) {
        acceptRequest(data)
    }

    override fun onRejectClick(data: ChatListData) {
        _navigationResponse.value = NavigationDirections.RejectReason(data)
        //rejectRequest(data)
    }

    fun navigateToGroupChat(data: ChatListData){
        _navigationResponse.value = NavigationDirections.OpenGroup(data)
    }

}