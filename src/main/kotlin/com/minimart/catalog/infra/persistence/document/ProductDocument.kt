package com.minimart.catalog.infra.persistence.document

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "product")
@Schema(name = "Product", description = "Product document stored in MongoDB")
data class ProductDocument(
    @Id
    val id: String? = null,

    @Indexed(unique = true)
    val sku: String,

    val name: String,
    val description: String? = null,
    val price: BigDecimal,
    val stock: Int,
    val category: String? = null,

    @CreatedDate
    var createdAt: Instant? = null,

    @LastModifiedDate
    var updatedAt: Instant? = null,

    @Version
    var version: Long? = null
)