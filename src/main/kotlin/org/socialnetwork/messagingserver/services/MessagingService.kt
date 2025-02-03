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
class MessageService(private val messageRepository: MessageRepository) {

    // ✅ Real-Time Message Sink
    private val messageSink: Sinks.Many<MessageModel> = Sinks.many().multicast().onBackpressureBuffer()


    fun sendMessage(chatId: String, senderId: String, content: String): Mono<MessageModel> {
        val message = MessageModel(
            id = null,                // (MongoDB will auto-generate)
            chatId = chatId,
            senderId = senderId,
            content = content,
            timestamp = LocalDateTime.now(),
            readBy = mutableListOf()
        )
        return messageRepository.save(message)
            .doOnSuccess { messageSink.tryEmitNext(it) } // Emit the new message in real-time
    }

    // ✅ Subscribe to All Messages for Real-Time Updates
    fun subscribeToAllMessages(): Flux<MessageModel> {
        return messageSink.asFlux()
    }

    // ✅ Get Unread Messages Count for a Specific Chat
    fun getUnreadCount(chatId: String, userId: String): Mono<Long> {
        return messageRepository.countByChatIdAndReadByNotContaining(chatId, userId)
    }

    // ✅ Mark Messages as Read
    fun markMessagesAsRead(chatId: String, userId: String): Mono<Void> {
        // Use Pageable.unpaged() to fetch all messages without pagination
        val unpaged: Pageable = Pageable.unpaged()

        return messageRepository.findAllByChatId(chatId, unpaged)
            .filter { !it.readBy!!.contains(userId) }
            .flatMap { message ->
                message.readBy!!.add(userId)
                messageRepository.save(message)
            }
            .then() // Return Mono<Void> after completion
    }
}
