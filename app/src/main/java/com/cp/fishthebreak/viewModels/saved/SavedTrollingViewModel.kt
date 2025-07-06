package com.cp.fishthebreak.viewModels.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.saved.SavedTrollingClickListener
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.points.SavePointsModel
import com.cp.fishthebreak.models.trolling.GetAllTrollingModel
import com.cp.fishthebreak.models.trolling.TrollingResponseData
import com.cp.fishthebreak.models.trolling.TrollingResponseModel
import com.cp.fishthebreak.utils.MapUiData
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
class SavedTrollingViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), SavedTrollingClickListener {

    private val _trollingResponse: MutableStateFlow<NetworkResult<GetAllTrollingModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val trollingResponse: StateFlow<NetworkResult<GetAllTrollingModel>> = _trollingResponse.asStateFlow()

    private val _deleteResponse: MutableStateFlow<NetworkResult<TrollingResponseModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val deleteResponse: StateFlow<NetworkResult<TrollingResponseModel>> = _deleteResponse.asStateFlow()

    private val _singleTrollingResponse: MutableStateFlow<NetworkResult<TrollingResponseModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val singleTrollingResponse: StateFlow<NetworkResult<TrollingResponseModel>> = _singleTrollingResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _nextPage: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val nextPage: StateFlow<Int?> = _nextPage.asStateFlow()

    private val _list: MutableStateFlow<List<TrollingResponseData>> = MutableStateFlow(ArrayList())
    val list: StateFlow<List<TrollingResponseData>> = _list.asStateFlow()

    private val _currentPage: MutableStateFlow<Int> = MutableStateFlow(1)

    private val _notifyAdapter: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val notifyAdapter: StateFlow<Boolean> = _notifyAdapter.asStateFlow()

    private val _searchText: MutableStateFlow<String> =
        MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _filterType: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val filterType: StateFlow<Int?> = _filterType.asStateFlow()

    private val _navigationResponse: MutableStateFlow<NavigationDirections?> =
        MutableStateFlow(null)
    val navigationResponse: StateFlow<NavigationDirections?> = _navigationResponse.asStateFlow()

    fun onFilterChangeEvent(type: Int?) {
        _filterType.value = type
    }
    fun onSearchChangeEvent(name: String) {
        _searchText.value = name
    }

    init {
        getAllSavedTrolling(1)
    }

    fun getAllSavedTrolling(page: Int? = null) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            if(page == null){
                _currentPage.value =  _currentPage.value + 1
            }else{
                _currentPage.value = 1
            }
            repository.getAllTrolling(10,_currentPage.value,_searchText.value, _filterType.value).collect { values ->
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
                _trollingResponse.value = values
                _loadingResponse.value = false
            }
        } else {
            _loadingResponse.value = false
            _trollingResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun deleteLocations(id: Int) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _deleteResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", id)
            repository.deleteTrollingById(jsonObject).collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200){
                    val data = _list.value as ArrayList
                    data.removeIf{item-> item.id == id}
                    _list.value = if(data.isEmpty()){
                        emptyList()
                    }else{
                        data
                    }
                }
                _deleteResponse.value = values
            }
        } else {
            _deleteResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }
    fun updateTrolling(data: TrollingResponseData, name: String) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _deleteResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", data.id)
            jsonObject.addProperty("trolling_name", name)
            repository.updateTrolling(jsonObject).collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200){
                    data.trolling_name = name
                }
                _deleteResponse.value = values
            }
        } else {
            _deleteResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun getTrollingById(id: Int) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _singleTrollingResponse.value = NetworkResult.Loading()
            repository.getTrollingById(id).collect { values ->
                _singleTrollingResponse.value = values
            }
        } else {
            _singleTrollingResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetResponse() {
        _trollingResponse.value = NetworkResult.NoCallYet()
        _singleTrollingResponse.value = NetworkResult.NoCallYet()
        _deleteResponse.value = NetworkResult.NoCallYet()
    }

    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }

    override fun onViewClick(data: TrollingResponseData) {
        _navigationResponse.value = NavigationDirections.CommonMap(MapUiData.TrollingData(data))
    }

    override fun onEditClick(data: TrollingResponseData) {
        _navigationResponse.value = NavigationDirections.ViewPoints(MapUiData.TrollingData(data))
    }

    override fun onDeleteClick(data: TrollingResponseData) {
        _navigationResponse.value = NavigationDirections.DeletePoints(MapUiData.TrollingData(data))
    }

    override fun onShareClick(data: TrollingResponseData) {
        _navigationResponse.value = NavigationDirections.Share(MapUiData.TrollingData(data))
    }

    fun resetNavigationResponse() {
        _navigationResponse.value = null
    }

}