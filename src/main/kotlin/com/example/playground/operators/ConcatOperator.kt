package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

class ConcatOperator

fun main() {
    val logger = LoggerFactory.getLogger(ConcatOperator::class.java)

    Flux.concat(Flux.just(1, 2, 3), Flux.just(4, 5))
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(100L)
}