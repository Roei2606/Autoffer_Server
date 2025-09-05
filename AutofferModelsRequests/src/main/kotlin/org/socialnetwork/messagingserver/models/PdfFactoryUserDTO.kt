package org.socialnetwork.messagingserver.models

data class PdfFactoryUserDTO(
    val factoryName: String? = null,
    val factor:      Double? = null,   // אם לא יישלח – נשתמש ב-factor מהבקשה או 1.0
    val firstName:   String? = null,
    val lastName:    String? = null,
    val email:       String? = null,
    val phone:       String? = null,
    val address:     String? = null,
    val businessId:  String? = null
)