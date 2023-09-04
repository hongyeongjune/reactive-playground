### Your first coroutine
> https://kotlinlang.org/docs/coroutines-basics.html#your-first-coroutine

* 코루틴은 개발자가 코드를 실행하는 도중 일시 중단하고 나중에 다시 이어서 실행할 수 있는 인스턴스이다.
* 코루틴은 코드 블록을 실행하면서 다른 코드와 동시에(concurrently) 실행된다는 점에서는 개념적으로 스레드와 비슷하다.
* 하지만, 코루틴은 특정 스레드에 묶이지 않는다.
* 코루틴은 한 스레드에서 실행을 일시 중단하고 다른 스레드에서 다시 실행할 수 있습니다. (즉, 코루틴 A는 B 스레드가 suspend 할 수 있고, C 스레드가 resume 할 수 있다.)

#### Coroutine Builder
* 주어진 스코프안에서 새 코루틴을 만드는 함수이며 이미 존재하는 스코프 안에서만 호출 가능
* 모든 코루틴 빌더 함수는 CoroutineScope 의 확장 함수이며 ```scopeA.launch는 scopeA``` 스코프 안에서 실행될 수 있는 새 코루틴을 생성한다.
* scopeA 가 생략되면 launch 가 호출되는 위치를 포함하는 최하위 스코프에서 새 코루틴을 생성하고 아무런 스코프도 없는 곳에서 launch 를 호출하면 ```Unresolved reference: launch``` 컴파일 에러 발생

#### launch
* ```launch```는 코루틴 빌더이다.
* 이것은 나머지 코드와 함께 동시에(concurrently) 새 코루틴을 시작하며, 독립적으로 작동한다. (아래에서 Hello 가 먼저 출력된 이유)

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

#### delay
* ```delay```는 특정 시간 동안 코투틴을 일시 중단한다.
* 코루틴을 일시 중단해도 코루틴이 실행중인 스레드를 차단하지는 않고, 다른 코루틴이 해당 스레드에서 자신들의 코드를 실행할 수 있도록 한다.

#### runBlocking
* ```runBlocking```또한 코루틴 빌더이고, 코루틴에 속하지않은 일반적인 함수(fun main)와 runBlocking 내부의 코루틴 코드를 연결시켜주는 역할을 한다.
* 이는 IDE 에서 runBlocking 시작 중괄호 바로 다음에 오는 ```this: CoroutineScope```힌트로 강조표시된다.
* 만약 코드 안에 runBlocking 이 없다면, launch 호출 시에 에러가 발생할 것이다. launch 는 CoroutineScope 내에서만 선언될 수 있다.
* GlobalScope 에서 실행될 수 있는 코루틴 생성

#### Structured concurrency
* 코루틴은 ```Structured concurrency```원칙을 따르는데, 이는 새로운 코루틴이 코루틴의 수명을 제한하는 특정 코루틴 스코프에서만 실행될 수 있다는 것을 의미한다.
* 위의 예제를 보면, runBlocking 이 해당 범위를 설정하고 delay(1000L) 후 World! 가 출력될 때까지 기다렸다가 종료하는 이유다.
* 실제 애플리케이션에서는 많은 코루틴을 실행하게 된다.
* ```Structured concurrency```는 이러한 코루틴이 손실되지 않고 누수되지 않도록 보장한다.
* 외부 Scope 는 모든 자식 코루틴이 완료될 때까지 완료될 수 없고, ```Structured concurrency```는 코드의 모든 오류가 제대로 보고되고 손실되지 않도록 보장합니다.
