package org.autoffer.controllers


import org.autoffer.models.AdModel
import org.autoffer.models.AdsRequest
import org.autoffer.services.AdsService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class AdsController(
    private val adsService: AdsService
) {

    @MessageMapping("ads.getAdsForAudience")
    fun getAdsForAudience(request: AdsRequest): Flux<AdModel> {
        println("📢 Fetching ads for audience: ${request.audience.name}")

        return adsService.getAdsForAudience(request.audience.name)
            .doOnNext { println("✅ Found ad: ${it.title}") }
    }
}
