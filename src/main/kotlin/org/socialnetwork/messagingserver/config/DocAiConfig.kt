package org.socialnetwork.messagingserver.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DocAiPythonProperties::class)
class DocAiConfig
