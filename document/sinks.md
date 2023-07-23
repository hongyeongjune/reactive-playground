### Sinks
* 일반적으로 generate() Operator 나 create() Operator 는 싱글 스레드 기반에서 signal 을 전송하는 데 사용하는 반면, Sinks 는 멀티스레드 방식으로 Signal 을 전송해도 스레드 안정성을 보장하기 때문에 예기치 않는 동작으로 이어지는 것을 방지해 줍니다.


### create() Operator 예시
```kotlin
class CreateOperator {
    companion object {
        fun doTasks(taskNumber: Int): String {
            return "task $taskNumber result"
        }
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(CreateOperator::class.java)

    val tasks = 6
    Flux.create {
        IntStream.range(1, tasks)
            .forEach { n ->
                it.next(
                    CreateOperator.doTasks(n),
                )
            }
    }
        .subscribeOn(Schedulers.boundedElastic())
        .doOnNext {
            logger.info("# creator(): $it")
        }
        .publishOn(Schedulers.parallel())
        .map { "$it success!" }
        .doOnNext {
            logger.info("# map(): {$it}")
        }
        .publishOn(Schedulers.parallel())
        .subscribe {
            logger.info("# onNext: {$it}")
        }

    Thread.sleep(500L)
}
```

```shell
15:22:35.019 [boundedElastic-1] INFO com.example.playground.sinks.CreateOperator -- # creator(): task 1 result
15:22:35.020 [boundedElastic-1] INFO com.example.playground.sinks.CreateOperator -- # creator(): task 2 result
15:22:35.020 [boundedElastic-1] INFO com.example.playground.sinks.CreateOperator -- # creator(): task 3 result
15:22:35.021 [boundedElastic-1] INFO com.example.playground.sinks.CreateOperator -- # creator(): task 4 result
15:22:35.021 [boundedElastic-1] INFO com.example.playground.sinks.CreateOperator -- # creator(): task 5 result
15:22:35.021 [parallel-2] INFO com.example.playground.sinks.CreateOperator -- # map(): {task 1 result success!}
15:22:35.021 [parallel-2] INFO com.example.playground.sinks.CreateOperator -- # map(): {task 2 result success!}
15:22:35.021 [parallel-2] INFO com.example.playground.sinks.CreateOperator -- # map(): {task 3 result success!}
15:22:35.021 [parallel-2] INFO com.example.playground.sinks.CreateOperator -- # map(): {task 4 result success!}
15:22:35.021 [parallel-2] INFO com.example.playground.sinks.CreateOperator -- # map(): {task 5 result success!}
15:22:35.021 [parallel-1] INFO com.example.playground.sinks.CreateOperator -- # onNext: {task 1 result success!}
15:22:35.021 [parallel-1] INFO com.example.playground.sinks.CreateOperator -- # onNext: {task 2 result success!}
15:22:35.021 [parallel-1] INFO com.example.playground.sinks.CreateOperator -- # onNext: {task 3 result success!}
15:22:35.021 [parallel-1] INFO com.example.playground.sinks.CreateOperator -- # onNext: {task 4 result success!}
15:22:35.021 [parallel-1] INFO com.example.playground.sinks.CreateOperator -- # onNext: {task 5 result success!}
```

* 3개의 스레드가 동시에 실행된 것을 알 수 있다.
* doTask() 메서드가 싱글스레드가 아닌 여러 개의 스레드에서 각각의 전혀 다른 작업들을 처리한 후, 처리 결과를 반환하는 상황이 발생한다면, Sinks 를 사용해야한다.

### Sinks Operator 예시
```kotlin
class SinkOperator {
    companion object {
        fun doTasks(taskNumber: Int): String {
            return "task $taskNumber result"
        }
    }
}

fun main() {
    val logger = LoggerFactory.getLogger(SinkOperator::class.java)

    val tasks = 6
    val unicastSink = Sinks.many().unicast().onBackpressureBuffer<String>()
    val fluxView = unicastSink.asFlux()
    IntStream.range(1, tasks)
        .forEach {
            try {
                Thread {
                    unicastSink.emitNext(
                        SinkOperator.doTasks(it),
                        Sinks.EmitFailureHandler.FAIL_FAST,
                    )
                    logger.info("# emitted: {$it}")
                }.start()
                Thread.sleep(100L)
            } catch (e: InterruptedException) {
                logger.error(e.message)
            }
        }


    fluxView.publishOn(Schedulers.parallel())
        .map { "$it success!" }
        .doOnNext {
            logger.info("# map(): {$it}")
        }
        .publishOn(Schedulers.parallel())
        .subscribe {
            logger.info("# onNext: {$it}")
        }

    Thread.sleep(500L)
}
```

```shell
15:22:52.135 [Thread-0] INFO com.example.playground.sinks.SinkOperator -- # emitted: {1}
15:22:52.236 [Thread-1] INFO com.example.playground.sinks.SinkOperator -- # emitted: {2}
15:22:52.338 [Thread-2] INFO com.example.playground.sinks.SinkOperator -- # emitted: {3}
15:22:52.438 [Thread-3] INFO com.example.playground.sinks.SinkOperator -- # emitted: {4}
15:22:52.544 [Thread-4] INFO com.example.playground.sinks.SinkOperator -- # emitted: {5}
15:22:52.675 [parallel-2] INFO com.example.playground.sinks.SinkOperator -- # map(): {task 1 result success!}
15:22:52.675 [parallel-2] INFO com.example.playground.sinks.SinkOperator -- # map(): {task 2 result success!}
15:22:52.675 [parallel-2] INFO com.example.playground.sinks.SinkOperator -- # map(): {task 3 result success!}
15:22:52.675 [parallel-2] INFO com.example.playground.sinks.SinkOperator -- # map(): {task 4 result success!}
15:22:52.675 [parallel-2] INFO com.example.playground.sinks.SinkOperator -- # map(): {task 5 result success!}
15:22:52.675 [parallel-1] INFO com.example.playground.sinks.SinkOperator -- # onNext: {task 1 result success!}
15:22:52.675 [parallel-1] INFO com.example.playground.sinks.SinkOperator -- # onNext: {task 2 result success!}
15:22:52.675 [parallel-1] INFO com.example.playground.sinks.SinkOperator -- # onNext: {task 3 result success!}
15:22:52.675 [parallel-1] INFO com.example.playground.sinks.SinkOperator -- # onNext: {task 4 result success!}
15:22:52.675 [parallel-1] INFO com.example.playground.sinks.SinkOperator -- # onNext: {task 5 result success!}
```

* doTask() 메서드가 돌때마다 Thread 가 새로 생성되므로 스레드 안정성을 보장할 수 있다.

### Sinks.One
```java
public final class Sinks {
	/**
	 * A {@link Sinks.One} that works like a conceptual promise: it can be completed
	 * with or without a value at any time, but only once. This completion is replayed to late subscribers.
	 * Calling {@link One#tryEmitValue(Object)} (or {@link One#emitValue(Object, Sinks.EmitFailureHandler)}) is enough and will
	 * implicitly produce a {@link Subscriber#onComplete()} signal as well.
	 * <p>
	 * Use {@link One#asMono()} to expose the {@link Mono} view of the sink to downstream consumers.
	 *
	 * @return a new {@link Sinks.One}
	 * @see RootSpec#one()
	 */
	public static <T> Sinks.One<T> one() {
        return SinksSpecs.DEFAULT_SINKS.one();
    }
}
```

```kotlin
class SinkOneOperator

fun main() {
    val logger = LoggerFactory.getLogger(SinkOneOperator::class.java)

   val sinkOne = Sinks.one<String>()
    val mono = sinkOne.asMono()

    sinkOne.emitValue("Hello Reactor", Sinks.EmitFailureHandler.FAIL_FAST)
//    sinkOne.emitValue("Error Reactor", Sinks.EmitFailureHandler.FAIL_FAST)

    mono.subscribe {
        logger.info("# subscriber1 : $it")
    }
    mono.subscribe {
        logger.info("# subscriber2 : $it")
    }
}
```

```shell
15:26:27.580 [main] INFO com.example.playground.sinks.SinkOneOperator -- # subscriber1 : Hello Reactor
15:26:27.581 [main] INFO com.example.playground.sinks.SinkOneOperator -- # subscriber2 : Hello Reactor
```

* ```Sinks.EmitFailureHandler.FAIL_FAST``` 이 부분은 emit 도중 발생한 에러에 대해 빠르게 실패 처리가 된다. 즉, 에러가 발생했을 때 재시도를 하지 않고 즉시 실패 처리를 하게된다. 이렇게 하면, 스레드 간의 경합 등으로 발생하는 교착 상태 등을 미연에 방지할 수 있는데, 이는 결과적으로 스레드의 안정성을 보장하기 위함이다.


* ```sinkOne.emitValue("Error Reactor", Sinks.EmitFailureHandler.FAIL_FAST)``` 위 코드에서 주석을 해제하면 아래 코드에서 ```onNextDropped``` 로그가 출력된다.
* 즉, 아무리 많은 수의 데이터를 emit 한다고 하더라도 처음 emit 한 데이터는 emit 되지만, 나머지 데이터는 모두 drop 된다.
```java
public abstract class Operators {
	/**
	 * An unexpected event is about to be dropped.
	 * <p>
	 * If no hook is registered for {@link Hooks#onNextDropped(Consumer)}, the dropped
	 * element is just logged at DEBUG level.
	 *
	 * @param <T> the dropped value type
	 * @param t the dropped data
	 * @param context a context that might hold a local next consumer
	 */
	public static <T> void onNextDropped(T t, Context context) {
		Objects.requireNonNull(t, "onNext");
		Objects.requireNonNull(context, "context");
		Consumer<Object> hook = context.getOrDefault(Hooks.KEY_ON_NEXT_DROPPED, null);
		if (hook == null) {
			hook = Hooks.onNextDroppedHook;
		}
		if (hook != null) {
			hook.accept(t);
		}
		else if (log.isDebugEnabled()) {
			log.debug("onNextDropped: " + t);
		}
	}
}
```

### Sinks.Many
```java
public final class Sinks {
    /**
     * Help building {@link Sinks.Many} sinks that will broadcast multiple signals to one or more {@link Subscriber}.
     * <p>
     * Use {@link Many#asFlux()} to expose the {@link Flux} view of the sink to the downstream consumers.
     *
     * @return {@link ManySpec}
     * @see RootSpec#many()
     */
    public static ManySpec many() {
        return SinksSpecs.DEFAULT_SINKS.many();
    }

    /**
     * Provides {@link Sinks.Many} specs for sinks which can emit multiple elements
     */
    public interface ManySpec {
        /**
         * Help building {@link Sinks.Many} that will broadcast signals to a single {@link Subscriber}
         *
         * @return {@link UnicastSpec}
         */
        UnicastSpec unicast();

        /**
         * Help building {@link Sinks.Many} that will broadcast signals to multiple {@link Subscriber}
         *
         * @return {@link MulticastSpec}
         */
        MulticastSpec multicast();

        /**
         * Help building {@link Sinks.Many} that will broadcast signals to multiple {@link Subscriber} with the ability to retain
         * and replay all or an arbitrary number of elements.
         *
         * @return {@link MulticastReplaySpec}
         */
        MulticastReplaySpec replay();
    }
}
```
* ManySpec 은 총 3가지 기능을 제공한다.

### Unicast
```kotlin
class SinkManyUnicastOperator

fun main() {
    val logger = LoggerFactory.getLogger(SinkManyUnicastOperator::class.java)

    val unicastSink = Sinks.many().unicast().onBackpressureBuffer<Int>()
    val fluxView = unicastSink.asFlux()

    unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
    unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe1: {$it}")
    }

    unicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)

//    fluxView.subscribe {
//        logger.info("# subscribe2: {$it}")
//    }
}
```

```shell
15:32:44.143 [main] INFO com.example.playground.sinks.SinkManyUnicastOperator -- # subscribe1: {1}
15:32:44.144 [main] INFO com.example.playground.sinks.SinkManyUnicastOperator -- # subscribe1: {2}
15:32:44.144 [main] INFO com.example.playground.sinks.SinkManyUnicastOperator -- # subscribe1: {3}
```

* unicast 는 단 하나의 subscriber 에게만 전달하기 때문에 위 코드에서 주석처리를 해제하면 에러가 발생한다.

```shell
15:33:38.552 [main] INFO com.example.playground.sinks.SinkManyUnicastOperator -- # subscribe1: {1}
15:33:38.554 [main] INFO com.example.playground.sinks.SinkManyUnicastOperator -- # subscribe1: {2}
15:33:38.554 [main] INFO com.example.playground.sinks.SinkManyUnicastOperator -- # subscribe1: {3}
15:33:38.556 [main] ERROR reactor.core.publisher.Operators -- Operator called default onErrorDropped
reactor.core.Exceptions$ErrorCallbackNotImplemented: java.lang.IllegalStateException: Sinks.many().unicast() sinks only allow a single Subscriber
Caused by: java.lang.IllegalStateException: Sinks.many().unicast() sinks only allow a single Subscriber
	at reactor.core.publisher.SinkManyUnicast.subscribe(SinkManyUnicast.java:422)
	at reactor.core.publisher.Flux.subscribe(Flux.java:8671)
	at reactor.core.publisher.Flux.subscribeWith(Flux.java:8792)
	at reactor.core.publisher.Flux.subscribe(Flux.java:8637)
	at reactor.core.publisher.Flux.subscribe(Flux.java:8561)
	at reactor.core.publisher.Flux.subscribe(Flux.java:8504)
	at com.example.playground.sinks.SinkManyUnicastOperatorKt.main(SinkManyUnicastOperator.kt:23)
	at com.example.playground.sinks.SinkManyUnicastOperatorKt.main(SinkManyUnicastOperator.kt)
```

### Mutlicast
```kotlin
class SinkManyMulticastOperator

fun main() {
    val logger = LoggerFactory.getLogger(SinkManyMulticastOperator::class.java)

    val multicastSink = Sinks.many().multicast().onBackpressureBuffer<Int>()
    val fluxView = multicastSink.asFlux()

    multicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
    multicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe1: {$it}")
    }

    fluxView.subscribe {
        logger.info("# subscribe2: {$it}")
    }

    multicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)

}
```

```shell
15:34:11.337 [main] INFO com.example.playground.sinks.SinkManyMulticastOperator -- # subscribe1: {1}
15:34:11.338 [main] INFO com.example.playground.sinks.SinkManyMulticastOperator -- # subscribe1: {2}
15:34:11.338 [main] INFO com.example.playground.sinks.SinkManyMulticastOperator -- # subscribe1: {3}
15:34:11.338 [main] INFO com.example.playground.sinks.SinkManyMulticastOperator -- # subscribe2: {3}
```

* Sinks 는 기본적으로 Hot Publisher 로 등록되어 동작하며 특히, ```onBackpressureBuffer()``` 메서드는 Warm up 의 특징을 가지고 있다.
  * Warm up : 최초 구독이 발생하기 전까지 소비 X

### Replay
```kotlin
class SinkManyReplayOperator

fun main() {
    val logger = LoggerFactory.getLogger(SinkManyReplayOperator::class.java)

    val replaySink = Sinks.many().replay().limit<Int>(2)
    val fluxView = replaySink.asFlux()

    replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST)
    replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST)
    replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe1: {$it}")
    }

    replaySink.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST)

    fluxView.subscribe {
        logger.info("# subscribe2: {$it}")
    }
}
```

```shell
15:35:52.630 [main] INFO com.example.playground.sinks.SinkManyReplayOperator -- # subscribe1: {2}
15:35:52.631 [main] INFO com.example.playground.sinks.SinkManyReplayOperator -- # subscribe1: {3}
15:35:52.631 [main] INFO com.example.playground.sinks.SinkManyReplayOperator -- # subscribe1: {4}
15:35:52.632 [main] INFO com.example.playground.sinks.SinkManyReplayOperator -- # subscribe2: {3}
15:35:52.632 [main] INFO com.example.playground.sinks.SinkManyReplayOperator -- # subscribe2: {4}
```

* replay 는 emit 된 데이터를 다시 replay 해서 구독 전에 이미 emit 된 데이터라도 Subscriber 가 전달받을 수 있게 하는 다양한 메서드들이 정의되어있다.
* limit 은 emit 된 데이터 중에서 파라미터로 입력한 개수만큼 가장 나중에 emit 된 데이터부터 Subscriber 에게 전달한다.
* 따라서, subscribe1 은 이미 세 개의 데이터가 emit 되었기 때문에 마지막 2개를 뒤로 되돌린 2부터 출력이 된다.
* subscribe2 는 4가 추가로 emit 되었기 때문에 2개 뒤로 되돌린 3부터 출력이 된다.