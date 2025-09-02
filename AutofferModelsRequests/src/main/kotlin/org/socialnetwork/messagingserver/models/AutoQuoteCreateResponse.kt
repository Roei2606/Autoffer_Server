package org.socialnetwork.messagingserver.models

data class AutoQuoteCreateResponse(
    val projectId: String,
    val factoryId: String,
    val quoteId: String,
    val itemsCount: Int,
    val issuesCount: Int,
    val finalPrice: Double
)
