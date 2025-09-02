package org.socialnetwork.messagingserver.integrations.docai

import com.fasterxml.jackson.annotation.JsonProperty

data class DocAiResponse(
    val results: List<DocAiResult> = emptyList(),
    val timing: DocAiTiming? = null
)

data class DocAiResult(
    val page: Int,
    val fields: Map<String, String> = emptyMap()
)

data class DocAiTiming(
    @JsonProperty("total_duration_seconds")
    val totalDurationSeconds: Double? = null,

    @JsonProperty("processing_duration_seconds")
    val processingDurationSeconds: Double? = null,

    @JsonProperty("pages_processed")
    val pagesProcessed: Int? = null,

    @JsonProperty("average_per_page_seconds")
    val averagePerPageSeconds: Double? = null
)
