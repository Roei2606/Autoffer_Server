package org.socialnetwork.messagingserver.services.boq

import org.socialnetwork.messagingserver.integrations.docai.DocAiResponse
import org.socialnetwork.messagingserver.models.*
import org.socialnetwork.messagingserver.services.boq.UsageAndAliasResolver.UsageGuess
import org.socialnetwork.messagingserver.services.catalog.CatalogProvider
import org.socialnetwork.messagingserver.services.catalog.ProfileCatalogEntry
import org.springframework.stereotype.Service

@Service
class BoqNormalizationService(
    private val catalog: CatalogProvider
) {
    data class Result(val items: List<ItemModelDTO>, val issues: List<NormalizationIssue>)

    fun normalize(doc: DocAiResponse, skipUnparseable: Boolean = true): Result {
        val issues = mutableListOf<NormalizationIssue>()
        val mergedById = mutableMapOf<String, Map<String, String>>()

        // מיזוג לפי id – מעדיפים רשומה "עשירה" יותר
        doc.results.forEachIndexed { idx, r ->
            if (r.fields.isEmpty()) return@forEachIndexed
            val id = r.fields["id"] ?: "PAGE_${r.page}_IDX_$idx"
            val prev = mergedById[id]
            if (prev == null || r.fields.size > prev.size) mergedById[id] = r.fields
        }

        val items = mutableListOf<ItemModelDTO>()
        var run = 1

        mergedById.entries.sortedBy { it.key }.forEach { (sourceId, f) ->
            val quantity = f["quantity"]?.toIntOrNull() ?: 1

            // מידות
            val dims = MeasurementParser.parse(f["measurements"])
            if (dims == null) {
                issues += NormalizationIssue(sourceId, run, IssueType.INCOMPLETE_MEASUREMENTS, f["measurements"])
                if (!skipUnparseable) return@forEach
                run++; return@forEach
            }
            var (w, h) = dims

            // שימוש + פרופיל
            val usageGuess: UsageGuess = UsageAndAliasResolver.guessUsage(f["configuration"], f["opening"])
            val aliasProfile = UsageAndAliasResolver.resolveProfileNumberAlias(f["model_number"])
            val usageType = UsageAndAliasResolver.usageTypeOf(aliasProfile, usageGuess)

            val profileEntry = chooseProfileEntry(aliasProfile, usageType, w, h)
            if (profileEntry == null) {
                issues += NormalizationIssue(sourceId, run, IssueType.UNKNOWN_PROFILE,
                    "alias=$aliasProfile, usage=$usageType, measures=$w/$h")
                if (!skipUnparseable) return@forEach
                run++; return@forEach
            }

            orientToFit(profileEntry, w, h)?.let { (nw, nh) -> w = nw; h = nh } ?: run {
                issues += NormalizationIssue(sourceId, run, IssueType.OUT_OF_RANGE,
                    "profile=${profileEntry.profileNumber}/${profileEntry.usageType}, measures=$w/$h")
                if (!skipUnparseable) return@forEach
                run++; return@forEach
            }

            // זכוכית
            val glassType = resolveGlassType(f["glazing"], profileEntry)
            val glassEntry = catalog.findGlassEntry(glassType)
            if (glassEntry == null) {
                issues += NormalizationIssue(sourceId, run, IssueType.INCOMPATIBLE_GLASS, "type=$glassType")
                if (!skipUnparseable) return@forEach
                run++; return@forEach
            }

            // מיפוי ל־DTOים שלך
            val profileDto = AlumProfileModelDTO(
                profileNumber = profileEntry.profileNumber,
                usageType = profileEntry.usageType,
                pricePerSquareMeter = profileEntry.pricePerSquareMeter
            )
            val glassDto = GlassModelDTO(
                type = glassEntry.type,
                pricePerSquareMeter = glassEntry.pricePerSquareMeter
            )

            items += ItemModelDTO(
                itemNumber = parseItemNumber(sourceId, run),
                profile = profileDto,
                glass = glassDto,
                height = h,
                width = w,
                quantity = quantity,
                location = f["location"] ?: "Unknown"
            )
            run++
        }

        return Result(items, issues)
    }

    private fun parseItemNumber(sourceId: String, fallback: Int): Int =
        sourceId.toIntOrNull() ?: sourceId.filter { it.isDigit() }.toIntOrNull() ?: fallback

    private fun chooseProfileEntry(
        aliasProfile: String?,
        usageType: AlumProfileUsageType,
        w: Double, h: Double
    ): ProfileCatalogEntry? {
        // 1) אם יש מספר פרופיל – ננסה אותו קודם
        if (aliasProfile != null) {
            catalog.findProfileEntry(aliasProfile, usageType)?.let { p ->
                if (orientToFit(p, w, h) != null) return p
            }
        }
        // 2) מועמדים לפי usageType (ממוין: זול תחילה)
        val candidates = catalog.findProfilesByUsage(usageType)
            .sortedWith(compareBy<ProfileCatalogEntry> { it.isExpensive }.thenBy { it.pricePerSquareMeter })
        return candidates.firstOrNull { orientToFit(it, w, h) != null }
            ?: catalog.allProfileEntries().firstOrNull { orientToFit(it, w, h) != null }
    }

    // בדיקת טווחים כולל החלפת רוחב/גובה אם זה מאפשר התאמה
    private fun orientToFit(p: ProfileCatalogEntry, w: Double, h: Double): Pair<Double, Double>? {
        val fitsWH = w in p.minWidth.toDouble()..p.maxWidth.toDouble() &&
                h in p.minHeight.toDouble()..p.maxHeight.toDouble()
        if (fitsWH) return w to h
        val fitsHW = h in p.minWidth.toDouble()..p.maxWidth.toDouble() &&
                w in p.minHeight.toDouble()..p.maxHeight.toDouble()
        if (fitsHW) return h to w
        return null
    }

    private fun resolveGlassType(glazingRaw: String?, profile: ProfileCatalogEntry): String {
        val s = glazingRaw ?: ""
        val isInsulated = s.contains("Insulated", true) || s.contains("בידוד") || s.contains("בידודית") || s.contains("בידודת")
        return if (isInsulated) "Insulated 12+18+12" else profile.recommendedGlassType
    }
}
