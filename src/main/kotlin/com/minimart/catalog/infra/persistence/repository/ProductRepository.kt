package com.minimart.catalog.infra.persistence.repository

import com.minimart.catalog.infra.persistence.document.ProductDocument
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ProductRepository: ReactiveMongoRepository<ProductDocument, String> {
}