package org.autoffer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AutofferServerApplication

fun main(args: Array<String>) {
	println("✅ Starting Autoffer Server...")
	runApplication<AutofferServerApplication>(*args)

}
