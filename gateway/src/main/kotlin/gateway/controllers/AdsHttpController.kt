package gateway.controllers

import gateway.services.AdsGateway
import org.autoffer.models.AdModel
import org.autoffer.models.AdsRequest
import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType
import reactor.core.publisher.Flux


@RestController
@RequestMapping("/api")
class AdsHttpController(
    private val gateway: AdsGateway
) {
    @PostMapping(
        "/ads/by-audience",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAdsForAudience(@RequestBody req: AdsRequest): Flux<AdModel> =
        gateway.getAdsForAudience(req)
}
