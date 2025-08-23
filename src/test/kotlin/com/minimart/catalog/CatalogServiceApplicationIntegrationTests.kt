package com.minimart.catalog

import com.minimart.catalog.api.dto.CreateProductRequest
import com.minimart.catalog.api.dto.CreateProductResponse
import com.minimart.catalog.api.dto.ProductResponse
import com.minimart.catalog.infra.persistence.repository.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import kotlin.random.Random
import kotlin.test.assertTrue

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

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var productRepository: ProductRepository // your reactive Mongo repository

    val uri = "http://localhost:$port/api/v1/products"

    @BeforeEach
    fun cleanDb() {
        // ensure tests are isolated
        productRepository.deleteAll().block()
    }

    @Test
    fun `createProduct persists entity and returns 201 with body`() {
        // given
        val req = createReq(
            sku = "SKU-${Random.nextInt(100000, 999999)}",
            name = "Organic Apple Juice 1L",
            description = "Cold-pressed apple juice",
            price = BigDecimal("9.99")
        )

        // when
        val responseBody = createProduct(req)

        // then
        assertNotNull(responseBody.id)
        assertNotNull(responseBody.createdAt)
        val saved = productRepository.findById(responseBody.id).block()
        assertNotNull(saved)
        assertEquals(req.sku, saved!!.sku)
        assertEquals(req.name, saved.name)
        assertEquals(req.price, saved.price)
        assertEquals(req.stock, saved.stock)
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
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(invalidRequest), CreateProductRequest::class.java)
            .exchange().expectStatus().isBadRequest
            .expectBody().jsonPath("$.status").isEqualTo(400)
            .jsonPath("$.error").isEqualTo("Bad Request")
            .jsonPath("$.path").isEqualTo("/api/v1/products")
    }

    @Test
    fun `getPrductById returns a single item`() {
        // given
        val req = createReq(
            sku = "SKU-${Random.nextInt(100000, 999999)}",
            name = "Organic Apple Juice 1L",
            description = "Cold-pressed apple juice",
            price = BigDecimal("9.99")
        )

        // when
        val createdProduct = createProduct(req)

        val responseBody = webTestClient.get()
            .uri(uri + "/${createdProduct.id}")
            .exchange().expectStatus().isOk
            .expectBody(ProductResponse::class.java)
            .returnResult().responseBody!!

        // then
        assertNotNull(responseBody.id)
        assertEquals(createdProduct.createdAt.toEpochMilli(), responseBody.createdAt?.toEpochMilli())
        assertEquals(createdProduct.id, responseBody.id)
        assertEquals(req.sku, responseBody.sku)
        assertEquals(req.name, responseBody.name)
        assertEquals(req.description, responseBody.description)
        assertEquals(req.price, responseBody.price)
        assertEquals(req.stock, responseBody.stock)
        assertEquals(req.category, responseBody.category)
    }

    @Test
    fun `getProductById returns 404 when invalid is used`() {
        val missingId = randomObjectId()

        webTestClient.get()
            .uri("$uri/$missingId")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `getAllProducts return a list of items`() {
        // given
        val req1 = createReq(
            sku = "SKU-${Random.nextInt(100000, 999999)}",
            name = "Organic Apple Juice 1L",
            description = "Cold-pressed apple juice",
            price = BigDecimal("9.99")
        )
        val req2 = createReq(
            sku = "SKU-${Random.nextInt(100000, 999999)}",
            name = "Organic Orange Juice 1L",
            description = "Cold-pressed orange juice",
            price = BigDecimal("10.99")
        )

        // when
        val created1 = createProduct(req1)
        val created2 = createProduct(req2)
        val items = webTestClient.get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(ProductResponse::class.java)
            .returnResult()
            .responseBody.orEmpty()

        // then
        val ids = items.map { it.id }.toSet()
        assert(ids.containsAll(listOf(created1.id, created2.id)))
        val first = items.first { it.id == created1.id }
        assertEquals(created1.id, first.id)
    }

    @Test
    fun `getAllProducts return empty list when no products exist`() {

        // when
        val items = webTestClient.get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(ProductResponse::class.java)
            .returnResult()
            .responseBody.orEmpty()

        // then
        assertTrue(items.isEmpty())
    }

    // helper functions
    private fun createReq(
        sku: String,
        name: String,
        description: String,
        price: BigDecimal,
        stock: Int = 10,
        category: String = "GROCERY"
    ) = CreateProductRequest(
        sku = sku,
        name = name,
        description = description,
        price = price,
        stock = stock,
        category = category
    )

    private fun createProduct(req: CreateProductRequest): CreateProductResponse =
        webTestClient.post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated
            .expectBody(CreateProductResponse::class.java)
            .returnResult()
            .responseBody!!

    private fun randomObjectId(): String =
        List(24) { "0123456789abcdef".random() }.joinToString("")
}
