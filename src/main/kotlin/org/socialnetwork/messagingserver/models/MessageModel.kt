package org.socialnetwork.messagingserver.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "messages")
data class MessageModel(
    @Id val id: String? = null,
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: String,
    val readBy: MutableList<String> = mutableListOf()

)
