package gateway.services

import org.springframework.stereotype.Service
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// ייבוא של המודלים/DTOs בדיוק כמו בשרת המקורי (מגיעים ממודול autoffer-dtos)
import org.socialnetwork.messagingserver.models.*

@Service
class ProjectGateway(private val rs: RSocketRequester) {

    fun createProject(req: CreateProjectRequest): Mono<ProjectDTO> =
        rs.route("project.create").data(req).retrieveMono(ProjectDTO::class.java)

    fun getById(projectId: String): Mono<ProjectDTO> =
        rs.route("projects.getById").data(projectId).retrieveMono(ProjectDTO::class.java)

    fun sendToFactories(req: SendBOQRequest): Mono<Void> =
        rs.route("projects.sendToFactories").data(req).send()

    fun getAllForUser(req: UserProjectRequest): Flux<ProjectDTO> =
        rs.route("projects.getAllForUser").data(req).retrieveFlux(ProjectDTO::class.java)

    fun delete(projectId: String): Mono<Void> =
        rs.route("projects.delete").data(projectId).send()

    fun generateQuotePdf(req: GenerateQuotePdfRequest): Mono<Void> =
        rs.route("projects.generateQuotePdf").data(req).send()

    fun updateFactoryStatus(req: UpdateFactoryStatusRequest): Mono<Void> =
        rs.route("projects.updateFactoryStatus").data(req).send()

    fun respondToBoqRequest(req: UpdateFactoryStatusRequest): Mono<Void> =
        rs.route("projects.respondToBoqRequest").data(req).send()

    fun getBoqPdf(projectId: String): Mono<ByteArray> =
        rs.route("projects.getBoqPdf").data(projectId).retrieveMono(ByteArray::class.java)

    fun getQuotePdfForFactory(projectId: String, factoryId: String): Mono<ByteArray> =
        rs.route("projects.getQuotePdfForFactory")
            .data(GetQuotePdfRequest(projectId, factoryId))
            .retrieveMono(ByteArray::class.java)

    fun hasAny(clientId: String): Mono<Boolean> =
        rs.route("projects.hasAny").data(clientId).retrieveMono(Boolean::class.java)
}
