package gateway.services

import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// DTOs/מודלים מהמודול המשותף
import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.models.UnreadCountRequest

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
