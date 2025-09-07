package org.autoffer.models

data class FactoryStatusDto(
    val factoryId: String,
    val quoteStatus: QuoteStatus,
    val sentAt: String?
)
