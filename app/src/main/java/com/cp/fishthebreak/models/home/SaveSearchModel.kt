package com.cp.fishthebreak.models.home

data class SaveSearchModel(
    val `data`: SearchData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)