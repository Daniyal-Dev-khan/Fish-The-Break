package com.cp.fishthebreak.viewModels.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.layers.MapLayerListener
import com.cp.fishthebreak.adapters.layers.MapStyleListener
import com.cp.fishthebreak.adapters.profile.OfflineMapListener
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.map.GetAllLayerModel
import com.cp.fishthebreak.models.map.MapLayer
import com.cp.fishthebreak.models.map.MapStyle
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.models.profile.OfflineMapModel
import com.cp.fishthebreak.utils.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineMapViewModel @Inject constructor
    (
    private val repository: Repository,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), OfflineMapListener {

    private val _mapResponse: MutableStateFlow<ArrayList<OfflineMapModel>> =
        MutableStateFlow(ArrayList())
    val mapResponse: StateFlow<ArrayList<OfflineMapModel>> = _mapResponse.asStateFlow()


    init {
        val map = ArrayList<OfflineMapModel>()
        map.add(OfflineMapModel(id = 1, name = "Default", type = "", image = 1))
        map.add(OfflineMapModel(id = 2, name = "Satellite", type = "", image = 1))
        map.add(OfflineMapModel(id = 3, name = "Terrain", type = "", image = 1))
        map.add(OfflineMapModel(id = 4, name = "Traffic", type = "", image = 1))
        _mapResponse.value = map
    }

    fun resetResponse(){

    }


    override fun onMapDeleteClick(data: OfflineMap) {

    }

    override fun onMapClick(data: OfflineMap) {

    }

}