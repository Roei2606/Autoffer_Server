package org.autoffer.services.boq

import org.autoffer.models.AlumProfileUsageType


object UsageAndAliasResolver {
    data class UsageGuess(val isDoor: Boolean, val isSlide: Boolean)

    fun guessUsage(configuration: String?, opening: String?): UsageGuess {
        val s = "${configuration ?: ""} ${opening ?: ""}"
        val isDoor  = s.contains("דלת") || s.contains("door", true)
        val isSlide = s.contains("הזזה") || s.contains("slide", true)
        val isOpen  = s.contains("ציר") || s.contains("דריי") || s.contains("קבוע")
        return when {
            isDoor && isSlide -> UsageGuess(true, true)
            isDoor && isOpen  -> UsageGuess(true, false)
            !isDoor && isSlide-> UsageGuess(false, true)
            else              -> UsageGuess(false, false)
        }
    }

    // 7600→7000, 4350→4500, 5600→5500, אחרת מחלץ ספרות
    fun resolveProfileNumberAlias(modelNumberRaw: String?): String? {
        val s = modelNumberRaw ?: return null
        val digits = Regex("""\d{3,4}""").find(s)?.value
        return when (digits) {
            "7600" -> "7000"
            "4350" -> "4500"
            "5600" -> "5500"
            else -> digits
        }
    }

    fun usageTypeOf(profileNumber: String?, usageGuess: UsageGuess): AlumProfileUsageType =
        when (profileNumber) {
            "1700","7000" -> AlumProfileUsageType.WINDOW_SLIDE
            "2200","9000" -> AlumProfileUsageType.DOOR_SLIDE
            "4500"        -> AlumProfileUsageType.WINDOW_OPEN
            "4300"        -> AlumProfileUsageType.DOOR_OPEN
            "5500"        -> if (usageGuess.isDoor) AlumProfileUsageType.DOOR_OPEN else AlumProfileUsageType.WINDOW_OPEN
            else -> if (usageGuess.isDoor && usageGuess.isSlide) AlumProfileUsageType.DOOR_SLIDE
            else if (usageGuess.isDoor) AlumProfileUsageType.DOOR_OPEN
            else if (usageGuess.isSlide) AlumProfileUsageType.WINDOW_SLIDE
            else AlumProfileUsageType.WINDOW_OPEN
        }
}
