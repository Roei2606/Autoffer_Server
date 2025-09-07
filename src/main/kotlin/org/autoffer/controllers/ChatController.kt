package org.autoffer.controllers

import org.autoffer.models.*
import org.autoffer.repositories.ChatRepository
import org.autoffer.repositories.MessageRepository
import org.autoffer.services.ChatService
import org.socialnetwork.messagingserver.modelsdata.UnreadCountResponse
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class ChatController(
    private val chatService: ChatService,
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository
) {

    @MessageMapping("chats.getOrCreate")
    fun getOrCreateChat(@Payload request: ChatRequest): Mono<ChatModel> {
        println("📥 ChatRequest received: $request")
        return chatService.getOrCreateChat(request.currentUserId, request.otherUserId)
    }

    @MessageMapping("chats.getAll")
    fun getUserChats(@Payload request: UserChatsRequest): Flux<ChatModel> {
        println("📥 getUserChats: userId=${request.userId}, page=${request.page}, size=${request.size}")
        return chatService.getUserChats(request.userId)
    }


    @MessageMapping("chats.getMessages")
    fun getChatMessages(@Payload request: org.autoffer.models.ChatMessagesRequest): Flux<MessageModel> {
        println("📥 getChatMessages: $request")
        return chatService.getChatMessages(request.chatId, request.page, request.size)
    }

    @MessageMapping("chats.streamMessages")
    fun streamMessages(@Payload chatId: String): Flux<MessageModel> {
        println("📥 streamMessages: chatId = $chatId")
        return chatService.streamMessages(chatId)
    }

    @MessageMapping("chats.getChatById")
    fun getChatById(@Payload request: Map<String, String>): Mono<ChatModel> {
        val chatId = request["chatId"]
            ?: return Mono.error(IllegalArgumentException("Chat ID is required"))
        println("📥 getChatById: chatId = $chatId")
        return chatService.getChatById(chatId)
    }

    @MessageExceptionHandler
    fun handleException(ex: Exception): Mono<String> {
        println("🚨 Error in ChatController: ${ex.message}")
        ex.printStackTrace()
        return Mono.error(ex)
    }

    @MessageMapping("chats.getUnreadCount")
    fun getUnreadCount(@Payload request: UnreadCountRequest): Mono<UnreadCountResponse> {
        return messageRepository.countByChatIdAndSenderIdNotAndReadByNotContaining(
            request.chatId,
            request.userId,
            request.userId
        ).map { count ->
            val safeCount = count?.toInt() ?: 0
            UnreadCountResponse(request.chatId, safeCount)
        }
    }

    @MessageMapping("chat.sendfile")
    fun handleFileMessage(request: FileMessageRequest): Mono<Void> {
        return chatService.sendFileMessage(request)
    }

    @MessageMapping("chats.hasChats")
    fun hasChats(userId: String): Mono<Boolean> {
        return chatRepository.findAllByParticipantsContaining(userId).hasElements()
    }

}
