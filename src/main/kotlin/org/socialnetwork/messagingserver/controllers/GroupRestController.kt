package org.socialnetwork.messagingserver.controllers





import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.services.GroupService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/groups")
class GroupRestController(private val groupService: GroupService) {

    @GetMapping(
        "/history/{groupId}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getGroupChatHistory(@PathVariable groupId: String): Flux<MessageModel> {
        return groupService.getGroupChatHistory(groupId)
    }
}
