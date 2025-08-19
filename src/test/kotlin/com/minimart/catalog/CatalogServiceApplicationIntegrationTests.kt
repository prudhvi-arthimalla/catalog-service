package com.minimart.catalog

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.CreateProductResponse
import com.minimart.catalog.infra.persistence.repository.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Full-stack integration test:
 * - Starts full Spring context on a random port
 * - Uses Testcontainers-backed Mongo via TestcontainersConfiguration
 * - Calls the real HTTP endpoint
 * - Asserts persisted state in Mongo
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@ContextConfiguration(classes = [CatalogServiceApplication::class, TestcontainersConfiguration::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CreateProductIntegrationTest() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CreateProductIntegrationTest::class.java)
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var productRepository: ProductRepository // your reactive Mongo repository

    @Test
    fun `createProduct persists entity and returns 201 with body`() {
        // given
        val req = CreateProductRequest(
            sku = "SKU-12345",
            name = "Organic Apple Juice 1L",
            description = "Cold-pressed apple juice",
            price = BigDecimal("9.99"),
            stock = 10,
            category = "GROCERY"
        )

        // when
        val responseBody = webTestClient
            .post()
            .uri("http://localhost:$port/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(req), CreateProductRequest::class.java)
            .exchange().expectStatus().isCreated
            .expectBody(CreateProductResponse::class.java)
            .returnResult()
            .responseBody!!

        // assert: basic response sanity
        assertNotNull(responseBody.id)
        assertNotNull(responseBody.createdAt)

        // assert: actually persisted in Mongo
        val saved = productRepository.findById(responseBody.id).block()
        assertNotNull(saved)
        assertEquals("SKU-12345", saved!!.sku)
        assertEquals("Organic Apple Juice 1L", saved.name)
        assertEquals(BigDecimal("9.99"), saved.price)
        assertEquals(10, saved.stock)
    }

    @Test
    fun `createProduct returns 401 on Invalid request`() {
        // given
        val invalidRequest = CreateProductRequest(
            sku = "",  // @NotBlank + too short
            name = " ",  // @NotBlank (blank string is invalid)
            description = "x".repeat(3000),  // exceeds @Size(max = 2000)
            price = BigDecimal(-5),  // @NotNull but also negative (business rule violation)
            stock = -10,  // @Min(0)
            category = "INVALID_CATEGORY_NAME_EXCEEDING_LENGTH_LIMIT_" + "Y".repeat(100)  // exceeds @Size(max=64) and not a valid enum
        )

        // when
        webTestClient.post()
            .uri("http://localhost:$port/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(invalidRequest), CreateProductRequest::class.java)
            .exchange().expectStatus().isBadRequest
            .expectBody().jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.error").isEqualTo("Bad Request")
            .jsonPath("$.path").isEqualTo("/api/v1/products")
    }
}
