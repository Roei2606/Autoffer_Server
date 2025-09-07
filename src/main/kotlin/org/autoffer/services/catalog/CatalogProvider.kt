package org.autoffer.services.catalog

import org.autoffer.models.AlumProfileUsageType

data class ProfileCatalogEntry(
    val profileNumber: String,
    val usageType: AlumProfileUsageType,
    val minHeight: Int, val maxHeight: Int,
    val minWidth: Int, val maxWidth: Int,
    val isExpensive: Boolean,
    val recommendedGlassType: String,
    val pricePerSquareMeter: Double
)
data class GlassCatalogEntry(
    val type: String,
    val pricePerSquareMeter: Double
)


interface CatalogProvider {
    fun findProfilesByUsage(usageType: AlumProfileUsageType): List<ProfileCatalogEntry>
    fun findProfileEntry(profileNumber: String, usageType: AlumProfileUsageType): ProfileCatalogEntry?
    fun allProfileEntries(): List<ProfileCatalogEntry>

    fun findGlassEntry(type: String): GlassCatalogEntry?
    fun allGlassEntries(): List<GlassCatalogEntry>
}
