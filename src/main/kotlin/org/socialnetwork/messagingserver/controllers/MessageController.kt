package org.socialnetwork.messagingserver.controllers



import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.repositories.MessageRepository
import org.socialnetwork.messagingserver.services.MessageService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class MessageController(
    private val messageService: MessageService,
    private val messageRepository: MessageRepository
) {


    @MessageMapping("messages.channel")
    fun messageChannel(messages: Flux<MessageModel>): Flux<MessageModel> {
        return messages.flatMap { message ->
            messageService.sendMessage(message.chatId, message.senderId, message.content)
                .doOnNext { println("Broadcasting message: ${it.content}") }
        }.mergeWith(messageService.subscribeToAllMessages())
    }



    @MessageMapping("messages.history")
    fun getChatHistory(request: Map<String, Any>): Flux<MessageModel> {
        val chatId = request["chatId"] as String
        val page = request["page"] as Int
        val size = request["size"] as Int

        return messageRepository.findAllByChatId(
            chatId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"))
        )
    }
}
