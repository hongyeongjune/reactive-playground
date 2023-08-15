### Context
* Context 는 어떠한 상황에서 그 상황을 처리하기 위해 필요한 정보
* ServletContext : Servlet 이 Servlet Container 와 통신하기 위해서 필요한 정보를 제공하는 인터페이스
* ApplicationContext : SpringFramework 에서 애플리케이션 정보를 제공하는 인터페이스
* SecurityContext : Spring Security 에서 애플리케이션 사용자의 인증 정보를 제공하는 인터페이스

### 정의
* Operator 같은 Reactor 구성요소 간에 전파되는 key/value 형태의 저장소
* Downstream -> Upstream 으로 전파되어 Operator 체인 상 각 Operator 가 해당 Context 정보를 동일하게 이용할 수 있다.
* 각각 실행 스레드와 매핑되는 ThreadLocal 과 유사하지만, Reactor Context 는 **Subscriber** 와 매핑된다.
* 즉, 구독이 발생할 때마다 해당 구독과 연결된 하나의 Context 가 생긴다고 생각하면 된다.

### Example
```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(SimpleContext::class.java)

    Mono.deferContextual {contextView ->
        Mono.just("Hello ${contextView.get<String>("firstName")}")
            .doOnNext {
                logger.info("# just doOnNext : {$it}")
            }
    }
        .subscribeOn(Schedulers.boundedElastic())
        .publishOn(Schedulers.parallel())
        .transformDeferredContextual { mono, contextView ->
            mono.map { "$it ${contextView.get<String>("lastName")}" }
        }
        .contextWrite {
            it.put("lastName", "Jobs")
        }
        .contextWrite {
            it.put("firstName", "Steve")
        }
        .subscribe {
            logger.info("# onNext : {$it}")
        }

    Thread.sleep(100L)
}
```

```shell
11:08:26.523 [boundedElastic-1] INFO com.example.playground.context.SimpleContext -- # just doOnNext : {Hello Steve}
11:08:26.528 [parallel-1] INFO com.example.playground.context.SimpleContext -- # onNext : {Hello Steve Jobs}
```

### contextWrite() 내부
```java
public abstract class Mono<T> implements CorePublisher<T> {
    public final Mono<T> contextWrite(Function<Context, Context> contextModifier) {
		if (ContextPropagation.shouldPropagateContextToThreadLocals()) {
			return onAssembly(new MonoContextWriteRestoringThreadLocals<>(
					this, contextModifier
			));
		}
		return onAssembly(new MonoContextWrite<>(this, contextModifier));
	}
}
```

### 컨텍스트에 데이터 쓰기
* contextWrite() Operator 를 통해서 Context 에 데이터를 쓸 수 있다.

### 컨텍스트에 데이터 읽기
* 데이터 읽기 방식 
  * 원본 데이터 소스 레벨에서 데이터 읽기 : deferContextual()
  * Operator 체인 중간에서 데이터 읽기 : transformDeferredContextual()
* Context 에 데이터를 쓸 때는 Context 를 사용하지만, 저장된 데이터를 읽을 때는 ContextView 를 사용한다.
* Reactor 에서는 Operator 체인상의 서로 다른 스레드들이 Context의 저장된 데이터에 손쉽게 접근할 수 있다.
* context.put() 을 통해 Context 에 데이터를 쓴 후에 매번 불변 객체를 다음 contextWrite() 에 전달함으로써 스레드 안정성 또한 보장한다.

### 자주 사용되는 Context API
Context API|설명
---|---
put(key, value)|key/value 형태로 Context 에 값을 쓴다.
of(key1, value1, key2, value2, ...)|key/value 형태로 Context 에 여러 개의 값을 쓴다.
putAll(ContextView)|현재 Context 와 파라미터로 입력된 ContextView 를 merge 한다.
delete(key)|Context 에서 현재 key 에 해당하는 value 를 삭제한다.

### 자주 사용되는 ContextView API 
ContextView API|설명
---|---
get(key)|ContextView 에서 key 에 해당하는 value 를 찾는다.
getOrEmpty(key)|ContextView 에서 key 에 해당하는 value 를 Optional 로 래핑해서 반환한다.
getOrDefault(key, default value)|ContextView 에서 key 에 해당하는 value 를 가져온다. key 에 해당하는 value 가 없으면 default value 를 가져온다.
hasKey(key)|ContextView 에서 특정 key 가 존재하는지 확인한다.
isEmpty()|Context 가 비어있는지 확인한다.
size()|Context 내에 있는 key/value 개수를 확인한다.

### Context 특징1
* Context 구독이 발생할 때마다 하나의 Context 가 해당 구독에 연결된다.
* 아래 코드를 보면 두 개의 데이터가 하나의 Context 에 저장될 것 같지만, Context 는 구독별로 연결되는 특징이 있기 때문에 **구독이 발생할때마다 해당하는 하나의 Context 가 하나의 구독에 연결**된다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(DetailContext::class.java)

    val key1 = "company"

    val mono = Mono.deferContextual {
        Mono.just("Company: ${it.get<String>(key1)}")
    }
        .publishOn(Schedulers.parallel())

    mono.contextWrite {
        it.put(key1, "Naver Webtoon")
    }
        .subscribe {
            logger.info("# subscribe1 onNext: {$it}")
        }

    mono.contextWrite {
        it.put(key1, "Naver")
    }
        .subscribe {
            logger.info("# subscribe2 onNExt: {$it}")
        }

    Thread.sleep(100L)
}
```

```shell
11:22:12.772 [parallel-2] INFO com.example.playground.context.DetailContext -- # subscribe2 onNExt: {Company: Naver}
11:22:12.772 [parallel-1] INFO com.example.playground.context.DetailContext -- # subscribe1 onNext: {Company: Naver Webtoon}
```

### Context 특징2
* Context 는 Operator 체인에서 아래에서 위로 전파된다.
* 그래서 보통 contextWrite() 을 Operator 체인 맨 마지막에 둔다.
* 동일한 키에 대해서 중복저장하면 Operator 체인에서 가장 위쪽에 위차한 contextWrite() 이 저장한 값으로 덮어쓴다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(DetailContext2::class.java)

    val key1 = "company"
    val key2 = "name"

    Mono.deferContextual { Mono.just(it.get<String>(key1)) }
        .publishOn(Schedulers.parallel())
        .contextWrite { it.put(key2, "KSM") }
        .transformDeferredContextual { mono, contextView ->
            mono.map { "$it ${contextView.getOrDefault(key2, "HYJ")}" }
        }
        .contextWrite { it.put(key1, "Naver") }
        .subscribe {
            logger.info("# onNext : {$it}")
        }

    Thread.sleep(100L)
}
```

```shell
12:09:06.523 [parallel-1] INFO com.example.playground.context.DetailContext2 -- # onNext : {Naver HYJ}
```

### Context 특징3
* Inner Sequence 내부에서는 외부 Context 에 저장된 데이터를 읽을 수 있다.
* Inner Sequence 외부에서는 Inner Sequnce 내부 Context 에 저장된 데이터를 읽을 수 없다.
* 아래 코드에서 flatMap() Operator 내부에 있는 Sequnce 를 Inner Sequence 라고 하는데, Inner Sequence 에서는 바깥쪽 Sequence 에 연결된 Context 값을 읽을 수 있다.
* 만약 아래 코드 주석을 해제하면 Context 에서 "role" 이라는 key 가 없어서 에러가 발생한다.

```kotlin
fun main() {
    val logger = LoggerFactory.getLogger(DetailContext3::class.java)

    val key1 = "company"

    Mono.just("HYJ")
//        .transformDeferredContextual { mono, contextView -> mono.map { contextView.get<String>("role") } }
        .flatMap { data ->
            Mono.deferContextual { contextView ->
                Mono.just("${contextView.get<String>(key1)}, $data")
                    .transformDeferredContextual { mono, innerCtxView ->
                        mono.map { "$it , ${innerCtxView.get<String>("role")}" }
                    }
                    .contextWrite { it.put("role", "CEO") }
            }
        }
        .publishOn(Schedulers.parallel())
        .contextWrite { it.put(key1, "Naver") }
        .subscribe {
            logger.info("# onNext : {$it}")
        }

    Thread.sleep(100L)
}
```

```shell
12:17:09.756 [parallel-1] INFO com.example.playground.context.DetailContext3 -- # onNext : {Naver, HYJ , CEO}
```