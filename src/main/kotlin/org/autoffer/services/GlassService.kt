package org.autoffer.services

import org.autoffer.models.GlassModel
import org.autoffer.repositories.GlassRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class GlassService(
    private val glassRepository: GlassRepository
) {
    fun getGlassesForProfile(profileNumber: String): Flux<GlassModel> {
        return glassRepository.findBySupportedProfilesContaining(profileNumber)
    }
}

