package org.autoffer.repositories

import org.autoffer.models.UserModel
import org.autoffer.models.UserType
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


interface UserRepository : ReactiveMongoRepository<UserModel, String>{
    fun findByEmail(email: String): Mono<UserModel>
    fun findByPhoneNumber(phoneNumber: String): Mono<UserModel>
    fun findAllByProfileType(profileType: UserType): Flux<UserModel>

}

