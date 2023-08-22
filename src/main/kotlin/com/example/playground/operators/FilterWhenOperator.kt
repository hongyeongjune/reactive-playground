package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class FilterWhenOperator

fun main() {
    val logger = LoggerFactory.getLogger(FilterWhenOperator::class.java)

    val corporateMap = mapOf(
        "naver" to "네이버",
        "webtoon" to "네이버웹툰",
    )

    Flux.fromIterable(listOf("naver", "webtoon"))
        .filterWhen {
            Mono.just(corporateMap[it]!!.startsWith("네이버"))
                .publishOn(Schedulers.parallel())
        }
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(1000L)
}