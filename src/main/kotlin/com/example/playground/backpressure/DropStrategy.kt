package com.example.playground.backpressure

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration

class DropStrategy

fun main() {
    val logger = LoggerFactory.getLogger(DropStrategy::class.java)

    Flux.interval(Duration.ofMillis(1L))
        .onBackpressureDrop {
            logger.info("# dropped: {$it}")
        }
        .publishOn(Schedulers.parallel())
        .subscribe(
            {
                try {
                    Thread.sleep(5L)
                } catch (e: InterruptedException) {
                    logger.error("# interruptedException: {$e}")
                }
                logger.info("# onNext: {$it}")
            },
            {
                logger.error("# onError $it")
            }
        )

    Thread.sleep(2000L)
}