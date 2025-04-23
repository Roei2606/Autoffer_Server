package org.socialnetwork.messagingserver.models

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class UserModel(
    @Id val id: String?,
    val username: String?,
    @JsonFormat(pattern = "dd-MM-yyyy" + "HH:mm:ss")
    val registeredAt: LocalDateTime? = LocalDateTime.now(),
    var chats: MutableList<String>? = mutableListOf()
){
    constructor() : this(null, null,null,null)
}
