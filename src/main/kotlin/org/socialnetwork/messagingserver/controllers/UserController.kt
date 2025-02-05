package org.socialnetwork.messagingserver.controllers

import org.socialnetwork.messagingserver.services.UserService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.socialnetwork.messagingserver.models.RegisterUserRequest
import org.socialnetwork.messagingserver.models.UserModel


@Controller
class UserController(
    private val userService: UserService)
{
    // Add this new login endpoint
    @MessageMapping("users.login")
    fun loginUser(@Payload username: String): Mono<UserModel> {
        println("Received login request for: $username")
        return userService.loginUser(username)
    }

    @MessageMapping("users.register")
    fun registerUser(@Payload request: RegisterUserRequest): Mono<UserModel> {
        println("Received register request for: ${request.username}")
        return userService.registerUser(request.username!!)
    }

    @MessageMapping("users.getAll")
    fun getAllUsers(): Flux<UserModel> {
        return userService.getAllUsers()
    }
}

