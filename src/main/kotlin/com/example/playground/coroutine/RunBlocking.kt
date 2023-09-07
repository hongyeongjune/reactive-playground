package com.example.playground.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        println("${Thread.currentThread().name} : start")
        launch {
            delay(2000L)
            println("${Thread.currentThread().name} : new routine")
        }
    }

    println("${Thread.currentThread().name} : end")
}