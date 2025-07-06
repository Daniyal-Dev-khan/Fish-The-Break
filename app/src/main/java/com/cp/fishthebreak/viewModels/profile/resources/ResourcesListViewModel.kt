package com.cp.fishthebreak.viewModels.profile.resources

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.layers.MapLayerListener
import com.cp.fishthebreak.adapters.layers.MapStyleListener
import com.cp.fishthebreak.adapters.profile.ResourceDataClickListener
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.map.GetAllLayerModel
import com.cp.fishthebreak.models.map.MapLayer
import com.cp.fishthebreak.models.map.MapStyle
import com.cp.fishthebreak.models.profile.AllResourcesModel
import com.cp.fishthebreak.models.profile.ResourceData
import com.cp.fishthebreak.models.profile.ResourceDetails
import com.cp.fishthebreak.models.profile.ResourceListModel
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.esri.arcgisruntime.mapping.BasemapStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourcesListViewModel @Inject constructor
    (
    private val repository: Repository,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), ResourceDataClickListener {

    private val _resourcesResponse: MutableStateFlow<NetworkResult<ResourceListModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val resourcesResponse: StateFlow<NetworkResult<ResourceListModel>> =
        _resourcesResponse.asStateFlow()

    private val _navigationResponse: MutableStateFlow<NavigationDirections?> =
        MutableStateFlow(null)
    val navigationResponse: StateFlow<NavigationDirections?> = _navigationResponse.asStateFlow()

    private val _searchResponse: MutableStateFlow<List<ResourceDetails>> = MutableStateFlow(
        ArrayList()
    )
    val searchResponse: StateFlow<List<ResourceDetails>> = _searchResponse.asStateFlow()

    fun onSearchTextChange(text: CharSequence) {
        _resourcesResponse.value.data?.data?.resources?.let {
            if(text.toString().trim().isNotEmpty()) {
                _searchResponse.value = it.filter { item ->
                    item.title.trim().lowercase().contains(text.toString().trim().lowercase())
                }
            }else{
                _searchResponse.value = it
            }
        }

    }

    fun getResourceById(id: Int) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _resourcesResponse.value = NetworkResult.Loading()
            repository.getResourceListById(id).collect { values ->
                if (values.data?.status == true && values.data.statusCode == 200) {
                    _searchResponse.value = values.data.data.resources
                }
                _resourcesResponse.value = values
            }
        } else {
            _resourcesResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

    fun resetResponse() {
        _resourcesResponse.value = NetworkResult.NoCallYet()
    }

    fun resetNavigationResponse() {
        _navigationResponse.value = null
    }

    override fun onViewMoreClick(data: ResourceData) {

    }

    override fun onResourceClick(data: ResourceDetails) {
        _navigationResponse.value = NavigationDirections.ResourceScreen(data)
    }

}