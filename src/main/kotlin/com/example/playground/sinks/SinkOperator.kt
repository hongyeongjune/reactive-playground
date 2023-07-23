package com.example.playground.sinks

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

class SinkOperator {
    companion object {
        fun doTasks(taskNumber: Int): String {
            return "task $taskNumber result"
        }
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(SinkOperator::class.java)

    val tasks = 6
    val unicastSink = Sinks.many().unicast().onBackpressureBuffer<String>()
    val fluxView = unicastSink.asFlux()
    IntStream.range(1, tasks)
        .forEach {
            try {
                Thread {
                    unicastSink.emitNext(
                        SinkOperator.doTasks(it),
                        Sinks.EmitFailureHandler.FAIL_FAST,
                    )
                    logger.info("# emitted: {$it}")
                }.start()
                Thread.sleep(100L)
            } catch (e: InterruptedException) {
                logger.error(e.message)
            }
        }


    fluxView.publishOn(Schedulers.parallel())
        .map { "$it success!" }
        .doOnNext {
            logger.info("# map(): {$it}")
        }
        .publishOn(Schedulers.parallel())
        .subscribe {
            logger.info("# onNext: {$it}")
        }

    Thread.sleep(500L)
}