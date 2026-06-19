package com.yediaz.jefac.domain.model

data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1
)