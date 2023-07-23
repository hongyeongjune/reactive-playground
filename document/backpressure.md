### Backpressure
* Publisher 가 끊임없이 emit 하는 무수히 많은 데이터를 제어하여 데이터 처리에 과부하가 걸리지 않도록 제어하는 것

### Reactor 에서 Backpressure 처리 방식 : Subscriber 가 적절히 처리할 수 있는 수준의 데이터 개수를 Publisher 에게 요청
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(RequestStrategy::class.java)

    Flux.range(1, 5)
        .doOnRequest {
            logger.info("# doOnRequest: {$it}")
        }
        .subscribe(object : BaseSubscriber<Int>() {
            override fun hookOnSubscribe(subscription: Subscription) {
                request(1)
            }

            override fun hookOnNext(value: Int) {
                Thread.sleep(2000L)
                logger.info("# hookOnNext: {$value}")
                request(1)
            }
        })
}
```

```shell
13:27:25.026 [main] INFO com.example.playground.backpressure.RequestStrategy -- # doOnRequest: {1}
13:27:27.037 [main] INFO com.example.playground.backpressure.RequestStrategy -- # hookOnNext: {1}
13:27:27.038 [main] INFO com.example.playground.backpressure.RequestStrategy -- # doOnRequest: {1}
13:27:29.039 [main] INFO com.example.playground.backpressure.RequestStrategy -- # hookOnNext: {2}
13:27:29.040 [main] INFO com.example.playground.backpressure.RequestStrategy -- # doOnRequest: {1}
13:27:31.044 [main] INFO com.example.playground.backpressure.RequestStrategy -- # hookOnNext: {3}
13:27:31.044 [main] INFO com.example.playground.backpressure.RequestStrategy -- # doOnRequest: {1}
13:27:33.046 [main] INFO com.example.playground.backpressure.RequestStrategy -- # hookOnNext: {4}
13:27:33.047 [main] INFO com.example.playground.backpressure.RequestStrategy -- # doOnRequest: {1}
13:27:35.051 [main] INFO com.example.playground.backpressure.RequestStrategy -- # hookOnNext: {5}
13:27:35.052 [main] INFO com.example.playground.backpressure.RequestStrategy -- # doOnRequest: {1}
```

### Backpressure 전략
종류|설명
---|---
IGNORE 전략|Backpressure를 적용하지 않는다.
ERROR 전략|Downstream으로 전달할 데이터가 버퍼에 가득 찰 경우, Exception을 발생시킨다.
DROP 전략|Downstream으로 전달할 데이터가 버퍼에 가득 찰 경우, 버퍼 밖에서 대기하는 먼저 emit된 데이터부터 Drop시킨다.
LATEST 전략|Downstream으로 전달할 데이터가 버퍼에 가득 찰 경우, 버퍼 밖에서 대기하는 가장 최근에(나중에) emit된 데이터부터 버퍼에 채운다.
BUFFER 전략|Downstream으로 전달할 데이터가 버퍼에 가득 찰 경우, 버퍼 안에 있는 데이터부터 Drop시킨다.

### Error 전략
#### Downstream 으로 전달할 데이터가 버퍼에 가득 찰 경우, Exception 을 발생시킨다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(ErrorStrategy::class.java)

    Flux.interval(Duration.ofMillis(1L))
        .onBackpressureError()
        .doOnNext {
            logger.info("# doOnNext : {$it}")
        }
        .publishOn(Schedulers.parallel())
        .subscribe(
            {
                try {
                    Thread.sleep(5L)
                } catch (e: InterruptedException) {}
                logger.info("# onNext: {$it}")
            },
            {
                logger.error("# onError $it")
            }
        )

    Thread.sleep(2000L)
}
```

```shell
...
13:32:14.811 [parallel-1] INFO com.example.playground.backpressure.ErrorStrategy -- # onNext: {249}
13:32:14.817 [parallel-1] INFO com.example.playground.backpressure.ErrorStrategy -- # onNext: {250}
13:32:14.823 [parallel-1] INFO com.example.playground.backpressure.ErrorStrategy -- # onNext: {251}
13:32:14.829 [parallel-1] INFO com.example.playground.backpressure.ErrorStrategy -- # onNext: {252}
13:32:14.836 [parallel-1] INFO com.example.playground.backpressure.ErrorStrategy -- # onNext: {253}
13:32:14.842 [parallel-1] INFO com.example.playground.backpressure.ErrorStrategy -- # onNext: {254}
13:32:14.847 [parallel-1] INFO com.example.playground.backpressure.ErrorStrategy -- # onNext: {255}
13:32:14.848 [parallel-1] ERROR com.example.playground.backpressure.ErrorStrategy -- # onError reactor.core.Exceptions$OverflowException: The receiver is overrun by more signals than expected (bounded queue...)
```

### DROP 전략
#### Downstream 으로 전달할 데이터가 버퍼에 가득 찰 경우, 버퍼 밖에서 대기하는 먼저 emit 된 데이터부터 Drop 시킨다.
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(DropStrategy::class.java)

    Flux.interval(Duration.ofMillis(1L))
        .onBackpressureDrop { 
            logger.info("# dropped: {$it}")
        }
        .publishOn(Schedulers.parallel())
        .subscribe(
            { 
                try { 
                    Thread.sleep(5L) 
                } catch (e: InterruptedException) {
                    logger.error("# interruptedException: {$e}")
                }
                logger.info("# onNext: {$it}") 
            }, 
            { 
                logger.error("# onError $it") 
            }
        )

    Thread.sleep(2000L)
}
```

```shell
...
13:36:49.556 [parallel-1] INFO com.example.playground.backpressure.DropStrategy -- # onNext: {1244}
13:36:49.556 [parallel-2] INFO com.example.playground.backpressure.DropStrategy -- # dropped: {1992}
13:36:49.557 [parallel-2] INFO com.example.playground.backpressure.DropStrategy -- # dropped: {1993}
13:36:49.558 [parallel-2] INFO com.example.playground.backpressure.DropStrategy -- # dropped: {1994}
13:36:49.559 [parallel-2] INFO com.example.playground.backpressure.DropStrategy -- # dropped: {1995}
13:36:49.560 [parallel-2] INFO com.example.playground.backpressure.DropStrategy -- # dropped: {1996}
13:36:49.561 [parallel-2] INFO com.example.playground.backpressure.DropStrategy -- # dropped: {1997}
13:36:49.561 [parallel-1] INFO com.example.playground.backpressure.DropStrategy -- # onNext: {1245}
13:36:49.562 [parallel-2] INFO com.example.playground.backpressure.DropStrategy -- # dropped: {1998}
13:36:49.563 [parallel-2] INFO com.example.playground.backpressure.DropStrategy -- # dropped: {1999}
```

### LATEST 전략
#### Downstream 으로 전달할 데이터가 버퍼에 가득 찰 경우, 버퍼 밖에서 대기하는 가장 최근에(나중에) emit 된 데이터부터 버퍼에 채운다. 즉, 가장 최근에 emit 된 데이터를 제외한 대기하고 있는 emit 데이터를 모두 지우고 가장 최근에 emit 된 데이터부터 버퍼에 채워 넣는다.
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(LatestStrategy::class.java)

    Flux.interval(Duration.ofMillis(1L))
        .onBackpressureLatest()
        .publishOn(Schedulers.parallel())
        .subscribe(
            { 
                try { 
                    Thread.sleep(5L) 
                } catch (e: InterruptedException) {
                    logger.error("# interruptedException: {$e}")
                }
                logger.info("# onNext: {$it}") 
            }, 
            { 
                logger.error("# onError $it") 
            }
        )

    Thread.sleep(2000L)
}
```

```shell
...
13:40:33.568 [parallel-1] INFO com.example.playground.backpressure.LatestStrategy -- # onNext: {253}
13:40:33.575 [parallel-1] INFO com.example.playground.backpressure.LatestStrategy -- # onNext: {254}
13:40:33.581 [parallel-1] INFO com.example.playground.backpressure.LatestStrategy -- # onNext: {255}
13:40:33.587 [parallel-1] INFO com.example.playground.backpressure.LatestStrategy -- # onNext: {1176}
13:40:33.594 [parallel-1] INFO com.example.playground.backpressure.LatestStrategy -- # onNext: {1177}
13:40:33.599 [parallel-1] INFO com.example.playground.backpressure.LatestStrategy -- # onNext: {1178}
13:40:33.605 [parallel-1] INFO com.example.playground.backpressure.LatestStrategy -- # onNext: {1179}
...
```

Subscriber 숫자가 '255' 에서 '1176' 으로 바로 출력되는 것을 확인할 수 있다.

### BUFFER DROP_LATEST 전략
#### Publisher 가 Downstream 으로 전달할 데이터가 버퍼에 가득 찰 경우, 가장 최근에(나중에) 버퍼 안에 채워진 데이터를 Drop 하여 폐기한 후, 이렇게 확보된 공간에 emit 된 데이터를 채우는 전략이다.
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(BufferDropLatestStrategy::class.java)

    Flux.interval(Duration.ofMillis(300L))
        .doOnNext {
            logger.info("# emitted by original Flux: {$it}")
        }
        .onBackpressureBuffer(
            2,
            {
                logger.info("** Overflow & Dropped: {$it} **", )
            },
            BufferOverflowStrategy.DROP_LATEST,
        )
        .doOnNext {
            logger.info("[ # emitted by Buffer: {$it} ]")
        }
        .publishOn(Schedulers.parallel(), false, 1)
        .subscribe(
            {
                try {
                    Thread.sleep(1000L)
                } catch (e: InterruptedException) {
                    logger.error("# interruptedException: {$e}")
                }
                logger.info("# onNext: {$it}")
            },
            {
                logger.error("# onError $it")
            }
        )

    Thread.sleep(2500L)
}
```

```shell
13:59:18.867 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # emitted by original Flux: {0}
13:59:18.869 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- [ # emitted by Buffer: {0} ]
13:59:19.165 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # emitted by original Flux: {1}
13:59:19.465 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # emitted by original Flux: {2}
13:59:19.765 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # emitted by original Flux: {3}
13:59:19.766 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- ** Overflow & Dropped: {3} **
13:59:19.876 [parallel-1] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # onNext: {0}
13:59:19.879 [parallel-1] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- [ # emitted by Buffer: {1} ]
13:59:20.061 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # emitted by original Flux: {4}
13:59:20.361 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # emitted by original Flux: {5}
13:59:20.361 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- ** Overflow & Dropped: {5} **
13:59:20.665 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # emitted by original Flux: {6}
13:59:20.665 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- ** Overflow & Dropped: {6} **
13:59:20.882 [parallel-1] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # onNext: {1}
13:59:20.882 [parallel-1] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- [ # emitted by Buffer: {2} ]
13:59:20.966 [parallel-2] INFO com.example.playground.backpressure.BufferDropLatestStrategy -- # emitted by original Flux: {7}
```

### BUFFER DROP OLDEST 전략
#### Publisher 가 Downstream 으로 전달할 데이터가 버퍼에 가득 찰 경우, 버퍼 안에 채워진 데이터 중에서 가장 오래된 데이터를 Drop 하여 폐기한 후, 확보된 공간에 emit 된 데이터를 채우는 전략이다.
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(BufferDropOldestStrategy::class.java)

    Flux.interval(Duration.ofMillis(300L))
        .doOnNext {
            logger.info("# emitted by original Flux: {$it}")
        }
        .onBackpressureBuffer(2,
            {
                logger.info("** Overflow & Dropped: {$it} **")
            },
            BufferOverflowStrategy.DROP_OLDEST,
        )
        .doOnNext {
            logger.info("[ # emitted by Buffer: {$it} ]")
        }
        .publishOn(Schedulers.parallel(), false, 1)
        .subscribe(
            {
                try {
                    Thread.sleep(1000L)
                } catch (e: InterruptedException) {
                    logger.error("# interruptedException: {$e}")
                }
                logger.info("# onNext: {$it}")
            }, {
                logger.error("# onError $it")
            }
        )

    Thread.sleep(2500L)
}
```

```shell
14:03:30.976 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # emitted by original Flux: {0}
14:03:30.978 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- [ # emitted by Buffer: {0} ]
14:03:31.279 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # emitted by original Flux: {1}
14:03:31.579 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # emitted by original Flux: {2}
14:03:31.875 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # emitted by original Flux: {3}
14:03:31.875 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- ** Overflow & Dropped: {1} **
14:03:31.983 [parallel-1] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # onNext: {0}
14:03:31.983 [parallel-1] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- [ # emitted by Buffer: {2} ]
14:03:32.176 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # emitted by original Flux: {4}
14:03:32.479 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # emitted by original Flux: {5}
14:03:32.479 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- ** Overflow & Dropped: {3} **
14:03:32.775 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # emitted by original Flux: {6}
14:03:32.775 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- ** Overflow & Dropped: {4} **
14:03:32.984 [parallel-1] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # onNext: {2}
14:03:32.984 [parallel-1] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- [ # emitted by Buffer: {5} ]
14:03:33.079 [parallel-2] INFO com.example.playground.backpressure.BufferDropOldestStrategy -- # emitted by original Flux: {7}
```