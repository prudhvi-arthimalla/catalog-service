package com.minimart.catalog.api.mapper

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.ProductResponse
import com.minimart.catalog.domain.model.Product
import com.minimart.catalog.infra.persistence.document.ProductDocument
import com.minimart.catalog.infra.utils.Category

object ProductMapper {

    // Convert a persistence-layer ProductDocument into a domain Product,
    // safely mapping the string category to the Category enum (null if invalid/absent).
    fun toDomain(productDocument: ProductDocument) =
        Product(
            id = productDocument.id,
            sku = productDocument.sku,
            name = productDocument.name,
            description = productDocument.description,
            price = productDocument.price,
            category =
                productDocument.category?.let { runCatching { Category.valueOf(it) }.getOrNull() })

    // Convert a domain Product into a persistence ProductDocument,
    // mapping the Category enum to its String name (null if absent).
    fun toDocument(domain: Product) =
        ProductDocument(
            id = domain.id,
            sku = domain.sku,
            name = domain.name,
            description = domain.description,
            price = domain.price,
            category = domain.category?.name)

    // Build a new domain Product from a CreateProductRequest,
    // trimming input strings and safely parsing category to enum; id remains null.
    fun fromCreate(req: CreateProductRequest) =
        Product(
            id = null,
            sku = req.sku.trim(),
            name = req.name.trim(),
            description = req.description?.trim(),
            price = req.price,
            category = req.category?.let { runCatching { Category.valueOf(it) }.getOrNull() })

    fun toResponse(productDocument: ProductDocument): ProductResponse =
        ProductResponse(
            id =
                requireNotNull(
                    productDocument.id, { "Product id cannot be null when mapping to response" }),
            sku = productDocument.sku,
            name = productDocument.name,
            description = productDocument.description,
            price = productDocument.price,
            category = productDocument.category,
            createdAt = productDocument.createdAt)
}
