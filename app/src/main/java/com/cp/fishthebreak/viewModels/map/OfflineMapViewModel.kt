package com.cp.fishthebreak.viewModels.map

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.local.LocalRepository
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.deleteMapData
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.rules.Validator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class OfflineMapViewModel @Inject constructor
    (
    private val localRepository: LocalRepository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var offlineMapUIState = MutableStateFlow(OfflineMapUIState())
    val offlineMapUIStates = offlineMapUIState.asStateFlow()

    private var _offlineMapList: MutableStateFlow<ArrayList<OfflineMap>> =
        MutableStateFlow(ArrayList())
    val offlineMapList: StateFlow<ArrayList<OfflineMap>> = _offlineMapList.asStateFlow()

    private val _offlineMapResponse: MutableStateFlow<Long?> =
        MutableStateFlow(null)
    val offlineMapResponse: StateFlow<Long?> =
        _offlineMapResponse.asStateFlow()

    private val _offlineMapModel: MutableStateFlow<OfflineMap?> =
        MutableStateFlow(null)
    val offlineMapModel: StateFlow<OfflineMap?> =
        _offlineMapModel.asStateFlow()

    init {
        deleteInCompleteMap()
    }

    fun onDateChangeEvent(date: Long) {
        onEvent(OfflineMapUIEvent.MapDateChanged(date))
    }

    fun onNameChangeEvent(name: CharSequence) {
        onEvent(OfflineMapUIEvent.NameChanged(name.toString()))
    }

    fun onIdChangeEvent(id: Int?) {
        onEvent(OfflineMapUIEvent.MapIdChanged(id))
    }

    fun onDescriptionChangeEvent(description: CharSequence) {
        onEvent(OfflineMapUIEvent.DescriptionChanged(description.toString()))
    }

    fun onImageChangeEvent(image: String) {
        onEvent(OfflineMapUIEvent.ImageChanged(image))
    }

    fun onPathChangeEvent(path: String) {
        onEvent(OfflineMapUIEvent.PathChanged(path))
    }


    fun onSaveClickEvent(view: View?) {
        onEvent(OfflineMapUIEvent.SaveButtonClicked, view)
    }


    private fun onEvent(event: OfflineMapUIEvent, view: View? = null) {
        when (event) {
            is OfflineMapUIEvent.MapIdChanged -> {
                offlineMapUIState.value = offlineMapUIState.value.copy(
                    mapId = event.id
                )
            }

            is OfflineMapUIEvent.MapDateChanged -> {
                offlineMapUIState.value = offlineMapUIState.value.copy(
                    mapDate = event.date
                )
            }

            is OfflineMapUIEvent.NameChanged -> {
                offlineMapUIState.value = offlineMapUIState.value.copy(
                    name = event.name
                )
                offlineMapUIState.value = offlineMapUIState.value.copy(
                    nameError = Validator.validateText(
                        text = offlineMapUIState.value.name
                    ).status
                )

            }

            is OfflineMapUIEvent.DescriptionChanged -> {
                offlineMapUIState.value = offlineMapUIState.value.copy(
                    description = event.description
                )
//                offlineMapUIState.value = offlineMapUIState.value.copy(
//                    descriptionError = Validator.validateText(
//                        text = offlineMapUIState.value.description
//                    ).status
//                )
            }

            is OfflineMapUIEvent.ImageChanged -> {
                offlineMapUIState.value = offlineMapUIState.value.copy(
                    image = event.image
                )
            }

            is OfflineMapUIEvent.PathChanged -> {
                offlineMapUIState.value = offlineMapUIState.value.copy(
                    mapPath = event.mapPath
                )
            }

            is OfflineMapUIEvent.SaveButtonClicked -> {
                saveMap(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    fun validateUIDataWithRules(): Boolean {
        val nameResult = Validator.validateText(
            text = offlineMapUIState.value.name
        )
//        val descriptionResult = Validator.validateText(
//            text = offlineMapUIState.value.description
//        )

        offlineMapUIState.value = offlineMapUIState.value.copy(
            nameError = nameResult.status,
            //descriptionError = descriptionResult.status,
        )

//        return nameResult.status && descriptionResult.status
        return nameResult.status

    }

    private fun saveMap(view: View?) = viewModelScope.launch {
        if (validateUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            val model = OfflineMap(
                id = 0,
                name = offlineMapUIState.value.name,
                description = offlineMapUIState.value.description,
                image = null,
                mapPath = null,
                date = offlineMapUIState.value.mapDate ?: Date().time
            )
            localRepository.saveOfflineMap(model).collect { values ->
                if (values > 0) {
                    onIdChangeEvent(values.toInt())
                    model.id = values.toInt()
                    _offlineMapModel.value = model
                }
            }
        }
    }

    fun getOfflineMap(addLiveMap: Boolean = false, selectedMap: OfflineMap? = null, isLiveMapLoaded : Boolean = false) = viewModelScope.launch {
        localRepository.getOfflineMap().collect { values ->
            val data = values as ArrayList<OfflineMap>
            if (addLiveMap) {
                val mapStyle = preference.getSavedMap()
                val map = OfflineMap(
                    id = -1,
                    name = "Live Map",
                    description = "Display live map",
                    image = "",
                    mapPath = "",
                    Date().time
                )
                if(selectedMap == null){
                    map.isSelected = isLiveMapLoaded
                }else{
                    val selection = data.filter { item-> item.id ==  selectedMap.id}
                    if(selection.isNotEmpty()){
                        selection.first().isSelected = true
                    }
                }
                map.placeholder =
                when(mapStyle){
                    "ARCGIS_NOVA"->{
                        R.drawable.ic_map_nova
                    }
                    "ARCGIS_STREETS"->{
                        R.drawable.ic_map_streets
                    }
                    "ARCGIS_IMAGERY"->{
                        R.drawable.ic_map_imagery
                    }
                    "ARCGIS_OCEANS"->{
                        R.drawable.ic_map_oceans
                    }else->{
                        null
                    }
                }
                data.add(
                    0,
                    map
                )
            }
            _offlineMapList.value = data
        }
    }

    fun updateMap() = viewModelScope.launch {
        _offlineMapModel.value?.mapPath = offlineMapUIState.value.mapPath
        if (offlineMapUIState.value.image.isNotEmpty()) {
            _offlineMapModel.value?.image = offlineMapUIState.value.image
        }
        _offlineMapModel.value?.let { model ->
            model.mapPath?.let { path ->
                localRepository.updateOfflineMap(model)
                    .collect { values ->
                        if (values > 0) {
                            //_offlineMapResponse.value = values.toLong()
                            resetMapResponse()
                        }

                    }
            }
        }
    }

    fun deleteInCompleteMap() = viewModelScope.launch {
        localRepository.deleteInCompleteMap().collect { values ->
            resetResponse()
        }
    }

    fun deleteMap(model: OfflineMap) = viewModelScope.launch {
        localRepository.deleteOfflineMap(model).collect { values ->
            if (values > 0) {
                model.date?.let { applicationContext.deleteMapData(it) }
                getOfflineMap()
            }
        }
    }

    fun resetResponse() {
        _offlineMapResponse.value = null
        _offlineMapModel.value = null
        onIdChangeEvent(null)
    }

    fun resetMapResponse() {
        _offlineMapResponse.value = null
        onIdChangeEvent(null)
        onNameChangeEvent("")
        onDescriptionChangeEvent("")
        onImageChangeEvent("")
        onPathChangeEvent("")
        _offlineMapModel.value = null
    }

}