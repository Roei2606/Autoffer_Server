package org.autoffer.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "docai.python")
data class DocAiPythonProperties(
    val baseUrl: String,                // ex: http://localhost:5001
    val parsePath: String,              // ex: /process_pdf/
    val authHeaderName: String = "X-API-Key",
    val authToken: String? = null,
    val connectTimeoutMs: Int = 5_000,
    val readTimeoutMs: Int = 20_000
)
