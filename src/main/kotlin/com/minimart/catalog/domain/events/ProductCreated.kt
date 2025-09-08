package com.minimart.catalog.domain.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("com.minimart.events.ProductListedForInventoryV1")
@Serializable
data class ProductCreated(
    val skuCode: String,
    val occurredAtEpochMs: Long,
    val eventId: String,
    val source: String = "catalog-service",
    val correlationId: String? = null
)
