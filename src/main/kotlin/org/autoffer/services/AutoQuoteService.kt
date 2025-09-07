package org.autoffer.services.autoquoteservice


import FactoryUserModel
import org.autoffer.integrations.docai.DocAiClient
import org.autoffer.models.*
import org.autoffer.repositories.QuoteRepository
import org.autoffer.services.boq.BoqNormalizationService
import org.autoffer.utils.PdfGenerator
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class AutoQuoteService(
    private val docAiClient: DocAiClient,
    private val normalization: BoqNormalizationService,
    private val pricing: org.autoffer.services.PricingService,
    private val quoteRepository: QuoteRepository,
    private val pdfGenerator: PdfGenerator
) {
    fun preview(req: AutoQuoteFromPdfRequest): AutoQuotePreviewResponse {
        val t0 = System.currentTimeMillis()
        val doc = docAiClient.processPdf(req.pdfBytes, req.filename ?: "boq.pdf")
        val t1 = System.currentTimeMillis()

        val norm = normalization.normalize(doc, skipUnparseable = req.skipUnparseable ?: true)
        val t2 = System.currentTimeMillis()

        val factor = req.factor ?: 1.0
        val priced = pricing.price(norm.items, factor)
        val t3 = System.currentTimeMillis()

        return AutoQuotePreviewResponse(
            projectId = req.projectId,
            factoryId = req.factoryId,
            items = norm.items,
            issues = norm.issues,
            finalPrice = priced.finalTotal,
            factorApplied = priced.factorApplied,
            timingMs = PreviewTiming(
                docaiMs = t1 - t0,
                normalizeMs = t2 - t1,
                pricingMs = t3 - t2,
                totalMs = t3 - t0
            )
        )
    }
    fun create(req: AutoQuoteFromPdfRequest): Mono<AutoQuoteCreateResponse> {
        return Mono.fromCallable {
            val doc = docAiClient.processPdf(req.pdfBytes, req.filename ?: "boq.pdf")
            val norm = normalization.normalize(doc, skipUnparseable = req.skipUnparseable ?: true)
            val factor = req.factor ?: req.factory?.factor ?: 1.0
            val priced = pricing.price(norm.items, factor)
            Triple(norm, factor, priced.finalTotal)
        }.flatMap { (norm, factor, finalTotal) ->
            val projectForPdf = ProjectModel(
                id = req.projectId,
                clientId = req.projectId + "-client",
                factoryIds = listOf(req.factoryId),
                projectAddress = req.projectAddress ?: "-",
                items = norm.items
            )

            val c: PdfClientDTO? = req.client
            val clientForPdf = UserModel(
                id = projectForPdf.clientId,
                firstName = c?.firstName ?: "Customer",
                lastName  = c?.lastName ?: "",
                email     = c?.email ?: "-",
                password  = "__PDF__",
                phoneNumber = c?.phone ?: "-",
                address     = c?.address ?: "-",
                profileType = UserType.PRIVATE_CUSTOMER
            )

            val f: PdfFactoryUserDTO? = req.factory
            val factoryUserForPdf = FactoryUserModel(
                businessId = f?.businessId ?: "",
                factor = factor,
                factoryName = f?.factoryName ?: (req.factoryId),
                id = req.factoryId,
                firstName = f?.firstName ?: "Factory",
                lastName  = f?.lastName ?: "",
                email     = f?.email ?: "-",
                password  = "__PDF__",
                phoneNumber = f?.phone ?: "-",
                address     = f?.address ?: "-",
                profileType = UserType.FACTORY
            )
            val logoBytes = loadFactoryLogo(req.factoryId)
            val pdfBytes = pdfGenerator.generateQuotePdf(
                projectForPdf, clientForPdf, factoryUserForPdf, logoBytes
            )
            val quote = QuoteModel(
                factoryId = req.factoryId,
                projectId = req.projectId,
                pricedItems = norm.items,
                factor = factor,
                finalPrice = finalTotal,
                quotePdf = pdfBytes.toList(),
                status = "RECEIVED",
                createdAt = Instant.now()
            )
            quoteRepository.save(quote).map {
                AutoQuoteCreateResponse(
                    projectId = it.projectId,
                    factoryId = it.factoryId,
                    quoteId = it.id,
                    itemsCount = it.pricedItems.size,
                    issuesCount = norm.issues.size,
                    finalPrice = it.finalPrice
                )
            }
        }
    }
    private fun loadFactoryLogo(factoryId: String?): ByteArray {
        val specific = factoryId?.let { "static/logos/${it}.jpg" }
        specific?.let {
            ClassPathResource(it).let { res ->
                if (res.exists()) return res.inputStream.use { it.readBytes() }
            }
        }
        val fallback = ClassPathResource("static/logos/default-factory.jpg")
        return if (fallback.exists()) fallback.inputStream.use { it.readBytes() } else ByteArray(0)
    }
}
