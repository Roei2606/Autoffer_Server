package org.socialnetwork.messagingserver.controllers




import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.services.GroupService
import org.springframework.http.MediaType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class GroupController(private val groupService: GroupService) {

    @MessageMapping("group.receiveMessages")
    fun receiveGroupMessages(): Flux<MessageModel> = groupService.receiveGroupMessages()

    @MessageMapping("group.sendMessage")
    fun sendGroupMessage(message: MessageModel): Mono<MessageModel> {
        return groupService.sendGroupMessage(message.chatId!!, message.senderId!!, message.content!!)
    }
}
