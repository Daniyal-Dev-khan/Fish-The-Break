package com.cp.fishthebreak.models.trolling

import com.cp.fishthebreak.models.auth.User

data class GetAllTrollingModel(
    val `data`: ArrayList<TrollingResponseData>,
    val last_page: Int?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)