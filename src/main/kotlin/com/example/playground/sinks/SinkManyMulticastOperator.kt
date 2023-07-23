package com.example.playground.sinks

import org.slf4j.LoggerFactory
import reactor.core.publisher.Sinks

class SinkManyMulticastOperator

fun main() {
    val logger = LoggerFactory.getLogger(SinkManyMulticastOperator::class.java)

    val multicastSink = Sinks.many().multicast().onBackpressureBuffer<Int>()
    val fluxView = multicastSink.asFlux()

    multicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
    multicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe1: {$it}")
    }

    fluxView.subscribe {
        logger.info("# subscribe2: {$it}")
    }

    multicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)

}