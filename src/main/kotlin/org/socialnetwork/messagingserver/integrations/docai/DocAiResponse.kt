package org.socialnetwork.messagingserver.integrations.docai

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class DocAiResponse(
    val results: List<DocAiResult> = emptyList(),
    val timing: DocAiTiming? = null
)

data class DocAiResult(
    val page: Int,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val fields: Map<String, String> = emptyMap(),
    val error: String? = null
)

data class DocAiTiming(
    val total_duration_seconds: Double? = null,
    val processing_duration_seconds: Double? = null,
    val pages_processed: Int? = null,
    val average_per_page_seconds: Double? = null
)
