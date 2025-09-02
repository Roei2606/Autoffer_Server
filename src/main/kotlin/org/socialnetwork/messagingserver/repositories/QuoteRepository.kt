package org.socialnetwork.messagingserver.repositories

import org.socialnetwork.messagingserver.models.QuoteModel
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface QuoteRepository : ReactiveMongoRepository<QuoteModel, String>
