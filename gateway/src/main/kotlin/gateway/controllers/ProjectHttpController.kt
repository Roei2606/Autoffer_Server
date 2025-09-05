package gateway.controllers

import gateway.services.ProjectGateway
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// DTOs/מודלים ממודול autoffer-dtos
import org.socialnetwork.messagingserver.models.*

@RestController
@RequestMapping("/api")
class ProjectHttpController(
    private val gateway: ProjectGateway
) {

    // RSocket: "project.create"
    @PostMapping("/projects", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createProject(@RequestBody req: CreateProjectRequest): Mono<ProjectDTO> =
        gateway.createProject(req)

    // RSocket: "projects.getById"
    @GetMapping("/projects/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getById(@PathVariable id: String): Mono<ProjectDTO> =
        gateway.getById(id)

    // RSocket: "projects.sendToFactories"
    @PostMapping("/projects/send-boq", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendToFactories(@RequestBody req: SendBOQRequest): Mono<Void> =
        gateway.sendToFactories(req)

    // RSocket: "projects.getAllForUser"
    @PostMapping("/projects/user", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllForUser(@RequestBody req: UserProjectRequest): Flux<ProjectDTO> =
        gateway.getAllForUser(req)

    // RSocket: "projects.delete"
    @DeleteMapping("/projects/{id}")
    fun delete(@PathVariable id: String): Mono<Void> =
        gateway.delete(id)

    // RSocket: "projects.generateQuotePdf"
    @PostMapping("/projects/generate-quote-pdf", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun generateQuotePdf(@RequestBody req: GenerateQuotePdfRequest): Mono<Void> =
        gateway.generateQuotePdf(req)

    // RSocket: "projects.updateFactoryStatus"
    @PostMapping("/projects/update-factory-status", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateFactoryStatus(@RequestBody req: UpdateFactoryStatusRequest): Mono<Void> =
        gateway.updateFactoryStatus(req)

    // RSocket: "projects.respondToBoqRequest"
    @PostMapping("/projects/respond-boq", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun respondToBoqRequest(@RequestBody req: UpdateFactoryStatusRequest): Mono<Void> =
        gateway.respondToBoqRequest(req)

    // RSocket: "projects.getBoqPdf"
    @GetMapping("/projects/{projectId}/boq.pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getBoqPdf(@PathVariable projectId: String): Mono<ResponseEntity<ByteArray>> =
        gateway.getBoqPdf(projectId)
            .map { bytes ->
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=boq_$projectId.pdf")
                    .body(bytes)
            }

    // RSocket: "projects.getQuotePdfForFactory"
    @GetMapping("/projects/{projectId}/factories/{factoryId}/quote.pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getQuotePdfForFactory(
        @PathVariable projectId: String,
        @PathVariable factoryId: String
    ): Mono<ResponseEntity<ByteArray>> =
        gateway.getQuotePdfForFactory(projectId, factoryId)
            .map { bytes ->
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=quote_${projectId}_$factoryId.pdf")
                    .body(bytes)
            }

    // RSocket: "projects.hasAny"
    @GetMapping("/projects/has-any")
    fun hasAny(@RequestParam clientId: String): Mono<Boolean> =
        gateway.hasAny(clientId)
}
