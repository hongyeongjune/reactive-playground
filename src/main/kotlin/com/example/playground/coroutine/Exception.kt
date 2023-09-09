package com.example.playground.coroutine

import kotlinx.coroutines.*

fun main(): Unit = runBlocking {
    val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println("${Thread.currentThread().name} : 예외발생")
    }

    CoroutineScope(Dispatchers.Default).launch(coroutineExceptionHandler) {
        throw IllegalArgumentException()
    }

    delay(1000L)
}