package com.cp.fishthebreak.models.routes

import android.location.Location
import com.cp.fishthebreak.adapters.base.ListAdapterItem
import com.cp.fishthebreak.utils.getNauticalLatitude
import com.cp.fishthebreak.utils.getNauticalLongitude
import com.cp.fishthebreak.utils.stringToDouble
import com.cp.fishthebreak.utils.toDate
import com.cp.fishthebreak.utils.toFormat
import java.io.Serializable

data class SaveRouteModel(
    val `data`: SaveRouteData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class SaveRouteData(
    val date: String,
    var description: String,
    override val id: Int,
    val local_db_id: String,
    val local_db_unique_id: String,
    var name: String,
    val points: List<SaveRoutePoint>,
    val time: String,
    val type: Int,
    val base_url: String,
    var image: String,
) : ListAdapterItem, Serializable {
    fun getDateFormat(): String {
        return "(${date.toDate("yyyy-MM-dd")?.toFormat("MMM dd, yyyy") ?: ""})"
    }
}

data class SaveRoutePoint(
    private val latitude: String?,
    private val longitude: String?,
    val name: String?
) : Serializable {
    fun getLongitudeFormat(): String {
        return try {
            getNauticalLongitude((longitude?.toDouble() ?: 0.0))
        } catch (ex: Exception) {
            ""
        }
//        return try {
//            if (!longitude.isNullOrEmpty()) {
//                (Location.convert(longitude.toDouble(), Location.FORMAT_MINUTES)).replace(":", "* ")
//            } else {
//                longitude ?: ""
//            }
//        } catch (ex: Exception) {
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
//                (Location.convert(latitude.toDouble(), Location.FORMAT_MINUTES)).replace(":", "* ")
//            } else {
//                latitude ?: ""
//            }
//        } catch (ex: Exception) {
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