package org.socialnetwork.messagingserver.services

import org.socialnetwork.messagingserver.models.UserModel
import org.socialnetwork.messagingserver.repositories.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class UserService(private val userRepository: UserRepository) {

    // ✅ הוספת משתמש חדש (RSocket Req-Res)
    fun registerUser(username: String): Mono<UserModel> {
        return userRepository.findByUsername(username)
            .switchIfEmpty(
                userRepository.save(
                    UserModel(
                        id = null,
                        username = username,
                        registeredAt = LocalDateTime.now(),
                        chats = mutableListOf()
                    )
                )
            )
    }

    fun loginUser(username: String): Mono<UserModel> {
        return userRepository.findByUsername(username)
            .switchIfEmpty(registerUser(username)) // Create user if not found
    }

    // ✅ Fetch All Users (RSocket Req-Stream)

    // ✅ שליפת כל המשתמשים (RSocket Req-Stream)
    fun getAllUsers(): Flux<UserModel> {
        return userRepository.findAll()
    }

    fun getUserById(userId: String): Mono<UserModel> {
        return userRepository.findById(userId)
    }

    fun addChatToUsers(chatId: String, userIds: List<String>): Mono<Void> {
        return Flux.fromIterable(userIds)
            .flatMap { userId ->
                userRepository.findById(userId)
                    .flatMap { user ->
                        val updatedChats = (user.chats ?: mutableListOf()).toMutableList()
                        if (!updatedChats.contains(chatId)) {
                            updatedChats.add(chatId)
                            val updatedUser = user.copy(chats = updatedChats)
                            userRepository.save(updatedUser)
                        } else {
                            Mono.just(user)
                        }
                    }
            }
            .then()
    }


}
