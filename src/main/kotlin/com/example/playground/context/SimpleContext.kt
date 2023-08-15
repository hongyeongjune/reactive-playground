package com.example.playground.context

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class SimpleContext

fun main() {
    val logger = LoggerFactory.getLogger(SimpleContext::class.java)

    Mono.deferContextual {contextView ->
        Mono.just("Hello ${contextView.get<String>("firstName")}")
            .doOnNext {
                logger.info("# just doOnNext : {$it}")
            }
    }
        .subscribeOn(Schedulers.boundedElastic())
        .publishOn(Schedulers.parallel())
        .transformDeferredContextual { mono, contextView ->
            mono.map { "$it ${contextView.get<String>("lastName")}" }
        }
        .contextWrite {
            it.put("lastName", "Jobs")
        }
        .contextWrite {
            it.put("firstName", "Steve")
        }
        .subscribe {
            logger.info("# onNext : {$it}")
        }

    Thread.sleep(100L)
}