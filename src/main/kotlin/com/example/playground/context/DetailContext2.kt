package com.example.playground.context

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class DetailContext2

fun main() {
    val logger = LoggerFactory.getLogger(DetailContext2::class.java)

    val key1 = "company"
    val key2 = "name"

    Mono.deferContextual { Mono.just(it.get<String>(key1)) }
        .publishOn(Schedulers.parallel())
        .contextWrite { it.put(key2, "KSM") }
        .transformDeferredContextual { mono, contextView ->
            mono.map { "$it ${contextView.getOrDefault(key2, "HYJ")}" }
        }
        .contextWrite { it.put(key1, "Naver") }
        .subscribe {
            logger.info("# onNext : {$it}")
        }

    Thread.sleep(100L)
}