package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

class CollectListOperator

fun main() {
    val logger = LoggerFactory.getLogger(CollectListOperator::class.java)

    Flux.just("naver", "naver webtoon", "line")
        .collectList()
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(2000L)
}