package org.autoffer.models

data class ChatMessagesRequest(
    val chatId: String,
    val page: Int = 0,
    val size: Int = 50
)