package org.socialnetwork.messagingserver.repositories
import org.socialnetwork.messagingserver.models.GroupModel
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface GroupRepository : ReactiveMongoRepository<GroupModel, String>
