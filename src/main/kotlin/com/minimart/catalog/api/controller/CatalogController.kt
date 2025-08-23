package com.minimart.catalog.api.controller

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.CreateProductResponse
import com.minimart.catalog.api.dto.ProductResponse
import com.minimart.catalog.domain.service.CatalogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import io.swagger.v3.oas.annotations.parameters.RequestBody
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.Error

@RestController
@RequestMapping("/api/v1/products")
@Validated
class CatalogController(
    val catalogService: CatalogService
) {

    @PostMapping(consumes = ["application/json"], produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new product",
        description = "Creates a new product and stores it in MongoDB. Requires name and price (≥ 0). Returns 201 with the created product ID.",
        responses = [ApiResponse(
            description = "Product successfully created and stored in the database",
            responseCode = "201",
            content = [Content(schema = Schema(implementation = CreateProductResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input"
            )
        ]
    )
    fun createProduct(
        @RequestBody(
            description = "Product creation payload",
            required = true,
            content = [Content(schema = Schema(implementation = CreateProductRequest::class))]
        )
        @org.springframework.web.bind.annotation.RequestBody
        @Valid createProductRequest: CreateProductRequest
    ): Mono<CreateProductResponse> {
        return catalogService.createProduct(createProductRequest)
    }

    @GetMapping("/{id}", produces = ["application/json"])
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Return a product from database",
        description = "Return a product saved in MongoDB by its id. Requires Id as a string. Returns 200 with the Product",
        responses = [ApiResponse(
            description = "Product found",
            responseCode = "200",
            content = [Content(schema = Schema(implementation = ProductResponse::class))]
        ), ApiResponse(
            description = "Product not found",
            responseCode = "404",
            content = [Content(schema = Schema(implementation = Error::class))]
        )]
    )
    fun getProductById(
        @Parameter(
            name = "id",
            description = "Product Id",
            required = true,
            example = "68aa1ca6930ac7f5d9efed26"
        )
        @org.springframework.web.bind.annotation.PathVariable("id") productId: String
    ): Mono<ProductResponse> {
        return catalogService.getProductById(productId)
    }

    @GetMapping(produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Return all the products from the database",
        description = "Return all the products saved in MongoDB. Returns 200 with list of Products",
        responses = [ApiResponse(
            description = "Products found",
            responseCode = "200",
            content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = ProductResponse::class)))]
        )]
    )
    fun getAllProducts(): Flux<ProductResponse> {
        return catalogService.getAllProducts()
    }
}