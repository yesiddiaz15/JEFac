package com.yediaz.jefac.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val category: ProductCategory = ProductCategory.DRINK,
    val price: Double = 0.0,
    val isActive: Boolean = true
)
