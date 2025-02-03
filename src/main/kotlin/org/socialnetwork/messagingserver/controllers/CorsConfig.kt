package org.socialnetwork.messagingserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurer(): WebFluxConfigurer {
        return object : WebFluxConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")  // Allow all origins
                    .allowedMethods("*")  // Allow all HTTP methods
                    .allowedHeaders("*")  // Allow all headers
            }
        }
    }
}
