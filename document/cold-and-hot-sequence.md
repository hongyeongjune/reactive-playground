### Cold / Hot
* cold : 처음부터 새로 시작해야하고, 새로 시작하기 때문에 같은 작업이 반복
* hot : 처음부터 다시 시작하지 않고, 같은 작업이 반복되지 않는다.

### Cold Sequence
* 구독 시점이 달라도 구독을 할 때마다 Publisher 가 데이터를 emit 하는 과정을 처음부터 다시 시작하는 데이터의 흐름을 Cold Sequence 라 한다.
![images](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FztMtP%2FbtsbTuCgzRs%2FjkOxv9LBrlZOMkgiUX2Qpk%2Fimg.png)

```kotlin
class ColdSequence

private val logger = LoggerFactory.getLogger(ColdSequence::class.java)

fun main() {
    val coldFlux = Flux
        .fromIterable(listOf("KOREA", "JAPAN", "CHINESE"))
        .map(String::lowercase);

    coldFlux.subscribe {
        logger.info("# Subscriber1: {}", it)
    }

    println("----------------------------------------------------------------------");

    Thread.sleep(2000L);

    coldFlux.subscribe {
        logger.info("# Subscriber2: {}", it)
    }
}
```

```shell
17:13:54.626 [main] INFO ColdSequence -- # Subscriber1: korea
17:13:54.629 [main] INFO ColdSequence -- # Subscriber1: japan
17:13:54.629 [main] INFO ColdSequence -- # Subscriber1: chinese
----------------------------------------------------------------------
17:13:56.635 [main] INFO ColdSequence -- # Subscriber2: korea
17:13:56.635 [main] INFO ColdSequence -- # Subscriber2: japan
17:13:56.635 [main] INFO ColdSequence -- # Subscriber2: chinese
```

### Hot Sequence
Hot Sequence 의 경우 구독이 발생한 시점 이전에 Publisher 로부터 emit 된 데이터는 Subscriber 가 전달받지 못하고, 구독이 발생한 시점 이후에 emit 된 데이터만 받을 수 있다.
![images](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fb2tAH3%2FbtsbVba4rOX%2FjEFumOrIEcDwPEhhcB2lZk%2Fimg.png)

```kotlin
class HotSequence

private val logger = LoggerFactory.getLogger(HotSequence::class.java)

fun main() {
    val singers = arrayOf("Singer A", "Singer B", "Singer C", "Singer D", "Singer E")

    logger.info("# Begin concert:")
    
    // share() : https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#share--
    val hotFlux = Flux
        .fromArray<String>(singers)
        .delayElements(Duration.ofSeconds(1))
        .share()

    hotFlux.subscribe {
        logger.info("# Subscriber1 is watching {}'s song", it)
    }

    Thread.sleep(2500)

    hotFlux.subscribe {
        logger.info("# Subscriber2 is watching {}'s song", it)
    }

    Thread.sleep(3000)
}
```

```shell
17:18:43.287 [main] INFO HotSequence -- # Begin concert:
17:18:44.369 [parallel-1] INFO HotSequence -- # Subscriber1 is watching Singer A's song
17:18:45.375 [parallel-2] INFO HotSequence -- # Subscriber1 is watching Singer B's song
17:18:46.381 [parallel-3] INFO HotSequence -- # Subscriber1 is watching Singer C's song
17:18:46.381 [parallel-3] INFO HotSequence -- # Subscriber2 is watching Singer C's song
17:18:47.387 [parallel-4] INFO HotSequence -- # Subscriber1 is watching Singer D's song
17:18:47.387 [parallel-4] INFO HotSequence -- # Subscriber2 is watching Singer D's song
17:18:48.392 [parallel-5] INFO HotSequence -- # Subscriber1 is watching Singer E's song
17:18:48.392 [parallel-5] INFO HotSequence -- # Subscriber2 is watching Singer E's song
```

### Http 요청과 응답에서 ColdSequence
```kotlin
class HttpRequestColdSequence

private val logger = LoggerFactory.getLogger(HttpRequestColdSequence::class.java)

fun main() {
    val worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
        .host("worldtimeapi.org")
        .port(80)
        .path("/api/timezone/Asia/Seoul")
        .build()
        .encode()
        .toUri()

    val mono = getWorldTime(worldTimeUri)
    mono.subscribe {
        logger.info("# dateTime 1: {}", it)
    }

    Thread.sleep(2000)

    mono.subscribe {
        logger.info("# dateTime 2: {}", it)
    }

    Thread.sleep(2000)
}

private fun getWorldTime(worldTimeUri: URI): Mono<String> {
    return WebClient.create()
        .get()
        .uri(worldTimeUri)
        .retrieve()
        .bodyToMono(JsonNode::class.java)
        .map { response ->
            val dateTime = response["datetime"].textValue()
            dateTime
        }
}
```

```shell
17:27:04.622 [reactor-http-nio-2] INFO HttpRequestColdSequence -- # dateTime 1: 2023-07-02T17:27:04.630580+09:00
17:27:06.425 [reactor-http-nio-2] INFO HttpRequestColdSequence -- # dateTime 2: 2023-07-02T17:27:06.463782+09:00
```

* 첫번째 구독이 발생하고, 2초의 지연 시간 후에 두번째 구독이 발생한다.
* 구독이 발생할때마다 데이터의 emit 과정이 처음부터 새로 시작되는 Cold Sequence 의 특징으로 인해, 두번의 구독이 발생함으로써 두번의 새로운 HTTP 요청이 발생한다.

### Http 요청과 응답에서 HotSequence
```kotlin
class HttpRequestColdSequence

private val logger = LoggerFactory.getLogger(HttpRequestColdSequence::class.java)

fun main() {
    val worldTimeUri = UriComponentsBuilder.newInstance().scheme("http")
        .host("worldtimeapi.org")
        .port(80)
        .path("/api/timezone/Asia/Seoul")
        .build()
        .encode()
        .toUri()

    // cache() : https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#cache--
    val mono = getWorldTime(worldTimeUri).cache()
    mono.subscribe {
        logger.info("# dateTime 1: {}", it)
    }

    Thread.sleep(2000)

    mono.subscribe {
        logger.info("# dateTime 2: {}", it)
    }

    Thread.sleep(2000)
}

private fun getWorldTime(worldTimeUri: URI): Mono<String> {
    return WebClient.create()
        .get()
        .uri(worldTimeUri)
        .retrieve()
        .bodyToMono(JsonNode::class.java)
        .map { response ->
            val dateTime = response["datetime"].textValue()
            dateTime
        }
}
```

```shell
17:29:46.787 [reactor-http-nio-2] INFO HttpRequestColdSequence -- # dateTime 1: 2023-07-02T17:29:46.801157+09:00
17:29:48.484 [main] INFO HttpRequestColdSequence -- # dateTime 2: 2023-07-02T17:29:46.801157+09:00
```

* cache() 메서드로 인해 Cold Sequence 가 Hot Sequence 로 동작하게 된다.
* cache() Operator 는 Cold Sequence 로 동작하는 Mono 를 Hot Sequence 로 변경해주고 emit 된 데이터를 캐시한 뒤, 구독이 발생할 때마다 캐시된 데이터를 전달한다.
* 결과적으로 캐시된 데이터를 전달하기 때문에 구독이 발생할때마다 Subscriber 는 동일한 데이터를 전달받게 된다.

> https://devfunny.tistory.com/913  
> 스프링으로 시작하는 리액티브 프로그래밍 - Spring WebFlux 를 이용한 Non-Blocking 애플리케이션 구현