package org.autoffer.repositories


import org.autoffer.models.MessageModel
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MessageRepository : ReactiveMongoRepository<MessageModel, String> {
    fun findAllByChatIdOrderByTimestampAsc(chatId: String, pageable: Pageable): Flux<MessageModel>
    fun countByChatIdAndSenderIdNotAndReadByNotContaining(
        chatId: String,
        senderId: String,
        userId: String
    ): Mono<Long>


}
