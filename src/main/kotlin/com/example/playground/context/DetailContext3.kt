package com.example.playground.context

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class DetailContext3

fun main() {
    val logger = LoggerFactory.getLogger(DetailContext3::class.java)

    val key1 = "company"

    Mono.just("HYJ")
//        .transformDeferredContextual { mono, contextView -> mono.map { contextView.get<String>("role") } }
        .flatMap { data ->
            Mono.deferContextual { contextView ->
                Mono.just("${contextView.get<String>(key1)}, $data")
                    .transformDeferredContextual { mono, innerCtxView ->
                        mono.map { "$it , ${innerCtxView.get<String>("role")}" }
                    }
                    .contextWrite { it.put("role", "CEO") }
            }
        }
        .publishOn(Schedulers.parallel())
        .contextWrite { it.put(key1, "Naver") }
        .subscribe {
            logger.info("# onNext : {$it}")
        }

    Thread.sleep(100L)
}