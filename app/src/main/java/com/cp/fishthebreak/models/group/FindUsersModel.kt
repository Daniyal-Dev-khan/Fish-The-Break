package com.cp.fishthebreak.models.group

import com.cp.fishthebreak.models.auth.User

data class FindUsersModel(
    val `data`: ArrayList<User>,
    val last_page: Int?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)