package org.socialnetwork.messagingserver.models

data class ChatRequest(
    val currentUserId: String,
    val otherUserId: String
)