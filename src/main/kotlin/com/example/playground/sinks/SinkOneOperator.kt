package com.example.playground.sinks

import org.slf4j.LoggerFactory
import reactor.core.publisher.Sinks

class SinkOneOperator

fun main() {
    val logger = LoggerFactory.getLogger(SinkOneOperator::class.java)

   val sinkOne = Sinks.one<String>()
    val mono = sinkOne.asMono()

    sinkOne.emitValue("Hello Reactor", Sinks.EmitFailureHandler.FAIL_FAST)
//    sinkOne.emitValue("Error Reactor", Sinks.EmitFailureHandler.FAIL_FAST)

    mono.subscribe {
        logger.info("# subscriber1 : $it")
    }
    mono.subscribe {
        logger.info("# subscriber2 : $it")
    }
}