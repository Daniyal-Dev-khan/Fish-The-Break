package com.cp.fishthebreak.worker

import android.content.Context
import android.location.Location
import android.util.Log
import android.webkit.URLUtil
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.trolling.Trolling
import com.cp.fishthebreak.utils.KmToKnots
import com.cp.fishthebreak.utils.KmToNauticalMiles
import com.cp.fishthebreak.utils.getTrollingTime
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.roundOffDecimal
import com.cp.fishthebreak.utils.toFormat
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Date

@HiltWorker
class SyncTrollingWorker @AssistedInject constructor(
    private val localRepository: LocalRepository,
    private val repository: Repository,
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        // Do your background work
        // Return result
        return withContext(Dispatchers.IO) {
            if (context.isNetworkAvailable()) {
                startDbSync()
            }
            Result.success()
        }
    }

    private suspend fun startDbSync() {
        localRepository.getAllUnSyncTrolling().collect { values ->
            if (values.isNotEmpty()) {
                values.forEach { trolling ->
                    Log.i("startDbSync", "uploadTrolling(trolling)")
                    uploadTrolling(trolling)
                }
            } else {
                Log.i("startDbSync", "getTrollingPoints()")
                getTrollingPoints()
            }
        }
    }

    private suspend fun getTrollingPoints() {
        localRepository.getAllSyncTrolling().collect { values ->
            if (values.isNotEmpty()) {
                values.forEach { trolling ->
                    Log.i("getTrollingPoints", "uploadSavedTrollingLocations(trolling)")
                    uploadSavedTrollingLocations(trolling)
                }
            } else {
                Log.i("getTrollingPoints", "uploadSavedLocations()")
                uploadSavedLocations()
            }
        }
    }

    private suspend fun uploadTrollingPoints(trolling: Trolling) {
        localRepository.getPointsByTrollingId(trolling.id).collect { point ->
            val jsonObject = JsonObject()
            val list = JsonArray()
            val listId = ArrayList<Int>()
            point.forEach { points ->
                listId.add(points.id)
                val obj = JsonObject()
                obj.addProperty("latitude", points.latitude.toString())
                obj.addProperty("longitude", points.longitude.toString())
                obj.addProperty("trolling_id", trolling.serverId)
                val date = Date(points.timeStamp)
                obj.addProperty("date", date.toFormat("yyyy-MM-dd"))
                obj.addProperty("time", date.toFormat("kk:mm:ss"))
                obj.addProperty("local_db_id", points.id)
                obj.addProperty("local_db_unique_id", points.timeStamp)
                list.add(obj)
            }
            if (point.isNotEmpty()) {
                jsonObject.add("points", list)
                repository.saveTrollingPoints(jsonObject).collect { response ->
                    Log.i("TrollingResponseSavePoints", response.message ?: "")
                    Log.i("TrollingResponseSavePoints", response.data?.message ?: "")
                    if (response.data?.status == true && response.data.statusCode == 200) {
                        localRepository.deleteTrollingPoints(listId).collect { deletedTrolling ->
                            if (deletedTrolling > 0) {
                                Log.i("uploadTrollingPoints", "uploadTrollingPoints(trolling)")
                                uploadTrollingPoints(trolling)
                            }
                        }
                    }
                }
            } else {
                localRepository.deleteTrolling(trolling).collect { deletedTrolling ->
                    if (deletedTrolling > 0) {
                        Log.i("uploadTrollingPoints", "startDbSync()")
                        startDbSync()
                    }
                }
            }
        }
    }

    private suspend fun uploadTrolling(trolling: Trolling) {
        localRepository.getMinMaxSpeed(trolling.id).collect { speed ->
            localRepository.getPointsByTrollingId(trolling.id, null).collect { point ->
                if (point.isEmpty()) {//TODO create a column isTrolling saved or not by user
                    localRepository.deleteTrolling(trolling).collect { deletedTrolling ->
                        if (deletedTrolling > 0) {
                            Log.i("uploadTrollingPoints", "startDbSync()")
                            startDbSync()
                        }
                    }
                    return@collect
                }
                var distance = 0.0F
                point.forEachIndexed { index, trollingPoint ->
                    if (index + 1 < point.size) {
                        val loc1 = Location("")
                        loc1.latitude = trollingPoint.latitude
                        loc1.longitude = trollingPoint.longitude
                        val loc2 = Location("")
                        loc2.latitude = point[index + 1].latitude
                        loc2.longitude = point[index + 1].longitude
                        distance += loc1.distanceTo(loc2)
                    }
                }
                val jsonObject = JsonObject()
                jsonObject.addProperty("trolling_name", trolling.name)
                jsonObject.addProperty("distance", "${distance.KmToNauticalMiles().roundOffDecimal()}NM")
                if (trolling.trollingTime.isNullOrEmpty()) {
                    jsonObject.addProperty(
                        "duration",
                        speed.startTime.getTrollingTime(speed.endTime) ?: "00:00"
                    )
                    jsonObject.addProperty("slowest_speed", "${(speed.minValue.KmToKnots()).toInt()}KN")
                    jsonObject.addProperty("average_speed", "${(speed.averageValue.KmToKnots()).toInt()}KN")
                    jsonObject.addProperty("highest_speed", "${(speed.maxValue.KmToKnots()).toInt()}KN")
                } else {
                    jsonObject.addProperty("duration", trolling.trollingTime ?: "00:00")
                    jsonObject.addProperty("slowest_speed", "${(trolling.minSpeed.KmToKnots()).toInt()}KN")
                    jsonObject.addProperty("average_speed", "${(trolling.avgSpeed.KmToKnots()).toInt()}KN")
                    jsonObject.addProperty("highest_speed", "${(trolling.maxSped.KmToKnots()).toInt()}KN")
                }
//        jsonObject.addProperty("lowest_water_temp","110°F")
//        jsonObject.addProperty("average_water_temp","115°F")
//        jsonObject.addProperty("highest_water_temp","120°F")
                jsonObject.addProperty("latitude", point.first().latitude.toString())
                jsonObject.addProperty("longitude", point.first().longitude.toString())
                jsonObject.addProperty("local_db_id", trolling.id)
                jsonObject.addProperty("local_db_unique_id", trolling.id.toString())
                val date = Date(trolling.id)
                jsonObject.addProperty("date", date.toFormat("yyyy-MM-dd"))
                jsonObject.addProperty("time", date.toFormat("kk:mm:ss"))
                Log.i("saveTrollingJson", jsonObject.toString())
                repository.saveTrolling(jsonObject).collect { response ->
                    Log.i("TrollingResponseSaveTrolling", response.message ?: "")
                    Log.i("TrollingResponseSaveTrolling", response.data?.message ?: "")
                    if (response.data?.status == true && response.data.statusCode == 200) {
                        trolling.isSync = true
                        trolling.serverId = response.data.data?.id?.toLong()
                        localRepository.updateTrollingName(trolling).collect { updated ->
                            Log.i("uploadTrolling", "uploadSavedLocations(trolling)")
                            uploadSavedTrollingLocations(trolling)
                        }
                    }
                }
            }
        }
    }

    private suspend fun uploadSavedTrollingLocations(trolling: Trolling) {
        localRepository.getSavePointByTrollingId(trolling.id).collect { points ->
            if (points.isNotEmpty()) {
                points.forEach { point ->
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("point_name", point.point_name)
                    jsonObject.addProperty("latitude", point.latitude)
                    jsonObject.addProperty("longitude", point.longitude)
                    jsonObject.addProperty("description", point.description)
                    jsonObject.addProperty("date", point.date)
                    jsonObject.addProperty("time", point.time)
                    jsonObject.addProperty("local_db_id", point.id)
                    jsonObject.addProperty("local_db_unique_id", point.local_db_unique_id)
                    jsonObject.addProperty("trolling_id", trolling.serverId)
                    Log.i("saveTrollingPointsJson", jsonObject.toString())
                    repository.savePoints(jsonObject).collect { response ->
                        Log.i("TrollingResponseSaveLocation", response.message ?: "")
                        Log.i("TrollingResponseSaveLocation", response.data?.message ?: "")
                        if (response.data?.status == true && response.data.statusCode == 200) {
                            localRepository.deletePoint(point).collect { deletedPoint ->
                                if (deletedPoint > 0) {
                                    Log.i(
                                        "uploadSavedTrollingLocations",
                                        "uploadSavedTrollingLocations(trolling)"
                                    )
                                    uploadSavedTrollingLocations(trolling)
                                }
                            }
                        }
                    }
                }
            } else {
                Log.i("uploadSavedLocations", "uploadSavedFishLogLocations(trolling)")
                uploadSavedFishLogLocations(trolling)
            }
        }
    }

    private suspend fun uploadSavedFishLogLocations(trolling: Trolling) {
        localRepository.getSaveFishLogByTrollingId(trolling.id).collect { points ->
            if (points.isNotEmpty()) {
                points.forEach { point ->
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("fish_name", point.fish_name)
                    jsonObject.addProperty("latitude", point.latitude)
                    jsonObject.addProperty("longitude", point.longitude)
                    jsonObject.addProperty("description", point.description)
                    if (point.length.isNullOrEmpty()) {
                        jsonObject.addProperty("length", "0")
                    } else {
                        jsonObject.addProperty("length", point.length ?: "0")
                    }
                    if (point.weight.isNullOrEmpty()) {
                        jsonObject.addProperty("weight", "0")
                    } else {
                        jsonObject.addProperty("weight", point.weight ?: "0")
                    }
                    jsonObject.addProperty("date", point.date)
                    jsonObject.addProperty("time", point.time)
                    jsonObject.addProperty("local_db_id", point.id)
                    jsonObject.addProperty("local_db_unique_id", point.local_db_unique_id)
                    jsonObject.addProperty("trolling_id", trolling.serverId)
                    Log.i("saveFishLogJson", jsonObject.toString())
                    repository.saveFishLog(jsonObject).collect { response ->
                        Log.i("TrollingResponseSaveFishLog", response.message ?: "")
                        Log.i("TrollingResponseSaveFishLog", response.data?.message ?: "")
                        if (response.data?.status == true && response.data.statusCode == 200) {
                            localRepository.deleteFishLog(point).collect { deletedPoint ->
                                if (deletedPoint > 0) {
                                    Log.i(
                                        "uploadSavedFishLog",
                                        "uploadSavedFishLogLocations(trolling)"
                                    )
                                    uploadSavedFishLogLocations(trolling)
                                }
                            }
                        }
                    }
                }
            } else {
                Log.i("uploadSavedFishLog", "uploadTrollingPoints(trolling)")
                uploadTrollingPoints(trolling)
            }
        }
    }

    private suspend fun uploadSavedLocations() {
        localRepository.getSavePoints().collect { points ->
            if (points.isNotEmpty()) {
                points.forEach { point ->
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("point_name", point.point_name)
                    jsonObject.addProperty("latitude", point.latitude)
                    jsonObject.addProperty("longitude", point.longitude)
                    jsonObject.addProperty("description", point.description)
                    jsonObject.addProperty("date", point.date)
                    jsonObject.addProperty("time", point.time)
                    jsonObject.addProperty("local_db_id", point.id)
                    jsonObject.addProperty("local_db_unique_id", point.local_db_unique_id)
                    Log.i("uploadSavedLocationsJson", jsonObject.toString())
                    if (point.image.isNullOrEmpty()) {
                        repository.savePoints(jsonObject).collect { response ->
                            Log.i("ResponseSaveLocation", response.message ?: "")
                            Log.i("ResponseSaveLocation", response.data?.message ?: "")
                            if (response.data?.status == true && response.data.statusCode == 200) {
                                localRepository.deletePoint(point).collect { deletedPoint ->
                                    if (deletedPoint > 0) {
                                        Log.i("uploadSavedLocations", "uploadSavedLocations()")
                                        uploadSavedLocations()
                                    }
                                }
                            }
                        }
                    } else {
                        val multipartBody: MultipartBody.Part? =
                            if (!URLUtil.isValidUrl(point.image)) {
                                val file: File? = File(point.image)
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
                        repository.savePoints(
                            (point.point_name ?: "").toRequestBody(),
                            (point.latitude ?: "").toRequestBody(),
                            (point.longitude ?: "").toRequestBody(),
                            (point.description ?: "").toRequestBody(),
                            point.date.toRequestBody(),
                            point.time.toRequestBody(),
                            point.id.toString().toRequestBody(),
                            point.local_db_unique_id.toRequestBody(),
                            multipartBody
                        ).collect { values ->
                            if (values.data?.status == true && values.data.statusCode == 200) {
                                localRepository.deletePoint(point).collect { deletedPoint ->
                                    if (deletedPoint > 0) {
                                        Log.i("uploadSavedLocations", "uploadSavedLocations()")
                                        uploadSavedLocations()
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Log.i("uploadSavedLocations", "uploadSavedFishLogs()")
                uploadSavedFishLogs()
            }
        }
    }

    private suspend fun uploadSavedFishLogs() {
        localRepository.getSaveFishLogs().collect { points ->
            if (points.isNotEmpty()) {
                points.forEach { point ->
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("fish_name", point.fish_name)
                    jsonObject.addProperty("latitude", point.latitude)
                    jsonObject.addProperty("longitude", point.longitude)
                    jsonObject.addProperty("description", point.description)
                    if (point.length.isNullOrEmpty()) {
                        jsonObject.addProperty("length", "0")
                    } else {
                        jsonObject.addProperty("length", point.length ?: "0")
                    }
                    if (point.weight.isNullOrEmpty()) {
                        jsonObject.addProperty("weight", "0")
                    } else {
                        jsonObject.addProperty("weight", point.weight ?: "0")
                    }
                    jsonObject.addProperty("date", point.date)
                    jsonObject.addProperty("time", point.time)
                    jsonObject.addProperty("local_db_id", point.id)
                    jsonObject.addProperty("local_db_unique_id", point.local_db_unique_id)
                    Log.i("savedFishLogsJson", jsonObject.toString())
                    if (point.image.isNullOrEmpty()) {
                        repository.saveFishLog(jsonObject).collect { response ->
                            Log.i("ResponseSaveFishLog", response.message ?: "")
                            Log.i("ResponseSaveFishLog", response.data?.message ?: "")
                            if (response.data?.status == true && response.data.statusCode == 200) {
                                localRepository.deleteFishLog(point).collect { deletedPoint ->
                                    if (deletedPoint > 0) {
                                        Log.i("uploadSavedFishLogs", "uploadSavedFishLogs()")
                                        uploadSavedFishLogs()
                                    }
                                }
                            }
                        }
                    } else {
                        val multipartBody: MultipartBody.Part? =
                            if (!URLUtil.isValidUrl(point.image)) {
                                val file: File? = File(point.image)
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
                        repository.saveFishLog(
                            (point.fish_name?:"").toRequestBody(),
                            (point.latitude?:"").toRequestBody(),
                            (point.longitude?:"").toRequestBody(),
                            (point.description?:"").toRequestBody(),
                            (point.length?:"").toRequestBody(),
                            (point.weight?:"").toRequestBody(),
                            (point.date?:"").toRequestBody(),
                            if (!point.time.isNullOrEmpty()) {
                                ((point.time ?: "").replace(" ", "") + ":00").toRequestBody()
                            } else {
                                "".toRequestBody()
                            },
                            point.id.toString().toRequestBody(),
                            point.local_db_unique_id.toRequestBody(),
                            multipartBody
                        ).collect { values ->
                            if (values.data?.status == true && values.data.statusCode == 200) {
                                localRepository.deleteFishLog(point).collect { deletedPoint ->
                                    if (deletedPoint > 0) {
                                        Log.i("uploadSavedFishLogs", "uploadSavedFishLogs()")
                                        uploadSavedFishLogs()
                                    }
                                }
                            }
                        }

                    }
                }
            } else {
                Log.i("uploadSavedFishLogs", "else")
            }
        }
    }
}

/*
 localRepository.getAllTrollingWithPoints().collect { values ->
                    values.forEach { trolling ->
                        val jsonObject = JsonObject()
                        val list = JsonArray()
                        trolling.points.forEach { points->
                            val obj = JsonObject()
                            obj.addProperty("latitude",points.latitude.toString())
                            obj.addProperty("longitude",points.longitude.toString())
                            list.add(obj)
                        }
                        jsonObject.addProperty("trolling_name",trolling.trolling.name)
                        jsonObject.addProperty("distance","12KM")
                        jsonObject.addProperty("slowest_speed","${trolling.trolling.minSpeed.toInt()}KM")
                        jsonObject.addProperty("average_speed","${trolling.trolling.avgSpeed.toInt()}KM")
                        jsonObject.addProperty("highest_speed","${trolling.trolling.maxSped.toInt()}KM")
                        jsonObject.addProperty("lowest_water_temp","110°F")
                        jsonObject.addProperty("average_water_temp","115°F")
                        jsonObject.addProperty("highest_water_temp","120°F")
                        jsonObject.addProperty("local_db_id",trolling.trolling.id)
                        jsonObject.addProperty("local_db_unique_id",trolling.trolling.id.toString())
                        jsonObject.add("trolling_point", list)
                        Log.i("saveTrolling", jsonObject.toString())
                        repository.saveTrolling(jsonObject).collect { response ->
                            Log.i("TrollingResponse", response.message?:"")
                            if (response.data?.status == true && response.data.statusCode == 200){
                                localRepository.deleteTrollingName(trolling.trolling).collect { deletedData ->
                                    Log.i("Trolling deleted","is deleted: $deletedData, data: ${trolling.trolling.id}")
                                }
                            }
                        }
                    }
                }
 */