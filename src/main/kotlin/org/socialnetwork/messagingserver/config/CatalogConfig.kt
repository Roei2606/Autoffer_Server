package org.socialnetwork.messagingserver.config

import org.socialnetwork.messagingserver.services.catalog.CatalogProvider
import org.socialnetwork.messagingserver.services.catalog.InMemoryCatalogProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CatalogConfig {
    @Bean fun catalogProvider(): CatalogProvider = InMemoryCatalogProvider()
}
