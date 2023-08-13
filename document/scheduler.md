### Scheduler

* Reactor Sequence 에서 사용되는 스레드를 관리해주는 관리자 역할을 한다.
* 즉, 어떤 스레드에서 무엇을 처리할 지 제어한다.
* Reactor 에서는 Scheduler 를 통해 스레드 간의 Race Condition 등의 문제를 최소화하고, 코드가 매우 간결해지며, Scheduler 가 스레드의 제어를 대신해 주기 때문에 개발자가 직접 스레드를 제어해야 하는 부담에서 벗어날 수 있다.

### subscribeOn()
* 구독이 발생한 직후 실행될 스레드를 지정하는 Operator
* 구독이 발생하면 원본 Publisher 가 데이터를 최초로 emit 하게 되는데, 해당 Operator 는 구독 시점 직후에 실행되기 때문에 원본 Publisher 의 동작을 수행하기 위한 스레드이다.
* doOnSubscribe는 구독이 시작될 때 호출되므로, 구독이 시작되자마자 호출된다. (최초 실행 스레드가 main 이기 때문에 main 에서 실행됨)

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(SubscribeOn::class.java)

    Flux.fromArray(arrayOf(1, 3, 5, 7))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext {
                logger.info("$ doOnNext: {$it}")
            }
            .doOnSubscribe {
                logger.info("# doOnSubscribe")
            }
            .subscribe {
                logger.info("# onNext: {$it}")
            }

    Thread.sleep(500)
}
```

```shell
10:49:07.483 [main] INFO com.example.hyjdemo.scheduler.SubscribeOn -- # doOnSubscribe
10:49:07.489 [boundedElastic-1] INFO com.example.hyjdemo.scheduler.SubscribeOn -- $ doOnNext: {1}
10:49:07.489 [boundedElastic-1] INFO com.example.hyjdemo.scheduler.SubscribeOn -- # onNext: {1}
10:49:07.489 [boundedElastic-1] INFO com.example.hyjdemo.scheduler.SubscribeOn -- $ doOnNext: {3}
10:49:07.489 [boundedElastic-1] INFO com.example.hyjdemo.scheduler.SubscribeOn -- # onNext: {3}
10:49:07.489 [boundedElastic-1] INFO com.example.hyjdemo.scheduler.SubscribeOn -- $ doOnNext: {5}
10:49:07.489 [boundedElastic-1] INFO com.example.hyjdemo.scheduler.SubscribeOn -- # onNext: {5}
10:49:07.489 [boundedElastic-1] INFO com.example.hyjdemo.scheduler.SubscribeOn -- $ doOnNext: {7}
10:49:07.489 [boundedElastic-1] INFO com.example.hyjdemo.scheduler.SubscribeOn -- # onNext: {7}
```

### publishOn()
* publishOn() Operator 를 기준으로 Downstream 의 실행 스레드를 변경한다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(PublishOn::class.java)

    Flux.fromArray(arrayOf(1, 3, 5, 7))
            .doOnNext {
                logger.info("# doOnNext: {$it}")
            }
            .doOnSubscribe {
                logger.info("doOnSubscribe")
            }
            .publishOn(Schedulers.parallel())
            .subscribe {
                logger.info("# onNext: {$it}")
            }

    Thread.sleep(500L)
}
```

```shell
10:53:18.138 [main] INFO com.example.hyjdemo.scheduler.PublishOn -- doOnSubscribe
10:53:18.149 [main] INFO com.example.hyjdemo.scheduler.PublishOn -- # doOnNext: {1}
10:53:18.149 [main] INFO com.example.hyjdemo.scheduler.PublishOn -- # doOnNext: {3}
10:53:18.149 [main] INFO com.example.hyjdemo.scheduler.PublishOn -- # doOnNext: {5}
10:53:18.149 [main] INFO com.example.hyjdemo.scheduler.PublishOn -- # doOnNext: {7}
10:53:18.150 [parallel-1] INFO com.example.hyjdemo.scheduler.PublishOn -- # onNext: {1}
10:53:18.150 [parallel-1] INFO com.example.hyjdemo.scheduler.PublishOn -- # onNext: {3}
10:53:18.150 [parallel-1] INFO com.example.hyjdemo.scheduler.PublishOn -- # onNext: {5}
10:53:18.150 [parallel-1] INFO com.example.hyjdemo.scheduler.PublishOn -- # onNext: {7}
```

### parallel()
* parallel() Operator 의 경우에는 병렬성을 가진 물리적인 스레드에 해당한다.
* parallel() 의 경우 라운드 로빈 방식으로 CPU 코어 개수만큼 스레드를 병렬로 실행한다.
* 여기서 CPU 코어 개수는 물리적인 코어 개수가 아니라 논리적인 코어(스레드)의 개수를 의미한다. ex. 4코어 8스레드 -> 8스레드를 사용하는 것
* parallel() Operator 는 emit 되는 데이터를 CPU 의 논리적인 코어 수에 맞게 골고루 분배하는 역할 (라운드 로빈)
* 실제로 병렬 작업을 수행할 스레드의 할당은 runOn() Operator 가 담당한다.
* 만약 스레드의 개수를 지정해주고싶다면 parallel() Operator 에 개수를 지정해주면 된다. (아래 주석에서 확인)

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(Parallel::class.java)

    Flux.fromArray(arrayOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19))
            .parallel()
//            .parallel(4)
            .runOn(Schedulers.parallel())
            .subscribe {
                logger.info("# onNext: {$it}")
            }

    Thread.sleep(500L)
}
```

```shell
10:57:49.568 [parallel-6] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {11}
10:57:49.568 [parallel-4] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {7}
10:57:49.568 [parallel-5] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {9}
10:57:49.568 [parallel-8] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {15}
10:57:49.568 [parallel-3] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {5}
10:57:49.568 [parallel-1] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {1}
10:57:49.568 [parallel-2] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {3}
10:57:49.568 [parallel-7] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {13}
10:57:49.575 [parallel-1] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {17}
10:57:49.575 [parallel-2] INFO com.example.hyjdemo.scheduler.Parallel -- # onNext: {19}
```

### publishOn() 과 subscribeOn 을 사용 안했을 때 스레드의 동작 과정
Operator|Thread
:---:|:---:
Flux.fromArray(...)|main thread
&downarrow;|-
.filter(...)|main thread
&downarrow;|-
.map(...)|main thread
&downarrow;|-
.subscribe(...)|main thread

### 하나의 publishOn() 만 사용했을 경우
Operator|Thread
:---:|:---:
Flux.fromArray(...)|main thread
&downarrow;|-
.publishOn(Schedulers.parallel())|-
&downarrow;|-
.filter(...)|A thread
&downarrow;|-
.map(...)|A thread
&downarrow;|-
.subscribe(...)|A thread

### publishOn 을 두 번 사용할 경우
Operator|Thread
:---:|:---:
Flux.fromArray(...)|main thread
&downarrow;|-
.publishOn(Schedulers.parallel())|-
&downarrow;|-
.filter(...)|A thread
&downarrow;|-
.publishOn(Schedulers.parallel())|-
&downarrow;|-
.map(...)|B thread
&downarrow;|-
.subscribe(...)|B thread

### subscribeOn() 과 publishOn() 을 함께 사용할 경우
Operator|Thread
:---:|:---:
Flux.fromArray(...)|A thread
&downarrow;|-
.subscribeOn(Schedulers.boundedElastic())|-
&downarrow;|-
.filter(...)|A thread
&downarrow;|-
.publishOn(Schedulers.parallel())|-
&downarrow;|-
.map(...)|B thread
&downarrow;|-
.subscribe(...)|B thread

### publishOn() 과 subscribeOn() 의 특징
* publishOn() Operator 는 한 개 이상 사용할 수 있으며, 실해 스레드를 목적에 맞게 적절하게 분리할 수 있다.
* subscribe() Operator 와 publishOn() Operator 를 함께 사용해서 원본 Publisher 에서 데이터를 emit 하는 스레드와 emit 된 데이터를 가공 처리하는 스레드를 적절하게 분리할 수 있다.
* subscribeOn() 은 Operator 체인상에서 어떤 위치에 있든 간에 구독 시점 직후, 즉 Publisher 가 데이터를 emit 하기 전에 실행 스레드를 변경한다.

### Schedulers.immediate()
* 별도의 스레드를 추가적으로 생성하지 않고, 현재 스레드에서 작업을 처리하고자 할 때 사용

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(SchedulersImmediate::class.java)

    Flux.fromArray(arrayOf(1, 3, 5, 7))
            .publishOn(Schedulers.parallel())
            .filter { it > 3 }
            .doOnNext {
                logger.info("# doOnNext filter: {$it}")
            }
            .publishOn(Schedulers.immediate())
            .map { it * 10 }
            .doOnNext {
                logger.info("# doOnNext map: {$it}")
            }
            .subscribe {
                logger.info("# onNext: {$it}")
            }

    Thread.sleep(200L)
}
```

```shell
11:16:06.883 [parallel-1] INFO com.example.hyjdemo.scheduler.SchedulersImmediate -- # doOnNext filter: {5}
11:16:06.887 [parallel-1] INFO com.example.hyjdemo.scheduler.SchedulersImmediate -- # doOnNext map: {50}
11:16:06.887 [parallel-1] INFO com.example.hyjdemo.scheduler.SchedulersImmediate -- # onNext: {50}
11:16:06.887 [parallel-1] INFO com.example.hyjdemo.scheduler.SchedulersImmediate -- # doOnNext filter: {7}
11:16:06.887 [parallel-1] INFO com.example.hyjdemo.scheduler.SchedulersImmediate -- # doOnNext map: {70}
11:16:06.887 [parallel-1] INFO com.example.hyjdemo.scheduler.SchedulersImmediate -- # onNext: {70}
```

### Schedulers.single()
* 스레드 하나만 생성해서 Scheduler가 제거되기 전까지 재사용하는 방식

```kotlin
class SchedulersSingle {
    companion object {
        fun doTask(taskName: String, logger: Logger): Flux<Int> {
            return Flux.fromArray(arrayOf(1, 3, 5, 7))
                    .publishOn(Schedulers.single())
                    .filter { it > 3 }
                    .doOnNext {
                        logger.info("# doOnNext filter: {$it}")
                    }
                    .publishOn(Schedulers.immediate())
                    .map { it * 10 }
                    .doOnNext {
                        logger.info("# doOnNext map: {$it}")
                    }
        }
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(SchedulersSingle::class.java)

    SchedulersSingle.doTask("task1", logger)
            .subscribe {
                logger.info("# onNext: {$it}")
            }

    SchedulersSingle.doTask("task2", logger)
            .subscribe {
                logger.info("# onNext: {$it}")
            }

    Thread.sleep(200L)
}
```

```shell
11:20:37.118 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # doOnNext filter: {5}
11:20:37.124 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # doOnNext map: {50}
11:20:37.125 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # onNext: {50}
11:20:37.125 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # doOnNext filter: {7}
11:20:37.125 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # doOnNext map: {70}
11:20:37.125 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # onNext: {70}
11:20:37.126 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # doOnNext filter: {5}
11:20:37.126 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # doOnNext map: {50}
11:20:37.127 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # onNext: {50}
11:20:37.127 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # doOnNext filter: {7}
11:20:37.127 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # doOnNext map: {70}
11:20:37.127 [single-1] INFO com.example.hyjdemo.scheduler.SchedulersSingle -- # onNext: {70}
```

### Schedulers.newSingle()
* 호출할 때마다 매번 새로운 스레드 하나를 생성합니다.

```kotlin
class SchedulersNewSingle {
    companion object {
        fun doTask(taskName: String, logger: Logger): Flux<Int> {
            return Flux.fromArray(arrayOf(1, 3, 5, 7))
                    .publishOn(Schedulers.newSingle("new-single", true))
                    .filter { it > 3 }
                    .doOnNext {
                        logger.info("# doOnNext filter: {$it}")
                    }
                    .publishOn(Schedulers.immediate())
                    .map { it * 10 }
                    .doOnNext {
                        logger.info("# doOnNext map: {$it}")
                    }
        }
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(SchedulersNewSingle::class.java)

    SchedulersNewSingle.doTask("task1", logger)
            .subscribe {
                logger.info("# onNext: {$it}")
            }

    SchedulersNewSingle.doTask("task2", logger)
            .subscribe {
                logger.info("# onNext: {$it}")
            }

    Thread.sleep(200L)
}
```

```shell
12:06:41.172 [new-single-2] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # doOnNext filter: {5}
12:06:41.172 [new-single-1] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # doOnNext filter: {5}
12:06:41.177 [new-single-2] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # doOnNext map: {50}
12:06:41.177 [new-single-1] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # doOnNext map: {50}
12:06:41.177 [new-single-2] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # onNext: {50}
12:06:41.177 [new-single-1] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # onNext: {50}
12:06:41.177 [new-single-2] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # doOnNext filter: {7}
12:06:41.177 [new-single-1] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # doOnNext filter: {7}
12:06:41.177 [new-single-2] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # doOnNext map: {70}
12:06:41.177 [new-single-1] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # doOnNext map: {70}
12:06:41.177 [new-single-2] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # onNext: {70}
12:06:41.177 [new-single-1] INFO com.example.hyjdemo.scheduler.SchedulersNewSingle -- # onNext: {70}
```

### Schedulers.boundedElastic()
* ExecutorService 기반의 스레드 풀을 생성한 후, 그 안에서 정해진 수만큼의 스레드를 사용하여 작업을 처리하고 작업이 종료된 스레드는 반납하여 재사용하는 방식이다.
* 기본적으로 CPU 코어 수 x 10만큼의 스레드를 생성하며, 풀에 있는 모든 스레드가 작업을 처리하고 있다면 이용 가능한 스레드가 생길 때까지 최대 100,000개의 작업이 큐에서 대기할 수 있다.
* 즉, 실행 시간이 긴 Blocking I/O 작업이 포함된 경우, 다른 Non-Blocking 처리에 영향을 주지 않도록 전용 스레드를 할당해서, Blocking I/O 작업을 처리하기 때문에 처리 시간을 효율적으로 사용할 수 있다.

### Schedulers.parallel()
* Non-Blocking I/O 에 최적화되어 있는 Scheduler 로서 CPU 코어 개수만큼의 스레드를 생성한다.