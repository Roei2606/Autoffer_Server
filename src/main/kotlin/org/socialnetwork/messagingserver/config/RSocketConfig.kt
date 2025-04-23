package org.socialnetwork.messagingserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder

@Configuration
class RSocketConfig {

    @Bean
    fun rSocketMessageHandler(): RSocketMessageHandler {
        val handler = RSocketMessageHandler()
        handler.rSocketStrategies = rSocketStrategies()
        return handler
    }

    @Bean
    fun rSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
            .encoders { it.add(Jackson2JsonEncoder()) }
            .decoders { it.add(Jackson2JsonDecoder()) }
            .build()
    }
}
