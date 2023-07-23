package com.example.playground.backpressure

import org.slf4j.LoggerFactory
import reactor.core.publisher.BufferOverflowStrategy
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration

class BufferDropLatestStrategy

fun main() {
    val logger = LoggerFactory.getLogger(BufferDropLatestStrategy::class.java)

    Flux.interval(Duration.ofMillis(300L))
        .doOnNext {
            logger.info("# emitted by original Flux: {$it}")
        }
        .onBackpressureBuffer(
            2,
            {
                logger.info("** Overflow & Dropped: {$it} **", )
            },
            BufferOverflowStrategy.DROP_LATEST,
        )
        .doOnNext {
            logger.info("[ # emitted by Buffer: {$it} ]")
        }
        .publishOn(Schedulers.parallel(), false, 1)
        .subscribe(
            {
                try {
                    Thread.sleep(1000L)
                } catch (e: InterruptedException) {
                    logger.error("# interruptedException: {$e}")
                }
                logger.info("# onNext: {$it}")
            },
            {
                logger.error("# onError $it")
            }
        )

    Thread.sleep(2500L)
}