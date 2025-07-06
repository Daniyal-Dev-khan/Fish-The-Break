package com.cp.fishthebreak.viewModels.profile.vessel

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.vessel.GetVesselModel
import com.cp.fishthebreak.models.vessel.SaveVesselModel
import com.cp.fishthebreak.models.vessel.VesselData
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.convertCoordinatesToLatLng
import com.cp.fishthebreak.utils.getNauticalLatitude
import com.cp.fishthebreak.utils.getNauticalLongitude
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.rules.Validator
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VesselViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var vesselUIState = MutableStateFlow(VesselUIState())
    val vesselUIStates = vesselUIState.asStateFlow()

    private val _saveVesselResponse: MutableStateFlow<NetworkResult<SaveVesselModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val saveVesselResponse: StateFlow<NetworkResult<SaveVesselModel>> = _saveVesselResponse.asStateFlow()

    private val _getVesselResponse: MutableStateFlow<NetworkResult<GetVesselModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val getVesselResponse: StateFlow<NetworkResult<GetVesselModel>> = _getVesselResponse.asStateFlow()

    private val _vesselResponse: MutableStateFlow<VesselData?> =
        MutableStateFlow(null)
    val vesselResponse: StateFlow<VesselData?> = _vesselResponse.asStateFlow()

    init {
        getVessel()
    }
    fun onNameEvent(name: CharSequence){
        onEvent(VesselUIEvent.NameChanged(name.toString()))
    }
    fun onMakeEvent(make: CharSequence){
        onEvent(VesselUIEvent.MakeChanged(make.toString()))
    }
    fun onModelEvent(model: CharSequence){
        onEvent(VesselUIEvent.ModelChanged(model.toString()))
    }
    fun onYearEvent(year: CharSequence){
        onEvent(VesselUIEvent.YearChanged(year.toString()))
    }
    fun onBoatRangeEvent(boatRange: String){
        onEvent(VesselUIEvent.BoatRangeChanged(boatRange))
        onBoatRangeNMEvent(boatRange)
    }
    private fun onBoatRangeNMEvent(boatRange: String){
        onEvent(VesselUIEvent.BoatRangeNMChanged(boatRange))
    }
    fun onDockLocationEvent(location: String){
        onEvent(VesselUIEvent.DockLocationChanged(location))
    }
    
    fun onSaveClickEvent(view: View?){
        onEvent(VesselUIEvent.SaveButtonClicked, view)
    }

    private fun onEvent(event: VesselUIEvent, view: View? = null) {
        when (event) {
            is VesselUIEvent.NameChanged -> {
                vesselUIState.value = vesselUIState.value.copy(
                    name = event.name
                )
//                vesselUIState.value = vesselUIState.value.copy(
//                    nameError = Validator.validateText(
//                        text = vesselUIState.value.name
//                    ).status
//                )
            }
            is VesselUIEvent.MakeChanged -> {
                vesselUIState.value = vesselUIState.value.copy(
                    make = event.make
                )
//                vesselUIState.value = vesselUIState.value.copy(
//                    makeError = Validator.validateText(
//                        text = vesselUIState.value.make
//                    ).status
//                )
            }
            is VesselUIEvent.ModelChanged -> {
                vesselUIState.value = vesselUIState.value.copy(
                    model = event.model
                )
//                vesselUIState.value = vesselUIState.value.copy(
//                    modelError = Validator.validateText(
//                        text = vesselUIState.value.model
//                    ).status
//                )
            }
            is VesselUIEvent.YearChanged -> {
                vesselUIState.value = vesselUIState.value.copy(
                    year = event.year
                )
//                vesselUIState.value = vesselUIState.value.copy(
//                    yearError = Validator.validateText(
//                        text = vesselUIState.value.year
//                    ).status
//                )
            }

            is VesselUIEvent.BoatRangeChanged -> {
                vesselUIState.value = vesselUIState.value.copy(
                    boatRange = event.boatRange
                )
            }
            is VesselUIEvent.BoatRangeNMChanged -> {
                vesselUIState.value = vesselUIState.value.copy(
                    boatRangeNM = event.boatRangeNM
//                    boatRangeNM = event.boatRangeNM.toFloat().milesToNauticalMiles().toString()
                )
            }
            is VesselUIEvent.DockLocationChanged -> {
                vesselUIState.value = vesselUIState.value.copy(
                    dockLocation = event.dockLocation
                )
//                vesselUIState.value = vesselUIState.value.copy(
//                    dockLocationError = Validator.validateText(
//                        text = vesselUIState.value.dockLocation
//                    ).status
//                )
            }

            is VesselUIEvent.SaveButtonClicked -> {
                if(_vesselResponse.value == null) {
                    saveVessel(view)
                }else{
                    updateVessel(view)
                }
            }
        }
        //validateUIDataWithRules()
    }

    private fun validateUIDataWithRules(): Boolean {
        val nameResult = Validator.validateText(
            text = vesselUIState.value.name
        )
        val makeResult = Validator.validateText(
            text = vesselUIState.value.make
        )
        val modelResult = Validator.validateText(
            text = vesselUIState.value.model
        )
        val yearResult = Validator.validateText(
            text = vesselUIState.value.year
        )
        val locationResult = Validator.validateText(
            text = vesselUIState.value.dockLocation
        )
        val rangeResult = Validator.validateText(
            text = vesselUIState.value.boatRange
        )

        vesselUIState.value = vesselUIState.value.copy(
            nameError = nameResult.status,
            makeError = makeResult.status,
            modelError = modelResult.status,
            yearError = yearResult.status,
            boatRangeError = rangeResult.status,
            dockLocationError = locationResult.status,
        )
//        return nameResult.status && makeResult.status && modelResult.status && yearResult.status && locationResult.status
        return !nameResult.status && !makeResult.status && !modelResult.status && !yearResult.status && !locationResult.status && !rangeResult.status

        //allValidationsPassed.value = emailResult.status && passwordResult.status

    }

    private fun saveVessel(view: View?) = viewModelScope.launch {
        if(!validateUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                val jsonObject = JsonObject()
                if (vesselUIState.value.name.isNotEmpty()) {
                    jsonObject.addProperty("name", vesselUIState.value.name)
                }
                if (vesselUIState.value.make.isNotEmpty()) {
                    jsonObject.addProperty("make", vesselUIState.value.make)
                }
                if (vesselUIState.value.model.isNotEmpty()) {
                    jsonObject.addProperty("model", vesselUIState.value.model)
                }
                if (vesselUIState.value.year.isNotEmpty()) {
                    jsonObject.addProperty("year", vesselUIState.value.year)
                }
                if(vesselUIState.value.boatRange.isNotEmpty() && vesselUIState.value.boatRange != "0") {
                    jsonObject.addProperty("range", vesselUIState.value.boatRange)
                }
                if(vesselUIState.value.dockLocation.isNotEmpty()) {
                    val latLng =
                        convertCoordinatesToLatLng(vesselUIState.value.dockLocation)
                    if (latLng != null) {
                        val (latitude, longitude) = latLng
                        Log.i("convertCoordinates", "Latitude: $latitude, Longitude: $longitude")
                        jsonObject.addProperty("latitude", latitude.toString())
                        jsonObject.addProperty("longitude", longitude.toString())
                    } else {
                        Log.i("convertCoordinates", "Invalid coordinate string format.")
                        return@launch
                    }
//                    val latLang = vesselUIState.value.dockLocation.split(",")
//                    if(latLang.size == 2) {
//                        jsonObject.addProperty("latitude", latLang.first().trim())
//                        jsonObject.addProperty("longitude", latLang[1].trim())
//                    }
                }
                _saveVesselResponse.value = NetworkResult.Loading()
                repository.saveVessel(jsonObject).collect { values ->
                    if(values.data?.status == true && values.data.statusCode == 200){
                        _vesselResponse.value = values.data.data
                        try {
                            val user = preference.getUser()
                            user?.user_configuration?.range = values.data.data?.range?.toInt()
                            user?.vessel = values.data.data
                            preference.saveUser(user)
                            getVessel()
                        }catch (ex: Exception){

                        }
                    }
                    _saveVesselResponse.value = values
                }
            } else {
                _saveVesselResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }
    private fun updateVessel(view: View?) = viewModelScope.launch {
        if(!validateUIDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                val jsonObject = JsonObject()
                if (vesselUIState.value.name.isNotEmpty()) {
                    jsonObject.addProperty("name", vesselUIState.value.name)
                }
                jsonObject.addProperty("id", _vesselResponse.value?.id?:-1)
                if (vesselUIState.value.make.isNotEmpty()) {
                    jsonObject.addProperty("make", vesselUIState.value.make)
                }
                if (vesselUIState.value.model.isNotEmpty()) {
                    jsonObject.addProperty("model", vesselUIState.value.model)
                }
                if (vesselUIState.value.year.isNotEmpty()) {
                    jsonObject.addProperty("year", vesselUIState.value.year)
                }
                if(vesselUIState.value.boatRange.isNotEmpty() && vesselUIState.value.boatRange != "0") {
                    jsonObject.addProperty("range", vesselUIState.value.boatRange)
                }
                if(vesselUIState.value.dockLocation.isNotEmpty()) {
                    val latLng =
                        convertCoordinatesToLatLng(vesselUIState.value.dockLocation)
                    if (latLng != null) {
                        val (latitude, longitude) = latLng
                        Log.i("convertCoordinates", "Latitude: $latitude, Longitude: $longitude")
                        jsonObject.addProperty("latitude", latitude.toString())
                        jsonObject.addProperty("longitude", longitude.toString())
                    } else {
                        Log.i("convertCoordinates", "Invalid coordinate string format.")
                        return@launch
                    }
//                    val latLang = vesselUIState.value.dockLocation.split(",")
//                    if(latLang.size == 2) {
//                        jsonObject.addProperty("latitude", latLang.first().trim())
//                        jsonObject.addProperty("longitude", latLang[1].trim())
//                    }
                }
                _saveVesselResponse.value = NetworkResult.Loading()
                repository.updateVessel(jsonObject).collect { values ->
                    if(values.data?.status == true && values.data.statusCode == 200){
                        _vesselResponse.value = values.data.data
                        try {
                            val user = preference.getUser()
                            user?.user_configuration?.range = values.data.data?.range?.toInt()?:0
                            user?.vessel = values.data.data
                            preference.saveUser(user)
                            getVessel()
                        }catch (ex: Exception){

                        }
                    }
                    _saveVesselResponse.value = values
                }
            } else {
                _saveVesselResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )

            }
        }
    }
    private fun getVessel() = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _getVesselResponse.value = NetworkResult.Loading()
            repository.getAllVessel().collect { values ->
                if(values.data?.status == true && values.data.statusCode == 200){
                    if(values.data.data.isNotEmpty()) {
                        val model = values.data.data.first()
                        _vesselResponse.value = model
                        onNameEvent(model.name?:"")
                        onMakeEvent(model.make?:"")
                        onModelEvent(model.model?:"")
                        onYearEvent(model.year?:"")
                        onBoatRangeEvent(model.range?:"0")
                        if (model.getLatFromString() != null && model.getLangFromString() != null) {
                            onDockLocationEvent("${getNauticalLatitude( model.getLatFromString()?:0.0)} , ${getNauticalLongitude( model.getLangFromString()?:0.0)}")
                        }else{
                            onDockLocationEvent("")
                        }
                        try {
                            val user = preference.getUser()
                            user?.user_configuration?.range = model.range?.toInt()
                            user?.vessel = model
                            preference.saveUser(user)
                        }catch (ex: Exception){

                        }
                    }
                }
                _getVesselResponse.value = values
            }
        } else {
            _getVesselResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }

    fun resetResponse(){
        _saveVesselResponse.value = NetworkResult.NoCallYet()
        _getVesselResponse.value = NetworkResult.NoCallYet()
    }

}