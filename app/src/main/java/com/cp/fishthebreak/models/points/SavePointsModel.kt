package com.cp.fishthebreak.models.points

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.cp.fishthebreak.adapters.base.ListAdapterItem
import com.cp.fishthebreak.utils.getNauticalLatitude
import com.cp.fishthebreak.utils.getNauticalLongitude
import com.cp.fishthebreak.utils.stringToDouble
import java.io.Serializable
import kotlin.math.abs

data class SavePointsModel(
    val `data`: SavePointsData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

@Entity
data class SavePointsData(
    @ColumnInfo(name = "base_url") val base_url: String?,
    @PrimaryKey(autoGenerate = true) override val id: Int,
    @ColumnInfo(name = "image") var image: String?,
    @ColumnInfo(name = "latitude") var latitude: String?,
    @ColumnInfo(name = "longitude") var longitude: String?,
    @ColumnInfo(name = "description") var description: String?,
    @ColumnInfo(name = "point_name") var point_name: String?,
    @ColumnInfo(name = "type") var type: Int?,
    @ColumnInfo(name = "trolling_id") val trolling_id: Long?,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "time") var time: String,
    @ColumnInfo(name = "local_db_unique_id") val local_db_unique_id: String,
) : ListAdapterItem, Serializable {
    @Ignore
    var isSelected: Boolean = false
    constructor(
        base_url: String?,
        image: String?,
        lat: String?,
        lang: String?,
        description: String?,
        point_name: String?,
        type: Int?,
        trolling_id: Long?,
        date: String,
        time: String,
        local_db_unique_id: String,
    )
            : this(base_url, 0, image, lat, lang, description, point_name, type,trolling_id,date,time,local_db_unique_id)

    fun getLongitudeFormat(): String {
        return try {
            getNauticalLongitude((longitude?.toDouble() ?: 0.0))
        } catch (ex: Exception) {
            ""
        }
//        return try {
//            if (!longitude.isNullOrEmpty()) {
//                (Location.convert(
//                    longitude?.toDouble() ?: 0.0,
//                    Location.FORMAT_MINUTES
//                )).replace(":", "* ")
//            } else {
//                longitude ?: ""
//            }
//        }catch (ex: Exception){
//            ""
//        }
    }

    fun getLatitudeFormat(): String {
        return try {
            getNauticalLatitude((latitude?.toDouble() ?: 0.0))
        } catch (ex: Exception) {
            ""
        }
//        return try {
//            if (!latitude.isNullOrEmpty()) {
//                (Location.convert(latitude?.toDouble()?:0.0, Location.FORMAT_MINUTES)).replace(":", "* ")
//            } else {
//                latitude ?: ""
//            }
//        }catch (ex: Exception){
//            ""
//        }
    }

    fun getLatFromString(): Double?{
        return latitude.stringToDouble()
    }
    fun getLangFromString(): Double?{
        return longitude.stringToDouble()
    }
}