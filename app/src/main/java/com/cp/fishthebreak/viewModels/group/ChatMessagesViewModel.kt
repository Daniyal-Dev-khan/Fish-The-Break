package com.cp.fishthebreak.viewModels.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.OnChatClickListeners
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.group.BookMarkModel
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.models.group.ChatListModel
import com.cp.fishthebreak.models.group.GroupDetailsData
import com.cp.fishthebreak.models.group.GroupDetailsModel
import com.cp.fishthebreak.models.group.GroupMember
import com.cp.fishthebreak.models.group.LeaveGroupModel
import com.cp.fishthebreak.models.group.Message
import com.cp.fishthebreak.screens.fragments.group.GroupDetailsFragmentArgs
import com.cp.fishthebreak.utils.Constants.Companion.MESSAGE_PAGINATION_LOAD
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Arrays
import javax.inject.Inject

@HiltViewModel
class ChatMessagesViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val state: SavedStateHandle,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private val _chatListResponse: MutableStateFlow<NetworkResult<GroupDetailsModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val chatListResponse: StateFlow<NetworkResult<GroupDetailsModel>> =
        _chatListResponse.asStateFlow()

    private val _bookMarkResponse: MutableStateFlow<NetworkResult<BookMarkModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val bookMarkResponse: StateFlow<NetworkResult<BookMarkModel>> = _bookMarkResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _list: MutableStateFlow<ArrayList<Message>> = MutableStateFlow(ArrayList())
    val list: StateFlow<ArrayList<Message>> = _list.asStateFlow()

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

    //Get the value of the userId here
    //val userId = state.get<String>("userId")

    //Also you can do this using safe args
    val navArgs = GroupDetailsFragmentArgs.fromSavedStateHandle(state)

    private val _leaveGroupResponse: MutableStateFlow<NetworkResult<LeaveGroupModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val leaveGroupResponse: StateFlow<NetworkResult<LeaveGroupModel>> =
        _leaveGroupResponse.asStateFlow()


    init {
        getAllChats(1)
    }

    fun getAllChats(page: Int? = null) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            if (page == null) {
                _currentPage.value = _currentPage.value + 1
            } else {
                _currentPage.value = 1
            }
            repository.getGroupChat(
                MESSAGE_PAGINATION_LOAD,
                _currentPage.value,
                navArgs.chatData.id
            ).collect { values ->
                if (values.data?.status == true && values.data.statusCode == 200) {
                    if (page != 1) {
                        _list.value.addAll(0, values.data.data.reversed())
                    } else {
                        _list.value.clear()
                        _list.value.addAll(values.data.data.reversed())
                    }
                    if (_currentPage.value >= (values.data.last_page ?: 0)) {
                        _nextPage.value = null
                    } else {
                        _nextPage.value = values.data.last_page
                    }
                }
                _chatListResponse.value = values
                _notifyAdapter.value = true
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

    fun bookMarkChat(data: Message) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _bookMarkResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("shareable_id", data.shareable_id)
            jsonObject.addProperty("shareable_type", data.shareable_type)
            repository.bookmarkChat(jsonObject).collect { values ->
                if(values.data?.status == true && values.data.data?.saved == true){
                    data.bookmarked = !data.bookmarked
                }
                _bookMarkResponse.value = values
            }
        } else {
            _chatListResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun leaveGroup() = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _leaveGroupResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", navArgs.chatData.id?:0)
            repository.leaveGroup(jsonObject).collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200 && values.data.data?.leaved == true){

                }else{
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

    fun resetResponse() {
        _chatListResponse.value = NetworkResult.NoCallYet()
        _leaveGroupResponse.value = NetworkResult.NoCallYet()
    }
    fun resetBookMarkResponse() {
        _bookMarkResponse.value = NetworkResult.NoCallYet()
    }

    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }

    fun resetNavigationResponse() {
        _navigationResponse.value = null
    }

}