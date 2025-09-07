package gateway.controllers

import gateway.services.ChatGateway
import org.autoffer.models.*
import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import org.socialnetwork.messagingserver.modelsdata.UnreadCountResponse

@RestController
@RequestMapping("/api/chats")
class ChatHttpController(
    private val gateway: ChatGateway
) {

    /** RSocket: "chats.getOrCreate" */
    @PostMapping("/get-or-create", consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOrCreate(@RequestBody req: ChatRequest): Mono<ChatModel> =
        gateway.getOrCreateChat(req)

    /** RSocket: "chats.getAll" */
    @PostMapping("/all", consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(@RequestBody req: UserChatsRequest): Flux<ChatModel> =
        gateway.getUserChats(req)

    /** RSocket: "chats.getMessages"  |  GET /api/chats/{chatId}/messages?page=&size= */
    @GetMapping("/{chatId}/messages", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMessages(
        @PathVariable chatId: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int
    ): Flux<MessageModel> =
        gateway.getChatMessages(org.autoffer.models.ChatMessagesRequest(chatId = chatId, page = page, size = size))

    /** RSocket: "chats.streamMessages"  |  SSE */
    @GetMapping(
        "/{chatId}/stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun stream(@PathVariable chatId: String): Flux<MessageModel> =
        gateway.streamMessages(chatId)

    /** RSocket: "chats.getChatById" */
    @GetMapping("/{chatId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable chatId: String): Mono<ChatModel> =
        gateway.getChatById(chatId)

    /** RSocket: "chats.getUnreadCount" */
    @PostMapping("/unread-count", consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun unreadCount(@RequestBody req: UnreadCountRequest): Mono<UnreadCountResponse> =
        gateway.getUnreadCount(req)

    /** RSocket: "chat.sendfile" */
    @PostMapping("/send-file", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendFile(@RequestBody req: FileMessageRequest): Mono<ResponseEntity<Void>> =
        gateway.sendFileMessage(req).thenReturn(ResponseEntity.noContent().build())
}
