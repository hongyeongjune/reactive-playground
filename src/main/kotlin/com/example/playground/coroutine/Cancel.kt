package com.example.playground.coroutine

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    val job1 = launch() {
        var i = 1
        var nextTime = System.currentTimeMillis()
        while (i <= 5) {
            if (nextTime <= System.currentTimeMillis()) {
                println("${Thread.currentThread().name} : ${i++}")
                nextTime += 1000L
            }
        }
    }

    delay(100L)
    job1.cancel()

    val job2 = launch(Dispatchers.Default) {
        var i = 1
        var nextTime = System.currentTimeMillis()
        while (isActive && i <= 5) {
            if (nextTime <= System.currentTimeMillis()) {
                println("${Thread.currentThread().name} : ${i++}")
                nextTime += 1000L
            }
        }
    }

    delay(100L)
    job2.cancel()
}