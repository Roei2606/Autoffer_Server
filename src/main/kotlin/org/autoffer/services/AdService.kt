package org.autoffer.services

import org.autoffer.models.AdModel
import org.autoffer.repositories.AdRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class AdsService(
    private val adRepository: AdRepository
) {
    fun getAdsForAudience(audience: String): Flux<AdModel> {
        return adRepository.findAdsByAudience(audience)
    }
    fun getAllAds(): Flux<AdModel> = adRepository.findAll()
}
