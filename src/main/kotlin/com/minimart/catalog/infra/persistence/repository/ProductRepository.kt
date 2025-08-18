package com.minimart.catalog.infra.persistence.repository

import com.minimart.catalog.domain.model.Product
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ProductRepository: ReactiveMongoRepository<Product, String> {
}