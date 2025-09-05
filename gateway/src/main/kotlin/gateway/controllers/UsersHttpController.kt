package gateway.controllers

import gateway.services.UsersGateway
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.socialnetwork.messagingserver.models.*

@RestController
@RequestMapping("/api/users")
class UsersHttpController(private val svc: UsersGateway) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): Mono<UserModel> = svc.login(req)

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterUserRequest): Mono<UserModel> = svc.register(req)

    @GetMapping
    fun getAll(): Flux<UserModel> = svc.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): Mono<UserModel> = svc.getById(id)

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resetPassword(@RequestBody req: ResetPasswordRequest): Mono<Void> = svc.resetPassword(req)

    @GetMapping("/type/{type}")
    fun getByType(@PathVariable type: String): Flux<UserModel> =
        svc.getByType(UserType.valueOf(type))
}
