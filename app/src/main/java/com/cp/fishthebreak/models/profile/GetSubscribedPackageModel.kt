package com.cp.fishthebreak.models.profile

import com.android.billingclient.api.Purchase

data class GetSubscribedPackageModel(
    var `data`: SubscribedData?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class SubscribedData(
    val created_at: String,
    val expiry_date: String,
    val id: Int,
    val order_id: String,
    val package_name: String,
    val platform: Int,
    val product_id: String,
    val product_name: String,
    val product_price: String,
    val purchase_token: String,
    val response_data: String,
    val updated_at: String,
    val user_id: Int
)

data class SubscribedModel(var serverData: SubscribedData?, val purchaseData: Purchase?)