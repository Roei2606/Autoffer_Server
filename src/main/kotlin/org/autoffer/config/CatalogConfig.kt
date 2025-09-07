package org.autoffer.config

import org.autoffer.services.catalog.CatalogProvider
import org.autoffer.services.catalog.InMemoryCatalogProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CatalogConfig {
    @Bean fun catalogProvider(): CatalogProvider = InMemoryCatalogProvider()
}
