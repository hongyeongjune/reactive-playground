package com.example.playground.sinks

import org.slf4j.LoggerFactory
import reactor.core.publisher.Sinks

class SinkManyReplayOperator

fun main() {
    val logger = LoggerFactory.getLogger(SinkManyReplayOperator::class.java)

    val replaySink = Sinks.many().replay().limit<Int>(2)
    val fluxView = replaySink.asFlux()

    replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
    replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)
    replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe1: {$it}")
    }

    replaySink.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe2: {$it}")
    }
}