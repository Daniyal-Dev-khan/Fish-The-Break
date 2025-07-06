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
import com.cp.fishthebreak.utils.toDate
import com.cp.fishthebreak.utils.toFormat
import java.io.Serializable

data class SaveFishLogModel(
    val `data`: SaveFishLogData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

@Entity
data class SaveFishLogData(
    @ColumnInfo(name = "base_url") val base_url: String?,
    @ColumnInfo(name = "date") var date: String?,
    @ColumnInfo(name = "description") var description: String?,
    @ColumnInfo(name = "fish_name") var fish_name: String?,
    @PrimaryKey(autoGenerate = true) override val id: Int,
    @ColumnInfo(name = "image") var image: String?,
    @ColumnInfo(name = "latitude") var latitude: String?,
    @ColumnInfo(name = "longitude") var longitude: String?,
    @ColumnInfo(name = "length") var length: String?,
    @ColumnInfo(name = "time") var time: String?,
    @ColumnInfo(name = "type") var type: Int?,
    @ColumnInfo(name = "weight") var weight: String?,
    @ColumnInfo(name = "trolling_id") val trolling_id: Long?,
    @ColumnInfo(name = "created_date") val created_date: String,
    @ColumnInfo(name = "created_time") val created_time: String,
    @ColumnInfo(name = "local_db_unique_id") val local_db_unique_id: String,
) : ListAdapterItem, Serializable {
    @Ignore
    var isSelected: Boolean = false
    constructor (
        base_url: String,
        date: String,
        description: String?,
        fish_name: String?,
        image: String?,
        lat: String?,
        lang: String?,
        length: String?,
        time: String,
        type: Int?,
        weight: String?,
        trolling_id: Long?,
        created_date: String,
        created_time: String,
        local_db_unique_id: String
    )
            : this(
        base_url,
        date,
        description,
        fish_name,
        0,
        image,
        lat,
        lang,
        length,
        time,
        type,
        weight,
        trolling_id,
        created_date,
        created_time,
        local_db_unique_id
    )

    fun getLongitudeFormat(): String {
        return try {
            getNauticalLongitude((longitude?.toDouble() ?: 0.0))
        } catch (ex: Exception) {
            "N/A"
        }
//        return try {
//            if (!longitude.isNullOrEmpty()) {
//                (Location.convert(longitude?.toDouble()?:0.0, Location.FORMAT_MINUTES)).replace(":", "* ")
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
            "N/A"
        }
//        return try{
//            if (!latitude.isNullOrEmpty()) {
//                (Location.convert(latitude?.toDouble()?:0.0, Location.FORMAT_MINUTES)).replace(":", "* ")
//            } else {
//                latitude ?: ""
//            }
//        }catch (ex: Exception){
//            ""
//        }
    }

    fun getWeightFormat(): String {
        return if (!weight.isNullOrEmpty()) {
            "Weight: ${weight}"
        } else {
            "Weight: N/A"
        }
    }

    fun getLengthFormat(): String {
        return if (!length.isNullOrEmpty()) {
            "Length: ${length}"
        } else {
            "Length: N/A"
        }
    }

    fun getDateFormat():String{
        if (date.isNullOrEmpty()){
            return "Date: N/A"
        }
        return "Date: ${date?.toDate("yyyy-MM-dd")?.toFormat("MMM dd, yyyy")?:""}"
    }

    fun getLatFromString(): Double?{
        return latitude.stringToDouble()
    }
    fun getLangFromString(): Double?{
        return longitude.stringToDouble()
    }
}