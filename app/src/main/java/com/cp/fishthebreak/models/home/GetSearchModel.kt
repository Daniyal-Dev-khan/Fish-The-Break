package com.cp.fishthebreak.models.home

import android.os.Parcelable
import com.cp.fishthebreak.utils.getNauticalLatitude
import com.cp.fishthebreak.utils.getNauticalLongitude
import com.cp.fishthebreak.utils.stringToDouble
import kotlinx.parcelize.Parcelize

data class GetSearchModel(
    val `data`: ArrayList<SearchData>,
    val message: String,
    val status: Boolean,
    val statusCode: Int,
    val last_page: Int?,
)
@Parcelize
data class SearchData(
    val created_at: String,
    val id: Int?,
    val search_text: String?,
    val updated_at: String,
    val user_id: Int,
    val description: String?,
    val latitude: String?,
    val longitude: String?,
    val name: String?,
    val type: String, //1 => map, 2 => fish log, 3 => location
) : Parcelable{
    fun getLongitudeFormat(): String {
        return try {
            getNauticalLongitude((longitude?.toDouble() ?: 0.0))
        } catch (ex: Exception) {
            ""
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
            ""
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

    fun getLatFromString(): Double?{
        return latitude.stringToDouble()
    }
    fun getLangFromString(): Double?{
        return longitude.stringToDouble()
    }
}