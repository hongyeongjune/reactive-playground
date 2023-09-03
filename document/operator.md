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

### filter
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/filterForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/filterForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#filter-java.util.function.Predicate-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#filter-java.util.function.Predicate-

* Upstream 에서 emit 된 데이터 중에서 조건에 일치하는 데이터만 Downstream 으로 emit 한다.
* 즉, filter() Operator 의 파라미터로 입력받은 Predicate 의 리턴 값이 true 인 데이터만 Downstream 으로 emit 한다.

### filterWhen
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/filterWhenForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/filterWhenForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#filterWhen-java.util.function.Function-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#filterWhen-java.util.function.Function-

* filterWhen() Operator 는 비동기적으로 필터링을 수행한다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(FilterWhenOperator::class.java)

    val corporateMap = mapOf(
        "naver" to "네이버",
        "webtoon" to "네이버웹툰",
    )

    Flux.fromIterable(listOf("naver", "webtoon"))
        .filterWhen {
            Mono.just(corporateMap[it]!!.startsWith("네이버"))
                .publishOn(Schedulers.parallel())
        }
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(1000L)
}
```

### skip
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/skip.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#skip-long-

* Upstream 에서 emit 된 데이터 중에서 파라미터로 입력받은 숫자만큼 건너뛴 후, 나머지 데이터를 Downstream으로 emit 한다.

### take
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/takeLimitRequestTrue.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#take-long-

* Upstream 에서 emit 되는 데이터 중에서 파라미터로 입력받은 숫자만큼만 Downstream 으로 emit 한다.

### next
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/next.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#next--

* Upstream 에서 emit 되는 데이터 중에서 첫 번째 데이터만 Downstream 으로 emit 한다.
* 만약 Upstream 에서 emit 되는 데이터가 empty 라면 Downstream 으로 empty Mono 를 emit 한다.

### map
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/mapForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/mapForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#map-java.util.function.Function-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#map-java.util.function.Function-

* Upstream 에서 emit 된 데이터를 mapper Function 을 사용하여 변환한 후, Downstream 으로 emit 한다.

### flatMap
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/flatMapForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/flatMapForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#flatMap-java.util.function.Function-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#flatMap-java.util.function.Function-

* Upstream 에서 emit 된 데이터 한 건이 InnerSequence 에서 여러 건의 데이터로 변환되고, InnerSequence 에서 평탄화 작업을 거치면서 하나의 Sequence 로 병합되어 Downstream 으로 emit 된다.
* flatMap 에서 내부의 InnerSequence 를 비동기적으로 실행하면 데이터 emit 의 순서를 보장하지 않는다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(FlatMapOperator::class.java)

    Flux.range(2, 8)
        .flatMap {
            Flux.range(1, 9)
                .publishOn(Schedulers.parallel())
                .map { num ->
                    "$it * $num = ${it * num}"
                }
        }
        .subscribe {
            logger.info(it)
        }

    Thread.sleep(100L)
}
```

```shell
22:10:41.450 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 1 = 8
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 1 = 2
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 2 = 4
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 3 = 6
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 4 = 8
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 5 = 10
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 6 = 12
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 7 = 14
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 8 = 16
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 2 * 9 = 18
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 3 * 1 = 3
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 3 * 2 = 6
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 3 * 3 = 9
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 3 * 4 = 12
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 3 * 5 = 15
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 3 * 6 = 18
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 3 * 7 = 21
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 3 * 8 = 24
...
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 9 * 7 = 63
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 9 * 8 = 72
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 9 * 9 = 81
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 2 = 16
22:10:41.453 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 3 = 24
22:10:41.454 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 4 = 32
22:10:41.454 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 5 = 40
22:10:41.454 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 6 = 48
22:10:41.454 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 7 = 56
22:10:41.454 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 8 = 64
22:10:41.454 [parallel-7] INFO com.example.playground.operators.FlatMapOperator -- 8 * 9 = 72
```

### concat
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/concatWithForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/concatWithForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#concatWith-org.reactivestreams.Publisher-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#concatWith-org.reactivestreams.Publisher-

* 파라미터로 입력되는 Publisher 의 Sequence 를 연결해서 데이터를 순차적으로 emit 한다.
* 특히, 먼저 입력된 Publisher 의 Sequence 가 종료될 때까지 나머지 Publisher 의 Sequence 는 subscribe 되지 않고, 대기하는 특성을 가진다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(ConcatOperator::class.java)

    Flux.concat(Flux.just(1, 2, 3), Flux.just(4, 5))
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(100L)
}
```

```shell
22:13:44.866 [main] INFO com.example.playground.operators.ConcatOperator -- # onNext: 1
22:13:44.868 [main] INFO com.example.playground.operators.ConcatOperator -- # onNext: 2
22:13:44.868 [main] INFO com.example.playground.operators.ConcatOperator -- # onNext: 3
22:13:44.868 [main] INFO com.example.playground.operators.ConcatOperator -- # onNext: 4
22:13:44.868 [main] INFO com.example.playground.operators.ConcatOperator -- # onNext: 5
```

### merge
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/mergeFixedSources.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#merge-int-org.reactivestreams.Publisher%2E%2E%2E-

* 파라미터로 입력되는 Publisher 의 Sequence 에서 emit 된 데이터를 인터리빙(interleave)방식으로 병합한다.
  * 인터리빙 : 두 개의 Sequence 에서 emit 되는 데이터가 서로 교차되는 방식으로 merge 되는 것
* concat() 처럼 먼저 입력된 Publisher 의 Sequence 가 종료될 때까지 나머지 Publisher 의 Sequence 가 Subscribe 되지 않고 대기하는 것이 아니라, 모든 Publisher 의 Sequence 가 즉시 subsribe 된다.
* 아래 예제를 보면 시간순서대로 emit 되는 것을 볼 수 있다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(MergeOperator::class.java)

    Flux.merge(
        Flux.just(1, 2, 3, 4).delayElements(Duration.ofMillis(300L)),
        Flux.just(5, 6, 7).delayElements(Duration.ofMillis(500L)),
    )
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(2000L)
}
```

```shell
22:18:19.783 [parallel-1] INFO com.example.playground.operators.MergeOperator -- # onNext: 1
22:18:19.979 [parallel-2] INFO com.example.playground.operators.MergeOperator -- # onNext: 5
22:18:20.090 [parallel-3] INFO com.example.playground.operators.MergeOperator -- # onNext: 2
22:18:20.395 [parallel-5] INFO com.example.playground.operators.MergeOperator -- # onNext: 3
22:18:20.481 [parallel-4] INFO com.example.playground.operators.MergeOperator -- # onNext: 6
22:18:20.699 [parallel-6] INFO com.example.playground.operators.MergeOperator -- # onNext: 4
22:18:20.982 [parallel-7] INFO com.example.playground.operators.MergeOperator -- # onNext: 7
```

### zip
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/zipIterableSourcesForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#zip-java.util.function.Function-org.reactivestreams.Publisher%2E%2E%2E-

* 파라미터로 입력되는 Publisher Sequence 에서 emit 된 데이터를 결합하는데, 각 Publisher 가 데이터를 하나씩 emit 할 때까지 기다렸다가 결합한다.
* 아래 결과를 보면 첫 번째 Publisher 의 4는 출력되지 않고, 각각 결합된 결과만 나온다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(ZipOperator::class.java)

    Flux.zip(
        Flux.just(1, 2, 3, 4).delayElements(Duration.ofMillis(300L)),
        Flux.just(5, 6, 7).delayElements(Duration.ofMillis(500L)),
    )
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(2000L)
}
```

```shell
22:20:17.352 [parallel-2] INFO com.example.playground.operators.ZipOperator -- # onNext: [1,5]
22:20:17.856 [parallel-4] INFO com.example.playground.operators.ZipOperator -- # onNext: [2,6]
22:20:18.362 [parallel-7] INFO com.example.playground.operators.ZipOperator -- # onNext: [3,7]
```

### collectList
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/collectList.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#collectList--

* Flux 에서 emit 된 데이터를 모아서 List 로 변환한 후, 변환된 List 를 emit 하는 Mono 를 반환한다.
* 만약 Upstream Sequence 가 비어있다면 비어 있는 List 를 Downstream 으로 emit 한다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(CollectListOperator::class.java)

    Flux.just("naver", "naver webtoon", "line")
        .collectList()
        .subscribe {
            logger.info("# onNext: $it")
        }

    Thread.sleep(2000L)
}
```

```shell
22:27:23.961 [main] INFO com.example.playground.operators.CollectListOperator -- # onNext: [naver, naver webtoon, line]
```

### doOnXXXX
* Upstream Publisher 에서 emit 되는 데이터의 변경 없이 부수 효과만을 수행하기 위한 Operator
* doOnXXXX 는 Consumer 또는 Runnable 타입의 함수형 인터페이스를 파라미터로 가지기 때문에 별도의 리턴 값이 없다.
* 따라서 Upstream Publisher 로부터 emit 되는 데이터를 이용해 Upstream Publisher 의 내부 동작을 엿볼 수 있으며 로그를 출력하는 등의 디버깅 용도로 많이 사용된다.

Operator|설명
---|---
doOnSubscribe()|Publisher 가 구독 중일 때 트러기되는 동작을 추가할 수 있다.
doOnRequest()|Publisher 가 요청을 수신할 떄 트리거되는 동작을 추가할 수 있다.
doOnNext()|Publisher 가 데이터를 emit 할 때 트리거되는 동작을 추가할 수 있다.
doOnComplete()|Publisher 가 성공적으로 완료되었을 때 트리거되는 동작을 추가할 수 있다.
doOnError()|Publisher 가 에러가 발생한 상태로 종료되었을 때 트리거되는 동작을 추가할 수 있다.
doOnCancel()|Publisher 가 취소되었을 때 트러기되는 동작을 추가할 수 있다.
doOnTerminate()|Publisher 가 성공적으로 완료되었을 때 또는 에러가 발생한 상태로 종료되었을 때 트러기되는 동작을 추가할 수 있다.
doOnEach()|Publisher 가 데이터를 emit 할 때, 성공적으로 완료되었을 때, 에러가 발생한 상태로 종료되었을 때 트리거되는 동작을 추가할 수 있다.
doOnDiscard()|Upstream 에 있는 전체 Operator 체인의 동작 중에서 Operator 에 의해 폐기되는 요소를 조건부로 정리할 수 있다.
doAfterTeminate()|Downstream 을 성공적으로 완료한 직후 또는 에러가 발생하여 Publisher 가 종료된 직후에 트리거되는 동작을 추가할 수 있다.
doFirst()|Publisher 가 구독되기 전에 트리거되는 동작을 추가할 수 있다.
doFinally()|에러를 포함해서 어떤 이유이든 간에 Publisher 가 종료된 후 트리거되는 동작을 추가할 수 있다.


### error
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/error.svg)
![imaegs](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/error.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#error-java.lang.Throwable-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#error-java.lang.Throwable-

* error() Operator 는 파라미터로 지정된 에러로 종료하는 Flux 를 생성한다.

```kotlin
fun main() {
  val logger = LoggerFactory.getLogger(ErrorOperator::class.java)

  Flux.range(1, 5)
    .flatMap {
      if (it * 2 % 3 == 0) {
        Flux.error(
          IllegalArgumentException("Not allowed multiple of 3")
        )
      } else {
        Mono.just(it * 2)
      }
    }
    .subscribe(
      { data -> logger.info("# onNext: $data") },
      { error -> logger.error("# onError: $error") }
    )
}
```

```shell
00:12:33.523 [main] INFO com.example.playground.operators.ErrorOperator -- # onNext: 2
00:12:33.524 [main] INFO com.example.playground.operators.ErrorOperator -- # onNext: 4
00:12:33.525 [main] ERROR com.example.playground.operators.ErrorOperator -- # onError: java.lang.IllegalArgumentException: Not allowed multiple of 3
```

### onErrorReturn
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/onErrorReturnForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/onErrorReturnForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#onErrorReturn-java.lang.Class-T-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#onErrorReturn-java.lang.Class-T-

* 에러 이벤트가 발생했을 때 에러 이벤트를 Downstream 으로 전파하지 않고 대체 값을 emit 한다.
* 꼭 Exception 을 명시하지않으면, Exception 이 발생하기만 하면 return 된다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(ErrorOnReturnOperator::class.java)

    Flux.just("naver", "naver webtoon", null, "line")
        .map { it!!.uppercase() }
        .onErrorReturn(NullPointerException::class.java, "no name")
        .subscribe {
            logger.info(it)
        }

    Thread.sleep(200L)
}
```

```shell
00:19:07.906 [main] INFO com.example.playground.operators.ErrorOnReturnOperator -- NAVER
00:19:07.907 [main] INFO com.example.playground.operators.ErrorOnReturnOperator -- NAVER WEBTOON
00:19:07.907 [main] INFO com.example.playground.operators.ErrorOnReturnOperator -- no name
```

### onErrorResume
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/onErrorResumeForMono.svg)
![images]([https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/onErrorResumeForMono.svg](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/onErrorResumeForFlux.svg))
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#onErrorResume-java.lang.Class-java.util.function.Function-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#onErrorResume-java.lang.Class-java.util.function.Function-

* 에러 이벤트가 발생했을 때, 에러 이벤트를 Downstream 으로 전파하지 않고, 대체 Publisher 를 리턴한다.

```kotlin
class OnErrorResumeOperator

fun main() {
    val logger = LoggerFactory.getLogger(OnErrorResumeOperator::class.java)
    val keyword = "line"

    val lineFlux = Flux.fromIterable(listOf("line"))
        .filter { it == keyword }
        .switchIfEmpty(Flux.error(IllegalArgumentException("is empty")))

    Flux.fromIterable(listOf("naver", "naver webtoon"))
        .filter { it == keyword }
        .switchIfEmpty(Flux.error(IllegalArgumentException("is empty")))
        .onErrorResume { lineFlux }
        .subscribe (
            { data -> logger.info("# onNext: $data") },
            { error -> logger.error("# onError: $error") }
        )

    Thread.sleep(200L)
}
```

```shell
00:19:07.906 [main] INFO com.example.playground.operators.ErrorOnResumeOperator -- LINE
```

### onErrorContinue
* 에러가 발생했을 때, 에러 영역 내에 있는 데이터는 제거하고, Upstream 에서 후속 데이터를 emit 하는 방식으로 에러를 복구할 수 있도록 해준다.
* 예를 들어, ```Flux.just(1, 2, 4, 0, 6, 12).map { 12 / it }```에서 4번 째 0 에서는 에러가 발생해도 6, 12 에 대한 값은 그대로 실행한다.

### retry
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/retryWithAttemptsForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/retryWithAttemptsForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#retry-long-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#retry-long-

* Publisher 가 데이터를 emit 하는 과정에서 에러가 발생하면 파라미터로 입력한 횟수만큼 원본 Flux 의 Sequence 를 다시 구독한다.

### elapsed
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/elapsedForMono.svg)
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/elapsedForFlux.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#elapsed-reactor.core.scheduler.Scheduler-
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#elapsed-reactor.core.scheduler.Scheduler-

* emit 된 데이터 사이의 경과 시간을 측정해서 Tuple<Long, T> 형태로 Downstream 에 emit 한다.
* Tuple 의 Long 은 시간, T 는 데이터이다.
* 첫 번째 데이터는 onSubscribe Signal 과 첫 번째 데이터 사이를 기준으로 시간을 측정한다.

### window
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/windowWithTimespan.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#window-java.time.Duration-

* Upstream 에서 emit 되는 첫 번째 데이터부터 maxSize 숫자만큼 데이터를 포함하는 새로운 Flux 로 분할한다.
* Reactor 에서는 이렇게 분리된 Flux 를 Window 라고 부른다.

```kotlin
class WindowOperator

fun main() {
    val logger = LoggerFactory.getLogger(WindowOperator::class.java)

    Flux.range(1, 11)
        .window(3)
        .flatMap {
            println("=======================")
            it
        }
        .subscribe(object : BaseSubscriber<Int>() {
            override fun hookOnSubscribe(subscription: Subscription) {
                subscription.request(2)
            }

            override fun hookOnNext(value: Int) {
                logger.info("# onNext: $value")
                request(2)
            }
        })

    Thread.sleep(200L)
}
```

```shell
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- =======================
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 1
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 2
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 3
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- =======================
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 4
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 5
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 6
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- =======================
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 7
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 8
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 9
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- =======================
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 10
00:19:07.906 [main] INFO com.example.playground.operators.WindowOperator -- onNext 11
```

### buffer
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/bufferWithTimespan.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#buffer-java.time.Duration-

* Upstream 에서 emit 되는 첫 번째 데이터부터 maxSize 숫자만큼의 데이터를 List 버퍼로 한 번에 emit 한다.
* 마지막 버퍼에 포함된 데이터의 개수는 maxSize 보다 더 적거나 같다.
* 예를 들어, ```Flux.range(1, 95).buffer(10).subsribe(...)```의 경우에는 emit 된 데이터가 최대 10개까지 버퍼에 담기면 List 버퍼 형태로 Downstream 에 emit 한다.

### bufferTimeout
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/bufferTimeoutWithMaxSizeAndTimespan.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#bufferTimeout-int-java.time.Duration-

* Upstream 에서 emit 되는 첫 번째 데이터부터 maxSize 숫자만큼의 데이터 또는 maxTime 내에 emit 된 데이터를 List 버퍼로 한 번에 emit 한다.
* 또한, maxSize 혹은 maxTime 중 먼저 조건에 부합할 때까지 emit 된 데이터를 List 버퍼로 emit 한다.

### groupBy
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/groupByWithKeyMapper.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#groupBy-java.util.function.Function-

* emit 되는 데이터를 keyMapper 로 생성한 key를 기준으로 그룹화한 GroupedFlux 를 리턴하며, 이 GroupedFlux 를 통해서 그룹별로 작업을 수행할 수 있다.
* groupBy 마블 다이어그램에서는 emit 되는 도형의 모양별로 그룹화하여 그룹별로 별도의 GroupedFlux Sequence 가 생성된 것을 볼 수 있다.

### publish
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/publish.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#publish--

* 구독을 하더라도 구독 시점에 즉시 데이터를 emit 하지 않고, connect() 를 호출하는 시점에 비로소 데이터를 emit 한다.
* 그리고 Hot Sequence 로 변환되기 때문에 구독 시점 이후에 emit 된 데이터만 전달받을 수 있다.
* 아래 예시를 보면, 구독3번은 1초 뒤에 시작하고 0.3마다 emit하기 때문에 1,2,3 은 구독1,2 만 4,5 는 전부다 출력되는 것을 볼 수 있다. (Hot Sequence)

```kotlin
class PublishOperator

fun main() {
    val logger = LoggerFactory.getLogger(PublishOperator::class.java)

    val flux = Flux.range(1, 5)
        .delayElements(Duration.ofMillis(300L))
        .publish()

    Thread.sleep(500L)
    flux.subscribe { logger.info("# subscribe1: $it") }

    Thread.sleep(200L)
    flux.subscribe { logger.info("# subscribe2: $it") }

    flux.connect()

    Thread.sleep(1000L)
    flux.subscribe { logger.info("# subscribe3: $it") }

    Thread.sleep(2000L)
}
```

```shell
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe1: 1
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe2: 1
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe1: 2
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe2: 2
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe1: 3
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe2: 3
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe1: 4
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe2: 4
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe3: 4
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe1: 5
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe2: 5
00:19:07.906 [main] INFO com.example.playground.operators.PublishOperator -- # subscribe3: 5
```

### autoConnect
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/autoConnectWithMinSubscribers.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/ConnectableFlux.html#autoConnect-int-

* publish()는 구독이 발생하더라도 connect()를 직접 호출하기 전까지는 데이터를 emit 하지 않기 때문에 코드상에서 connect()를 직접 호출해야 한다.
* 반면에 autoConnect()는 파라미터로 지정하는 숫자만큼의 구독이 발생하는 시점에 Upstream 소스에 자동으로 연결되기 때문에 별도의 connect() 호출이 필요하지 않습니다.

### refCount
![images](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/refCountWithMinSubscribers.svg)
> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/ConnectableFlux.html#refCount-int-

* 입력된 숫자만큼의 구독이 발생하는 시점에 Upstream 소스에 연결되며, 모든 구독이 취소되거나 Upstream 의 데이터 emit 이 종료되면 연결이 해제된다.
* refCount() 는 주로 무한 스트림 상황에서 모든 구독이 취소될 경우 연결을 해제하는 데 사용할 수 있다.
* 아래 코드에서 2.1초 후에 구독을 해제해서 연결이 해제되고, 두 번째 구독이 발생할 경우에 다시 Upstream 에 연결된다.

```kotlin
class RefCountOperator

fun main() {
    val logger = LoggerFactory.getLogger(RefCountOperator::class.java)

    val publisher = Flux.interval(Duration.ofMillis(500L))
        .publish()
//        .autoConnect(1)
        .refCount(1)

    val disposable = publisher.subscribe { logger.info("# subscribe 1:  $it") }

    Thread.sleep(2100L)
    disposable.dispose()

    publisher.subscribe { logger.info("# subscribe 2: $it") }

    Thread.sleep(2500L)
}
```

```shell
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 1: 0
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 1: 1
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 1: 2
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 1: 3
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 0
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 1
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 2
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 3
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 4
```

* 만약 위의 주석인 autoConnect() 를 해제하면 다음과 같은 결과를 볼 수 있다.

```shell
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 1: 0
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 1: 1
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 1: 2
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 1: 3
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 4
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 5
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 6
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 7
00:19:07.906 [main] INFO com.example.playground.operators.RefCountOperator -- # subscribe 2: 8
```
