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

### Coroutine Builder
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

### Coroutine Cancel
> https://kotlinlang.org/docs/cancellation-and-timeouts.html

* 필요하지 않은 코루틴을 적절히 취소해 컴퓨터 자원을 아끼는 것이 중요하다.
* delay 나 yield 같은 suspend 함수를 사용하면 취소 여부를 체크해서 취소를 할 수 있다.
  * delay 같은 함수도 내부적으로는 CancellationException 예외를 발생시켜 취소를 하고 있다. 
  > All the suspending functions in kotlinx.coroutines are cancellable.  
  > kotlinx.coroutines 패키지의 모든 일시 중단 함수들은 취소 가능하다.  
  > They check for cancellation of coroutine and throw CancellationException when cancelled.  
  > 그들은 Coroutine이 취소되었는지 확인하고 취소되었을 경우 CancellationException을 발생시킨다.  
* isActive 를 통해서 현재 코루틴이 활성화되어있는지 취소신호를 받았는지 확인할 수 있다. (Dispatchers.DEFAULT : 코루틴을 다른 스레드에 배정)
  * isActive 로 확인 후 CancellationException 을 던지면 곧장 취소가되고, 예외를 던지지 않아도 조건 설정을 잘하면 코루틴을 종료할 수 있다.

```kotlin
fun main(): Unit = runBlocking {
  val job = launch {
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
  job.cancel()
}
```
```shell
main @coroutine#2 : 1
main @coroutine#2 : 2
main @coroutine#2 : 3
main @coroutine#2 : 4
main @coroutine#2 : 5
```
![images](https://github.com/hongyeongjune/reactive-playground/assets/39120763/eea3de55-9848-4543-832b-df88aa035a24)

```kotlin
fun main(): Unit = runBlocking {
    val job = launch(Dispatchers.Default) {
        var i = 1
        var nextTime = System.currentTimeMillis()
        while (i <= 5) {
            if (nextTime <= System.currentTimeMillis()) {
                println("${Thread.currentThread().name} : ${i++}")
                nextTime += 1000L
            }

            if (!isActive) {
                throw CancellationException()
            }
        }
    }

    delay(100L)
    job.cancel()
}
```
```shell
DefaultDispatcher-worker-1 @coroutine#2 : 1
```
![images](https://github.com/hongyeongjune/reactive-playground/assets/39120763/124bd19d-d3f4-4284-b0ae-555e66f6bf6a)

### Coroutine Exception
> https://kotlinlang.org/docs/exception-handling.html

* launch 는 예외가 발생하면, 예외를 출력하고 코루틴이 종료된다.
* 부모 코루틴이 다른 상황에서 async 는 예외가 발생해도 예외를 출력하지 않는다. await()이 필요하다.
* 즉, 코루틴의 예외는 부모에 전파된다.
* SupervisorJob()을 사용하면 부모 코루틴에게 예외를 전파하지 않는다.
* CoroutineExceptionHandler 는 launch 에만 적용이 가능하고, 부모 코루틴이 있으면 동작하지 않는다.
* delay 같은 함수에서 CancelException 이 발생하면 취소로 간주하고 부모 코루틴으로 전파하지 않는다.
* 그 외 다른 Exception 이 발생하면 취소로 간주하고 부모 코루틴으로 전판한다.
* 다만, 내부적으로는 둘 다 "취소됨" 상태로 관리한다.
* 공식문서를 보면 다음과 같은 문구가 있다. : We already know that a cancelled coroutine throws CancellationException in suspension points and that it is ignored by the coroutines' machinery.
  * 우리는 취소된 Coroutine이 일시중단 지점에서 CancellationException을 발생시키고 이것이 Coroutine의 동작원리에 의해서 무시되는 것을 알고 있다. 

```kotlin
fun main(): Unit = runBlocking {
    val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println("${Thread.currentThread().name} : 예외발생")
    }

    CoroutineScope(Dispatchers.Default).launch(coroutineExceptionHandler) {
        throw IllegalArgumentException()
    }

    delay(1000L)
}
```
```shell
DefaultDispatcher-worker-1 @coroutine#2 : 예외발생
```

#### Cancellation and exceptions
* Coroutine은 내부적으로 취소를 위해 CancellationException을 사용하며, 이 예외는 모든 Handler에서 무시된다.
* 따라서 이들은 catch블록으로부터 얻을 수 있는 추가적인 디버그 정보를 위해서만 사용되어야 한다.
* Coroutine이 Job.cancel을 사용해 취소될 경우 종료되지만, 부모 Coroutine의 실행을 취소하지는 않는다.
* 만약 Coroutine이 CancellationException 말고 다른 예외를 만난다면, 그 예외로 부모 Coroutine까지 취소한다. 
* 이 동작은 재정의할 수 없으며, 구조화된 동시성을 위해 안정적인 Coroutine 계층구조를 제공하는데 사용된다. 
> Coroutines internally use CancellationException for cancellation, these exceptions are ignored by all handlers,  
> so they should be used only as the source of additional debug information, which can be obtained by catch block.     
> When a coroutine is cancelled using Job.cancel, it terminates, but it does not cancel its parent.  
> If a coroutine encounters an exception other than CancellationException, it cancels its parent with that exception.  
> This behaviour cannot be overridden and is used to provide stable coroutines hierarchies for structured concurrency.  

### Structured concurrency
* 코루틴은 ```Structured concurrency```원칙을 따르는데, 이는 새로운 코루틴이 코루틴의 수명을 제한하는 특정 코루틴 스코프에서만 실행될 수 있다는 것을 의미한다.
* 실제 애플리케이션에서는 많은 코루틴을 실행하게 된다. ```Structured concurrency```는 이러한 코루틴이 손실되지 않고 누수되지 않도록 보장한다.
* 외부 Scope 는 모든 자식 코루틴이 완료될 때까지 완료될 수 없고, ```Structured concurrency```는 코드의 모든 오류가 제대로 보고되고 손실되지 않도록 보장한다.
* 즉, 자식 코루틴에서 예외가 발생하면 Structured Concurrency 에 의해 부모 코루틴이 취소되고, 부모 코루틴의 다른 자식 코루틴들도 취소된다.
* 또한, 자식 코루틴에서 예외가 발생하지 않더라도, 부모 코루틴이 취소되면, 자식 코루틴들이 취소된다.
* 다만, CancellationException 은 정상적인 취소로 간주하기 대문에 부모 코루틴에게 전파되지 않고, 부모 코루틴의 다른 자식 코루틴을 취소시키지도 않는다.

### CoroutineScope
> https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/index.html
* 코루틴이 실행될 수 있는 영역/범위
* 코루틴은 스코프 내에서만 실행될 수 있고, 아무런 스코프가 없는 곳에서는 실행될 수 없다
* 일반적으로 모든 스코프는 결국 GlobalScope 를 뿌리로 해서 생겨나며 따라서 일반적인 스코프는 모두 GlobalScope 의 하위 스코프다.
* 예외적으로 GlobalScope.async 나 GlobalScope.launch 를 사용해서 각각 독립적인 최상위 스코프를 생성해서 사용할 수도 있다.
* 하위 스코프에서 생성된 코루틴이 완료되기 전에는 상위 스코프의 코루틴도 완료될 수 없다.
* CoroutineContext 를 보관하는 역할을 한다.

```kotlin
public interface CoroutineScope {
  public val coroutineContext: CoroutineContext
}
```

### CoroutineContext
> https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/
* CoroutineScope 의 property 로서 스코프와 생명주기를 함께 한다.
* 스코프 안에 있는 코루틴(들)이 스코프 안에서 전역적으로 사용될 수 있는 문맥(정보 및 함수 저장소)
* 즉, 코루틴과 관련된 데이털르 보관한다.
* 컨텍스트 내용 중 중요한 것은 Job 과 Dispatcher
* indexed set 이라는, set 과 map 을 혼합한 자료 구조 사용
* 컨텍스트의 요소는 모두 Element 를 상속받은 타입인데 Element 타입도 CoroutineContext 를 상속받은 타입이다
* 따라서 하나의 디스패처가 곧 컨텍스트이기도 하며, + 연산자를 사용해서 디스패처 + 잡 + 이름 + ...와 같이 모두를 포함하는 컨텍스트를 만들 수도 있다
* 컨텍스트 내용은 불변이며, 스코프의 컨텍스트도 val 로 선언돼 있으므로 교체 불가
* +, -를 사용해서 컨텍스트 내용에 추가/삭제한 새로운 컨텍스트를 만들어서 withContext 의 인자로 전달해줄 수는 있음
* 기본값으로 아무 정보도 포함하지 않는 EmptyCoroutineContext 가 주로 사용 된다.

### CoroutineDispatcher
* 코루틴이 어느 스레드에서 실행/재개 될 지 지정하는 역할
* Coroutine Dispatcher 는 Coroutine 의 실행될 사용될 스레드를 특정 스레드로 제한하거나 스레드풀에 분배하거나, 제한 없이 실행 되도록 할 수 있다
  > The coroutine dispatcher can confine coroutine execution to a specific thread, dispatch it to a thread pool, or let it run unconfined.

```kotlin
launch { // context of the parent, main runBlocking coroutine
    println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
}
launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher 
    println("Default               : I'm working in thread ${Thread.currentThread().name}")
}
launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
    println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
}
```

* 순서는 다를 수 있음
```shell
Default               : I'm working in thread DefaultDispatcher-worker-1
newSingleThreadContext: I'm working in thread MyOwnThread
main runBlocking      : I'm working in thread main
```

* launch { ... }가 파라미터 없이 사용된다면, 실행되는 CoroutineScope 으로 부터 Context 를 상속 받는다(Dispatcher도 같이). 
* 이런 경우 main 함수의 runBlocking Coroutine 으로부터 Context 를 상속 받아 Main Thread 에서 실행되게 된다.
* Default Dispatcher 은 Scope 내에서 다른 Dispatcher 을 사용이 명시적으로 지정되지 않았을 때 사용된다. Dispatchers.Default 로 표기되며, 스레드들이 공유하는 Background Pool 을 사용한다.
* newSingleThreadContext 는 Coroutine 이 실행되기 위한 새로운 단일 스레드를 생성한다. 
* 전용 스레드는 매우 비싼 리소스이다. 실제 어플리케이션에서 더 이상 필요하지 않을 때 close 함수를 사용해 해제되어야 하며, 최상위 레벨의 변수에 저장하여 어플리케이션이 실행되는 동안 재사용 될 수 있도록 해야 한다.
> When launch { ... } is used without parameters, it inherits the context (and thus dispatcher) from the CoroutineScope it is being launched from. In this case, it inherits the context of the main runBlocking coroutine which runs in the main thread.  
> The default dispatcher is used when no other dispatcher is explicitly specified in the scope. It is represented by Dispatchers.Default and uses a shared background pool of threads.  
> newSingleThreadContext creates a thread for the coroutine to run. A dedicated thread is a very expensive resource. In a real application it must be either released, when no longer needed, using the close function, or stored in a top-level variable and reused throughout the application.  

#### Dispatchers.Default
* 명시적으로 지정하지 않으면 사용되는 디스패처
* JVM 의 공유 스레드풀을 사용하고 동시 작업 가능한 최대 갯수는 CPU 의 코어 수와 같다.
* CPU 를 많이 소모하는 연산 집중 코루틴에 적합

#### Dispatchers.IO
* 필요에 따라 추가적으로 스레드를 더 생성하거나 줄일 수 있으며 최대 64개까지 생성이 가능하다.
* File I/O, blocking socket I/O 처럼 블로킹 연산에 적합

#### Dispatchers.Main
* UI 객체가 사용되는 main 스레드에서 실행/재개
* 보통 싱글 스레드 환경에서 사용

### suspend
* 코루틴이 중지 되었다가 재개 될 수 있는 지점 

### Continuation
* CPS(Continuation Passing Style)패러다임이 적용되어 있다. (호출되는 함수에 Conitnuation 을 전달하고, 각 함수의 작업이 완료되는 대로 전달받은 Continuation 을 호출하는 패러다임)

```kotlin
public interface Continuation<in T> {
    public val context: CoroutineContext
    public fun resumeWith(result: Result<T>)
}
```

* resumeWith : 특정 함수가 suspend 되어야할 때, 현재 함수에서 결과 값을 T 로 받게 해주는 함수
* context : 각 Continuation 이 특정 스레드 혹은 스레드 풀에서 실행되는 것을 허용
* 즉, 코루틴은 suspend-resume 을 내부적으로 사용하면서 중단되었다가 콜백 호출하여 다시 진행하는 것 이다.