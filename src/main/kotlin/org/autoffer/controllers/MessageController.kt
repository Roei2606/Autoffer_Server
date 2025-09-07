
package org.autoffer.controllers

import org.autoffer.models.MessageModel
import org.autoffer.models.UnreadCountRequest
import org.autoffer.services.ChatService
import org.autoffer.services.MessagingService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class MessageController(
    private val messagingService: MessagingService,
    private val chatService: ChatService
) {

    @MessageMapping("messages.send")
    fun sendMessage(@Payload message: MessageModel): Mono<MessageModel> {
        println("📨 messages.send invoked: from=${message.senderId} to=${message.receiverId} chatId=${message.chatId}")
        return messagingService.sendMessage(message)
    }

    @MessageMapping("messages.stream")
    fun streamMessages(@Payload chatId: String): Flux<MessageModel> {
        println("🔄 messages.stream invoked: chatId=$chatId")
        return chatService.streamMessages(chatId)
    }
    @MessageMapping("messages.markAsRead")
    fun markAsRead(@Payload request: UnreadCountRequest): Mono<Void> {
        return messagingService.markMessagesAsRead(request.chatId, request.userId)
    }

}
