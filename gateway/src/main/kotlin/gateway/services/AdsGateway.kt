package gateway.services

import org.springframework.stereotype.Service
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.core.publisher.Flux

// DTOs/מודלים בדיוק מהמודול המשותף (ללא שינוי)
import org.socialnetwork.messagingserver.models.AdsRequest
import org.socialnetwork.messagingserver.models.AdModel

@Service
class AdsGateway(
    private val rs: RSocketRequester
) {
    /** תואם: @MessageMapping("ads.getAdsForAudience") */
    fun getAdsForAudience(req: AdsRequest): Flux<AdModel> =
        rs.route("ads.getAdsForAudience")
            .data(req)
            .retrieveFlux(AdModel::class.java)
}
