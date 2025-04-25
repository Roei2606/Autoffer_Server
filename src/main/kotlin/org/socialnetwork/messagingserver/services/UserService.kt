package org.socialnetwork.messagingserver.services

import org.socialnetwork.messagingserver.models.ProfileType
import org.socialnetwork.messagingserver.models.RegisterUserRequest
import org.socialnetwork.messagingserver.models.UserModel
import org.socialnetwork.messagingserver.repositories.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class UserService(private val userRepository: UserRepository) {

    // ✅ רישום משתמש חדש עם כל השדות
    fun registerUser(request: RegisterUserRequest): Mono<UserModel> {
        return userRepository.findByEmail(request.email)
            .flatMap {
                Mono.error<UserModel>(IllegalStateException("User with this email already exists"))
            }
            .switchIfEmpty(
                userRepository.save(
                    UserModel(
                        id = null,
                        firstName = request.firstName,
                        lastName = request.lastName,
                        email = request.email,
                        password = request.password,
                        phoneNumber = request.phoneNumber,
                        address = request.address,
                        profileType = request.profileType,
                        registeredAt = LocalDateTime.now(),
                        chats = mutableListOf()
                    )
                )
            )
    }




    // ✅ התחברות לפי אימייל וסיסמה
    fun loginUser(email: String, password: String): Mono<UserModel> {
        return userRepository.findByEmail(email)
            .flatMap { user ->
                if (user.password == password) {
                    Mono.just(user)
                } else {
                    Mono.error(IllegalArgumentException("Invalid password"))
                }
            }
    }

    // ✅ שליפת כל המשתמשים
    fun getAllUsers(): Flux<UserModel> {
        return userRepository.findAll()
    }

    // ✅ שליפה לפי ID
    fun getUserById(userId: String): Mono<UserModel> {
        return userRepository.findById(userId)
    }

    // ✅ הוספת מזהה צ'אט לרשימת המשתמשים
    fun addChatToUsers(chatId: String, userIds: List<String>): Mono<Void> {
        return Flux.fromIterable(userIds)
            .flatMap { userId ->
                userRepository.findById(userId)
                    .flatMap { user ->
                        val updatedChats = (user.chats ?: mutableListOf()).toMutableList()
                        if (!updatedChats.contains(chatId)) {
                            val updatedUser = user.copy(chats = updatedChats.apply { add(chatId) })
                            userRepository.save(updatedUser)
                        } else {
                            Mono.just(user)
                        }
                    }
            }
            .then()
    }


    fun resetPassword(phoneNumber: String, newPassword: String): Mono<Void> {
        return userRepository.findByPhoneNumber(phoneNumber)
            .switchIfEmpty(Mono.error(IllegalArgumentException("User with this phone number not found")))
            .flatMap { user ->
                val updatedUser = user.copy(password = newPassword)
                userRepository.save(updatedUser)
            }
            .then() // return Mono<Void>
    }

}
