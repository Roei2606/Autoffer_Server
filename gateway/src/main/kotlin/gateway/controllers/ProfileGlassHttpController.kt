package gateway.controllers

import gateway.services.ProfileGlassGateway
import org.autoffer.models.AlumProfileModel
import org.autoffer.models.GlassModel
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api")
class ProfileGlassHttpController(
    private val gateway: ProfileGlassGateway
) {
    // קיים: profiles.matchBySize → GET /api/profiles/match?width=&height=
    @GetMapping("/profiles/match", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun matchProfilesBySize(
        @RequestParam width: Int,
        @RequestParam height: Int
    ): Mono<List<AlumProfileModel>> =
        gateway.matchBySize(org.autoffer.models.SizeRequest(width = width, height = height)).collectList()

    // קיים: glasses.getByProfile → GET /api/glasses?profileNumber=
    @GetMapping("/glasses", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getGlassesByProfile(@RequestParam profileNumber: String): Mono<List<GlassModel>> =
        gateway.glassesByProfile(profileNumber.trim('"')).collectList()

    // רשות: דורש route חדש "profiles.getAll"
    @GetMapping("/profiles", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllProfiles(): Mono<List<AlumProfileModel>> =
        gateway.getAllProfiles().collectList()

    // רשות: דורש route חדש "profiles.getByProfileNumber"
    @GetMapping("/profiles/by-number/{profileNumber}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByProfileNumber(@PathVariable profileNumber: String): Mono<List<AlumProfileModel>> =
        gateway.getByProfileNumber(profileNumber.trim('"')).collectList()
}
