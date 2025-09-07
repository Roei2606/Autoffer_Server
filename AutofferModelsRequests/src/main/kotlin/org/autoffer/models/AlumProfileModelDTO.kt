package org.autoffer.models

data class AlumProfileModelDTO(
    val profileNumber: String,
    val usageType: AlumProfileUsageType,
    val pricePerSquareMeter: Double
)