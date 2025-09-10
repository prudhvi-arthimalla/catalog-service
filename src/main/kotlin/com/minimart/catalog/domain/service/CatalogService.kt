package com.minimart.catalog.domain.service

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.CreateProductResponse
import com.minimart.catalog.api.dto.ProductResponse
import com.minimart.catalog.api.dto.UpdateProductRequest
import com.minimart.catalog.api.mapper.ProductMapper
import com.minimart.catalog.domain.events.ProductCreated
import com.minimart.catalog.infra.persistence.repository.ProductRepository
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CatalogService(
    private val productRepository: ProductRepository,
    private val eventPublisher: EventPublisher,
    @param:Value("\${kafka.topics.productRegistered}") private val productRegisteredTopic: String
) {

    fun createProduct(createProductRequest: CreateProductRequest): Mono<CreateProductResponse> {
        // normalize the req and create a domain model object and save it
        val normalizedRequest = ProductMapper.fromCreate(createProductRequest)
        return productRepository.save(ProductMapper.toDocument(normalizedRequest)).map { product ->
            val event =
                ProductCreated(
                    skuCode = product.sku,
                    occurredAtEpochMs = System.currentTimeMillis(),
                    eventId = UUID.randomUUID().toString(),
                )
            eventPublisher.publish(productRegisteredTopic, product.sku, event)
            CreateProductResponse(id = product.id!!, createdAt = product.createdAt!!)
        }
    }

    fun getProductById(productId: String): Mono<ProductResponse> {
        // get product by ID from repo and return a mono
        return productRepository
            .findById(productId)
            .switchIfEmpty(
                Mono.error(
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product does not exist with provided productId")))
            .map { product -> ProductMapper.toResponse(product) }
    }

    fun getAllProducts(): Flux<ProductResponse> {
        // get all Products from database and return a flux
        return (productRepository.findAll()).map(ProductMapper::toResponse)
    }

    fun updateProductById(
        productId: String,
        newProduct: UpdateProductRequest
    ): Mono<ProductResponse> {
        // get product by ID from database and update it, return a mono
        return productRepository
            .findById(productId)
            .switchIfEmpty(
                Mono.error(
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product does not exist with provided productId")))
            .flatMap { productDocument ->
                val updatedProduct =
                    productDocument.copy(
                        name = newProduct.name ?: productDocument.name,
                        description = newProduct.description ?: productDocument.description,
                        price = newProduct.price ?: productDocument.price,
                        category = newProduct.category ?: productDocument.category)
                if (updatedProduct == productDocument) {
                    Mono.just(ProductMapper.toResponse(productDocument))
                } else {
                    productRepository.save(updatedProduct).map(ProductMapper::toResponse)
                }
            }
    }

    fun deleteProductById(productId: String): Mono<Void> {
        // delete a product from database by its ID
        return productRepository
            .findById(productId)
            .switchIfEmpty(
                Mono.error(
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product does not exist with provided productId")))
            .flatMap { productDocument -> productRepository.delete(productDocument) }
    }
}
