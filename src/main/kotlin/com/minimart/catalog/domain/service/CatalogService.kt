package com.minimart.catalog.domain.service

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.CreateProductResponse
import com.minimart.catalog.api.mapper.ProductMapper
import com.minimart.catalog.infra.persistence.repository.ProductRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class CatalogService(
    val productRepository: ProductRepository
) {

    fun createProduct(createProductRequest: CreateProductRequest): Mono<CreateProductResponse> {
        // normalize the req and create a domain model object and save it
        val normalizedRequest = ProductMapper.fromCreate(createProductRequest)
        return productRepository.save(normalizedRequest)
            .map { product ->
                CreateProductResponse(id = product.id!!, createdAt = Instant.now()!!)
            }
    }
}