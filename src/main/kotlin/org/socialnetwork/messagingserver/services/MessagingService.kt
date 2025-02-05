package org.socialnetwork.messagingserver.services



import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.repositories.MessageRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.LocalDateTime

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val chatService: ChatService
) {

    private val messageSink: Sinks.Many<MessageModel> = Sinks.many().multicast().directBestEffort()


    fun sendMessage(chatId: String, senderId: String, content: String): Mono<MessageModel> {
        val message = MessageModel(
            chatId = chatId,
            senderId = senderId,
            content = content,
            timestamp = LocalDateTime.now(),
            readBy = mutableListOf()
        )
        return messageRepository.save(message)
            .doOnNext { messageSink.tryEmitNext(it) }
            .flatMap { savedMessage ->
                chatService.updateLastMessage(chatId, savedMessage)
                    .thenReturn(savedMessage)
            }
    }

    fun subscribeToAllMessages(): Flux<MessageModel> {
        return messageSink.asFlux()
    }

    fun getUnreadCount(chatId: String, userId: String): Mono<Long> {
        return messageRepository.countByChatIdAndReadByNotContaining(chatId, userId)
    }


    fun markMessagesAsRead(chatId: String, userId: String): Mono<Void> {
        val unpaged: Pageable = Pageable.unpaged()

        return messageRepository.findAllByChatId(chatId, unpaged)
            .filter { !it.readBy!!.contains(userId) }
            .flatMap { message ->
                message.readBy!!.add(userId)
                messageRepository.save(message)
            }
            .then()
    }
}
