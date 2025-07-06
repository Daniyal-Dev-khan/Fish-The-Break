package com.cp.fishthebreak.di.local

import android.content.Context
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.trolling.TrillingWithPoints
import com.cp.fishthebreak.models.trolling.Trolling
import com.cp.fishthebreak.models.trolling.TrollingPoint
import com.cp.fishthebreak.utils.SharePreferenceHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class LocalRepository  @Inject constructor(
    private val localDataSource: TrollingRepository,
    private val preferenceHelper: SharePreferenceHelper,
    @ApplicationContext private val mContext: Context
){
    suspend fun saveTrolling(
        trolling: Trolling
    ): Flow<Long> {
        return flow {
            emit(localDataSource.saveTrolling(trolling))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateTrollingName(
        trolling: Trolling
    ): Flow<Int> {
        return flow {
            emit(localDataSource.updateTrollingName(trolling))
        }.flowOn(Dispatchers.IO)
    }
    suspend fun saveTrollingPoints(
        points: TrollingPoint
    ): Flow<Long> {
        return flow {
            emit(localDataSource.saveTrollingPoints(points))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllTrollingWithPoints(
    ): Flow<List<TrillingWithPoints>> {
        return flow {
            emit(localDataSource.getAllTrollingWithPoints())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTrollingById(
        id: Long
    ): Flow<TrillingWithPoints> {
        return flow {
            emit(localDataSource.getTrollingById(id))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun savePoints(
        points: SavePointsData
    ): Flow<Long> {
        return flow {
            emit(localDataSource.savePoints(points))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveFishLog(
        fishLog: SaveFishLogData
    ): Flow<Long> {
        return flow {
            emit(localDataSource.saveFishLog(fishLog))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deletePoint(
        pointId: Int
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deletePoint(pointId))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteFishLog(
        fishLogId: Int
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deleteFishLog(fishLogId))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteOfflineMap(
        map: OfflineMap
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deleteOfflineMap(map))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteInCompleteMap(
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deleteInCompleteMap())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getOfflineMap(
    ): Flow<List<OfflineMap>> {
        return flow {
            emit(localDataSource.getOfflineMap())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveOfflineMap(
        map: OfflineMap
    ): Flow<Long> {
        return flow {
            emit(localDataSource.saveOfflineMap(map))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateOfflineMap(
        map: OfflineMap
    ): Flow<Int> {
        return flow {
            emit(localDataSource.updateOfflineMap(map))
        }.flowOn(Dispatchers.IO)
    }
    suspend fun updateOfflineMapPath(
        mapPath: String,
        id: Int
    ): Flow<Int> {
        return flow {
            emit(localDataSource.updateOfflineMap(mapPath,id))
        }.flowOn(Dispatchers.IO)
    }

}