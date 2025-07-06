package com.cp.fishthebreak.viewModels.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.saved.SavedClickListener
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.points.GetSavedPointsModel
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.points.SavePointsModel
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
class SavedLocationViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), SavedClickListener {

    private val _locationsResponse: MutableStateFlow<NetworkResult<GetSavedPointsModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val locationsResponse: StateFlow<NetworkResult<GetSavedPointsModel>> = _locationsResponse.asStateFlow()

    private val _deleteResponse: MutableStateFlow<NetworkResult<SavePointsModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val deleteResponse: StateFlow<NetworkResult<SavePointsModel>> = _deleteResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _nextPage: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val nextPage: StateFlow<Int?> = _nextPage.asStateFlow()

    private val _list: MutableStateFlow<List<SavePointsData>> = MutableStateFlow(ArrayList())
    val list: StateFlow<List<SavePointsData>> = _list.asStateFlow()

    private val _currentPage: MutableStateFlow<Int> = MutableStateFlow(1)

    private val _notifyAdapter: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val notifyAdapter: StateFlow<Boolean> = _notifyAdapter.asStateFlow()

    private val _navigationResponse: MutableStateFlow<NavigationDirections?> =
        MutableStateFlow(null)
    val navigationResponse: StateFlow<NavigationDirections?> = _navigationResponse.asStateFlow()

    private val _searchText: MutableStateFlow<String> =
        MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _filterType: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val filterType: StateFlow<Int?> = _filterType.asStateFlow()

    fun onFilterChangeEvent(type: Int?) {
        _filterType.value = type
    }
    fun onSearchChangeEvent(name: String) {
        _searchText.value = name
    }
    init {
        getAllSavedLocations(1)
    }

    fun getAllSavedLocations(page: Int? = null) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _loadingResponse.value = true
            if(page == null){
                _currentPage.value =  _currentPage.value + 1
            }else{
                _currentPage.value = 1
            }
            repository.getSavedPoints(_searchText.value,10,_currentPage.value,_filterType.value,null).collect { values ->
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
            _loadingResponse.value = false
            _locationsResponse.value = NetworkResult.NoInternet(
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
            repository.deletePointsById(jsonObject).collect { values ->
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

    fun resetResponse() {
        _locationsResponse.value = NetworkResult.NoCallYet()
        _deleteResponse.value = NetworkResult.NoCallYet()
    }

    fun resetNotifyResponse() {
        _notifyAdapter.value = false
    }

    override fun onViewClick(data: SavePointsData) {
        _navigationResponse.value = NavigationDirections.ViewPoints(MapUiData.LocationData(data))
    }

    override fun onDeleteClick(data: SavePointsData) {
        _navigationResponse.value = NavigationDirections.DeletePoints(MapUiData.LocationData(data))
    }

    override fun onShareClick(data: SavePointsData) {
        _navigationResponse.value = NavigationDirections.Share(MapUiData.LocationData(data))
    }

    override fun onViewOnMapClick(data: SavePointsData) {
        _navigationResponse.value = NavigationDirections.CommonMap(MapUiData.LocationData(data))
    }

    fun resetNavigationResponse() {
        _navigationResponse.value = null
    }

}