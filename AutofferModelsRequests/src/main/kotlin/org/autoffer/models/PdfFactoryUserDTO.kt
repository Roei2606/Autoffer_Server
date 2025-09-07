package org.autoffer.models

data class PdfFactoryUserDTO(
    val factoryName: String? = null,
    val factor:      Double? = null,
    val firstName:   String? = null,
    val lastName:    String? = null,
    val email:       String? = null,
    val phone:       String? = null,
    val address:     String? = null,
    val businessId:  String? = null
)