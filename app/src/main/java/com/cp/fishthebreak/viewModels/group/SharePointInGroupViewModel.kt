package com.cp.fishthebreak.viewModels.group

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.OnFindUserClickListeners
import com.cp.fishthebreak.adapters.group.OnSelectGroupClickListeners
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.models.group.ChatListModel
import com.cp.fishthebreak.models.group.ShareInGroupModel
import com.cp.fishthebreak.utils.MapUiData
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
class SharePointInGroupViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), OnSelectGroupClickListeners {

    private val _groupListResponse: MutableStateFlow<NetworkResult<ChatListModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val groupListResponse: StateFlow<NetworkResult<ChatListModel>> = _groupListResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _list: MutableStateFlow<List<ChatListData>> = MutableStateFlow(ArrayList())
    val list: StateFlow<List<ChatListData>> = _list.asStateFlow()

    private val _selectedList: MutableStateFlow<List<ChatListData>> = MutableStateFlow(ArrayList())
    val selectedList: StateFlow<List<ChatListData>> = _selectedList.asStateFlow()

    private val _nextPage: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val nextPage: StateFlow<Int?> = _nextPage.asStateFlow()

    private val _currentPage: MutableStateFlow<Int> = MutableStateFlow(1)

    private val _notifyAdapter: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val notifyAdapter: StateFlow<Boolean> = _notifyAdapter.asStateFlow()

    private val _shareData: MutableStateFlow<NavigationDirections?> =
        MutableStateFlow(null)
    val shareData: StateFlow<NavigationDirections?> = _shareData.asStateFlow()


    private val _searchText: MutableStateFlow<String> =
        MutableStateFlow("")

    private val _shareResponse: MutableStateFlow<NetworkResult<ShareInGroupModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val shareResponse: StateFlow<NetworkResult<ShareInGroupModel>> = _shareResponse.asStateFlow()


    fun onShareDataChange(data: NavigationDirections){
        _shareData.value = data
    }

    fun onSearchChangeEvent(name: CharSequence) {
        _searchText.value = name.toString()
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
                _groupListResponse.value = values
                _loadingResponse.value = false
            }
        } else {
            _loadingResponse.value = false
            _groupListResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun shareInGroup(view: View) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _shareResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            val list = ArrayList<Int>()
            _selectedList.value.forEach { item->
                list.add(item.id)
            }
            jsonObject.add("room_id",  Gson().toJsonTree(
                Arrays.toString(list.toArray())
            ))
            //jsonObject.addProperty("room_id", _selectedList.value.first().id)
            when(_shareData.value){
                is NavigationDirections.Share->{
                    when((_shareData.value as NavigationDirections.Share).data){
                        is MapUiData.TrollingData->{
                            jsonObject.addProperty("shareable_type", "trolling")
                            jsonObject.addProperty("shareable_id", ((_shareData.value as NavigationDirections.Share).data as MapUiData.TrollingData).data.id)
                        }
                        is MapUiData.FishLogData -> {
                            jsonObject.addProperty("shareable_type", "fish_log")
                            jsonObject.addProperty("shareable_id", ((_shareData.value as NavigationDirections.Share).data as MapUiData.FishLogData).data.id)
                        }
                        is MapUiData.LocationData -> {
                            jsonObject.addProperty("shareable_type", "location")
                            jsonObject.addProperty("shareable_id", ((_shareData.value as NavigationDirections.Share).data as MapUiData.LocationData).data.id)
                        }
                        is MapUiData.RouteData -> {
                            jsonObject.addProperty("shareable_type", "route")
                            jsonObject.addProperty("shareable_id", ((_shareData.value as NavigationDirections.Share).data as MapUiData.RouteData).data.id)
                        }else->{}
                    }
                }
                else->{}
            }
            repository.sharePointInGroup(jsonObject).collect { values ->
                _shareResponse.value = values
            }
        } else {
            _shareResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }
    fun shareInGroup(groupId: Int, shareableType: String, pointId: Int) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _shareResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            val list = ArrayList<Int>()
            list.add(groupId)
            jsonObject.add("room_id",  Gson().toJsonTree(
                Arrays.toString(list.toArray())
            ))
            jsonObject.addProperty("shareable_type", shareableType)
            jsonObject.addProperty("shareable_id", pointId)
            repository.sharePointInGroup(jsonObject).collect { values ->
                _shareResponse.value = values
            }
        } else {
            _shareResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetResponse() {
        _groupListResponse.value = NetworkResult.NoCallYet()
        _shareResponse.value = NetworkResult.NoCallYet()
    }
    fun resetList() {
        _list.value = ArrayList()
        _searchText.value = ""
    }

    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }

    override fun onSelectClick(data: ChatListData) {
        data.isSelected = !data.isSelected
        _selectedList.value = _list.value.filter { item-> item.isSelected }
    }


}