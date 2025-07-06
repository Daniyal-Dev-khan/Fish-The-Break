package com.cp.fishthebreak.models.group

data class BookMarkModel(
    val `data`: BookMarkData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class BookMarkData(
    val saved: Boolean
)