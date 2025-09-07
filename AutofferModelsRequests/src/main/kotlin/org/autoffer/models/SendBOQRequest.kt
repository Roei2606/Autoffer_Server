package org.autoffer.models

data class SendBOQRequest(
    val projectId: String,
    val factoryIds: List<String>
)
