package org.socialnetwork.messagingserver.controllers

import org.socialnetwork.messagingserver.models.LoginRequest
import org.socialnetwork.messagingserver.services.UserService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.socialnetwork.messagingserver.models.RegisterUserRequest
import org.socialnetwork.messagingserver.models.UserIdRequest
import org.socialnetwork.messagingserver.models.UserModel


@Controller
class UserController(
    private val userService: UserService)
{

    @MessageMapping("users.login")
    fun loginUser(@Payload request: LoginRequest): Mono<UserModel> {
        println("Received login request for: ${request.username}")
        return userService.loginUser(request.username)
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

    @MessageMapping("users.getById")
    fun getUserById(@Payload request: UserIdRequest): Mono<UserModel> {
        return userService.getUserById(request.userId)
    }


}

