package org.socialnetwork.messagingserver.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "messages")
data class MessageModel(
    @Id val id: String? = null,
    val chatId: String,
    val senderId: String,
    val content: String,
    val timestamp: LocalDateTime? = LocalDateTime.now(),
    val readBy: MutableList<String>? = mutableListOf()
){
    constructor() : this(null, "", "", "", LocalDateTime.now(), mutableListOf())
}
