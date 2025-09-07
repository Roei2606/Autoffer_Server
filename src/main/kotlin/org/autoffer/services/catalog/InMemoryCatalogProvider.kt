package org.autoffer.services.catalog

import org.autoffer.models.AlumProfileUsageType


class InMemoryCatalogProvider : CatalogProvider {

    private val profiles = listOf(
        ProfileCatalogEntry("1700", AlumProfileUsageType.WINDOW_SLIDE, 600,1500, 600,1800, true,  "Triplex 3+3",       320.0),
        ProfileCatalogEntry("2200", AlumProfileUsageType.DOOR_SLIDE,   1800,2600,1800,4000, true, "Triplex 4+4",       747.0),
        ProfileCatalogEntry("4300", AlumProfileUsageType.DOOR_OPEN,    1800,2300,800,1000,  false,"Triplex 5+5",       301.0),
        ProfileCatalogEntry("4500", AlumProfileUsageType.WINDOW_OPEN,  600,1500, 600,1800, false,"Triplex 5+5",       293.0),
        ProfileCatalogEntry("5500", AlumProfileUsageType.DOOR_OPEN,    1800,2300,800,1000,  true, "Triplex 6+6",       320.0),
        ProfileCatalogEntry("5500", AlumProfileUsageType.WINDOW_OPEN,  600,1500, 600,1800, true, "Triplex 6+6",       320.0),
        ProfileCatalogEntry("7000", AlumProfileUsageType.WINDOW_SLIDE, 600,1500, 600,1800, false,"Triplex 4+4",       213.0),
        ProfileCatalogEntry("9000", AlumProfileUsageType.DOOR_SLIDE,   1800,2600,1800,4000, false,"Insulated 12+18+12",293.0)
    )
    private val glass = listOf(
        GlassCatalogEntry("Triplex 3+3",        187.0),
        GlassCatalogEntry("Triplex 4+4",        240.0),
        GlassCatalogEntry("Triplex 5+5",        227.0),
        GlassCatalogEntry("Triplex 6+6",        307.0),
        GlassCatalogEntry("Insulated 12+18+12", 280.0)
    )

    override fun findProfilesByUsage(usageType: AlumProfileUsageType) =
        profiles.filter { it.usageType == usageType }
    override fun findProfileEntry(profileNumber: String, usageType: AlumProfileUsageType) =
        profiles.firstOrNull { it.profileNumber == profileNumber && it.usageType == usageType }
    override fun allProfileEntries() = profiles
    override fun findGlassEntry(type: String) =
        glass.firstOrNull { it.type.equals(type, ignoreCase = true) }
    override fun allGlassEntries() = glass
}
