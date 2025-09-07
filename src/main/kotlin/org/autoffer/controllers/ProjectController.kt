package org.autoffer.controllers

import org.autoffer.integrations.docai.DocAiClient
import org.autoffer.models.*
import org.autoffer.services.ProjectService
import org.autoffer.services.autoquoteservice.AutoQuoteService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Controller
class ProjectController(
    private val projectService: ProjectService,
    private val docAiClient: DocAiClient,                 // קיים
    private val autoQuoteService: AutoQuoteService
) {
    @MessageMapping("project.create")
    fun createProject(@Payload request: CreateProjectRequest): Mono<ProjectDTO> =
        projectService.createProjectFromDto(request)

    @MessageMapping("projects.getById")
    fun getProjectById(@Payload projectId: String): Mono<ProjectDTO> =
        projectService.getProjectById(projectId)

    @MessageMapping("projects.sendToFactories")
    fun sendToFactories(@Payload request: org.autoffer.models.SendBOQRequest): Mono<Void> =
        projectService.sendBOQToFactories(request)

    @MessageMapping("projects.getAllForUser")
    fun getProjectsForUser(@Payload request: UserProjectRequest): Flux<ProjectDTO> {
        return when (request.profileType) {
            UserType.FACTORY -> {
                println("🔍 Loading projects for FACTORY=${request.userId}")
                projectService.getApprovedProjectsByFactoryId(request.userId)
            }
            else -> {
                println("🔍 Loading projects for CLIENT=${request.userId}")
                projectService.getProjectsByClientId(request.userId)
            }
        }
    }
    @MessageMapping("projects.delete")
    fun deleteProject(@Payload projectId: String): Mono<Void> =
        projectService.deleteProjectById(projectId)

    @MessageMapping("projects.generateQuotePdf")
    fun generateQuotePdf(@Payload request: GenerateQuotePdfRequest): Mono<Void> =
        projectService.generateAndSaveQuotePdf(
            request.projectId,
            request.client,
            request.factoryUser,
            request.factoryLogoBytes
        )
    @MessageMapping("projects.updateFactoryStatus")
    fun updateFactoryStatus(@Payload request: UpdateFactoryStatusRequest): Mono<Void> =
        projectService.updateFactoryStatus(request)

    @MessageMapping("projects.respondToBoqRequest")
    fun respondToBoqRequest(@Payload request: UpdateFactoryStatusRequest): Mono<Void> {
        val isApproved = request.newStatus == QuoteStatus.ACCEPTED
        return projectService.respondToBOQRequest(request, approved = isApproved)
    }

    @MessageMapping("projects.getBoqPdf")
    fun getBoqPdf(@Payload projectId: String): Mono<ByteArray> =
        projectService.getBoqPdf(projectId)

    @MessageMapping("projects.getQuotePdfForFactory")
    fun getQuotePdf(@Payload request: GetQuotePdfRequest): Mono<ByteArray> =
        projectService.getQuotePdfForFactory(request.projectId, request.factoryId)

    @MessageMapping("projects.hasAny")
    fun hasAnyProjects(@Payload clientId: String): Mono<Boolean> =
        projectService.hasProjects(clientId)

    // parse (אופציונלי לדיבוג)
    @MessageMapping("projects.autoQuote.parse")
    fun parseBoqPdf(@Payload req: AutoQuoteFromPdfRequest): Mono<org.autoffer.integrations.docai.DocAiResponse> =
        Mono.fromCallable<org.autoffer.integrations.docai.DocAiResponse> {                      // 👈 הוספת טיפוס גנרי
            val filename = req.filename ?: "boq.pdf"
            docAiClient.processPdf(req.pdfBytes, filename)
        }.subscribeOn(Schedulers.boundedElastic())

    // preview (נרמול + תמחור)
    @MessageMapping("projects.autoQuote.preview")
    fun autoQuotePreview(@Payload req: AutoQuoteFromPdfRequest): Mono<AutoQuotePreviewResponse> =
        Mono.fromCallable<AutoQuotePreviewResponse> {
            autoQuoteService.preview(req)   // ← כעת יימצא
        }.subscribeOn(Schedulers.boundedElastic())

    @MessageMapping("projects.autoQuote.create")
    fun autoQuoteCreate(@Payload req: AutoQuoteFromPdfRequest): Mono<AutoQuoteCreateResponse> =
        autoQuoteService.create(req)
            .subscribeOn(Schedulers.boundedElastic())

}
