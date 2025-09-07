package org.autoffer.models

data class UserProjectRequest(
    val userId: String,
    val profileType: UserType
)
