package com.example.playground.context

import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ContextTest {
    @Test
    fun simpleContextExamples() {
        val key = "message"

        val mono = Mono.just("Hello")
            .flatMap {
                Mono.deferContextual { contextView ->
                    Mono.just("$it ${contextView.get<String>(key)}")
                }
            }
            .contextWrite {
                it.put(key, "World")
            }

        StepVerifier.create(mono)
            .expectNext("Hello World")
            .verifyComplete()
    }
}
