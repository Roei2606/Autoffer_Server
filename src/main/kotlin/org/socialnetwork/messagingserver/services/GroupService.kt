package org.socialnetwork.messagingserver.services

import org.socialnetwork.messagingserver.models.MessageModel
import org.socialnetwork.messagingserver.repositories.GroupRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.LocalDateTime

@Service
class GroupService(private val groupRepository: GroupRepository) {

    private val groupSink: Sinks.Many<MessageModel> = Sinks.many().multicast().onBackpressureBuffer()

    fun sendGroupMessage(groupId: String, senderId: String, content: String): Mono<MessageModel> {
        return groupRepository.findById(groupId)
            .flatMap { group ->
                val message = MessageModel(id = null, senderId = senderId, chatId = groupId, content = content, timestamp = LocalDateTime.now(), readBy = null)
                group.messages!!.add(message)
                groupRepository.save(group).thenReturn(message)
            }
            .doOnSuccess { groupSink.tryEmitNext(it) }
    }

    fun receiveGroupMessages(): Flux<MessageModel> = groupSink.asFlux()

    fun getGroupChatHistory(groupId: String): Flux<MessageModel> {
        return groupRepository.findById(groupId).flatMapMany { group -> Flux.fromIterable(group.messages!!) }
    }
}
