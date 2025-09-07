package org.autoffer.services

import org.autoffer.models.ItemModelDTO
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class PricingService {

    data class LinePrice(
        val itemNumber: Int,
        val quantity: Int,
        val areaM2: Double,
        val unitPricePerM2: Double,
        val lineTotal: Double
    )
    data class PricingResult(
        val lines: List<org.autoffer.services.PricingService.LinePrice>,
        val subtotal: Double,
        val factorApplied: Double,
        val finalTotal: Double
    )

    fun price(items: List<ItemModelDTO>, factor: Double = 1.0): org.autoffer.services.PricingService.PricingResult {
        val lines = items.map {
            val areaM2 = (it.width / 100.0) * (it.height / 100.0)
            val unit = it.profile.pricePerSquareMeter + it.glass.pricePerSquareMeter
            val line = unit * areaM2 * it.quantity
            org.autoffer.services.PricingService.LinePrice(
                itemNumber = it.itemNumber,
                quantity = it.quantity,
                areaM2 = r3(areaM2),
                unitPricePerM2 = unit,
                lineTotal = r2(line)
            )
        }
        val subtotal = r2(lines.sumOf { it.lineTotal })
        val final = r2(subtotal * factor)
        return org.autoffer.services.PricingService.PricingResult(lines, subtotal, factor, final)
    }
    private fun r2(v: Double) = round(v * 100.0) / 100.0
    private fun r3(v: Double) = round(v * 1000.0) / 1000.0
}