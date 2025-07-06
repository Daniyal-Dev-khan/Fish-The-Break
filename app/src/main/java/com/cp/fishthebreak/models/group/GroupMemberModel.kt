package com.cp.fishthebreak.models.group

import com.cp.fishthebreak.adapters.base.ListAdapterItem
import java.io.Serializable


data class GroupMemberModel(
    val `data`: ArrayList<GroupMember>,
    val last_page: Int?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class GroupMember(
    val base_url: String,
    val is_admin: Int,
    val name: String,
    override val id: Int,
    val profile_pic: String,
    val room_id: Int,
    val user_id: Int,
    val username: String
): ListAdapterItem, Serializable