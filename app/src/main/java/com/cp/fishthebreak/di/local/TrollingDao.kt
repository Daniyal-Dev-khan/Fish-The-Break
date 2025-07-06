package com.cp.fishthebreak.di.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.trolling.SpeedMinMax
import com.cp.fishthebreak.models.trolling.TrillingWithPoints
import com.cp.fishthebreak.models.trolling.Trolling
import com.cp.fishthebreak.models.trolling.TrollingPoint

@Dao
interface TrollingDao {

    @Insert
    suspend fun saveTrolling(trolling: Trolling): Long

    @Update
    suspend fun updateTrollingName(trolling: Trolling): Int

    @Query("UPDATE trolling SET is_saved = 1 WHERE is_saved=0")
    suspend fun updateTrollingStatus(): Int

    @Delete
    suspend fun deleteTrolling(trolling: Trolling): Int

    @Query("delete from trolling where id =:trollingId")
    suspend fun deleteTrollingById(trollingId: Long): Int

    @Query("delete from trollingpoint where id in (:idList)")
    suspend fun deleteTrollingPoints(idList: List<Int>): Int

    @Insert
    suspend fun saveTrollingPoints(points: TrollingPoint): Long

    @Transaction
    @Query("SELECT * FROM trolling")
    suspend fun getAllTrollingWithPoints(): List<TrillingWithPoints>

    @Query("SELECT * FROM trolling WHERE is_sync=0 AND is_saved=1 LIMIT 1")
    suspend fun getAllUnSyncTrolling(): List<Trolling>

    @Query("SELECT * FROM trolling WHERE is_sync=1 LIMIT 1")
    suspend fun getAllSyncTrolling(): List<Trolling>

    @Transaction
    @Query("SELECT min(time_stamp) AS startTime, max(time_stamp) AS endTime, min(speed) AS minValue, max(speed) AS maxValue, avg(speed) AS averageValue FROM trollingpoint WHERE trollingId = :id")
    suspend fun getMinMaxSpeed(id: Long): SpeedMinMax

    @Transaction
    @Query("SELECT * FROM trolling WHERE id = :id")
    suspend fun getByTrollingId(id: Long): TrillingWithPoints

    @Transaction
    @Query("SELECT * FROM trollingpoint WHERE trollingId = :id LIMIT :limit")
    suspend fun getPointsByTrollingId(id: Long, limit: Int): List<TrollingPoint>

    @Transaction
    @Query("SELECT * FROM trollingpoint WHERE trollingId = :id")
    suspend fun getPointsByTrollingId(id: Long): List<TrollingPoint>

    @Insert
    suspend fun savePoint(points: SavePointsData): Long

    @Insert
    suspend fun saveFishLog(fishLog: SaveFishLogData): Long

    @Transaction
    @Query("SELECT * FROM SavePointsData WHERE trolling_id = :id LIMIT 1")
    suspend fun getSavePointByTrollingId(id: Long): List<SavePointsData>

    @Transaction
    @Query("SELECT * FROM SaveFishLogData WHERE trolling_id = :id LIMIT 1")
    suspend fun getSaveFishLogByTrollingId(id: Long): List<SaveFishLogData>

    @Transaction
    @Query("SELECT * FROM SavePointsData WHERE trolling_id IS NULL LIMIT 1")
    suspend fun getSavePoints(): List<SavePointsData>

    @Transaction
    @Query("SELECT * FROM SaveFishLogData WHERE trolling_id IS NULL LIMIT 1")
    suspend fun getSaveFishLogs(): List<SaveFishLogData>

    @Delete
    suspend fun deletePoint(points: SavePointsData): Int

    @Query("delete from savepointsdata where id = :pointId")
    suspend fun deletePoint(pointId: Int): Int

    @Query("delete from savepointsdata where trolling_id = :trollingId")
    suspend fun deletePointByTrollingId(trollingId: Long): Int

    @Delete
    suspend fun deleteFishLog(fishLog: SaveFishLogData): Int

    @Query("delete from savefishlogdata where id = :fishLogId")
    suspend fun deleteFishLog(fishLogId: Int): Int

    @Query("delete from savefishlogdata where trolling_id = :trollingId")
    suspend fun deleteFishLogByTrollingId(trollingId: Long): Int

    @Insert
    suspend fun saveOfflineMap(map: OfflineMap): Long

    @Update
    suspend fun updateOfflineMap(map: OfflineMap): Int

    @Query("UPDATE offlinemap SET map_path = :mapPath WHERE id = :id")
    suspend fun updateOfflineMapPath(mapPath: String, id: Int): Int

    @Delete
    suspend fun deleteOfflineMap(map: OfflineMap): Int

    @Query("DELETE from offlinemap WHERE map_path IS NULL")
    suspend fun deleteInCompleteMap(): Int

    @Transaction
    @Query("SELECT * FROM offlinemap WHERE map_path IS NOT NULL")
    suspend fun getOfflineMap(): List<OfflineMap>
}