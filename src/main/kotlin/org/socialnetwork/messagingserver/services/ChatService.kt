package org.socialnetwork.messagingserver.services

import org.socialnetwork.messagingserver.models.ChatModel
import org.socialnetwork.messagingserver.models.MessageModel

import org.socialnetwork.messagingserver.repositories.ChatRepository
import org.socialnetwork.messagingserver.repositories.MessageRepository
import org.springframework.data.domain.PageRequest

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) {

    fun createChat(participants: List<String>, isGroup: Boolean, name: String?): Mono<ChatModel> {
        val chat = ChatModel(
            id=null,
            name = name,
            participants = participants,
            isGroup = isGroup
        )
        return chatRepository.save(chat)
    }

    fun getUserChats(userId: String, page: Int, size: Int): Flux<ChatModel> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "name"))
        return chatRepository.findByParticipantsContaining(userId, pageable)
    }

    fun getUnreadCount(chatId: String, userId: String): Mono<Long> {
        return messageRepository.countByChatIdAndReadByNotContaining(chatId, userId)
    }

    fun markMessagesAsRead(chatId: String, userId: String): Mono<Void> {
        return messageRepository.findByChatIdAndReadByNotContaining(chatId, userId)
            .flatMap { message ->
                message.readBy!!.add(userId)
                messageRepository.save(message)
            }
            .then()
    }

    fun getChatMessages(chatId: String, page: Int, size: Int): Flux<MessageModel> {
        val pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"))
        return messageRepository.findAllByChatId(chatId, pageRequest)
    }

    fun getOrCreateChat(user1: String, user2: String): Mono<ChatModel> {
        val participants = listOf(user1, user2).sorted() // Ensure consistent order for queries

        return chatRepository.findByParticipants(participants)
            .switchIfEmpty(
                chatRepository.save(
                    ChatModel(
                        id= UUID.randomUUID().toString(),
                        name = "Private Chat",
                        participants = participants,
                        isGroup = false
                    )
                )
            )
    }
}
