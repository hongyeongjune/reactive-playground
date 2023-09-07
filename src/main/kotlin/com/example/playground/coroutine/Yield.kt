package com.example.playground.coroutine

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

fun main(): Unit = runBlocking {
    println("${Thread.currentThread().name} : start")
    launch {
        newRoutine()
    }
    yield()
    println("${Thread.currentThread().name} : end")
}

suspend fun newRoutine() {
    yield()
    println("${Thread.currentThread().name} : new routine")
}