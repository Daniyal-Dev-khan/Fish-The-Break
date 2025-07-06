package com.cp.fishthebreak.models.auth

import com.cp.fishthebreak.adapters.base.ListAdapterItem
import com.cp.fishthebreak.models.vessel.VesselData
import java.io.Serializable

data class RegisterModel(
    val `data`: User?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class User(
    val base_url: String,
    val created_at: String,
    val email: String,
    val email_verified_at: String,
    val first_name: String,
    override val id: Int,
    val is_enabled_emails: Int,
    val is_enabled_notifications: Int,
    val last_name: String,
    var profile_pic: String?,
    val profile_pic_thumbnail: String,
    val social_login_id: String?,
    val social_platform: String?,
    val status: Int,
    val token: String,
    val updated_at: String,
    val user_configuration: UserConfiguration?,
    var vessel: VesselData?,
    val username: String,
    val user_verified: Boolean,
    var isSelected: Boolean = false,
    val expiry_date: String?,
    val is_subscribed: Int,
    val mobile: String?,
    val otp: String,
    val purchase_status: Int,
    val purchase_token: String?,
    val subscription_id: String?,
    val subscription_platform: String?,
    val trial_end_date: String?,
    val trial_start_date: String?,

    ): ListAdapterItem, Serializable

data class UserConfiguration(
    val boat_range: Int,
    var range: Int?,
    val created_at: String,
    val id: Int,
    val other_vessels: Int,
    val updated_at: String,
    val user_id: Int,
    val view_mode: Int
)