package gateway.shared

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestControllerAdvice
class GlobalErrorHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun badReq(e: IllegalArgumentException): Mono<Void> =
        Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, e.message))
    @ExceptionHandler(IllegalStateException::class)
    fun conflict(e: IllegalStateException): Mono<Void> =
        Mono.error(ResponseStatusException(HttpStatus.CONFLICT, e.message))
    @ExceptionHandler(NoSuchElementException::class)
    fun notFound(e: NoSuchElementException): Mono<Void> =
        Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, e.message))
}
