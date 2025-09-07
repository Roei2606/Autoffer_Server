package org.autoffer.integrations.docai

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import io.netty.channel.ChannelOption
import org.autoffer.config.DocAiPythonProperties
import reactor.util.retry.Retry
import java.time.Duration

@Component
class DocAiClient(
    private val props: DocAiPythonProperties
) {

    private val webClient: WebClient by lazy {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.connectTimeoutMs)
            .responseTimeout(Duration.ofMillis(props.readTimeoutMs.toLong()))

        val builder = WebClient.builder()
            .baseUrl(props.baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { it.defaultCodecs().maxInMemorySize(32 * 1024 * 1024) }

        if (!props.authToken.isNullOrBlank()) {
            builder.defaultHeader(props.authHeaderName, props.authToken!!)
        }
        builder.build()
    }
    fun processPdf(pdfBytes: ByteArray, filename: String = "boq.pdf"): org.autoffer.integrations.docai.DocAiResponse {
        val resource = object : ByteArrayResource(pdfBytes) {
            override fun getFilename(): String = filename
        }

        val multipart = MultipartBodyBuilder().apply {
            part("file", resource).filename(filename).contentType(MediaType.APPLICATION_PDF)
        }.build()

        return webClient.post()
            .uri(props.parsePath)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipart))
            .retrieve()
            .bodyToMono<org.autoffer.integrations.docai.DocAiResponse>()
            .retryWhen(
                Retry.backoff(2, Duration.ofMillis(300))
                    .filter { it is org.springframework.web.reactive.function.client.WebClientRequestException }
            )
            .block(Duration.ofMillis(props.readTimeoutMs.toLong() + 5_000))
            ?: throw RuntimeException("Empty response from DocAI")
    }
}
