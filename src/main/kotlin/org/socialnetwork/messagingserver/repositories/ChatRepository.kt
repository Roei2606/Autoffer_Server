package org.socialnetwork.messagingserver.repositories

import org.socialnetwork.messagingserver.models.ChatModel
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ChatRepository : ReactiveMongoRepository<ChatModel, String> {

    fun findByParticipantsContaining(userId: String, pageable: Pageable): Flux<ChatModel>
    @Query("{ 'participants': { \$all: ?0 } }")
    fun findByParticipants(participants: List<String>): Mono<ChatModel>

}
