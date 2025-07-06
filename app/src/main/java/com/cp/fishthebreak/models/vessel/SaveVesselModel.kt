package com.cp.fishthebreak.models.vessel

import com.cp.fishthebreak.utils.stringToDouble

data class SaveVesselModel(
    val `data`: VesselData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class VesselData(
    val id: Int,
    private val latitude: String?,
    private val longitude: String?,
    val make: String?,
    val model: String?,
    val name: String?,
    val range: String?,
    val year: String?
){
    fun getLatFromString(): Double?{
        return latitude.stringToDouble()
    }
    fun getLangFromString(): Double?{
        return longitude.stringToDouble()
    }
}