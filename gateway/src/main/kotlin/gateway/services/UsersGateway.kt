package gateway.services

import org.autoffer.models.*
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Service
class UsersGateway(private val rs: RSocketRequester) {

    fun login(req: LoginRequest): Mono<UserModel> =
        rs.route("users.login").data(req).retrieveMono(UserModel::class.java)

    fun register(req: RegisterUserRequest): Mono<UserModel> =
        rs.route("users.register").data(req).retrieveMono(UserModel::class.java)

    fun getAll(): Flux<UserModel> =
        rs.route("users.getAll").retrieveFlux(UserModel::class.java)

    fun getById(userId: String): Mono<UserModel> =
        rs.route("users.getById").data(UserIdRequest(userId)).retrieveMono(UserModel::class.java)

    fun resetPassword(req: ResetPasswordRequest): Mono<Void> =
        rs.route("users.resetPassword").data(req).retrieveMono(Void::class.java).then()

    fun getByType(type: UserType): Flux<UserModel> =
        rs.route("users.getByType").data(type.name).retrieveFlux(UserModel::class.java)
}
