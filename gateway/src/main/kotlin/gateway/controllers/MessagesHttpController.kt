package gateway.controllers

import gateway.services.MessagesGateway
import org.autoffer.models.MessageModel
import org.autoffer.models.UnreadCountRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api/messages")
class MessagesHttpController(
    private val gateway: MessagesGateway
) {

    /** RSocket: "messages.send" */
    @PostMapping(
        "/send",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun send(@RequestBody message: MessageModel): Mono<MessageModel> =
        gateway.send(message)

    /** RSocket: "messages.stream" (SSE) */
    @GetMapping(
        "/stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun stream(@RequestParam chatId: String): Flux<MessageModel> =
        gateway.stream(chatId)

    /** RSocket: "messages.markAsRead" */
    @PostMapping("/mark-read", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun markRead(@RequestBody req: UnreadCountRequest): Mono<ResponseEntity<Void>> =
        gateway.markAsRead(req).thenReturn(ResponseEntity.noContent().build())
}
