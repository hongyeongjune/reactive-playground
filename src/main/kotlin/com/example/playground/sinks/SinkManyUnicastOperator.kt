package com.example.playground.sinks

import org.slf4j.LoggerFactory
import reactor.core.publisher.Sinks

class SinkManyUnicastOperator

fun main() {
    val logger = LoggerFactory.getLogger(SinkManyUnicastOperator::class.java)

    val unicastSink = Sinks.many().unicast().onBackpressureBuffer<Int>()
    val fluxView = unicastSink.asFlux()

    unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
    unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe1: {$it}")
    }

    unicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe2: {$it}")
    }
}