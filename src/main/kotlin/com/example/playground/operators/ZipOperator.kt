package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import java.time.Duration

class ZipOperator

fun main() {
    val logger = LoggerFactory.getLogger(ZipOperator::class.java)

    Flux.zip(
        Flux.just(1, 2, 3, 4).delayElements(Duration.ofMillis(300L)),
        Flux.just(5, 6, 7).delayElements(Duration.ofMillis(500L)),
    )
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(2000L)
}