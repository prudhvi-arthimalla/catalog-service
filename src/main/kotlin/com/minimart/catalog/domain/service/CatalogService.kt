package com.minimart.catalog.domain.service

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.CreateProductResponse
import com.minimart.catalog.api.dto.ProductResponse
import com.minimart.catalog.api.mapper.ProductMapper
import com.minimart.catalog.infra.persistence.repository.ProductRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CatalogService(
    val productRepository: ProductRepository
) {

    fun createProduct(createProductRequest: CreateProductRequest): Mono<CreateProductResponse> {
        // normalize the req and create a domain model object and save it
        val normalizedRequest = ProductMapper.fromCreate(createProductRequest)
        return productRepository.save(ProductMapper.toDocument(normalizedRequest))
            .map { product ->
                CreateProductResponse(id = product.id!!, createdAt = product.createdAt!!)
            }
    }

    fun getProductById(productId: String): Mono<ProductResponse> {
        // get product by ID from repo and return a mono
        return productRepository.findById(productId)
            .switchIfEmpty(
                Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Product does not exist with provided productId"))
            )
            .map { product ->
                ProductMapper.toResponse(product)
            }
    }

    fun getAllProducts(): Flux<ProductResponse> {
        // get all Products from database and return a flux
        return (productRepository.findAll())
            .map(ProductMapper::toResponse)
    }
}