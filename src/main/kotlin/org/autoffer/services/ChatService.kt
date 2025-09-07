package org.autoffer.services

import org.autoffer.models.*
import org.autoffer.repositories.ChatRepository
import org.autoffer.repositories.MessageRepository
import org.autoffer.repositories.UserRepository
import org.socialnetwork.messagingserver.modelsdata.UnreadCountResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    private val chatMessageSinks = mutableMapOf<String, Sinks.Many<MessageModel>>()
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.of("UTC"))

    fun getOrCreateChat(user1Id: String, user2Id: String): Mono<ChatModel> {
        val participants = listOf(user1Id, user2Id).sorted()
        return chatRepository.findChatByExactParticipants(participants)
            .switchIfEmpty(
                chatRepository.save(
                    ChatModel(
                        participants = participants,
                        lastMessage = "",
                        lastMessageTimestamp = ""
                    )
                ).flatMap { newChat ->
                    userService.addChatToUsers(newChat.id!!, participants)
                        .thenReturn(newChat)
                }
            )
    }
    fun getUserChats(userId: String): Flux<ChatModel> {
        return chatRepository.findAllByParticipantsContaining(userId)
    }
    fun hasChats(userId: String): Mono<Boolean> {
        return chatRepository.findAllByParticipantsContaining(userId)
            .hasElements() // ✅ מחזיר Mono<Boolean>
    }
    fun getChatMessages(chatId: String, page: Int = 0, size: Int = 50): Flux<MessageModel> =
        messageRepository.findAllByChatIdOrderByTimestampAsc(chatId, PageRequest.of(page, size))
    fun getChatById(chatId: String): Mono<ChatModel> =
        chatRepository.findById(chatId)
    fun streamMessages(chatId: String): Flux<MessageModel> {
        val sink = chatMessageSinks.getOrPut(chatId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }
        return sink.asFlux()
    }
    fun getOrCreateChatId(user1Id: String, user2Id: String): Mono<String> {
        val participants = setOf(user1Id, user2Id).toList()

        return chatRepository.findByParticipants(participants)
            .switchIfEmpty(
                chatRepository.save(
                    ChatModel(
                        participants = participants,
                        lastMessage = "",
                        lastMessageTimestamp = ""
                    )
                )
            )
            .map { it.id!! }
    }
    fun emitMessage(chatId: String, message: MessageModel) {
        val sink = chatMessageSinks.getOrPut(chatId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }
        sink.tryEmitNext(message)
    }
    fun updateChatLastMessage(chatId: String, message: String, timestamp: String): Mono<Void> {
        return chatRepository.findById(chatId)
            .flatMap { chat ->
                val updated = chat.copy(
                    lastMessage = message,
                    lastMessageTimestamp = timestamp
                )
                chatRepository.save(updated).then()
            }
    }
    fun sendFileMessage(request: FileMessageRequest): Mono<Void> {
        val safeTimestamp = if (request.timestamp.isNullOrBlank()) {
            formatter.format(Instant.now())
        } else request.timestamp

        val message = MessageModel(
            chatId = request.chatId,
            senderId = request.sender,
            receiverId = request.receiver,
            content = "[BOQ_REQUEST] ${request.fileName}",
            timestamp = safeTimestamp,
            fileBytes = request.fileBytes,
            fileName = request.fileName,
            fileType = request.fileType
        )
        return messageRepository.save(message).then()
    }
    fun sendTextMessage(request: TextMessageRequest): Mono<Void> {
        val safeTimestamp = if (request.timestamp.isNullOrBlank()) {
            formatter.format(Instant.now())
        } else request.timestamp

        val message = MessageModel(
            chatId = request.chatId,
            senderId = request.sender,
            receiverId = request.receiver,
            content = request.content,
            timestamp = safeTimestamp,
            readBy = mutableListOf(request.sender),
            fileBytes = null,
            fileName = null,
            fileType = null
        )
        return messageRepository.save(message).then()
    }
    fun countUnreadMessages(request: UnreadCountRequest): Mono<UnreadCountResponse> {
        return messageRepository.countByChatIdAndSenderIdNotAndReadByNotContaining(
            request.chatId, request.userId, request.userId
        ).map { count ->
            UnreadCountResponse(request.chatId, count?.toInt() ?: 0)
        }
    }
}
