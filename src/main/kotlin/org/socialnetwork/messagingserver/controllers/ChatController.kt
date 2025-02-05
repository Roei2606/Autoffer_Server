package org.socialnetwork.messagingserver.controllers
import org.socialnetwork.messagingserver.models.ChatModel
import org.socialnetwork.messagingserver.models.ChatRequest
import org.socialnetwork.messagingserver.services.ChatService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.socialnetwork.messagingserver.models.MessageModel

@Controller
class ChatController(private val chatService: ChatService) {

    @MessageMapping("chats.getAll")
    fun getUserChats(@Payload userId: String): Flux<ChatModel> {
        return chatService.getUserChats(userId, 0, 20)
    }

    @MessageMapping("chats.getMessages")
    fun getChatMessages(@Payload chatId: String): Flux<MessageModel> {
        return chatService.getChatMessages(chatId, 0, 50)
    }

    @MessageMapping("chats.markRead")
    fun markMessagesAsRead(@Payload data: Map<String, String>): Mono<Void> {
        val chatId = data["chatId"]!!
        val userId = data["userId"]!!
        return chatService.markMessagesAsRead(chatId, userId)
    }

    @MessageMapping("chats.getOrCreate")
    fun getOrCreateChat(@Payload request: ChatRequest): Mono<ChatModel> {
        return chatService.getOrCreateChat(request.currentUserId, request.otherUserId)
    }
}
