package org.socialnetwork.messagingserver.services

import org.socialnetwork.messagingserver.models.ChatModel
import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.repositories.ChatRepository
import org.socialnetwork.messagingserver.repositories.MessageRepository
import org.socialnetwork.messagingserver.repositories.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.LocalDateTime

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    private val chatMessageSinks = mutableMapOf<String, Sinks.Many<MessageModel>>()

//    fun getOrCreateChat(currentUserId: String, otherUserId: String): Mono<ChatModel> {
//        return chatRepository.findByParticipantsContaining(listOf(currentUserId, otherUserId))
//            .switchIfEmpty(
//                chatRepository.save(
//                    ChatModel(
//                        participants = listOf(currentUserId, otherUserId),
//                        lastMessage = "",
//                        lastMessageTimestamp = ""
//                    )
//                ).flatMap { savedChat ->
//                    updateUsersWithNewChat(savedChat.id!!, currentUserId, otherUserId)
//                        .thenReturn(savedChat)
//                }
//            )
//    }
//fun getOrCreateChat(currentUserId: String, otherUserId: String): Mono<ChatModel> {
//    println("📩 getOrCreateChat called with currentUserId=$currentUserId, otherUserId=$otherUserId")
//    return chatRepository.findByParticipantsContaining(listOf(currentUserId, otherUserId))
//        .doOnNext { println("✅ Found existing chat: ${it.id}, participants=${it.participants}") }
//        .switchIfEmpty(
//            chatRepository.save(
//                ChatModel(
//                    participants = listOf(currentUserId, otherUserId),
//                    lastMessage = "",
//                    lastMessageTimestamp = ""
//                )
//            ).doOnNext { println("🆕 Created new chat: ${it.id}, participants=${it.participants}") }
//        )
//}

    fun getOrCreateChat(currentUserId: String, otherUserId: String): Mono<ChatModel> {
        return chatRepository.findByParticipantsContaining(listOf(currentUserId, otherUserId))
            .switchIfEmpty(
                chatRepository.save(
                    ChatModel(
                        participants = listOf(currentUserId, otherUserId),
                        lastMessage = "",
                        lastMessageTimestamp = ""
                    )
                ).flatMap { newChat ->
                    // הוספת הצ'אט למשתמשים
                    userService.addChatToUsers(newChat.id!!, listOf(currentUserId, otherUserId))
                        .thenReturn(newChat)
                }
            )
    }

    fun getUserChats(userId: String): Flux<ChatModel> {
        println("📨 getUserChats called for userId=$userId")
        return chatRepository.findAllByParticipantsContaining(userId)
            .doOnNext { println("📦 Returning chat ${it.id} with participants=${it.participants}") }
    }

//    private fun updateUsersWithNewChat(chatId: String, user1Id: String, user2Id: String): Mono<Void> {
//        return userRepository.findById(user1Id).zipWith(userRepository.findById(user2Id))
//            .flatMap { tuple ->
//                val user1 = tuple.t1
//                val user2 = tuple.t2
//
//                user1.chats = (user1.chats ?: mutableListOf()).apply {
//                    if (!contains(chatId)) add(chatId)
//                }
//
//                user2.chats = (user2.chats ?: mutableListOf()).apply {
//                    if (!contains(chatId)) add(chatId)
//                }
//
//                userRepository.save(user1).then(userRepository.save(user2)).then()
//            }
//    }





//    fun getUserChats(userId: String): Flux<ChatModel> =
//        chatRepository.findAllByParticipantsContaining(userId)

    fun getChatMessages(chatId: String, page: Int = 0, size: Int = 50): Flux<MessageModel> =
        messageRepository.findAllByChatIdOrderByTimestampAsc(chatId, PageRequest.of(page, size))


    fun getChatById(chatId: String): Mono<ChatModel> =
        chatRepository.findById(chatId)


    fun streamMessages(chatId: String): Flux<MessageModel> {
        println("🔄 streamMessages subscribed for chatId=$chatId")
        val sink = chatMessageSinks.getOrPut(chatId) {
            println("📌 Creating new sink for chatId=$chatId")
            Sinks.many().multicast().onBackpressureBuffer()
        }
        return sink.asFlux()
    }

    fun emitMessage(chatId: String, message: MessageModel) {
        val sink = chatMessageSinks.getOrPut(chatId) {
            Sinks.many().multicast().onBackpressureBuffer() // ✅ במקום replay().latest()
        }
        val result = sink.tryEmitNext(message)
        if (result.isFailure) {
            println("⚠️ Failed to emit message to sink for chatId=$chatId: $result")
        } else {
            println("✅ Emitted message to sink for chatId=$chatId")
        }
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


}

