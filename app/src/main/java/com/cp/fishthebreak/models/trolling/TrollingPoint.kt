package com.cp.fishthebreak.models.trolling

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(
    foreignKeys = [ForeignKey(
        entity = Trolling::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("trollingId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )]
)
data class TrollingPoint(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "trollingId") val trollingId: Long,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "speed") val speed: Float,
    @ColumnInfo(name = "time_stamp") val timeStamp: Long
){
    constructor(trollingId: Long, latitude: Double, longitude: Double, speed: Float, timeStamp: Long) : this(0,trollingId , latitude, longitude, speed, timeStamp)
}