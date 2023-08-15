package com.example.playground.context

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class DetailContext

fun main() {
    val logger = LoggerFactory.getLogger(DetailContext::class.java)

    val key1 = "company"

    val mono = Mono.deferContextual {
        Mono.just("Company: ${it.get<String>(key1)}")
    }
        .publishOn(Schedulers.parallel())

    mono.contextWrite {
        it.put(key1, "Naver Webtoon")
    }
        .subscribe {
            logger.info("# subscribe1 onNext: {$it}")
        }

    mono.contextWrite {
        it.put(key1, "Naver")
    }
        .subscribe {
            logger.info("# subscribe2 onNExt: {$it}")
        }

    Thread.sleep(100L)
}