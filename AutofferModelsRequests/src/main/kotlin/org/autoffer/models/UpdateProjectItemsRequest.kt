package org.autoffer.models

data class UpdateProjectItemsRequest(
    val projectId: String,
    val items: List<ItemModel>
)
