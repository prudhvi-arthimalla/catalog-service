package com.minimart.catalog.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateProductRequest(
    @field:Size(max = 140)
    @field:Schema(
        description = "Product name (max 140 chars)",
        example = "Organic Apple Juice 1L",
        required = true,
        maxLength = 140)
    val name: String?,
    @field:Size(max = 2000)
    @field:Schema(
        description = "Product description (max 2000 chars)",
        example = "Cold-pressed organic apple juice with no added sugar.",
        nullable = true,
        maxLength = 2000)
    val description: String?,
    @field:Schema(description = "Unit price in default currency", example = "9.99", required = true)
    val price: BigDecimal?,
    @field:Size(max = 64)
    @field:Schema(
        description = "Product category (max 64 chars)",
        example = "BEVERAGES",
        nullable = true,
        maxLength = 64)
    val category: String?
)
