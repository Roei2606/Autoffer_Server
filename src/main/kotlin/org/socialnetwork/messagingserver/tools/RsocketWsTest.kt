package org.socialnetwork.messagingserver.tools

import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.util.MimeTypeUtils
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

// נשתמש במחלקת הבקשה האמיתית שלך מהשרת:
import org.socialnetwork.messagingserver.models.AutoQuoteFromPdfRequest

private const val PDF_PATH = "/Users/roeihakmon/Desktop/Autoffer/exBOQ.pdf"

fun main() {
    val mapper = jacksonObjectMapper() // כולל Kotlin module אם הוא על ה־classpath

    val strategies = RSocketStrategies.builder()
        .encoders { it.add(Jackson2JsonEncoder(mapper)) }
        .decoders { it.add(Jackson2JsonDecoder(mapper)) }
        .build()

    val requester = RSocketRequester.builder()
        .rsocketStrategies(strategies)
        .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
        .metadataMimeType(MimeTypeUtils.parseMimeType("message/x.rsocket.composite-metadata.v0"))
        .tcp("127.0.0.1", 7071)



    val pdfBytes = Files.readAllBytes(Paths.get(PDF_PATH)) // ByteArray
    val payload = AutoQuoteFromPdfRequest(
        projectId = "proj-123",
        factoryId = "fact-456",
        factor = 1.0,
        filename = "exBOQ.pdf",
        skipUnparseable = true,
        includeRaw = false,
        pdfBytes = pdfBytes                         // 👈 שולחים ByteArray אמיתי; Jackson יבייס64 אוטומטית
    )

    fun call(route: String): Mono<String> =
        requester.route(route).data(payload).retrieveMono(String::class.java)

    println("=== projects.autoQuote.parse ===")
    println(call("projects.autoQuote.parse").block())

    println("\n=== projects.autoQuote.preview ===")
    println(call("projects.autoQuote.preview").block())

    println("\n=== projects.autoQuote.create ===")
    println(call("projects.autoQuote.create").block())
}
