package gateway.controllers

import gateway.services.AdsGateway
import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType
import reactor.core.publisher.Flux


// DTOs/מודלים מהמוביל המשותף
import org.socialnetwork.messagingserver.models.AdsRequest
import org.socialnetwork.messagingserver.models.AdModel

@RestController
@RequestMapping("/api")
class AdsHttpController(
    private val gateway: AdsGateway
) {
    /**
     * iOS → HTTP:
     * POST /api/ads/by-audience
     * Body: AdsRequest (בדיוק כמו בצד השרת המקורי)
     *
     * ממופה 1:1 ל־RSocket: "ads.getAdsForAudience"
     */
    @PostMapping(
        "/ads/by-audience",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAdsForAudience(@RequestBody req: AdsRequest): Flux<AdModel> =
        gateway.getAdsForAudience(req)
}
