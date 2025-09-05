package org.socialnetwork.messagingserver.controllers

import org.bson.types.Binary
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import org.socialnetwork.messagingserver.repositories.QuoteRepository

@RestController
class QuoteDownloadController(
    private val quoteRepository: QuoteRepository
) {
    @GetMapping("/api/quotes/{id}/pdf")
    fun downloadPdf(@PathVariable id: String): Mono<ResponseEntity<ByteArray>> =
        quoteRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Quote not found")))
            .map { q ->
                val bytes = toBytes(q.quotePdf)
                val fname = "quote-${q.projectId}-${q.factoryId}.pdf"
                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$fname\"")
                    .contentLength(bytes.size.toLong())
                    .body(bytes)
            }

    /**
     * תומך בכמה מבנים אפשריים של השדה quotePdf:
     * - ByteArray
     * - List<Int> / List<Number>
     * - org.bson.types.Binary
     */
    @Suppress("UNCHECKED_CAST")
    private fun toBytes(src: Any?): ByteArray = when (src) {
        null -> ByteArray(0)
        is ByteArray -> src
        is Binary -> src.data
        is List<*> -> {
            val list = src as List<*>
            ByteArray(list.size) { i -> (list[i] as Number).toByte() }
        }
        else -> throw IllegalStateException("Unsupported quotePdf type: ${src::class.java.name}")
    }
}
