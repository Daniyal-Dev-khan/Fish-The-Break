package com.cp.fishthebreak.viewModels.group.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.OnChatClickListeners
import com.cp.fishthebreak.adapters.group.OnFindUserClickListeners
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.models.group.ChatListModel
import com.cp.fishthebreak.models.group.FindUsersModel
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindUserViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), OnFindUserClickListeners {

    private val _chatListResponse: MutableStateFlow<NetworkResult<FindUsersModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val chatListResponse: StateFlow<NetworkResult<FindUsersModel>> = _chatListResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _list: MutableStateFlow<List<User>> = MutableStateFlow(ArrayList())
    val list: StateFlow<List<User>> = _list.asStateFlow()

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

    private val _roomId: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    //val searchText: StateFlow<String> = _searchText.asStateFlow()
    
//    init {
//        getAllUser(1)
//    }

    fun onSearchChangeEvent(name: CharSequence) {
        _searchText.value = name.toString()
    }
    fun onRoomIdEvent(roomId: Int?) {
        _roomId.value = roomId
    }

    fun getAllUser(page: Int? = null) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            if(page == null){
                _currentPage.value =  _currentPage.value + 1
            }else{
                _currentPage.value = 1
            }
            repository.findUsers(20,_currentPage.value,_searchText.value,_roomId.value).collect { values ->
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
    }
    fun resetList() {
        _list.value = ArrayList()
        _selectedList.value = ArrayList()
        _roomId.value = null
        _searchText.value = ""
    }

    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }

    override fun onUserClick(data: User) {
        data.isSelected = !data.isSelected
//        if(data.isSelected){
//            _selectedList.value.add(data)
//        }else{
//            _selectedList.value.remove(data)
//        }
        _selectedList.value = _list.value.filter { item-> item.isSelected }
    }

    override fun onCrossClick(data: User) {
        data.isSelected = false
        _selectedList.value = _list.value.filter { item-> item.isSelected }
        _notifyAdapter.value = true
    }

}