package com.cp.fishthebreak.models.trolling

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Trolling(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "server_id") var serverId: Long?,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "min_speed") val minSpeed: Float,
    @ColumnInfo(name = "max_speed") val maxSped: Float,
    @ColumnInfo(name = "avg_speed") val avgSpeed: Float,
    @ColumnInfo(name = "trolling_time") val trollingTime: String?,
    @ColumnInfo(name = "is_sync") var isSync: Boolean,
    @ColumnInfo(name = "is_saved") var isSaved: Boolean?
)