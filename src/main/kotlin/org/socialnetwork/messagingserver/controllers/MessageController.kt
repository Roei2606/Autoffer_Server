package org.socialnetwork.messagingserver.controllers



import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.services.MessageService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class MessageController(private val messageService: MessageService) {

    // 📡 Bi-Directional Streaming for Sending and Receiving Messages
    @MessageMapping("messages.channel")
    fun messageChannel(messages: Flux<MessageModel>): Flux<MessageModel> {
        return messages.flatMap { message ->
            messageService.sendMessage(message.chatId!!, message.senderId!!, message.content!!)
        }.mergeWith(messageService.subscribeToAllMessages()) // Merge with real-time messages
    }
}
