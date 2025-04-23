package org.socialnetwork.messagingserver.repositories

import org.socialnetwork.messagingserver.models.UserModel
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono


interface UserRepository : ReactiveMongoRepository<UserModel, String>{
    fun findByUsername(userID: String): Mono<UserModel>
}

