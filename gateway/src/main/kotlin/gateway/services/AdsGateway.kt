package gateway.services

import org.autoffer.models.AdModel
import org.autoffer.models.AdsRequest
import org.springframework.stereotype.Service
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.core.publisher.Flux


@Service
class AdsGateway(
    private val rs: RSocketRequester
) {
    fun getAdsForAudience(req: AdsRequest): Flux<AdModel> =
        rs.route("ads.getAdsForAudience")
            .data(req)
            .retrieveFlux(AdModel::class.java)
}
