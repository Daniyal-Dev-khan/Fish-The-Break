package com.cp.fishthebreak.viewModels.group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.OnChatClickListeners
import com.cp.fishthebreak.adapters.group.OnReasonClickListeners
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.models.group.ChatListModel
import com.cp.fishthebreak.models.group.GroupMember
import com.cp.fishthebreak.models.group.LeaveReasonData
import com.cp.fishthebreak.models.group.LeaveReasonModel
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
class ReasonViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), OnReasonClickListeners {

    private val _reasonResponse: MutableStateFlow<NetworkResult<LeaveReasonModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val reasonResponse: StateFlow<NetworkResult<LeaveReasonModel>> = _reasonResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _list: MutableStateFlow<List<LeaveReasonData>> = MutableStateFlow(ArrayList())
    val list: StateFlow<List<LeaveReasonData>> = _list.asStateFlow()

    private val _selected: MutableStateFlow<LeaveReasonData?> = MutableStateFlow(null)
    val selected: StateFlow<LeaveReasonData?> = _selected.asStateFlow()


    private val _notifyAdapter: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val notifyAdapter: StateFlow<Boolean> = _notifyAdapter.asStateFlow()


    init {
        getRoomLeavingReason()
    }

    fun getRoomLeavingReason() = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            repository.getRoomLeavingReason().collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200) {
                    _list.value = values.data.data
                }
                _reasonResponse.value = values
                _loadingResponse.value = false
            }
        } else {
            _loadingResponse.value = false
            _reasonResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetResponse() {
        _reasonResponse.value = NetworkResult.NoCallYet()
    }

    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }


    override fun onClick(data: LeaveReasonData) {
        _list.value.forEach {item->
            item.isSelected = false
        }
        data.isSelected = true
        _selected.value = data
        _notifyAdapter.value = true
    }

    fun clearSelection(){
        _list.value.forEach {item->
            item.isSelected = false
        }
    }
    fun restoreSelection(){
        _list.value.forEach {item->
            item.isSelected = false
        }
        _selected.value?.let { data->
            data.isSelected = true
        }
    }

}