package gateway.services

import org.springframework.stereotype.Service
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// DTOs/מודלים מהמודול המשותף (ללא שינוי)
import org.socialnetwork.messagingserver.models.*
import org.socialnetwork.messagingserver.modelsdata.UnreadCountResponse

@Service
class ChatGateway(
    private val rs: RSocketRequester
) {
    /** תואם: @MessageMapping("chats.getOrCreate") */
    fun getOrCreateChat(req: ChatRequest): Mono<ChatModel> =
        rs.route("chats.getOrCreate")
            .data(req)
            .retrieveMono(ChatModel::class.java)

    /** תואם: @MessageMapping("chats.getAll") */
    fun getUserChats(req: UserChatsRequest): Flux<ChatModel> =
        rs.route("chats.getAll")
            .data(req)
            .retrieveFlux(ChatModel::class.java)

    /** תואם: @MessageMapping("chats.getMessages") */
    fun getChatMessages(req: ChatMessagesRequest): Flux<MessageModel> =
        rs.route("chats.getMessages")
            .data(req)
            .retrieveFlux(MessageModel::class.java)

    /** תואם: @MessageMapping("chats.streamMessages") */
    fun streamMessages(chatId: String): Flux<MessageModel> =
        rs.route("chats.streamMessages")
            .data(chatId)
            .retrieveFlux(MessageModel::class.java)

    /** תואם: @MessageMapping("chats.getChatById") (במקור מקבל Map<String,String>) */
    fun getChatById(chatId: String): Mono<ChatModel> =
        rs.route("chats.getChatById")
            .data(mapOf("chatId" to chatId))
            .retrieveMono(ChatModel::class.java)

    /** תואם: @MessageMapping("chats.getUnreadCount") */
    fun getUnreadCount(req: UnreadCountRequest): Mono<UnreadCountResponse> =
        rs.route("chats.getUnreadCount")
            .data(req)
            .retrieveMono(UnreadCountResponse::class.java)

    /** תואם: @MessageMapping("chat.sendfile") */
    fun sendFileMessage(req: FileMessageRequest): Mono<Void> =
        rs.route("chat.sendfile")
            .data(req)
            .send()
}
