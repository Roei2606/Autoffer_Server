package org.autoffer.services

import org.autoffer.models.ImageMeasurementRequest
import org.autoffer.models.MeasurementResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class WindowMeasurementService(
    @Value("\${measurement.python.base-url:http://localhost:8001}")
    private val pythonBaseUrl: String,

    @Value("\${measurement.python.measure-path:/measure}")
    private val measurePath: String,

    @Value("\${measurement.python.read-timeout-ms:90000}")
    private val readTimeoutMs: Long
) {
    private val webClient = WebClient.builder()
        .baseUrl(pythonBaseUrl)
        .build()

    fun measureWindow(request: ImageMeasurementRequest): Mono<MeasurementResult> {
        println("📏 Starting window measurement for file: ${request.fileName}")

        return webClient.post()
            .uri(measurePath)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(createMultipartBody(request).build()))
            .retrieve()
            .bodyToMono(MeasurementResult::class.java)
            .timeout(Duration.ofMillis(readTimeoutMs))
            .doOnSuccess { result ->
                println("✅ Measurement successful - Width: ${result.width}, Height: ${result.height}")
            }
            .doOnError { error ->
                when (error) {
                    is WebClientResponseException -> {
                        println("❌ Measurement failed with status ${error.statusCode}: ${error.responseBodyAsString}")
                    }
                    else -> {
                        println("❌ Measurement failed: ${error.message}")
                    }
                }
            }
    }

    private fun createMultipartBody(request: ImageMeasurementRequest): MultipartBodyBuilder {
        val builder = MultipartBodyBuilder()

        val resource = object : ByteArrayResource(request.imageData) {
            override fun getFilename(): String = request.fileName
        }

        builder.part("file", resource)
            .contentType(MediaType.parseMediaType(request.contentType))

        return builder
    }
}
