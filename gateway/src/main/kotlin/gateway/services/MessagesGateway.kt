package gateway.services

import org.autoffer.models.MessageModel
import org.autoffer.models.UnreadCountRequest
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono



@Service
class MessagesGateway(
    private val rs: RSocketRequester
) {
    /** תואם: @MessageMapping("messages.send") */
    fun send(message: MessageModel): Mono<MessageModel> =
        rs.route("messages.send")
            .data(message)
            .retrieveMono(MessageModel::class.java)

    /** תואם: @MessageMapping("messages.stream") */
    fun stream(chatId: String): Flux<MessageModel> =
        rs.route("messages.stream")
            .data(chatId)
            .retrieveFlux(MessageModel::class.java)

    /** תואם: @MessageMapping("messages.markAsRead") */
    fun markAsRead(req: UnreadCountRequest): Mono<Void> =
        rs.route("messages.markAsRead")
            .data(req)
            .send()
}
