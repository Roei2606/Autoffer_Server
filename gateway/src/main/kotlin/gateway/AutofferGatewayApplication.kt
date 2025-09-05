package gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["gateway"])
class AutofferGatewayApplication

fun main(args: Array<String>) {
    runApplication<AutofferGatewayApplication>(*args)
}
