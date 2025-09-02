package org.socialnetwork.messagingserver.services.autoquote

import org.socialnetwork.messagingserver.integrations.docai.DocAiClient
import org.socialnetwork.messagingserver.models.*
import org.socialnetwork.messagingserver.repositories.QuoteRepository
import org.socialnetwork.messagingserver.services.PricingService
import org.socialnetwork.messagingserver.services.boq.BoqNormalizationService
import org.socialnetwork.messagingserver.services.pdf.QuotePdfService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

@Service
class AutoQuoteService(
    private val docAiClient: DocAiClient,
    private val normalization: BoqNormalizationService,
    private val pricing: PricingService,
    private val quoteRepository: QuoteRepository
) {
    private val pdf = QuotePdfService() // פשוט ל-MVP; אפשר @Bean אם תרצה

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
            // 1) DocAI
            val doc = docAiClient.processPdf(req.pdfBytes, req.filename ?: "boq.pdf")
            // 2) Normalize
            val norm = normalization.normalize(doc, skipUnparseable = req.skipUnparseable ?: true)
            // 3) Pricing
            val factor = req.factor ?: 1.0
            val priced = pricing.price(norm.items, factor)
            Triple(norm, factor, priced.finalTotal)
        }.flatMap { (norm, factor, finalTotal) ->
            // 4) Build QuoteModel
            val pdfBytes = pdf.render(req.projectId, req.factoryId, norm.items, finalTotal)
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
            // 5) Save
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
}
