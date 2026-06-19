package com.yediaz.jefac.data.model

import com.yediaz.jefac.domain.model.SaleItem
import kotlinx.serialization.Serializable

@Serializable
data class SaleItemDto(
    val productId: String = "",
    val productName: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1
)

fun SaleItemDto.toDomain() = SaleItem(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)

fun SaleItem.toDto() = SaleItemDto(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)