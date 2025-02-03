package org.socialnetwork.messagingserver.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.socialnetwork.messagingserver.models.MessageModel

@Document(collection = "groups")
data class GroupModel(
    @Id val id: String?,
    val name: String?,
    val members: MutableList<String>?,
    val messages: MutableList<MessageModel>? = mutableListOf()
)
