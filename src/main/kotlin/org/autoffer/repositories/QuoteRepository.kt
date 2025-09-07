package org.autoffer.repositories


import org.autoffer.models.QuoteModel
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface QuoteRepository : ReactiveMongoRepository<QuoteModel, String>
