package com.example.playground.operators

import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux


class DoOnOperator

fun main() {
    val logger = LoggerFactory.getLogger(DoOnOperator::class.java)

    Flux.range(1, 5)
        .doFinally { logger.info("# doFinally 1: $it") }
        .doFinally { logger.info("# doFinally 2: $it") }
        .doOnNext { logger.info("# range > doOnNext(): $it") }
        .doOnRequest { logger.info("# doOnRequest: $it") }
        .doOnSubscribe { logger.info("# doOnSubscribe 1") }
        .doFirst { logger.info("# doFirst()") }
        .filter { it % 2 == 1 }
        .doOnNext { logger.info("# filter > doOnNext(): $it") }
        .doOnComplete { logger.info("# doOnComplete()") }
        .subscribe(object : BaseSubscriber<Int>() {
            override fun hookOnSubscribe(subscription: Subscription) {
                request(1)
            }

            override fun hookOnNext(value: Int) {
                logger.info("# hookOnNext: $value")
                request(1)
            }
        })

    Thread.sleep(5000L)
}