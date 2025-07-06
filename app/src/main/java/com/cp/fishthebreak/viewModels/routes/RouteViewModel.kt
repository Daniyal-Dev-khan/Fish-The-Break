package com.cp.fishthebreak.viewModels.routes

import android.app.Application
import android.view.View
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.models.points.GetSavedPointsModel
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.routes.MeasureDistanceModel
import com.cp.fishthebreak.models.routes.SaveRouteModel
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.rules.Validator
import com.cp.fishthebreak.utils.toFormat
import com.cp.fishthebreak.viewModels.profile.locations.LocationsUIEvent
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var routeUIState = MutableStateFlow(RouteUIState())
    val routeUIStates = routeUIState.asStateFlow()
    
    private val _routeResponse: MutableStateFlow<NetworkResult<SaveRouteModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val routeResponse: StateFlow<NetworkResult<SaveRouteModel>> = _routeResponse.asStateFlow()

    fun onNameChangeEvent(name: CharSequence) {
        onEvent(RouteUIEvent.NameChanged(name.toString()))
    }
    fun onRouteIdChangeEvent(routeId: Int?) {
        onEvent(RouteUIEvent.RouteIdChanged(routeId))
    }

    fun onImageChangeEvent(image: String) {
        onEvent(RouteUIEvent.ImageChanged(image))
    }

    fun onDescriptionChangeEvent(description: CharSequence) {
        onEvent(RouteUIEvent.DescriptionChanged(description.toString()))
    }
    fun onLocationsChangeEvent(locations: ArrayList<MeasureDistanceModel>) {
        onEvent(RouteUIEvent.LocationsChanged(locations))
    }

    fun onSaveClickEvent(view: View?) {
        onEvent(RouteUIEvent.SaveButtonClicked, view)
    }

    private fun onEvent(event: RouteUIEvent, view: View? = null) {
        when (event) {
            is RouteUIEvent.RouteIdChanged -> {
                routeUIState.value = routeUIState.value.copy(
                    routeId = event.routeId
                )
            }

            is RouteUIEvent.NameChanged -> {
                routeUIState.value = routeUIState.value.copy(
                    name = event.name
                )
                routeUIState.value = routeUIState.value.copy(
                    nameError = Validator.validateLocationName(
                        name = routeUIState.value.name
                    ).status
                )
            }

            is RouteUIEvent.DescriptionChanged -> {
                routeUIState.value = routeUIState.value.copy(
                    description = event.description
                )
//                routeUIState.value = routeUIState.value.copy(
//                    descriptionError = Validator.validateText(
//                        text = routeUIState.value.description
//                    ).status
//                )
            }

            is RouteUIEvent.ImageChanged -> {
                routeUIState.value = routeUIState.value.copy(
                    image = event.image
                )
            }

            is RouteUIEvent.LocationsChanged -> {
                routeUIState.value = routeUIState.value.copy(
                    locations = event.locations
                )
            }
            
            is RouteUIEvent.SaveButtonClicked -> {
                saveRoute(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateDataWithRules(): Boolean {
        val nameResult = Validator.validateLocationName(
            name = routeUIState.value.name
        )
//        val descriptionResult = Validator.validateText(
//            text = routeUIState.value.description
//        )


        routeUIState.value = routeUIState.value.copy(
            nameError = nameResult.status,
            //descriptionError = descriptionResult.status,
        )

//        return nameResult.status && descriptionResult.status
        return nameResult.status
    }

    private fun saveRoute(view: View?) = viewModelScope.launch {
        //list: ArrayList<MeasureDistanceModel>
        if (validateDataWithRules()) {
            view?.let { applicationContext.hideKeyboardFrom(it) }
            if (applicationContext.isNetworkAvailable()) {
                _routeResponse.value = NetworkResult.Loading()
                if(routeUIState.value.image.isEmpty()) {
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("name", routeUIState.value.name)
                    jsonObject.addProperty("description", routeUIState.value.description)
                    if(routeUIState.value.routeId == null){
                        val date = Date()
                        jsonObject.addProperty("date", date.toFormat("yyyy-MM-dd"))
                        jsonObject.addProperty("time", date.toFormat("kk:mm:ss"))
                        val jsonArray = JsonArray()
                        routeUIState.value.locations.forEachIndexed { index, item ->
                            val obj = JsonObject()
                            when (item.point1) {
                                is SavePointsData -> {
                                    obj.addProperty("pointable_type", "location")
                                    obj.addProperty("pointable_id", item.point1.id)
                                }

                                is SaveFishLogData -> {
                                    obj.addProperty("pointable_type", "fish_log")
                                    obj.addProperty("pointable_id", item.point1.id)
                                }
                                is SearchData -> {
                                    if (item.point1.type == "1"){
                                        obj.addProperty("pointable_type", "new")
                                        obj.addProperty("point_name", item.point1.search_text?:item.point1.name)
                                        obj.addProperty("latitude", item.point1.latitude)
                                        obj.addProperty("longitude", item.point1.longitude)
                                    }else if (item.point1.type == "2"){
                                        obj.addProperty("pointable_type", "fish_log")
                                        obj.addProperty("pointable_id", item.point1.id)
                                    }else if (item.point1.type == "3"){
                                        obj.addProperty("pointable_type", "location")
                                        obj.addProperty("pointable_id", item.point1.id)
                                    }
                                }
                            }
                            jsonArray.add(obj)
                            if(index == routeUIState.value.locations.size-1){
                                val obj1 = JsonObject()
                                when (item.point2) {
                                    is SavePointsData -> {
                                        obj1.addProperty("pointable_type", "location")
                                        obj1.addProperty("pointable_id", item.point2.id)
                                    }

                                    is SaveFishLogData -> {
                                        obj1.addProperty("pointable_type", "fish_log")
                                        obj1.addProperty("pointable_id", item.point2.id)
                                    }
                                    is SearchData -> {
                                        if (item.point2.type == "1"){
                                            obj1.addProperty("pointable_type", "new")
                                            obj1.addProperty("point_name", item.point2.search_text?:item.point2.name)
                                            obj1.addProperty("latitude", item.point2.latitude)
                                            obj1.addProperty("longitude", item.point2.longitude)
                                        }else if (item.point2.type == "2"){
                                            obj1.addProperty("pointable_type", "fish_log")
                                            obj1.addProperty("pointable_id", item.point2.id)
                                        }else if (item.point2.type == "3"){
                                            obj1.addProperty("pointable_type", "location")
                                            obj1.addProperty("pointable_id", item.point2.id)
                                        }
                                    }
                                }
                                jsonArray.add(obj1)
                            }

                        }
                        //jsonObject.add("locations", jsonArray)
                        jsonObject.addProperty("locations", jsonArray.toString())
                        repository.saveRoute(jsonObject).collect { values ->
                            _routeResponse.value = values
                        }
                    }else{
                        jsonObject.addProperty("id", routeUIState.value.routeId)
                        repository.updateRoute(jsonObject).collect { values ->
                            _routeResponse.value = values
                        }
                    }
                }else{
                    val multipartBody: MultipartBody.Part? =
                        if (!URLUtil.isValidUrl(routeUIState.value.image)) {
                            val file: File? = File(routeUIState.value.image)
                            if (file != null) {
                                val requestFile =
                                    file.asRequestBody("image/png".toMediaTypeOrNull())
                                MultipartBody.Part.createFormData(
                                    "image", file.name, requestFile
                                )
                            } else {
                                null
                            }
                        } else
                            null
                    if(routeUIState.value.routeId == null){
                        val date = Date()
                        //val jsonObject = JsonObject()
                        val jsonArray = JsonArray()
                        routeUIState.value.locations.forEachIndexed { index, item ->
                            val obj = JsonObject()
                            when (item.point1) {
                                is SavePointsData -> {
                                    obj.addProperty("pointable_type", "location")
                                    obj.addProperty("pointable_id", item.point1.id)
                                }

                                is SaveFishLogData -> {
                                    obj.addProperty("pointable_type", "fish_log")
                                    obj.addProperty("pointable_id", item.point1.id)
                                }
                                is SearchData -> {
                                    if (item.point1.type == "1"){
                                        obj.addProperty("pointable_type", "new")
                                        obj.addProperty("point_name", item.point1.search_text?:item.point1.name)
                                        obj.addProperty("latitude", item.point1.latitude)
                                        obj.addProperty("longitude", item.point1.longitude)
                                    }else if (item.point1.type == "2"){
                                        obj.addProperty("pointable_type", "fish_log")
                                        obj.addProperty("pointable_id", item.point1.id)
                                    }else if (item.point1.type == "3"){
                                        obj.addProperty("pointable_type", "location")
                                        obj.addProperty("pointable_id", item.point1.id)
                                    }
                                }
                            }
                            jsonArray.add(obj)
                            if(index == routeUIState.value.locations.size-1){
                                val obj1 = JsonObject()
                                when (item.point2) {
                                    is SavePointsData -> {
                                        obj1.addProperty("pointable_type", "location")
                                        obj1.addProperty("pointable_id", item.point2.id)
                                    }

                                    is SaveFishLogData -> {
                                        obj1.addProperty("pointable_type", "fish_log")
                                        obj1.addProperty("pointable_id", item.point2.id)
                                    }
                                    is SearchData -> {
                                        if (item.point2.type == "1"){
                                            obj1.addProperty("pointable_type", "new")
                                            obj1.addProperty("point_name", item.point2.search_text?:item.point2.name)
                                            obj1.addProperty("latitude", item.point2.latitude)
                                            obj1.addProperty("longitude", item.point2.longitude)
                                        }else if (item.point2.type == "2"){
                                            obj1.addProperty("pointable_type", "fish_log")
                                            obj1.addProperty("pointable_id", item.point2.id)
                                        }else if (item.point2.type == "3"){
                                            obj1.addProperty("pointable_type", "location")
                                            obj1.addProperty("pointable_id", item.point2.id)
                                        }
                                    }
                                }
                                jsonArray.add(obj1)
                            }
                        }
                        //jsonObject.add("locations", jsonArray)
                        repository.saveRoute(
                            routeUIState.value.name.toRequestBody(),
                            routeUIState.value.description.toRequestBody(),
                            date.toFormat("yyyy-MM-dd").toRequestBody(),
                            date.toFormat("kk:mm:ss").toRequestBody(),
                            jsonArray.toString().toRequestBody(),
                            multipartBody
                        ).collect { values ->
                            _routeResponse.value = values
                        }
                    }else{
                        repository.updateRoute(
                            routeUIState.value.routeId.toString().toRequestBody(),
                            routeUIState.value.name.toRequestBody(),
                            routeUIState.value.description.toRequestBody(),
                            multipartBody
                        ).collect { values ->
                            _routeResponse.value = values
                        }
                    }
                }
            } else {
                _routeResponse.value = NetworkResult.NoInternet(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    )
                )
            }
        }
    }

    fun resetResponse() {
        _routeResponse.value = NetworkResult.NoCallYet()
    }
}