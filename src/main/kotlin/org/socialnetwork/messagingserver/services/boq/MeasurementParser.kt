package org.socialnetwork.messagingserver.services.boq

object MeasurementParser {
    private val twoNumsRegex = Regex("""(?:(\d{2,4})\s*[/\\xX]\s*(\d{2,4}))""")

    fun parse(raw: String?): Pair<Double, Double>? {
        if (raw.isNullOrBlank()) return null
        twoNumsRegex.find(raw)?.let { m ->
            val a = m.groupValues[1].toDoubleOrNull()
            val b = m.groupValues[2].toDoubleOrNull()
            if (a != null && b != null) return a to b
        }
        val numbers = raw.lines()
            .mapNotNull { it.trim().takeIf { s -> s.matches(Regex("""^\d{2,4}$""")) } }
            .mapNotNull { it.toDoubleOrNull() }
        return if (numbers.size >= 2) numbers[0] to numbers[1] else null
    }
}
