package com.cp.fishthebreak.viewModels.trolling

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.routes.RouteClickListener
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.points.GetSavedPointsModel
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrollingLocationViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), RouteClickListener {

    private val _locationsResponse: MutableStateFlow<NetworkResult<GetSavedPointsModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val locationsResponse: StateFlow<NetworkResult<GetSavedPointsModel>> = _locationsResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _nextPage: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val nextPage: StateFlow<Int?> = _nextPage.asStateFlow()

    private val _list: MutableStateFlow<List<SavePointsData>> = MutableStateFlow(ArrayList())
    val list: StateFlow<List<SavePointsData>> = _list.asStateFlow()

    private val _currentPage: MutableStateFlow<Int> = MutableStateFlow(1)

    private val _selectUnselectResponse: MutableStateFlow<SavePointsData?> =
        MutableStateFlow(null)
    val selectUnselectResponse: StateFlow<SavePointsData?> = _selectUnselectResponse.asStateFlow()

    private val _notifyAdapter: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val notifyAdapter: StateFlow<Boolean> = _notifyAdapter.asStateFlow()

    private val _trollingId: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val trollingId: StateFlow<Int?> = _trollingId.asStateFlow()

    fun onTrollingIdChange(id: Int?){
        _trollingId.value = id
    }

    fun getAllSavedLocations(page: Int? = null) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            if(page == null){
                _currentPage.value =  _currentPage.value + 1
            }else{
                _currentPage.value = 1
            }
            repository.getSavedPoints("",10,_currentPage.value, null, _trollingId.value).collect { values ->
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
                _locationsResponse.value = values
                _loadingResponse.value = false
            }
        } else {
            _locationsResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetResponse() {
        _locationsResponse.value = NetworkResult.NoCallYet()
    }
    fun resetSelectResponse() {
        _selectUnselectResponse.value = null
    }

    fun resetAllSelection() {
        _list.value.forEach {
            it.isSelected = false
        }
    }
    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }

    override fun onSelectLocation(data: SavePointsData) {
        data.isSelected =  !data.isSelected
        _selectUnselectResponse.value = data
    }

    override fun onSelectFishLog(data: SaveFishLogData) {
    }

    fun removeSelection(data: SavePointsData){
        data.isSelected = false
        _notifyAdapter.value = true
    }
}