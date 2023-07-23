package com.example.playground.backpressure

import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux

class RequestStrategy

fun main() {
    val logger = LoggerFactory.getLogger(RequestStrategy::class.java)

    Flux.range(1, 5)
        .doOnRequest {
            logger.info("# doOnRequest: {$it}")
        }
        .subscribe(object : BaseSubscriber<Int>() {
            override fun hookOnSubscribe(subscription: Subscription) {
                request(1)
            }

            override fun hookOnNext(value: Int) {
                Thread.sleep(2000L)
                logger.info("# hookOnNext: {$value}")
                request(1)
            }
        })
}