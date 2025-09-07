package org.autoffer.models

data class AutoQuotePreviewResponse(
    val projectId: String,
    val factoryId: String,
    val items: List<ItemModelDTO>,
    val issues: List<NormalizationIssue>,
    val finalPrice: Double,
    val factorApplied: Double,
    val timingMs: PreviewTiming = PreviewTiming()
)

data class PreviewTiming(
    val docaiMs: Long = 0,
    val normalizeMs: Long = 0,
    val pricingMs: Long = 0,
    val totalMs: Long = 0
)

data class NormalizationIssue(
    val sourceId: String?,      // למשל "008" מ-DocAI
    val itemIndex: Int?,        // fallback אם אין id
    val type: IssueType,
    val detail: String? = null
)

enum class IssueType {
    INCOMPLETE_MEASUREMENTS,
    OUT_OF_RANGE,
    INCOMPATIBLE_GLASS,
    PROFILE_AMBIGUOUS,
    UNKNOWN_PROFILE,
    UNSUPPORTED_GLASS_STYLE
}
