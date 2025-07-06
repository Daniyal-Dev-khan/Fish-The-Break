package com.cp.fishthebreak.viewModels.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.home.GetSearchModel
import com.cp.fishthebreak.models.home.SaveSearchModel
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.screens.fragments.home.SearchLocationFragmentArgs
import com.cp.fishthebreak.utils.convertCoordinatesToLatLng
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.isValidLatLangNauticalFormat
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchLocationViewModel @Inject constructor
    (
    private val repository: Repository,
    private val state: SavedStateHandle,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext){

    private val _saveHistoryResponse: MutableStateFlow<NetworkResult<SaveSearchModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val saveHistoryResponse: StateFlow<NetworkResult<SaveSearchModel>> = _saveHistoryResponse.asStateFlow()


    private val _searchHistoryResponse: MutableStateFlow<NetworkResult<GetSearchModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val searchHistoryResponse: StateFlow<NetworkResult<GetSearchModel>> = _searchHistoryResponse.asStateFlow()

    private val _loadingResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(true)
    val loadingResponse: StateFlow<Boolean> = _loadingResponse.asStateFlow()

    private val _nextPage: MutableStateFlow<Int?> =
        MutableStateFlow(null)
    val nextPage: StateFlow<Int?> = _nextPage.asStateFlow()

    private val _list: MutableStateFlow<List<SearchData>> = MutableStateFlow(ArrayList())
    val list: StateFlow<List<SearchData>> = _list.asStateFlow()

    private val _currentPage: MutableStateFlow<Int> = MutableStateFlow(1)
    val currentPage: StateFlow<Int?> = _currentPage.asStateFlow()

    private val _notifyAdapter: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val notifyAdapter: StateFlow<Boolean> = _notifyAdapter.asStateFlow()

    val navArgs = SearchLocationFragmentArgs.fromSavedStateHandle(state)

    init {
        getSearchHistory("", type = 1, page = 1)
    }

    fun getSearchHistory(searchText: String, type: Int, page: Int? = null) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            if(page == null){
                _currentPage.value =  _currentPage.value + 1
            }else{
                _currentPage.value = 1
            }
            _loadingResponse.value = true
            val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
            jsonObject.addProperty("limit", 20)
            jsonObject.addProperty("page", _currentPage.value)
            if(navArgs.isFromRoute && type != 1){
                jsonObject.addProperty("custom_search", true)
            }
//            if(searchText.isValidLatLang()){
            if(searchText.isValidLatLangNauticalFormat()){
                val latLng = convertCoordinatesToLatLng(searchText.trim())
                if (latLng != null) {
                    val (latitude, longitude) = latLng
                    jsonObject.addProperty("latitude", latitude.toString())
                    jsonObject.addProperty("longitude", longitude.toString())
                } else{
                    jsonObject.addProperty("search_text", searchText)
                }
                /*
                 val latLang = searchText.split(",")
                if(latLang.size == 2){
                    jsonObject.addProperty("latitude", latLang.first().trim())
                    jsonObject.addProperty("longitude", latLang[1].trim())
                }else{
                    jsonObject.addProperty("search_text", searchText)
                }
                 */
            }else{
                jsonObject.addProperty("search_text", searchText)
            }
            jsonObject.addProperty("searchable_type", type)
            repository.getSearchHistory(jsonObject).collect { values ->
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
                _loadingResponse.value = false
                _searchHistoryResponse.value = values
            }
        } else {
            _loadingResponse.value = false
            _searchHistoryResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun saveHistory(text: String, type: Int, id: Int?) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _saveHistoryResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
//            val digitsOnly = TextUtils.isDigitsOnly(email)
            jsonObject.addProperty("search_text", text.trim())
            jsonObject.addProperty("searchable_type", type)
            jsonObject.addProperty("searchable_id", id?:0)
            repository.saveSearch(jsonObject).collect { values ->
                _saveHistoryResponse.value = values
            }
        } else {
            _saveHistoryResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetResponse(){
        _searchHistoryResponse.value = NetworkResult.NoCallYet()
        _saveHistoryResponse.value = NetworkResult.NoCallYet()
    }

}