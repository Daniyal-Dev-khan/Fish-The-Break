package com.cp.fishthebreak.models.group

import com.cp.fishthebreak.utils.stringToDouble

data class GroupDetailsModel(
    val `data`: ArrayList<Message>,
    val last_page: Int?,
    val message: String,
    val status: Boolean,
    val owner: Boolean,
    val statusCode: Int
)

data class GroupDetailsData(
    val date: String,
    val messages: ArrayList<Message>
)

data class Message(
    val base_url: String,
    val user_base_url: String,
    val sent_date: String,
    var bookmarked: Boolean,
    val image: String,
    val label: String,
    val description: String?,
    val room_id: Int,
    val shareable_id: Int,
    val sender_id: Int,
    val sender_name: String,
    val sender_photo: String?,
    private val latitude: String?,
    private val longitude: String?,
    val sent_at: String,
    val shareable_type: String,
    val self_created: Boolean
){
    fun getLatFromString(): Double?{
        return latitude.stringToDouble()
    }
    fun getLangFromString(): Double?{
        return longitude.stringToDouble()
    }
}