package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ErrorOperator

fun main() {
    val logger = LoggerFactory.getLogger(ErrorOperator::class.java)

    Flux.range(1, 5)
        .flatMap {
            if (it * 2 % 3 == 0) {
                Flux.error(
                    IllegalArgumentException("Not allowed multiple of 3")
                )
            } else {
                Mono.just(it * 2)
            }
        }
        .subscribe(
            { data -> logger.info("# onNext: $data") },
            { error -> logger.error("# onError: $error") }
        )
}

