package org.autoffer.models

data class LoginRequest(
    val email: String = "",
    val password: String = ""
)
