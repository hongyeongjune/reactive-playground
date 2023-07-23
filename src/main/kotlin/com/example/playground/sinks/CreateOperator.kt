package com.example.playground.sinks

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

class CreateOperator {
    companion object {
        fun doTasks(taskNumber: Int): String {
            return "task $taskNumber result"
        }
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(CreateOperator::class.java)

    val tasks = 6
    Flux.create {
        IntStream.range(1, tasks)
            .forEach { n ->
                it.next(
                    CreateOperator.doTasks(n),
                )
            }
    }
        .subscribeOn(Schedulers.boundedElastic())
        .doOnNext {
            logger.info("# creator(): $it")
        }
        .publishOn(Schedulers.parallel())
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