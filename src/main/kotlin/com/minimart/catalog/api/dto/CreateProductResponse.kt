package com.minimart.catalog.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class CreateProductResponse(
    @field:Schema(
        description = "Unique identifier of the product",
        example = "63a5e3e2b4f6c7d8e9f0a1b2",
    )
    val id: String,
    @field:Schema(description = "Creation timestamp in UTC", example = "2025-08-18T23:20:50Z")
    val createdAt: Instant,
)
