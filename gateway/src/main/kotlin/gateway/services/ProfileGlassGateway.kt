package gateway.services

import org.autoffer.models.AlumProfileModel
import org.autoffer.models.GlassModel
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux


@Service
class ProfileGlassGateway(
    private val rs: RSocketRequester
) {
    /** קיים אצלך: @MessageMapping("profiles.matchBySize") */
    fun matchBySize(req: org.autoffer.models.SizeRequest): Flux<AlumProfileModel> =
        rs.route("profiles.matchBySize")
            .data(req)
            .retrieveFlux(AlumProfileModel::class.java)

    /** קיים אצלך: @MessageMapping("glasses.getByProfile") */
    fun glassesByProfile(profileNumber: String): Flux<GlassModel> =
        rs.route("glasses.getByProfile")
            .data(profileNumber)
            .retrieveFlux(GlassModel::class.java)

    /** רשות: דורש route חדש בצד השרת: "profiles.getAll" */
    fun getAllProfiles(): Flux<AlumProfileModel> =
        rs.route("profiles.getAll")
            .retrieveFlux(AlumProfileModel::class.java)

    /** רשות: דורש route חדש בצד השרת: "profiles.getByProfileNumber" */
    fun getByProfileNumber(profileNumber: String): Flux<AlumProfileModel> =
        rs.route("profiles.getByProfileNumber")
            .data(profileNumber)
            .retrieveFlux(AlumProfileModel::class.java)
}
