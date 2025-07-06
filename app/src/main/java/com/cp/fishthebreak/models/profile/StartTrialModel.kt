package com.cp.fishthebreak.models.profile

import com.cp.fishthebreak.models.auth.User

data class StartTrialModel(
    val `data`: User?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)