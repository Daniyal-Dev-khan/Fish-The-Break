package com.cp.fishthebreak.models.trolling

import com.cp.fishthebreak.adapters.base.ListAdapterItem
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.utils.stringToDouble
import com.cp.fishthebreak.utils.toDate
import com.cp.fishthebreak.utils.toFormat
import java.io.Serializable

data class TrollingResponseModel(
    val `data`: TrollingResponseData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class TrollingResponseData(
    val average_speed: String,
    val average_water_temp: String,
    val distance: String,
    val highest_speed: String,
    val highest_water_temp: String,
    override val id: Int,
    val lowest_water_temp: String,
    val slowest_speed: String,
    var trolling_name: String,
    val trolling_point: List<TrollingPointData>,
    val locations: List<SavePointsData>,
    val fishlogs: List<SaveFishLogData>,
    val type: Int?,
    val user: User?,
    val date: String,
    val duration: String?,
    val local_db_id: String,
    val local_db_unique_id: String,
    val time: String,
): ListAdapterItem, Serializable{
    fun getDateFormat():String{
        return "(${date.toDate("yyyy-MM-dd")?.toFormat("MMM dd, yyyy")?:""})"
    }
}

data class TrollingPointData(
    private val latitude: String?,
    private val longitude: String?
): Serializable{
    fun getLatFromString(): Double?{
        return latitude.stringToDouble()
    }
    fun getLangFromString(): Double?{
        return longitude.stringToDouble()
    }
}