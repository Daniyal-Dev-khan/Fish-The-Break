package com.cp.fishthebreak.models.profile

import com.cp.fishthebreak.models.auth.User

data class AddSubscriptionModel(
    val `data`: User?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)