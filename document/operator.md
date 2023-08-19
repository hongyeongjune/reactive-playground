### Operator
> https://projectreactor.io/docs/core/release/reference/

### justOrEmpty
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/justOrEmpty.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#justOrEmpty-java.util.Optional-
* emit 할 데이터가 null 일 경우 NullPointException 이 발생하지 않고 onComplete signal 을 전송
* 즉, null 이 아닐 경우 해당 데이터를 emit 하는 Mono 를 생성

### fromIterable
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromIterable.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#fromIterable-java.lang.Iterable-
* Iterable 에 포함된 데이터를 emit 하는 Flux 생성

### fromStream
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromStream.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#fromStream-java.util.function.Supplier- 
* Stream 에 포함된 데이터를 emit 하는 Flux 를 생성

### range
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/range.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#range-int-int-
* n 부터 1 씩 증가한 연속된 수를 m 개 emit 하는 Flux 를 생성

### defer
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/deferForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/deferForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#defer-java.util.function.Supplier-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/deferForFlux.svg
* Operator 를 선언한 시점에 데이터를 emit 하는 것이 아니라 구독하는 시점에 데이터를 emit 하는 Flux 또는 Mono 를 생성
* defer()는 데이터 emit 를 지연시키기 때문에 꼭 필요한 시점에 데이터를 emit 하여 불필요한 프로세스를 줄일 수 있다.

### using
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/usingForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#using-java.util.concurrent.Callable-java.util.function.Function-java.util.function.Consumer-boolean-
* 파라미터로 전달받은 resource 를 emit 하는 Flux 를 생성
* 첫 번째 파라미터는 읽어 올 resource 이고, 두 번째 파라미터는 읽어 온 resource 를 emit 하는 Flux 이고, 세 번째 파라미터는 종료 Signal(onComplete 또는 onError)이 발생할 경우, resource 를 해제하는 등의 후처리를 할 수 있게 해주는 파라미터
* 아래 코드 설명
  * 첫 번째 파라미터 : 파일을 한 라인씩 읽는다.
  * 두 번째 파라미터 : fromStream() Operator 사용 후 emit
  * 세 번째 파라미터 : emit 이 끝나면 Stream 종료 처리
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(UsingOperator::class.java)

    val path = Paths.get("D::\\resources\\using_operator.txt")

    Flux.using(
        { Files.lines(path) },
        { s -> Flux.fromStream(s) },
        { obj -> obj.close() },
    )
        .subscribe { logger.info(it) }
}
```

### generate
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/generate.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#generate-java.util.concurrent.Callable-java.util.function.BiFunction-
* 프로그래밍 방식으로 Signal 이벤트를 발생시키며, 특히 동기적으로 데이터를 하나씩 순차적으로 emit 할 때 사용
* 첫 번 째 파라미터는 emit 할 숫자의 초깃값을 지정 (양수/음수 가능)
* 두 번째 파라미터의 람다 표현식에는 초깃값으로 지정한 숫자부터 emit 하고 emit 한 숫자를 1씩 증가시켜 다시 emit 작업을 반복한다. (State)
* 꼭 초깃값에 숫자만 들어가야하는 건 아니고 객체가 들어갈 수도 있는데 1씩 증가하는 숫자를 포함해야한다.
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(GeneratorOperator::class.java)

    Flux.generate(
        { Tuples.of(3, 1) },
        { state: Tuple2<Int, Int>, sink: SynchronousSink<String> ->
            sink.next("${state.t1} * ${state.t2} = ${state.t1 * state.t2}")
            if (state.t2 == 9) {
                sink.complete()
            }
            Tuples.of(state.t1, state.t2 + 1)
        },
        { state: Tuple2<Int, Int> ->
            logger.info("# 구구단 ${state.t1}단 종료")
        }
    )
        .subscribe { logger.info("# onNext: $it") }
}
```

### create
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/createWithOverflowStrategy.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#create-java.util.function.Consumer-reactor.core.publisher.FluxSink.OverflowStrategy-
* generator() 처럼 프로그래밍 방식으로 Signal 이벤트를 발생시키지만, 한 번에 여러 건의 데이터를 비동기적으로 emit 할 수 있다.
* Backperssure 전략도 같이 사용할 수 있다.
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(CreatorOperator::class.java)

    var size = 0
    var count = -1
    val sources = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    Flux.create(
        { sink: FluxSink<Int> ->
            sink.onRequest { n: Long ->
                Thread.sleep(1000L)
                for (i in 0 until n) {
                    if (count >= 9) {
                        sink.complete()
                    } else {
                        count++
                        sink.next(sources[count])
                    }
                }
            }
            sink.onDispose { logger.info("# clean up") }
        },
        FluxSink.OverflowStrategy.DROP
    ).subscribe(object : BaseSubscriber<Int>() {
        override fun hookOnSubscribe(subscription: Subscription) {
            request(2)
        }

        override fun hookOnNext(value: Int) {
            size++
            logger.info("# onNext: $value")
            if (size == 2) {
                request(2)
                size = 0
            }
        }

        override fun hookOnComplete() {
            logger.info("# onComplete")
        }
    })
}
```