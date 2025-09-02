package org.socialnetwork.messagingserver.dev

import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File

@Profile("dev")
@Component
class DevSidecarsShutdown {

    @PreDestroy
    fun stop() {
        val projectRoot = File(System.getProperty("user.dir"))
        println("⏹ Stopping sidecars ...")
        ProcessBuilder(File(projectRoot, "scripts/stop-dev.sh").absolutePath)
            .directory(projectRoot)
            .inheritIO()
            .start()
            .waitFor()
        println("✓ Sidecars stopped")
    }
}
