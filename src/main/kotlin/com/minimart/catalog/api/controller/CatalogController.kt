package com.minimart.catalog.api.controller

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.CreateProductResponse
import com.minimart.catalog.api.dto.ProductResponse
import com.minimart.catalog.api.dto.UpdateProductRequest
import com.minimart.catalog.domain.service.CatalogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import java.lang.Error
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/products")
@Validated
@ApiResponse(
    description = "Product not found",
    responseCode = "404",
    content = [Content(schema = Schema(implementation = Error::class))],
)
class CatalogController(val catalogService: CatalogService) {

  @PostMapping(
      consumes = [MediaType.APPLICATION_JSON_VALUE],
      produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create a new product",
      description =
          "Creates a new product and stores it in MongoDB. Requires name and price (≥ 0). Returns 201 with the created product ID.",
      responses =
          [
              ApiResponse(
                  description = "Product successfully created and stored in the database",
                  responseCode = "201",
                  content =
                      [
                          Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = Schema(implementation = CreateProductResponse::class),
                          )
                      ],
              ),
              ApiResponse(
                  responseCode = "400",
                  description = "Invalid input",
                  content =
                      [
                          Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = Schema(implementation = Error::class),
                          )
                      ],
              ),
          ],
  )
  fun createProduct(
      @RequestBody(
          description = "Product creation payload",
          required = true,
          content =
              [
                  Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = Schema(implementation = CreateProductRequest::class),
                  )
              ],
      )
      @org.springframework.web.bind.annotation.RequestBody
      @Valid
      createProductRequest: CreateProductRequest
  ): Mono<CreateProductResponse> {
    return catalogService.createProduct(createProductRequest)
  }

  @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(value = HttpStatus.OK)
  @Operation(
      summary = "Return a product from database",
      description =
          "Return a product saved in MongoDB by its id. Requires Id as a string. Returns 200 with the Product",
      responses =
          [
              ApiResponse(
                  description = "Product found",
                  responseCode = "200",
                  content =
                      [
                          Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = Schema(implementation = ProductResponse::class),
                          )
                      ],
              )
          ],
  )
  fun getProductById(
      @Parameter(
          name = "id",
          description = "Product Id",
          required = true,
          example = "68aa1ca6930ac7f5d9efed26",
      )
      @org.springframework.web.bind.annotation.PathVariable("id")
      productId: String
  ): Mono<ProductResponse> {
    return catalogService.getProductById(productId)
  }

  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Return all the products from the database",
      description = "Return all the products saved in MongoDB. Returns 200 with list of Products",
      responses =
          [
              ApiResponse(
                  description = "Products found",
                  responseCode = "200",
                  content =
                      [
                          Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              array =
                                  ArraySchema(
                                      schema = Schema(implementation = ProductResponse::class)
                                  ),
                          )
                      ],
              )
          ],
  )
  fun getAllProducts(): Flux<ProductResponse> {
    return catalogService.getAllProducts()
  }

  @PatchMapping(
      "/{id}",
      consumes = [MediaType.APPLICATION_JSON_VALUE],
      produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Partially update a product",
      description =
          "Update the provided field of a product and return the updated product from DB. If no fields are provided ({}), the operation is no-op and current product is returned",
      responses =
          [
              ApiResponse(
                  description = "Product updated or no-op",
                  responseCode = "200",
                  content =
                      [
                          Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = Schema(implementation = ProductResponse::class),
                          )
                      ],
              ),
              ApiResponse(
                  description = "Product not found",
                  responseCode = "404",
                  content =
                      [
                          Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = Schema(implementation = Error::class),
                          )
                      ],
              ),
          ],
  )
  fun updateProductById(
      @Parameter(
          name = "id",
          description = "Product Id",
          required = true,
          example = "68aa1ca6930ac7f5d9efed26",
      )
      @org.springframework.web.bind.annotation.PathVariable("id")
      productId: String,
      @RequestBody(
          description = "Product update payload",
          required = true,
          content =
              [
                  Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = Schema(implementation = UpdateProductRequest::class),
                  )
              ],
      )
      @org.springframework.web.bind.annotation.RequestBody
      @Valid
      newProduct: UpdateProductRequest,
  ): Mono<ProductResponse> {
    return catalogService.updateProductById(productId = productId, newProduct = newProduct)
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete a product from the database",
      description = "Delete a product from DB. Returns 204",
      responses =
          [
              ApiResponse(
                  description = "Product successfully deleted",
                  responseCode = "204",
              ),
              ApiResponse(
                  description = "Product with id not found",
                  responseCode = "404",
                  content =
                      [
                          Content(
                              mediaType = MediaType.APPLICATION_JSON_VALUE,
                              schema = Schema(implementation = Error::class),
                          )
                      ],
              ),
          ],
  )
  fun deleteProductById(
      @Parameter(
          name = "id",
          description = "Product Id",
          required = true,
          example = "68aa1ca6930ac7f5d9efed26",
      )
      @org.springframework.web.bind.annotation.PathVariable("id")
      productId: String
  ): Mono<Void> {
    return catalogService.deleteProductById(productId)
  }
}
