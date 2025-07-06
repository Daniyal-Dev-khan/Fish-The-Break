package com.cp.fishthebreak.di.local

import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.trolling.Trolling
import com.cp.fishthebreak.models.trolling.TrollingPoint
import javax.inject.Inject

class TrollingRepository @Inject constructor(
    private val trollingDao: TrollingDao
) {
    suspend fun saveTrolling(trolling: Trolling) =
        trollingDao.saveTrolling(trolling)

    suspend fun updateTrollingName(trolling: Trolling) =
        trollingDao.updateTrollingName(trolling)
    suspend fun updateTrollingStatus() =
        trollingDao.updateTrollingStatus()

    suspend fun deleteTrolling(trolling: Trolling) =
        trollingDao.deleteTrolling(trolling)

    suspend fun deleteTrollingById(trollingId: Long) =
        trollingDao.deleteTrollingById(trollingId)

    suspend fun deleteTrollingPoints(idList: List<Int>) =
        trollingDao.deleteTrollingPoints(idList)

    suspend fun saveTrollingPoints(point: TrollingPoint) =
        trollingDao.saveTrollingPoints(point)

    suspend fun getAllTrollingWithPoints() =
        trollingDao.getAllTrollingWithPoints()

    suspend fun getAllUnSyncTrolling() =
        trollingDao.getAllUnSyncTrolling()

    suspend fun getAllSyncTrolling() =
        trollingDao.getAllSyncTrolling()

    suspend fun getTrollingById(id: Long) =
        trollingDao.getByTrollingId(id)

    suspend fun getPointsByTrollingId(id: Long, limit: Int?) =
        if(limit == null){
            trollingDao.getPointsByTrollingId(id)
        }else{
            trollingDao.getPointsByTrollingId(id, limit)
        }

    suspend fun getMinMaxSpeed(id: Long) =
        trollingDao.getMinMaxSpeed(id)

    suspend fun savePoints(points: SavePointsData) =
        trollingDao.savePoint(points)

    suspend fun saveFishLog(fishLog: SaveFishLogData) =
        trollingDao.saveFishLog(fishLog)

    suspend fun getSavePointByTrollingId(trollingId: Long) =
        trollingDao.getSavePointByTrollingId(trollingId)

    suspend fun getSaveFishLogByTrollingId(trollingId: Long) =
        trollingDao.getSaveFishLogByTrollingId(trollingId)

    suspend fun getSavePoints() =
        trollingDao.getSavePoints()

    suspend fun getSaveFishLogs() =
        trollingDao.getSaveFishLogs()

    suspend fun deletePoint(points: SavePointsData) =
        trollingDao.deletePoint(points)

    suspend fun deletePoint(pointId: Int) =
        trollingDao.deletePoint(pointId)

    suspend fun deletePointByTrollingId(trollingId: Long) =
        trollingDao.deletePointByTrollingId(trollingId)

    suspend fun deleteFishLog(fishLog: SaveFishLogData) =
        trollingDao.deleteFishLog(fishLog)

    suspend fun deleteFishLogByTrollingId(trollingId: Long) =
        trollingDao.deleteFishLogByTrollingId(trollingId)

    suspend fun deleteFishLog(fishLogId: Int) =
        trollingDao.deleteFishLog(fishLogId)

    suspend fun saveOfflineMap(map: OfflineMap) =
        trollingDao.saveOfflineMap(map)

    suspend fun updateOfflineMap(map: OfflineMap) =
        trollingDao.updateOfflineMap(map)

    suspend fun updateOfflineMap(mapPath: String, id: Int) =
        trollingDao.updateOfflineMapPath(mapPath, id)

    suspend fun deleteOfflineMap(map: OfflineMap) =
        trollingDao.deleteOfflineMap(map)

    suspend fun deleteInCompleteMap() =
        trollingDao.deleteInCompleteMap()

    suspend fun getOfflineMap() =
        trollingDao.getOfflineMap()

}