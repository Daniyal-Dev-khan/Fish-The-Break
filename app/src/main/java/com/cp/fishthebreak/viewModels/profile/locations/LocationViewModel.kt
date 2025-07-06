package com.cp.fishthebreak.viewModels.profile.locations

import android.app.Application
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.di.local.LocalRepository
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SaveFishLogModel
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.points.SavePointsModel
import com.cp.fishthebreak.utils.convertCoordinatesToLatLng
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.rules.Validator
import com.cp.fishthebreak.utils.toFormat
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Arrays
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor
    (
    private val localRepository: LocalRepository,
    private val repository: Repository,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private var locationUIState = MutableStateFlow(LocationUIState())
    val locationUIStates = locationUIState.asStateFlow()


    private val _savePointResponse: MutableStateFlow<NetworkResult<SavePointsModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val savePointResponse: StateFlow<NetworkResult<SavePointsModel>> =
        _savePointResponse.asStateFlow()

    private val _saveFishLogResponse: MutableStateFlow<NetworkResult<SaveFishLogModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val saveFishLogResponse: StateFlow<NetworkResult<SaveFishLogModel>> =
        _saveFishLogResponse.asStateFlow()

    fun onIdChangeEvent(id: Int) {
        onEvent(LocationsUIEvent.IdChanged(id))
    }

    fun onNameChangeEvent(name: CharSequence) {
        onEvent(LocationsUIEvent.NameChanged(name.toString()))
    }

    fun onLatChangeEvent(lat: CharSequence) {
        onEvent(LocationsUIEvent.LatChanged(lat.toString()))
    }

    fun onLangChangeEvent(lang: CharSequence) {
        onEvent(LocationsUIEvent.LangChanged(lang.toString()))
    }

    fun onLatFormatChangeEvent(lat: CharSequence) {
        onEvent(LocationsUIEvent.LatFormatChanged(lat.toString()))
    }

    fun onLangFormatChangeEvent(lang: CharSequence) {
        onEvent(LocationsUIEvent.LangFormatChanged(lang.toString()))
    }

    fun onDateChangeEvent(date: CharSequence) {
        onEvent(LocationsUIEvent.DateChanged(date.toString()))
    }

    fun onTimeChangeEvent(time: CharSequence) {
        onEvent(LocationsUIEvent.TimeChanged(time.toString()))
    }

    fun onWeightChangeEvent(weight: CharSequence) {
        onEvent(LocationsUIEvent.WeightChanged(weight.toString()))
    }

    fun onLengthChangeEvent(length: CharSequence) {
        onEvent(LocationsUIEvent.LengthChanged(length.toString()))
    }
    fun onLengthInchesChangeEvent(length: CharSequence) {
        onEvent(LocationsUIEvent.LengthInchesChanged(length.toString()))
    }

    fun onDescriptionChangeEvent(description: CharSequence) {
        onEvent(LocationsUIEvent.DescriptionChanged(description.toString()))
    }

    fun onImageChangeEvent(image: String) {
        onEvent(LocationsUIEvent.ImageChanged(image))
    }

    fun onRadioButtonChangeEvent(id: Int) {
        onEvent(LocationsUIEvent.RadioButtonChanged(id))
    }

    fun onTrollingIdChangeEvent(id: Long) {
        onEvent(LocationsUIEvent.TrollingIdChanged(id))
    }

    fun onSaveClickEvent(view: View?) {
        onEvent(LocationsUIEvent.SaveButtonClicked, view)
    }

    private fun onEvent(event: LocationsUIEvent, view: View? = null) {
        when (event) {
            is LocationsUIEvent.IdChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    pointId = event.id
                )
            }

            is LocationsUIEvent.NameChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    name = event.name
                )
                locationUIState.value = locationUIState.value.copy(
                    nameError = Validator.validateLocationName(
                        name = locationUIState.value.name
                    ).status
                )
            }

            is LocationsUIEvent.LatChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    lat = event.lat
                )
                locationUIState.value = locationUIState.value.copy(
                    latError = Validator.validateLatLang(
                        point = locationUIState.value.lat
                    ).status
                )
            }

            is LocationsUIEvent.LangChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    lang = event.lang
                )
                locationUIState.value = locationUIState.value.copy(
                    langError = Validator.validateLatLang(
                        point = locationUIState.value.lang
                    ).status
                )
            }

            is LocationsUIEvent.LatFormatChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    latFormat = event.lat
                )
                locationUIState.value = locationUIState.value.copy(
                    latError = Validator.validateLatFormat(
                        point = locationUIState.value.latFormat
                    ).status
                )
            }

            is LocationsUIEvent.LangFormatChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    langFormat = event.lang
                )
                locationUIState.value = locationUIState.value.copy(
                    langError = Validator.validateLngFormat(
                        point = locationUIState.value.langFormat
                    ).status
                )
            }

            is LocationsUIEvent.DateChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    date = event.date
                )
//                locationUIState.value = locationUIState.value.copy(
//                    dateError = Validator.validateDate(
//                        date = locationUIState.value.date
//                    ).status
//                )
            }

            is LocationsUIEvent.TimeChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    time = event.time
                )
//                locationUIState.value = locationUIState.value.copy(
//                    timeError = Validator.validateTime(
//                        time = locationUIState.value.time
//                    ).status
//                )
            }

            is LocationsUIEvent.WeightChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    weight = event.weight
                )
//                locationUIState.value = locationUIState.value.copy(
//                    weightError = Validator.validateText(
//                        text = locationUIState.value.weight
//                    ).status
//                )
            }

            is LocationsUIEvent.LengthChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    length = event.length
                )
//                locationUIState.value = locationUIState.value.copy(
//                    lengthError = Validator.validateText(
//                        text = locationUIState.value.length
//                    ).status
//                )
            }

            is LocationsUIEvent.LengthInchesChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    lengthInches = event.length
                )
//                locationUIState.value = locationUIState.value.copy(
//                    lengthError = Validator.validateText(
//                        text = locationUIState.value.length
//                    ).status
//                )
            }

            is LocationsUIEvent.DescriptionChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    description = event.description
                )
//                locationUIState.value = locationUIState.value.copy(
//                    descriptionError = Validator.validateText(
//                        text = locationUIState.value.description
//                    ).status
//                )
            }

            is LocationsUIEvent.ImageChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    image = event.image
                )
            }

            is LocationsUIEvent.RadioButtonChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    radioButton = event.id
                )
            }

            is LocationsUIEvent.TrollingIdChanged -> {
                locationUIState.value = locationUIState.value.copy(
                    trollingId = event.id
                )
            }

            is LocationsUIEvent.SaveButtonClicked -> {
                savePoint(view)
            }
        }
        //validateLoginUIDataWithRules()
    }

    private fun validateLocationDataWithRules(): Boolean {
        if (locationUIState.value.trollingId != null) {
            return true
        }
        val nameResult = Validator.validateLocationName(
            name = locationUIState.value.name
        )
        val latResult = Validator.validateLatFormat(
            point = locationUIState.value.latFormat
        )
        val langResult = Validator.validateLngFormat(
            point = locationUIState.value.langFormat
        )
//        val descriptionResult = Validator.validateText(
//            text = locationUIState.value.description
//        )


        locationUIState.value = locationUIState.value.copy(
            nameError = nameResult.status,
            latError = latResult.status,
            langError = langResult.status,
            //descriptionError = descriptionResult.status,
        )

//        return nameResult.status && latResult.status && langResult.status && descriptionResult.status
        return nameResult.status && latResult.status && langResult.status
    }

    private fun validateFishDataWithRules(): Boolean {
        if (locationUIState.value.trollingId != null) {
            return true
        }

        val nameResult = Validator.validateLocationName(
            name = locationUIState.value.name
        )
        val latResult = Validator.validateLatFormat(
            point = locationUIState.value.latFormat
        )
        val langResult = Validator.validateLngFormat(
            point = locationUIState.value.langFormat
        )
//        val descriptionResult = Validator.validateText(
//            text = locationUIState.value.description
//        )
//
//        val dateResult = Validator.validateDate(
//            date = locationUIState.value.date
//        )
//        val timeResult = Validator.validateTime(
//            time = locationUIState.value.time
//        )
//        val weightResult = Validator.validateText(
//            text = locationUIState.value.weight
//        )
//        val lengthResult = Validator.validateText(
//            text = locationUIState.value.length
//        )


        locationUIState.value = locationUIState.value.copy(
            nameError = nameResult.status,
            latError = latResult.status,
            langError = langResult.status,
//            descriptionError = descriptionResult.status,
//            dateError = dateResult.status,
//            timeError = timeResult.status,
//            weightError = weightResult.status,
//            lengthError = lengthResult.status,
        )

//        return dateResult.status && timeResult.status && weightResult.status && lengthResult.status  && nameResult.status && latResult.status && langResult.status && descriptionResult.status
        return nameResult.status && latResult.status && langResult.status
    }

    private fun savePoint(view: View?) = viewModelScope.launch {
        when (locationUIState.value.radioButton) {
            R.id.rbLocation -> {
                if (validateLocationDataWithRules()) {
                    val latLng =
                        convertCoordinatesToLatLng(locationUIState.value.latFormat + "," + locationUIState.value.langFormat)
                    if (latLng != null) {
                        val (latitude, longitude) = latLng
                        Log.i("convertCoordinates", "Latitude: $latitude, Longitude: $longitude")
                        if (locationUIState.value.trollingId == null) {
                            locationUIState.value.lat = latitude.toString()
                            locationUIState.value.lang = longitude.toString()
                        }
                    } else {
                        Log.i("convertCoordinates", "Invalid coordinate string format.")
                        return@launch
                    }
                    view?.let { applicationContext.hideKeyboardFrom(it) }
                    if (applicationContext.isNetworkAvailable() && locationUIState.value.trollingId == null) {
                        _savePointResponse.value = NetworkResult.Loading()
                        val date = Date()
                        if (locationUIState.value.image.isEmpty()) {
                            val model = SavePointsData(
                                null,
                                0,
                                locationUIState.value.image,
                                locationUIState.value.lat,
                                locationUIState.value.lang,
                                locationUIState.value.description,
                                locationUIState.value.name,
                                0,
                                locationUIState.value.trollingId,
                                date.toFormat("yyyy-MM-dd"),
                                date.toFormat("kk:mm:ss"),
                                "${date.time}"
                            )
                            localRepository.savePoints(model).collect { localValues ->
                                val jsonObject = JsonObject()
                                jsonObject.addProperty("point_name", locationUIState.value.name)
                                jsonObject.addProperty("latitude", locationUIState.value.lat)
                                jsonObject.addProperty("longitude", locationUIState.value.lang)
                                jsonObject.addProperty(
                                    "description",
                                    locationUIState.value.description
                                )
                                jsonObject.addProperty("date", date.toFormat("yyyy-MM-dd"))
                                jsonObject.addProperty("time", date.toFormat("kk:mm:ss"))
                                jsonObject.addProperty("local_db_id", localValues.toInt())
                                jsonObject.addProperty("local_db_unique_id", date.time.toString())
                                if (locationUIState.value.trollingId != null) {
                                    jsonObject.addProperty(
                                        "trolling_id",
                                        locationUIState.value.trollingId
                                    )
                                }
                                if (locationUIState.value.pointId != null) {
                                    jsonObject.addProperty("id", locationUIState.value.pointId)
                                    repository.updatePoints(jsonObject).collect { values ->
                                        if (values.data?.status == true && values.data.statusCode == 200) {
                                            localRepository.deletePoint(localValues.toInt())
                                                .collect() {
                                                    _savePointResponse.value = values
                                                }
                                        } else {
                                            _savePointResponse.value = values
                                        }
                                    }
                                } else {
                                    repository.savePoints(jsonObject).collect { values ->
                                        if (values.data?.status == true && values.data.statusCode == 200) {
                                            localRepository.deletePoint(localValues.toInt())
                                                .collect() {
                                                    _savePointResponse.value = values
                                                }
                                        } else {
                                            _savePointResponse.value = values
                                        }
                                    }
                                }
                            }
                        } else {
                            val multipartBody: MultipartBody.Part? =
                                if (!URLUtil.isValidUrl(locationUIState.value.image)) {
                                    val file: File? = File(locationUIState.value.image)
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
                            val model = SavePointsData(
                                null,
                                0,
                                locationUIState.value.image,
                                locationUIState.value.lat,
                                locationUIState.value.lang,
                                locationUIState.value.description,
                                locationUIState.value.name,
                                0,
                                locationUIState.value.trollingId,
                                date.toFormat("yyyy-MM-dd"),
                                date.toFormat("kk:mm:ss"),
                                "${date.time}"
                            )
                            localRepository.savePoints(model).collect { localValues ->
                                if (locationUIState.value.pointId != null) {
                                    repository.updatePoints(
                                        locationUIState.value.pointId.toString().toRequestBody(),
                                        locationUIState.value.name.toRequestBody(),
                                        locationUIState.value.lat.toRequestBody(),
                                        locationUIState.value.lang.toRequestBody(),
                                        locationUIState.value.description.toRequestBody(),
                                        multipartBody
                                    ).collect { values ->
                                        if (values.data?.status == true && values.data.statusCode == 200) {
                                            localRepository.deletePoint(localValues.toInt())
                                                .collect {
                                                    _savePointResponse.value = values
                                                }
                                        } else {
                                            _savePointResponse.value = values
                                        }
                                    }
                                } else {
                                    repository.savePoints(
                                        locationUIState.value.name.toRequestBody(),
                                        locationUIState.value.lat.toRequestBody(),
                                        locationUIState.value.lang.toRequestBody(),
                                        locationUIState.value.description.toRequestBody(),
                                        model.date.toRequestBody(),
                                        model.time.toRequestBody(),
                                        localValues.toString().toRequestBody(),
                                        model.local_db_unique_id.toRequestBody(),
                                        multipartBody
                                    ).collect { values ->
                                        if (values.data?.status == true && values.data.statusCode == 200) {
                                            localRepository.deletePoint(localValues.toInt())
                                                .collect {
                                                    _savePointResponse.value = values
                                                }
                                        } else {
                                            _savePointResponse.value = values
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        _savePointResponse.value = NetworkResult.Loading()
                        val date = Date()
                        if (locationUIState.value.trollingId != null) {
                            val dummyData =
                                "Location saved in trolling at ${date.toFormat("yyyy-MM-dd")} ${
                                    date.toFormat("kk:mm:ss")
                                }"
                            onNameChangeEvent("${date.toFormat("yyyy-MM-dd")} ${date.toFormat("kk:mm:ss")}")
                            onDescriptionChangeEvent(dummyData)
                        }
                        val model = SavePointsData(
                            null,
                            0,
                            locationUIState.value.image,
                            locationUIState.value.lat,
                            locationUIState.value.lang,
                            locationUIState.value.description,
                            locationUIState.value.name,
                            0,
                            locationUIState.value.trollingId,
                            date.toFormat("yyyy-MM-dd"),
                            date.toFormat("kk:mm:ss"),
                            "${date.time}"
                        )
                        localRepository.savePoints(model).collect { values ->
                            if (values > 0) {
                                _savePointResponse.value = NetworkResult.Success(
                                    SavePointsModel(
                                        model,
                                        applicationContext.resources.getString(R.string.location_saved_successfully),
                                        true,
                                        200
                                    )
                                )
                            } else {
                                _savePointResponse.value = NetworkResult.Success(
                                    SavePointsModel(
                                        model,
                                        applicationContext.resources.getString(R.string.something_went_wrong),
                                        false,
                                        400
                                    )
                                )
                            }
                        }
//                        _savePointResponse.value = NetworkResult.NoInternet(
//                            applicationContext.resources.getString(
//                                R.string.no_internet
//                            )
//                        )
                    }
                }
            }

            R.id.rbFishlog -> {
                if (validateFishDataWithRules()) {
                    val latLng =
                        convertCoordinatesToLatLng(locationUIState.value.latFormat + "," + locationUIState.value.langFormat)
                    if (latLng != null) {
                        val (latitude, longitude) = latLng
                        Log.i("convertCoordinates", "Latitude: $latitude, Longitude: $longitude")
                        if (locationUIState.value.trollingId == null) {
                            locationUIState.value.lat = latitude.toString()
                            locationUIState.value.lang = longitude.toString()
                        }
                    } else {
                        Log.i("convertCoordinates", "Invalid coordinate string format.")
                        return@launch
                    }
                    view?.let { applicationContext.hideKeyboardFrom(it) }
                    if (applicationContext.isNetworkAvailable() && locationUIState.value.trollingId == null) {
                        _saveFishLogResponse.value = NetworkResult.Loading()
                        val date = Date()
                        var length = ""
                        if(locationUIState.value.length.isNotEmpty()){
                            length = "${locationUIState.value.length}'"
                        }
                        if(locationUIState.value.lengthInches.isNotEmpty()){
                            length = length+"${locationUIState.value.lengthInches}\""
                        }
                        length = length.replace(" ","")
                        if (locationUIState.value.image.isEmpty()) {
                            val model = SaveFishLogData(
                                null,
                                locationUIState.value.date,
                                locationUIState.value.description,
                                locationUIState.value.name,
                                0,
                                locationUIState.value.image,
                                locationUIState.value.lat,
                                locationUIState.value.lang,
                                length,
                                locationUIState.value.time.replace(" ", "") + ":00",
                                0,
                                locationUIState.value.weight,
                                locationUIState.value.trollingId,
                                date.toFormat("yyyy-MM-dd"),
                                date.toFormat("kk:mm:ss"),
                                "${date.time}"
                            )
                            localRepository.saveFishLog(model).collect { localValues ->
                                val jsonObject = JsonObject()
                                jsonObject.addProperty("fish_name", locationUIState.value.name)
                                jsonObject.addProperty("latitude", locationUIState.value.lat)
                                jsonObject.addProperty("longitude", locationUIState.value.lang)
                                jsonObject.addProperty(
                                    "description",
                                    locationUIState.value.description
                                )
                                jsonObject.addProperty("length", length)
                                jsonObject.addProperty("weight", locationUIState.value.weight)
                                jsonObject.addProperty("date", locationUIState.value.date)
                                if (locationUIState.value.time.isNotEmpty()) {
                                    jsonObject.addProperty(
                                        "time",
                                        locationUIState.value.time.replace(" ", "") + ":00"
                                    )
                                }
                                jsonObject.addProperty("local_db_id", localValues.toInt())
                                jsonObject.addProperty("local_db_unique_id", date.time.toString())
                                if (locationUIState.value.trollingId != null) {
                                    jsonObject.addProperty(
                                        "trolling_id",
                                        locationUIState.value.trollingId
                                    )
                                }
                                if (locationUIState.value.pointId != null) {
                                    jsonObject.addProperty("id", locationUIState.value.pointId)
                                    repository.updateFishLog(jsonObject).collect { values ->
                                        if (values.data?.status == true && values.data.statusCode == 200) {
                                            localRepository.deleteFishLog(localValues.toInt())
                                                .collect {
                                                    _saveFishLogResponse.value = values
                                                }
                                        } else {
                                            _saveFishLogResponse.value = values
                                        }
                                    }
                                } else {
                                    repository.saveFishLog(jsonObject).collect { values ->
                                        if (values.data?.status == true && values.data.statusCode == 200) {
                                            localRepository.deleteFishLog(localValues.toInt())
                                                .collect {
                                                    _saveFishLogResponse.value = values
                                                }
                                        } else {
                                            _saveFishLogResponse.value = values
                                        }
                                    }
                                }
                            }
                        } else {
                            val multipartBody: MultipartBody.Part? =
                                if (!URLUtil.isValidUrl(locationUIState.value.image)) {
                                    val file: File? = File(locationUIState.value.image)
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
                            val model = SaveFishLogData(
                                null,
                                locationUIState.value.date,
                                locationUIState.value.description,
                                locationUIState.value.name,
                                0,
                                locationUIState.value.image,
                                locationUIState.value.lat,
                                locationUIState.value.lang,
                                length,
                                locationUIState.value.time.replace(" ", "") + ":00",
                                0,
                                locationUIState.value.weight,
                                locationUIState.value.trollingId,
                                date.toFormat("yyyy-MM-dd"),
                                date.toFormat("kk:mm:ss"),
                                "${date.time}"
                            )
                            localRepository.saveFishLog(model).collect { localValues ->
                                if (locationUIState.value.pointId != null) {
                                    repository.updateFishLog(
                                        locationUIState.value.pointId.toString().toRequestBody(),
                                        locationUIState.value.name.toRequestBody(),
                                        locationUIState.value.lat.toRequestBody(),
                                        locationUIState.value.lang.toRequestBody(),
                                        locationUIState.value.description.toRequestBody(),
                                        length.toRequestBody(),
                                        locationUIState.value.weight.toRequestBody(),
                                        locationUIState.value.date.toRequestBody(),
                                        if (locationUIState.value.time.isNotEmpty()) {
                                            (locationUIState.value.time.replace(
                                                " ",
                                                ""
                                            ) + ":00").toRequestBody()
                                        } else {
                                            "".toRequestBody()
                                        },
                                        multipartBody
                                    ).collect { values ->
                                        if (values.data?.status == true && values.data.statusCode == 200) {
                                            localRepository.deleteFishLog(localValues.toInt())
                                                .collect {
                                                    _saveFishLogResponse.value = values
                                                }
                                        } else {
                                            _saveFishLogResponse.value = values
                                        }
                                    }
                                } else {
                                    repository.saveFishLog(
                                        locationUIState.value.name.toRequestBody(),
                                        locationUIState.value.lat.toRequestBody(),
                                        locationUIState.value.lang.toRequestBody(),
                                        locationUIState.value.description.toRequestBody(),
                                        length.toRequestBody(),
                                        locationUIState.value.weight.toRequestBody(),
                                        locationUIState.value.date.toRequestBody(),
                                        if (locationUIState.value.time.isNotEmpty()) {
                                            (locationUIState.value.time.replace(
                                                " ",
                                                ""
                                            ) + ":00").toRequestBody()
                                        } else {
                                            "".toRequestBody()
                                        },
                                        localValues.toString().toRequestBody(),
                                        model.local_db_unique_id.toRequestBody(),
                                        multipartBody
                                    ).collect { values ->
                                        if (values.data?.status == true && values.data.statusCode == 200) {
                                            localRepository.deleteFishLog(localValues.toInt())
                                                .collect {
                                                    _saveFishLogResponse.value = values
                                                }
                                        } else {
                                            _saveFishLogResponse.value = values
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        _saveFishLogResponse.value = NetworkResult.Loading()
                        val date = Date()
                        var length = ""
                        if(locationUIState.value.length.isNotEmpty()){
                            length = "${locationUIState.value.length}'"
                        }
                        if(locationUIState.value.lengthInches.isNotEmpty()){
                            length = length+"${locationUIState.value.lengthInches}\""
                        }
                        length = length.replace(" ","")
                        if (locationUIState.value.trollingId != null) {
                            onDateChangeEvent(date.toFormat("yyyy-MM-dd"))
                            onTimeChangeEvent(date.toFormat("kk:mm:ss"))
                            val dummyData =
                                "Fish log saved in trolling at ${date.toFormat("yyyy-MM-dd")} ${
                                    date.toFormat("kk:mm:ss")
                                }"
                            onNameChangeEvent("${date.toFormat("yyyy-MM-dd")} ${date.toFormat("kk:mm:ss")}")
                            onDescriptionChangeEvent(dummyData)
                        }
                        val model = SaveFishLogData(
                            null,
                            locationUIState.value.date,
                            locationUIState.value.description,
                            locationUIState.value.name,
                            0,
                            locationUIState.value.image,
                            locationUIState.value.lat,
                            locationUIState.value.lang,
                            length,
                            locationUIState.value.time.replace(" ", ""),
                            0,
                            locationUIState.value.weight,
                            locationUIState.value.trollingId,
                            date.toFormat("yyyy-MM-dd"),
                            date.toFormat("kk:mm:ss"),
                            "${date.time}"
                        )
                        localRepository.saveFishLog(model).collect { values ->
                            if (values > 0) {
                                _saveFishLogResponse.value = NetworkResult.Success(
                                    SaveFishLogModel(
                                        model,
                                        applicationContext.resources.getString(R.string.fish_log_saved_successfully),
                                        true,
                                        200
                                    )
                                )
                            } else {
                                _saveFishLogResponse.value = NetworkResult.Success(
                                    SaveFishLogModel(
                                        model,
                                        applicationContext.resources.getString(R.string.something_went_wrong),
                                        false,
                                        400
                                    )
                                )
                            }
                        }
//                        _saveFishLogResponse.value = NetworkResult.NoInternet(
//                            applicationContext.resources.getString(
//                                R.string.no_internet
//                            )
//                        )
                    }
                }
            }
        }
    }

    fun getLocationById(pointId: Int) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _savePointResponse.value = NetworkResult.Loading()
            repository.getPointById(pointId).collect { values ->
                _savePointResponse.value = values
            }
        } else {
            _savePointResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }

    fun getFishLogById(pointId: Int) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _saveFishLogResponse.value = NetworkResult.Loading()
            repository.getFishLogById(pointId).collect { values ->
                _saveFishLogResponse.value = values
            }
        } else {
            _saveFishLogResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }

    fun resetResponse() {
        _savePointResponse.value = NetworkResult.NoCallYet()
        _saveFishLogResponse.value = NetworkResult.NoCallYet()
    }

}