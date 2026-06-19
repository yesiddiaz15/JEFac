package com.yediaz.jefac.data.model

import com.yediaz.jefac.domain.model.Product
import com.yediaz.jefac.domain.model.ProductCategory
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String = "",
    val name: String = "",
    val category: String = "DRINK",
    val price: Double = 0.0,
    val isActive: Boolean = true
)

fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    category = ProductCategory.fromString(category),
    price = price,
    isActive = isActive
)

fun Product.toDto() = ProductDto(
    id = id,
    name = name,
    category = category.name,
    price = price,
    isActive = isActive
)