package com.example.playground.coroutine

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main(): Unit = runBlocking {
    val time = measureTimeMillis {
        val job1 = async { api1() }
        val job2 = async { api2() }
        println("${Thread.currentThread().name} : ${job1.await()} , ${job2.await()}")
    }
    println("${Thread.currentThread().name} : time : $time")
}

suspend fun api1(): Int {
    delay(1000L)
    return 1
}

suspend fun api2(): Int {
    delay(1000L)
    return 2
}