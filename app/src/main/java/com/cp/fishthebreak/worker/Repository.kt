package com.cp.fishthebreak.worker

import android.content.Context
import com.cp.fishthebreak.di.BaseApiResponse
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.RemoteDataSource
import com.cp.fishthebreak.models.points.GetFishLogsModel
import com.cp.fishthebreak.models.points.GetSavedPointsModel
import com.cp.fishthebreak.models.points.SaveFishLogModel
import com.cp.fishthebreak.models.points.SavePointsModel
import com.cp.fishthebreak.models.trolling.TrollingResponseModel
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val preferenceHelper: SharePreferenceHelper,
    @ApplicationContext private val mContext: Context
) : BaseApiResponse() {

    suspend fun savePoints(
        pointName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        date: RequestBody,
        time: RequestBody,
        local_db_id: RequestBody,
        local_db_unique_id: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.savePoints(
                    "Bearer $token",
                    pointName,
                    lat,
                    lang,
                    description,
                    date,
                    time,
                    local_db_id,
                    local_db_unique_id,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun savePoints(
        body: JsonObject
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.savePoints(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updatePoints(
        body: JsonObject
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updatePoints(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updatePoints(
        id: RequestBody,
        pointName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updatePoints(
                    "Bearer $token",
                    id,
                    pointName,
                    lat,
                    lang,
                    description,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getPointById(
        id: Int
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getPointById(
                    "Bearer $token",
                    id
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveFishLog(
        body: JsonObject
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveFishLog(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveFishLog(
        fishName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        length: RequestBody,
        weight: RequestBody,
        date: RequestBody,
        time: RequestBody,
        local_db_id: RequestBody,
        local_db_unique_id: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveFishLog(
                    "Bearer $token",
                    fishName,
                    lat,
                    lang,
                    description,
                    length,
                    weight,
                    date,
                    time,
                    local_db_id,
                    local_db_unique_id,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateFishLog(
        body: JsonObject
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateFishLog(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateFishLog(
        id: RequestBody?,
        fishName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        length: RequestBody,
        weight: RequestBody,
        date: RequestBody,
        time: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateFishLog(
                    "Bearer $token",
                    id,
                    fishName,
                    lat,
                    lang,
                    description,
                    length,
                    weight,
                    date,
                    time,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteFishLogById(
        body: JsonObject
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.deleteFishLogById("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }


    suspend fun saveTrolling(
        body: JsonObject
    ): Flow<NetworkResult<TrollingResponseModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveTrolling(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveTrollingPoints(
        body: JsonObject
    ): Flow<NetworkResult<TrollingResponseModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveTrollingPoints(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

}