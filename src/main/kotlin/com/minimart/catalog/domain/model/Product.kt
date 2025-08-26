package com.minimart.catalog.domain.model

import com.minimart.catalog.infra.utils.Category
import java.math.BigDecimal

data class Product(
    val id: String?,
    val sku: String,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stock: Int,
    val category: Category?
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank." }
        require(sku.isNotBlank()) { "SKU cannot be blank." }
        require(price >= BigDecimal.ZERO) { "Price must be >= 0." }
        require(stock >= 0) { "Stock must be >= 0." }
    }
}
