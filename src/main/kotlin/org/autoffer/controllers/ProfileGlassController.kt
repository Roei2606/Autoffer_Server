package org.autoffer.controllers


import org.autoffer.models.GlassModel
import org.autoffer.models.ImageMeasurementRequest
import org.autoffer.models.MeasurementResult
import org.autoffer.services.AlumProfileService
import org.autoffer.models.AlumProfileModel
import org.autoffer.services.WindowMeasurementService
import org.autoffer.services.GlassService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class ProfileGlassController(
    private val alumProfileService: AlumProfileService,
    private val glassService: GlassService,
    private val windowMeasurementService : WindowMeasurementService
) {

    @MessageMapping("profiles.matchBySize")
    fun matchProfilesBySize(size: org.autoffer.models.SizeRequest): Flux<AlumProfileModel> {
        println("📩 Received request to match profiles with width=${size.width}, height=${size.height}")
        return alumProfileService.getMatchingProfiles(size.width, size.height)
            .doOnNext { println("✅ Matching profile found: ${it.profileNumber}") }
            .doOnComplete { println("✅ Done matching profiles") }
    }

    @MessageMapping("glasses.getByProfile")
    fun getGlassesByProfile(profileNumber: String): Flux<GlassModel> {
        println("📩 Received request for glasses of profile: \"$profileNumber\"")

        val cleanProfileNumber = profileNumber.trim().replace("\"", "")
        println("🔍 Looking for glasses matching profileNumber='$cleanProfileNumber'")

        return glassService.getGlassesForProfile(cleanProfileNumber)
            .doOnNext {
                println("👓 Found glass in DB: ${it.type}, supports: ${it.supportedProfiles}")
            }
            .doOnComplete {
                println("✅ Done retrieving glasses")
            }
    }

    @MessageMapping("measurement.processImage")
    fun measureImageDimensions(request: ImageMeasurementRequest): Mono<MeasurementResult> {
        println("📷 Received measurement-only request for file: ${request.fileName}")

        return windowMeasurementService.measureWindow(request)
            .doOnNext { result ->
                println("📏 Measurement result - Width: ${result.width}mm, Height: ${result.height}mm")
            }
            .onErrorResume { error: Throwable ->
                println("❌ Measurement failed: ${error.message}")
                Mono.empty<MeasurementResult>()
            }
    }

    @MessageMapping("projects.autoItem.find")
    fun findProfilesFromImage(request: ImageMeasurementRequest): Flux<AlumProfileModel> {
        println("📷 Received image measurement request for file: ${request.fileName}")

        return windowMeasurementService.measureWindow(request)
            .flatMapMany { measurement ->
                alumProfileService.getMatchingProfiles(measurement.width, measurement.height)
            }
            .doOnNext { profile ->
                println("✅ Found matching profile: ${profile.profileNumber}")
            }
            .onErrorResume { error: Throwable ->
                println("❌ Error processing image: ${error.message}")
                Flux.empty<AlumProfileModel>()
            }
    }



}
