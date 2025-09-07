package org.autoffer.services


import org.autoffer.models.AlumProfileModel
import org.autoffer.repositories.AlumProfileRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class AlumProfileService(
    private val profileRepository: AlumProfileRepository
) {
    fun getMatchingProfiles(width: Int, height: Int): Flux<AlumProfileModel> {
        println("🔍 Searching profiles for height=$height, width=$width")
        return profileRepository
            .findAllByMinHeightLessThanEqualAndMaxHeightGreaterThanEqualAndMinWidthLessThanEqualAndMaxWidthGreaterThanEqual(
                height, height, width, width
            )
    }
    fun getAllProfiles(): Flux<AlumProfileModel> {
        return profileRepository.findAll()
    }
}
