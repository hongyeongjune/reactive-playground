package com.example.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactivePlaygroundApplication

fun main(args: Array<String>) {
    runApplication<ReactivePlaygroundApplication>(*args)
}
