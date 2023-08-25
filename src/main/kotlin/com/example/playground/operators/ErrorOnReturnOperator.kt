package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

class ErrorOnReturnOperator

fun main() {
    val logger = LoggerFactory.getLogger(ErrorOnReturnOperator::class.java)

    Flux.just("naver", "naver webtoon", null, "line")
        .map { it!!.uppercase() }
        .onErrorReturn(NullPointerException::class.java, "no name")
        .subscribe {
            logger.info(it)
        }

    Thread.sleep(200L)
}

