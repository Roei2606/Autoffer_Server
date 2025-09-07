package org.autoffer.models

data class ResetPasswordRequest(
    val phoneNumber: String,
    val newPassword: String
)
