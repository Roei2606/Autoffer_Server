// src/main/kotlin/org/socialnetwork/messagingserver/services/autoquote/AutoQuoteService.kt
package org.socialnetwork.messagingserver.services.autoquote

import FactoryUserModel
import org.socialnetwork.messagingserver.integrations.docai.DocAiClient
import org.socialnetwork.messagingserver.models.*
import org.socialnetwork.messagingserver.repositories.QuoteRepository
import org.socialnetwork.messagingserver.services.PricingService
import org.socialnetwork.messagingserver.services.boq.BoqNormalizationService
import org.socialnetwork.messagingserver.utils.PdfGenerator   // 👈 להשתמש במחלקה שלך
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class AutoQuoteService(
    private val docAiClient: DocAiClient,
    private val normalization: BoqNormalizationService,
    private val pricing: PricingService,
    private val quoteRepository: QuoteRepository,
    private val pdfGenerator: PdfGenerator       // 👈 הזרקה של PdfGenerator
) {
    // הסר את QuotePdfService הישן אם קיים

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

            // --- Build models for PdfGenerator (בלי לשנות את החתימה) ---
            val projectForPdf = ProjectModel(
                id = req.projectId,
                clientId = req.projectId + "-client",              // מזהה זמני
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
                password  = "__PDF__",                 // ערך דמה – נדרש ע"י המודל
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
                password  = "__PDF__",                 // ערך דמה – נדרש ע"י המודל
                phoneNumber = f?.phone ?: "-",
                address     = f?.address ?: "-",
                profileType = UserType.FACTORY
            )

            val logoBytes = loadFactoryLogo(req.factoryId)
            val pdfBytes = pdfGenerator.generateQuotePdf(
                projectForPdf, clientForPdf, factoryUserForPdf, logoBytes
            )
            // ------------------------------------------------------------

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
        // 1) נסה לוגו לפי מפעל (למשל resources/static/logos/<factoryId>.jpg)
        val specific = factoryId?.let { "static/logos/${it}.jpg" }
        specific?.let {
            ClassPathResource(it).let { res ->
                if (res.exists()) return res.inputStream.use { it.readBytes() }
            }
        }
        // 2) ברירת מחדל (למשל resources/static/logos/default-factory.jpg)
        val fallback = ClassPathResource("static/logos/default-factory.jpg")
        return if (fallback.exists()) fallback.inputStream.use { it.readBytes() } else ByteArray(0)
    }
}
