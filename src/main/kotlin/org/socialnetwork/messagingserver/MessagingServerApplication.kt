package org.socialnetwork.messagingserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MessagingServerApplication

fun main(args: Array<String>) {
	runApplication<MessagingServerApplication>(*args)
}
