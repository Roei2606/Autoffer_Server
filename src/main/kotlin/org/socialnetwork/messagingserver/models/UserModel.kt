package org.socialnetwork.messagingserver.models

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class UserModel(
    @Id val id: String? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val address: String,
    val profileType: ProfileType,
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    val registeredAt: LocalDateTime = LocalDateTime.now(),
    val chats: MutableList<String> = mutableListOf()
)
