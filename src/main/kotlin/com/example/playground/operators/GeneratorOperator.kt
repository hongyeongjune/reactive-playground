package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.SynchronousSink
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

class GeneratorOperator

fun main() {
    val logger = LoggerFactory.getLogger(GeneratorOperator::class.java)

    Flux.generate(
        { Tuples.of(3, 1) },
        { state: Tuple2<Int, Int>, sink: SynchronousSink<String> ->
            sink.next("${state.t1} * ${state.t2} = ${state.t1 * state.t2}")
            if (state.t2 == 9) {
                sink.complete()
            }
            Tuples.of(state.t1, state.t2 + 1)
        },
        { state: Tuple2<Int, Int> ->
            logger.info("# 구구단 ${state.t1}단 종료")
        }
    )
        .subscribe { logger.info("# onNext: $it") }
}