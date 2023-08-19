package com.example.playground.operators

import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink


class CreatorOperator

fun main() {
    val logger = LoggerFactory.getLogger(CreatorOperator::class.java)

    var size = 0
    var count = -1
    val sources = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    Flux.create(
        { sink: FluxSink<Int> ->
            sink.onRequest { n: Long ->
                Thread.sleep(1000L)
                for (i in 0 until n) {
                    if (count >= 9) {
                        sink.complete()
                    } else {
                        count++
                        sink.next(sources[count])
                    }
                }
            }
            sink.onDispose { logger.info("# clean up") }
        },
        FluxSink.OverflowStrategy.DROP
    ).subscribe(object : BaseSubscriber<Int>() {
        override fun hookOnSubscribe(subscription: Subscription) {
            request(2)
        }

        override fun hookOnNext(value: Int) {
            size++
            logger.info("# onNext: $value")
            if (size == 2) {
                request(2)
                size = 0
            }
        }

        override fun hookOnComplete() {
            logger.info("# onComplete")
        }
    })
}
