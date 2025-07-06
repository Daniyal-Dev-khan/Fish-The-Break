package com.cp.fishthebreak.viewModels.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.layers.MapLayerListener
import com.cp.fishthebreak.adapters.layers.MapStyleListener
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.map.GetAllLayerModel
import com.cp.fishthebreak.models.map.LayersStatusModel
import com.cp.fishthebreak.models.map.MapLayer
import com.cp.fishthebreak.models.map.MapStyle
import com.cp.fishthebreak.models.map.WmsLayersStatusModel
import com.cp.fishthebreak.utils.Constants.Companion.Feature_Type
import com.cp.fishthebreak.utils.Constants.Companion.Self_Hosted_Type
import com.cp.fishthebreak.utils.Constants.Companion.TILE
import com.cp.fishthebreak.utils.Constants.Companion.WMS_TYPE
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.WebTiledLayer
import com.esri.arcgisruntime.layers.WmsLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.UserCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LayersViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext), MapStyleListener, MapLayerListener {

    private val _mapResponse: MutableStateFlow<ArrayList<MapStyle>> =
        MutableStateFlow(ArrayList())
    val mapResponse: StateFlow<ArrayList<MapStyle>> = _mapResponse.asStateFlow()


    private val _layersResponse: MutableStateFlow<NetworkResult<GetAllLayerModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val layersResponse: StateFlow<NetworkResult<GetAllLayerModel>> = _layersResponse.asStateFlow()

    private val _layerResponse: MutableStateFlow<List<MapLayer>> =
        MutableStateFlow(emptyList())
    val layerResponse: StateFlow<List<MapLayer>> = _layerResponse.asStateFlow()

    private val _layerToggleResponse: MutableStateFlow<HashMap<String, WmsLayersStatusModel>> =
        MutableStateFlow(HashMap())
    val layerToggleResponse: StateFlow<HashMap<String, WmsLayersStatusModel>> =
        _layerToggleResponse.asStateFlow()

    private val _clearAllLayersResponse: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val clearAllLayersResponse: StateFlow<Boolean> =
        _clearAllLayersResponse.asStateFlow()

    private val _selectedMapResponse: MutableStateFlow<String> =
        MutableStateFlow("")
    val selectedMapResponse: StateFlow<String> = _selectedMapResponse.asStateFlow()

    private val _filterClickResponse: MutableStateFlow<NavigationDirections?> =
        MutableStateFlow(null)
    val filterClickResponse: StateFlow<NavigationDirections?> = _filterClickResponse.asStateFlow()

    init {
        getAllLayers()
        val map = ArrayList<MapStyle>()
        val selectedMap = preference.getSavedMap()
//        BasemapStyle.values()
//            .forEachIndexed { index, mapName ->
//                map.add(
//                    MapStyle(
//                        id = index,
//                        name = mapName.name.replace("_", " "),
//                        type = "",
//                        image = 1,
//                        isSelected = (selectedMap?:"") == mapName.name
//                    )
//                )
//            }
        map.add(
            MapStyle(
                id = 1,
                name = "ARCGIS OCEANS",
                type = "",
                image = 1,
                isSelected = (selectedMap?:"") == "ARCGIS_OCEANS"
            )
        )
        map.add(
            MapStyle(
                id = 2,
                name = "ARCGIS IMAGERY",
                type = "",
                image = 1,
                isSelected = (selectedMap?:"") == "ARCGIS_IMAGERY"
            )
        )
        map.add(
            MapStyle(
                id = 3,
                name = "ARCGIS NOVA",
                type = "",
                image = 1,
                isSelected = (selectedMap?:"") == "ARCGIS_NOVA"
            )
        )
        map.add(
            MapStyle(
                id = 4,
                name = "ARCGIS STREETS",
                type = "",
                image = 1,
                isSelected = (selectedMap?:"") == "ARCGIS_STREETS"
            )
        )
//        map.add(MapStyle(id = 1, name = "Default", type = "", image = 1))
//        map.add(MapStyle(id = 2, name = "Satellite", type = "", image = 1))
//        map.add(MapStyle(id = 3, name = "Terrain", type = "", image = 1))
//        map.add(MapStyle(id = 4, name = "Traffic", type = "", image = 1))
        _mapResponse.value = map
    }

    fun loadLayersFromSharedPreference() {
        val list = HashMap<String, WmsLayersStatusModel>()
        layersResponse.value.data?.data?.let {
            it.forEach { item ->
                if(item.isSelected) {
                    if (item.layer_type == WMS_TYPE){
                        val wmsLayerNames = listOf(item.layer_calling_name)
                        // create a new WmsLayer with the WMS service url and the layers name list
                        val wmsLayer = WmsLayer(item.layer_calling_url, wmsLayerNames)
                        list.put(item.layer_calling_name, WmsLayersStatusModel(item, wmsLayer))
                    }
                    else if (item.layer_type == Feature_Type){
                        val serviceFeatureTable =
                            ServiceFeatureTable(item.layer_calling_url)
//                                    .apply {
//                                    // set user credentials to authenticate with the service
//                                    //credential = UserCredential("viewer01", "I68VGU^nMurF")
//                                    // NOTE: Never hardcode login information in a production application
//                                    // This is done solely for the sake of the sample
//                                }
                        val layer = FeatureLayer(serviceFeatureTable)

                        list.put(item.layer_calling_name, WmsLayersStatusModel(item, layer))
                    }
                    else if (item.layer_type == Self_Hosted_Type){
                        val portalItemId = item.portal_item_id?:""
                        val portal = Portal(applicationContext.resources.getString(R.string.portal_url), true).apply {
//                            credential = UserCredential(
//                                "dev.fishthebreak", "18241Killingit1108!"
//                            )
                        }
                        val portalItem = PortalItem(portal, portalItemId)
                        val layer = FeatureLayer(portalItem)
                        layer.definitionExpression = "esrignss_receiver_id = '${preference.getUser()?.id}'"
                        list.put(item.layer_calling_name, WmsLayersStatusModel(item, layer))
                    }

                    else if (item.layer_type==TILE){
                        val tiledLayer = ArcGISTiledLayer(item.layer_calling_url)
                        list.put(item.layer_calling_name, WmsLayersStatusModel(item, tiledLayer))

                    }
                }
            }
        }
        _layerToggleResponse.value = list
    }
    /*private fun loadLayersFromSharedPreference() {
        val list = HashMap<String, WmsLayersStatusModel>()
        layersResponse.value.data?.data?.let {
            it.forEach { item ->
                if(item.isSelected) {
                    if(item.self_hosted == 0) {
                        if(!item.portal_item_id.isNullOrEmpty()) {
                            val serviceFeatureTable =
                                ServiceFeatureTable(item.layer_calling_url)
//                                    .apply {
//                                    // set user credentials to authenticate with the service
//                                    //credential = UserCredential("viewer01", "I68VGU^nMurF")
//                                    // NOTE: Never hardcode login information in a production application
//                                    // This is done solely for the sake of the sample
//                                }
                            val layer = FeatureLayer(serviceFeatureTable)

                            list.put(item.layer_calling_name, WmsLayersStatusModel(item, layer))
                        }else {
                            val wmsLayerNames = listOf(item.layer_calling_name)
                            // create a new WmsLayer with the WMS service url and the layers name list
                            val wmsLayer = WmsLayer(item.layer_calling_url, wmsLayerNames)
                            list.put(item.layer_calling_name, WmsLayersStatusModel(item, wmsLayer))
                        }
                    }else{
                        val portalItemId = item.portal_item_id?:""
                        val portal = Portal(applicationContext.resources.getString(R.string.portal_url), false)
                        val portalItem = PortalItem(portal, portalItemId)
                        val layer = FeatureLayer(portalItem)
                        layer.definitionExpression = "esrignss_receiver_id = '${preference.getUser()?.id}'"
                        list.put(item.layer_calling_name, WmsLayersStatusModel(item, layer))
                    }
                }
            }
        }
        _layerToggleResponse.value = list
    }*/

    fun getAllLayers() = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _layersResponse.value = NetworkResult.Loading()
            repository.getAllLayers().collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200) {
                    _clearAllLayersResponse.value = true
                    val selectedLayers = preference.getSavedLayers()
                    selectedLayers?.data?.let { list ->
                        values.data?.data?.forEach { item ->
                            val selected = list.filter { data -> data.id == item.id }
                            if (selected.isNotEmpty()) {
                                item.isSelected = true
                                item.opacity = selected.first().opacity
                            }
                        }
                    }
                }
                _layersResponse.value = values
                loadLayersFromSharedPreference()
                getRecentLayers()
            }
        } else {
            _layersResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )

        }
    }

   /* fun refreshAllLayers() = viewModelScope.launch {
        _clearAllLayersResponse.value = true
        val selectedLayers = preference.getSavedLayers()
        selectedLayers?.data?.let { list ->
            _layersResponse.value.data?.data?.forEach { item ->
                val selected = list.filter { data -> data.id == item.id }
                if (selected.isNotEmpty()) {
                    item.isSelected = true
                    item.opacity = selected.first().opacity
                }
            }
        }
//        val list = HashMap<String, WmsLayersStatusModel>()
//        _layerToggleResponse.value.forEach{
//            list.put(it.key,it.value)
//        }
//        _layerToggleResponse.value = list
        //loadLayersFromSharedPreference()
        getRecentLayers()
    }*/
    fun refreshAllLayers(){
       // _clearAllLayersResponse.value = true
       // when bottom sheet opens from common map ui this function is used to sync layer state with home map layers
        val selectedLayers = preference.getSavedLayers()
        selectedLayers?.data?.let { list ->
            _layersResponse.value.data?.data?.forEach { item ->
                val selected = list.filter { data -> data.id == item.id }
                if (selected.isNotEmpty()) {
                    item.isSelected = true
                    item.opacity = selected.first().opacity
                }else{
                    item.isSelected = false
                }
            }
        }
//        val list = HashMap<String, WmsLayersStatusModel>()
//        _layerToggleResponse.value.forEach{
//            list.put(it.key,it.value)
//        }
//        _layerToggleResponse.value = list
        //loadLayersFromSharedPreference()
        getRecentLayers()
    }

    fun getRecentLayers(){
        _layersResponse.value.data?.data?.let { list ->
            if (list.size > 5) {
                val newList = ArrayList<MapLayer>()
                val selected = list.filter { item-> item.isSelected }
                if(selected.size >= 5){
                    newList.addAll(selected.take(5))
                }else{
                    newList.addAll(selected)
                    newList.addAll((list.filter { item-> !item.isSelected }).take(5-selected.size))
                }
                _layerResponse.value = newList
            } else {
                _layerResponse.value = list
            }
        }
    }

    fun resetResponse() {
        _layersResponse.value = NetworkResult.NoCallYet()
    }
    fun resetFilterClickResponse() {
        _filterClickResponse.value = null
    }

    fun resetMapResponse() {
        _selectedMapResponse.value = ""
    }
    fun resetClearLayerResponse() {
        _clearAllLayersResponse.value = false
    }

    fun resetLayerToggleResponse() {
        _layerToggleResponse.value = HashMap()
    }
    override fun onLayerToggle(data: MapLayer, isChecked: Boolean) {
        data.isSelected = isChecked
        if (!_layerToggleResponse.value.containsKey(data.layer_calling_name)) {
            if (data.layer_type == WMS_TYPE){
                val wmsLayerNames = listOf(data.layer_calling_name)
                // create a new WmsLayer with the WMS service url and the layers name list
                val wmsLayer = WmsLayer(data.layer_calling_url, wmsLayerNames)
                _layerToggleResponse.value.set(
                    data.layer_calling_name,
                    WmsLayersStatusModel(data, wmsLayer)
                )

            }
            else if (data.layer_type == Feature_Type){
                val serviceFeatureTable =
                    ServiceFeatureTable(data.layer_calling_url).apply {
                        // set user credentials to authenticate with the service
                        //credential = UserCredential("viewer01", "I68VGU^nMurF")
                        // NOTE: Never hardcode login information in a production application
                        // This is done solely for the sake of the sample
                    }
                val layer = FeatureLayer(serviceFeatureTable)

                _layerToggleResponse.value.set(
                    data.layer_calling_name,
                    WmsLayersStatusModel(data, layer)
                )
            }
            else if (data.layer_type == Self_Hosted_Type){
                val portalItemId = data.portal_item_id?:""
                val portal = Portal(applicationContext.resources.getString(R.string.portal_url), true).apply {
//                   credential = UserCredential(
//                        "dev.fishthebreak", "18241Killingit1108!"
//                    )
                }
                val portalItem = PortalItem(portal, portalItemId)
                val layer = FeatureLayer(portalItem)
                layer.definitionExpression = "esrignss_receiver_id = '${preference.getUser()?.id}'"
                _layerToggleResponse.value.set(
                    data.layer_calling_name,
                    WmsLayersStatusModel(data, layer)
                )
            }

            else if (data.layer_type==TILE){

                val tiledLayer = ArcGISTiledLayer(data.layer_calling_url)

                _layerToggleResponse.value.set(
                    data.layer_calling_name,
                    WmsLayersStatusModel(data, tiledLayer)
                )


            }
        }
//        viewModelScope.launch {
//            _layersResponse.value.data?.data?.let {
//                preference.saveLayers(LayersStatusModel((it.filter { item -> item.isSelected })))
//            }
//        }
        saveDataInSharedPreference()
    }
    /*override fun onLayerToggle(data: MapLayer, isChecked: Boolean) {
        data.isSelected = isChecked
        if (!_layerToggleResponse.value.containsKey(data.layer_calling_name)) {
            if(data.self_hosted == 0) {
                if(!data.portal_item_id.isNullOrEmpty()) {
                    val serviceFeatureTable =
                        ServiceFeatureTable(data.layer_calling_url).apply {
                            // set user credentials to authenticate with the service
                            //credential = UserCredential("viewer01", "I68VGU^nMurF")
                            // NOTE: Never hardcode login information in a production application
                            // This is done solely for the sake of the sample
                        }
                    val layer = FeatureLayer(serviceFeatureTable)

                    _layerToggleResponse.value.set(
                        data.layer_calling_name,
                        WmsLayersStatusModel(data, layer)
                    )
                }else {
                    val wmsLayerNames = listOf(data.layer_calling_name)
                    // create a new WmsLayer with the WMS service url and the layers name list
                    val wmsLayer = WmsLayer(data.layer_calling_url, wmsLayerNames)
                    _layerToggleResponse.value.set(
                        data.layer_calling_name,
                        WmsLayersStatusModel(data, wmsLayer)
                    )
                }
            }else{
                val portalItemId = data.portal_item_id?:""
                val portal = Portal(applicationContext.resources.getString(R.string.portal_url), false)
                val portalItem = PortalItem(portal, portalItemId)
                val layer = FeatureLayer(portalItem)
                layer.definitionExpression = "esrignss_receiver_id = '${preference.getUser()?.id}'"
                _layerToggleResponse.value.set(
                    data.layer_calling_name,
                    WmsLayersStatusModel(data, layer)
                )
            }
        }
//        viewModelScope.launch {
//            _layersResponse.value.data?.data?.let {
//                preference.saveLayers(LayersStatusModel((it.filter { item -> item.isSelected })))
//            }
//        }
        saveDataInSharedPreference()
    }*/

    override fun onFilterClick(data: MapLayer) {
        if(data.isSelected) {
            _filterClickResponse.value = NavigationDirections.LayerFilterScreen(data)
        }
    }

    override fun onReadMoreClick(data: MapLayer) {
        _filterClickResponse.value = NavigationDirections.ReadMoreFilterScreen(data)
    }

    fun saveDataInSharedPreference(){
        viewModelScope.launch {
            _layersResponse.value.data?.data?.let {
                preference.saveLayers(LayersStatusModel((it.filter { item -> item.isSelected })))
            }
        }
    }

    override fun onMapSelect(data: MapStyle) {
        _mapResponse.value.forEach { item ->
            item.isSelected = false
        }
        data.isSelected = true
        _selectedMapResponse.value = data.name
        viewModelScope.launch {
            preference.saveMap(data.name.replace(" ","_"))
        }
    }

}