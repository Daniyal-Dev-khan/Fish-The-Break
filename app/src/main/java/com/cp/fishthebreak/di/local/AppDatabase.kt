package com.cp.fishthebreak.di.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.trolling.Trolling
import com.cp.fishthebreak.models.trolling.TrollingPoint

@Database(entities = [Trolling::class, TrollingPoint::class, SavePointsData::class, SaveFishLogData::class, OfflineMap::class], version = 8,
    autoMigrations = [
        AutoMigration (
            from = 1,
            to = 2
        ),
        AutoMigration (
            from = 2,
            to = 3
        ),
        AutoMigration (
            from = 3,
            to = 4
        ),
        AutoMigration (
            from = 4,
            to = 5
        ),
        AutoMigration (
            from = 5,
            to = 6
        ),
        AutoMigration (
            from = 6,
            to = 7
        ),
        AutoMigration (
            from = 7,
            to = 8
        ),
    ],
    exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trollingDao(): TrollingDao
}
//https://developer.android.com/training/data-storage/room/migrating-db-versions#kotlin_1