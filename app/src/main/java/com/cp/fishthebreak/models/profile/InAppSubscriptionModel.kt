package com.cp.fishthebreak.models.profile

import com.android.billingclient.api.ProductDetails

data class InAppSubscriptionModel(val product: ProductDetails, var isSelected: Boolean = false)