package org.autoffer.models

data class UserChatsRequest(
    val userId: String,
    val page: Int,
    val size: Int
)
