package com.cp.fishthebreak.worker

import android.content.Context
import com.cp.fishthebreak.di.local.TrollingRepository
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.trolling.SpeedMinMax
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
import javax.inject.Singleton

@Singleton
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
    suspend fun updateTrollingStatus(
    ): Flow<Int> {
        return flow {
            emit(localDataSource.updateTrollingStatus())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteTrolling(
        trolling: Trolling
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deleteTrolling(trolling))
        }.flowOn(Dispatchers.IO)
    }
    suspend fun deleteTrollingById(
        trollingId: Long
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deleteTrollingById(trollingId))
        }.flowOn(Dispatchers.IO)
    }
    suspend fun deleteTrollingPoints(
        idList: List<Int>
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deleteTrollingPoints(idList))
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

    suspend fun getAllUnSyncTrolling(
    ): Flow<List<Trolling>> {
        return flow {
            emit(localDataSource.getAllUnSyncTrolling())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllSyncTrolling(
    ): Flow<List<Trolling>> {
        return flow {
            emit(localDataSource.getAllSyncTrolling())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTrollingById(
        id: Long
    ): Flow<TrillingWithPoints> {
        return flow {
            emit(localDataSource.getTrollingById(id))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getPointsByTrollingId(
        id: Long,
        limit: Int? = 100
    ): Flow<List<TrollingPoint>> {
        return flow {
            emit(localDataSource.getPointsByTrollingId(id, limit))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getMinMaxSpeed(
        id: Long
    ): Flow<SpeedMinMax> {
        return flow {
            emit(localDataSource.getMinMaxSpeed(id))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSavePointByTrollingId(
        trollingId: Long
    ): Flow<List<SavePointsData>> {
        return flow {
            emit(localDataSource.getSavePointByTrollingId(trollingId))
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getSaveFishLogByTrollingId(
        trollingId: Long
    ): Flow<List<SaveFishLogData>> {
        return flow {
            emit(localDataSource.getSaveFishLogByTrollingId(trollingId))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSavePoints(
    ): Flow<List<SavePointsData>> {
        return flow {
            emit(localDataSource.getSavePoints())
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getSaveFishLogs(
    ): Flow<List<SaveFishLogData>> {
        return flow {
            emit(localDataSource.getSaveFishLogs())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deletePoint(
        points: SavePointsData
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deletePoint(points))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deletePointByTrollingId(
        trollingId: Long
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deletePointByTrollingId(trollingId))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteFishLog(
        fishLog: SaveFishLogData
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deleteFishLog(fishLog))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteFishLogByTrollingId(
        trollingId: Long
    ): Flow<Int> {
        return flow {
            emit(localDataSource.deleteFishLogByTrollingId(trollingId))
        }.flowOn(Dispatchers.IO)
    }
}