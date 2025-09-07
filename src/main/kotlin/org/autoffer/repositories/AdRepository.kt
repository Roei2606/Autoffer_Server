package org.autoffer.repositories

import org.autoffer.models.AdModel
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface AdRepository : ReactiveMongoRepository<AdModel, String> {
     fun findAdsByAudience(audience: String): Flux<AdModel>
}
