// src/main/kotlin/org/socialnetwork/messagingserver/dev/DevSidecars.kt
package org.socialnetwork.messagingserver.dev

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Profile("dev")
@Component
class DevSidecars : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val projectRoot = File(System.getProperty("user.dir")) // root של messaging-server
        val docaiPort = System.getenv("DOCAI_PORT")?.toIntOrNull() ?: 5051
        val gatewayPort = System.getenv("GATEWAY_PORT")?.toIntOrNull() ?: 8090

        println("▶︎ Starting sidecars (DocAI:$docaiPort + gateway:$gatewayPort) ...")

        // מריץ את הסקריפט שמרים את שני השירותים
        ProcessBuilder(File(projectRoot, "scripts/start-dev.sh").absolutePath)
            .directory(projectRoot)
            .inheritIO()
            .start()

        waitForHttp("http://localhost:$docaiPort/docs", timeoutMs = 30_000)
        waitForHttp("http://localhost:$gatewayPort/actuator/health", timeoutMs = 60_000)
        println("✓ Sidecars are up (DocAI:$docaiPort + gateway:$gatewayPort)")
    }

    private fun waitForHttp(url: String, timeoutMs: Long) {
        val client = HttpClient.newHttpClient()
        val req = HttpRequest.newBuilder(URI.create(url)).GET().build()
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            try {
                val res = client.send(req, HttpResponse.BodyHandlers.discarding())
                if (res.statusCode() in 200..399) return
            } catch (_: Exception) { }
            Thread.sleep(500)
        }
        println("⚠️ Timeout waiting for $url")
    }


    private fun waitForAny(urls: List<String>, timeoutMs: Long) {
        val client = HttpClient.newHttpClient()
        val deadline = System.currentTimeMillis() + timeoutMs

        while (System.currentTimeMillis() < deadline) {
            for (url in urls) {
                try {
                    val req = HttpRequest.newBuilder(URI.create(url)).GET().build()
                    val res = client.send(req, HttpResponse.BodyHandlers.discarding())
                    if (res.statusCode() in 200..399) return
                } catch (_: Exception) { /* עדיין לא עלה */ }
            }
            Thread.sleep(500)
        }
        println("⚠️  Timeout waiting for any of:\n${urls.joinToString("\n")}")
    }
}
