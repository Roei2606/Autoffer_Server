package org.autoffer.dev

import org.springframework.beans.factory.annotation.Value
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
class DevSidecars(
    @Value("\${docai.python.base-url:http://127.0.0.1:8000}")
    private val docAiBaseUrl: String,
    @Value("\${app.gateway.port:8090}")
    private val defaultGatewayPort: Int
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val projectRoot = File(System.getProperty("user.dir"))
        val base = URI.create(docAiBaseUrl)
        val docaiPort = when {
            base.port != -1 -> base.port
            base.scheme.equals("https", ignoreCase = true) -> 443
            else -> 80
        }
        val gatewayPort = System.getenv("GATEWAY_PORT")?.toIntOrNull() ?: defaultGatewayPort

        val pb = ProcessBuilder(File(projectRoot, "scripts/start-dev.sh").absolutePath)
            .directory(projectRoot)
            .inheritIO()

        val env = pb.environment()
        env.putIfAbsent("DOCAI_PORT", docaiPort.toString())
        env.putIfAbsent("GATEWAY_PORT", gatewayPort.toString())
        pb.start()

        waitForHttp("${base.scheme}://${base.host}:${docaiPort}/docs", timeoutMs = 100_000)
        waitForHttp("http://localhost:$gatewayPort/actuator/health", timeoutMs = 100_000)

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
}
