### Your first coroutine
> https://kotlinlang.org/docs/coroutines-basics.html#your-first-coroutine

* 코루틴은 개발자가 코드를 실행하는 도중 일시 중단하고 나중에 다시 이어서 실행할 수 있는 인스턴스이다.
* 코루틴은 코드 블록을 실행하면서 다른 코드와 동시에(concurrently) 실행된다는 점에서는 개념적으로 스레드와 비슷하다.
* 하지만, 코루틴은 특정 스레드에 묶이지 않는다.
* 코루틴은 한 스레드에서 실행을 일시 중단하고 다른 스레드에서 다시 실행할 수 있습니다. (즉, 코루틴 A는 B 스레드가 suspend 할 수 있고, C 스레드가 resume 할 수 있다.)
* 또한 동일 스레드에서 코루틴을 실행하면, 이때는 메모리를 전부 공유하므로 스레드보다 context switching cost 가 낮다. (메모리 교체가 없다.)
  * 예를 들어, 코루틴1 (코드1, 코드2) / 코루틴2(코드3)을 하나의 스레드에서 실행한다고했을 때 코드1이 동작하고 일시중지 후 코드3이 실행하고 코드2가 다시 실행된다고해보자.
  * 두 코루틴이 동시에 실행되는 것 처럼 보인다. = 동시성
  * 즉, 하나의 스레드에서도 동시성을 확보할 수 있다.

#### Coroutine Builder
* 주어진 스코프안에서 새 코루틴을 만드는 함수이며 이미 존재하는 스코프 안에서만 호출 가능
* 모든 코루틴 빌더 함수는 CoroutineScope 의 확장 함수이며 ```scopeA.launch는 scopeA``` 스코프 안에서 실행될 수 있는 새 코루틴을 생성한다.
* scopeA 가 생략되면 launch 가 호출되는 위치를 포함하는 최하위 스코프에서 새 코루틴을 생성하고 아무런 스코프도 없는 곳에서 launch 를 호출하면 ```Unresolved reference: launch``` 컴파일 에러 발생

#### runBlocking
* ```runBlocking```또한 코루틴 빌더이고, 코루틴에 속하지않은 일반적인 함수(fun main)와 runBlocking 내부의 코루틴 코드를 연결시켜주는 역할을 한다.
* 이는 IDE 에서 runBlocking 시작 중괄호 바로 다음에 오는 ```this: CoroutineScope```힌트로 강조표시된다.
* 만약 코드 안에 runBlocking 이 없다면, launch 호출 시에 에러가 발생할 것이다. launch 는 CoroutineScope 내에서만 선언될 수 있다.
* GlobalScope 에서 실행될 수 있는 코루틴 생성
* runBlocking 은 이름 그대로 runBlocking 안에 있는 코드가 모두 완료될 때까지 스레드를 block 시킨다.
* 따라서, 보통 코루틴을 만들고싶을때마다 사용하지않고, 프로그램을 진입할 때 최초로 작성해주는 것이 좋다.
* 아래 코드를 보면, end 는 2초를 2초를 기다렸다가 출력하게된다.

```kotlin
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
```

```shell
main @coroutine#1 : start
main @coroutine#2 : new routine
main : end

```

#### launch
* ```launch```는 새로운 코루틴을 시작하는 코루틴 빌더이다.
* 이것은 나머지 코드와 함께 동시에(concurrently) 새 코루틴을 시작하며, 독립적으로 작동한다. (아래에서 Hello 가 먼저 출력된 이유)
* 코루틴을 제어(시작, 취소, 종료시까지 대기)를 할 수 있는 객체 Job 을 반환한다.
* job 을 통해 start, cancel, join 에 대한 예시는 아래 코드와 같다.
* 아래 예시 코드 중 .join() 관련된 코드에서 만약 join 이 없다면 약 1.1초 정도면 출력이 완료된다.

![images](https://github.com/hongyeongjune/reactive-playground/assets/39120763/a21c83c0-bb21-4aac-a689-27e0c0be6caf)

```kotlin
fun main() = runBlocking { // this: CoroutineScope
    launch { // launch a new coroutine and continue
        delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
        println("World!") // print after delay
    }
    println("Hello") // main coroutine continues while a previous one is delayed
}
```
```shell
Hello
World!
```

```kotlin
fun main(): Unit = runBlocking {
    // LAZY 옵션을 줘서 1초뒤에 시작할 수 있게함
    val job1 = launch(start = CoroutineStart.LAZY) {
        println("${Thread.currentThread().name} : start")
    }

    delay(1000L)
    job1.start()
}
```
```kotlin
fun main(): Unit = runBlocking {
    val job2 = launch {
        // 1,2 만 출력됨 (서로 delay 를 사용하고있기 때문에)
        (1..5).forEach {
            println("${Thread.currentThread().name} : $it")
            delay(500L)
        }
    }
    delay(1000L)
    job2.cancel()
}
```
```kotlin
fun main(): Unit = runBlocking {
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
```

#### async
* 코루틴 빌더이며 Deferred 객체를 반환한다. Deferred 객체는 Future 나 Promise 라는 이름으로 불리기도 하는 개념이다.
* 연산을 저장하지만 결과를 받을 수 있는 시점을 뒤로 미룬다.
* launch 는 특별한 결과값을 반환하지 않아도 되는 연산에 사용하는 반면에, async 는 반환값이 있는 연산에 사용한다. 
* Deferred 는 Job 을 상속받는 제네릭 타입이며, async 호춮 결과 Deferred<Int>나 Deferred<CustomType>이 반환된다. 
* Deferred 객체의 await() 메서드를 호출하면 코루틴 연산 결과값을 얻을 수 있다. await()이 호출된 코루틴은 suspend 된다.
* 아래 예시 코드도 각각 대기하기 때문에 아래 이미지와 같이 실제 실행 시간은 약 1.1초 정도된다.

![images](https://github.com/hongyeongjune/reactive-playground/assets/39120763/a21c83c0-bb21-4aac-a689-27e0c0be6caf)

```kotlin
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
```
```shell
main @coroutine#1 : 1 , 2
main @coroutine#1 : time : 1020
```

#### delay
* ```delay```는 특정 시간 동안 코투틴을 일시 중단한다.
* 코루틴을 일시 중단해도 코루틴이 실행중인 스레드를 차단하지는 않고, 다른 코루틴이 해당 스레드에서 자신들의 코드를 실행할 수 있도록 한다.

#### yield
* 현재 코루틴을 중단하고 다른 코루틴이 실행되도록 한다. (스레드를 양보한다.)
* 아래 예시 코드와 출력을 보면 순서가 start -> end -> new routine 인데, yield 가 양보했기 때문에 순서 이렇게 된다.
* 자세히 살명하면, 1번 코루틴에서 start 를 출력하고 launch 아래에 있는 yield 로 인해 그 위의 launch 코루틴에게 양도한다.
* newRoutine 을 실행하면 바로 또 yield 를 만날 수 있는데 다시 또 1번 코루틴에게 양도하게된다.

```kotlin
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
```

```shell
main @coroutine#1 : start
main @coroutine#1 : end
main @coroutine#2 : new routine
```

#### Structured concurrency
* 코루틴은 ```Structured concurrency```원칙을 따르는데, 이는 새로운 코루틴이 코루틴의 수명을 제한하는 특정 코루틴 스코프에서만 실행될 수 있다는 것을 의미한다.
* 위의 예제를 보면, runBlocking 이 해당 범위를 설정하고 delay(1000L) 후 World! 가 출력될 때까지 기다렸다가 종료하는 이유다.
* 실제 애플리케이션에서는 많은 코루틴을 실행하게 된다.
* ```Structured concurrency```는 이러한 코루틴이 손실되지 않고 누수되지 않도록 보장한다.
* 외부 Scope 는 모든 자식 코루틴이 완료될 때까지 완료될 수 없고, ```Structured concurrency```는 코드의 모든 오류가 제대로 보고되고 손실되지 않도록 보장합니다.