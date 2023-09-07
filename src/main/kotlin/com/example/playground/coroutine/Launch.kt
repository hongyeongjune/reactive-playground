package com.example.playground.coroutine

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    // LAZY 옵션을 줘서 1초뒤에 시작할 수 있게함
    val job1 = launch(start = CoroutineStart.LAZY) {
        println("${Thread.currentThread().name} : start")
    }

    delay(1000L)
    job1.start()

    val job2 = launch {
        // 1,2 만 출력됨 (서로 delay 를 사용하고있기 때문에)
        (1..5).forEach {
            println("${Thread.currentThread().name} : $it")
            delay(500L)
        }
    }
    delay(1000L)
    job2.cancel()

    val job3 = launch {
        delay(1000L)
        println("${Thread.currentThread().name} : job3")
    }

    // 코루틴이 끝날때까지 대기
    // 따라서, 1초 뒤에 job3 출력하고 다시 1초를 기다렸다가 job4 를 출력한다. (약 2초)
    job3.join()

    val job4 = launch {
        delay(1000L)
        println("${Thread.currentThread().name} : job4")
    }
}