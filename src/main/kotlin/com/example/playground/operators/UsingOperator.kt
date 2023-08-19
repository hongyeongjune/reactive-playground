package com.example.playground.operators

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import java.nio.file.Files
import java.nio.file.Paths

class UsingOperator

fun main() {
    val logger = LoggerFactory.getLogger(UsingOperator::class.java)

    val path = Paths.get("D::\\resources\\using_operator.txt")

    Flux.using(
        { Files.lines(path) },
        { s -> Flux.fromStream(s) },
        { obj -> obj.close() },
    )
        .subscribe { logger.info(it) }
}