package gateway.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.transport.netty.client.WebsocketClientTransport
import io.rsocket.transport.netty.client.TcpClientTransport
import org.springframework.util.MimeTypeUtils
import java.net.URI
import java.time.Duration

@Configuration
class RSocketClientConfig(
    @Value("\${autoffer.rsocket.transport}") private val transport: String,
    @Value("\${autoffer.rsocket.host}") private val host: String,
    @Value("\${autoffer.rsocket.port}") private val port: Int,
    @Value("\${autoffer.rsocket.path:/rsocket}") private val path: String,
    private val objectMapper: ObjectMapper
) {

    // משתמשים ב-ObjectMapper של Spring (עם kotlin-module) לקודקים של RSocket
    @Bean
    fun rSocketStrategies(): RSocketStrategies =
        RSocketStrategies.builder()
            .decoder(Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON))
            .encoder(Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
            .build()

    @Bean
    fun rSocketRequester(
        builder: RSocketRequester.Builder,
        strategies: RSocketStrategies
    ): RSocketRequester {
        val transportClient =
            if (transport.equals("ws", ignoreCase = true)) {
                WebsocketClientTransport.create(URI.create("ws://$host:$port$path"))
            } else {
                TcpClientTransport.create(host, port)
            }

        return builder
            .rsocketStrategies(strategies) // ← חשוב: לא להשתמש ב-strategies ברירת־מחדל
            .rsocketConnector {
                it.payloadDecoder(PayloadDecoder.ZERO_COPY)
                    .keepAlive(Duration.ofSeconds(20), Duration.ofSeconds(90))
            }
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .connect(transportClient)
            .block()!!
    }
}
