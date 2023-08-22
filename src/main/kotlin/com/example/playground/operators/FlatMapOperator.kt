package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

class FlatMapOperator

fun main() {
    val logger = LoggerFactory.getLogger(FlatMapOperator::class.java)

    Flux.range(2, 8)
        .flatMap {
            Flux.range(1, 9)
                .publishOn(Schedulers.parallel())
                .map { num ->
                    "$it * $num = ${it * num}"
                }
        }
        .subscribe {
            logger.info(it)
        }

    Thread.sleep(100L)
}