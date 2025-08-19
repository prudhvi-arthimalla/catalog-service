package com.minimart.catalog.api.controller

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.CreateProductResponse
import com.minimart.catalog.domain.service.CatalogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/products")
@Validated
class CatalogController(
    val catalogService: CatalogService
) {

    @PostMapping(consumes = ["application/json"], produces = ["application/json"] )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new product",
        description = "Creates a new product and stores it in MongoDB. Requires name and price (≥ 0). Returns 201 with the created product ID.",
        responses = [ApiResponse(
            description = "Product successfully created and stored in the database",
            responseCode = "201",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input"
            )
        ]
    )
    fun createProduct(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Product creation payload",
            required = true,
            content = [Content(schema = Schema(implementation = CreateProductRequest::class))]
        )
        @RequestBody @Valid createProductRequest: CreateProductRequest
    ): Mono<CreateProductResponse> {
        return catalogService.createProduct(createProductRequest)
    }
}