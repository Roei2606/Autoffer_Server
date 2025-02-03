package org.socialnetwork.messagingserver.controllers



import org.socialnetwork.messagingserver.models.ChatModel
import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.services.ChatService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/chats")
class ChatRestController(val chatService: ChatService) {

    @GetMapping("/{userId}")
    fun getUserChats(
        @PathVariable userId: String,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): Flux<ChatModel> {
        return chatService.getUserChats(userId, page, size)
    }

    @GetMapping("/{chatId}/messages")
    fun getChatMessages(
        @PathVariable chatId: String,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): Flux<MessageModel> {
        return chatService.getChatMessages(chatId, page, size)
    }
}
