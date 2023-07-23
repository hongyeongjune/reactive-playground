package com.example.playground.backpressure

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration

class LatestStrategy

fun main() {
    val logger = LoggerFactory.getLogger(LatestStrategy::class.java)

    Flux.interval(Duration.ofMillis(1L))
        .onBackpressureLatest()
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