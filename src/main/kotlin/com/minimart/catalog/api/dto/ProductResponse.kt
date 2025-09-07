package com.minimart.catalog.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.Instant

data class ProductResponse(
    @field:Schema(
        description = "Unique identifier of the product", example = "66c1d7f4c3a1a2b3c4d5e6f7")
    val id: String,
    @field:Schema(description = "Stock Keeping Unit identifier", example = "SKU-12345")
    val sku: String,
    @field:Schema(description = "Name of the product", example = "Organic Apple Juice 1L")
    val name: String,
    @field:Schema(
        description = "Detailed description of the product",
        example = "Cold-pressed apple juice with no added sugar")
    val description: String? = null,
    @field:Schema(
        description = "Unit price of the product",
        example = "19.99",
        type = "number",
        format = "bigdecimal")
    val price: BigDecimal,
    @field:Schema(description = "Category to which this product belongs", example = "Beverages")
    val category: String? = null,
    @field:Schema(
        description = "Timestamp when the product was created", example = "2025-08-22T14:35:10Z")
    val createdAt: Instant? = null
)
