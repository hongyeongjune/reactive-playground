> version : 3.5.8
> https://projectreactor.io/docs/core/release/reference/index.html#context

### Adding a Context to a Reactive Sequence
* 명령형 프로그래밍에서 리액티브 프로그래밍으로 사고방식을 전환할 때 만나는 기술적인 어려움 중 하나는 스레드 처리 방식에 있다.
* 리액티브 프로그래밍에선 ```Thread```가 거의 동시에 (정확히는 Non-Blocking 잠금단계에서) 비동기 시퀀스를 처리하기 때문에, 익숙하기보단 오히려그 정반대일 것이다.
* 실행 중에 자주, 그것도 쉽게 스레드 간 전환이 이루어진다.
* 이 때문에 ```ThreadLocal```같이 더 "안정적인" 스레드 모델에 익숙한 개발자에겐 특히 더 어렵다.
* 이런 모델은 데이터를 스레드와 연결시켜 생각하게 만들기 때문에, 리액티브 컨텍스트 안에서 사용하긴 어렵다.
* 결과적으로 ```ThreadLocal```에 의존하는 라이브러리는 리액터와 사용하면, 잘해도 결국 새로운 이슈를 직면하게 된다. 
* 최악의 경우 잘못 동작하거나 심지어 실패할 수도 있다. 
* 대표적으로 Logback의 MDC를 사용해서 관련 ID를 저장하고 함께 로깅하는 경우가 그렇다.
* ```ThreadLocal```을 해결하기 위해 보통 시도하는 방법은 시퀀스에 문맥 상의 데이터(```C```)를 비지니스 데이터(```T```)와 함께 사용하는 것이다 (예를 들어```Tuple2<T,C>```). 사용하는 메소드와 ```Flux``` 시그니처에 관련 없는 관심사를 (컨텍스트 데이터) 노출하기 때문에 딱히 좋은 방법은 아니다.
* 리액터 3.1.0부터 다소 ```ThreadLocal```과 비슷하지만, ```Thread```대신 ```Flux```나 ```Mono```에 적용할 수 있는 고급기능을 제공한다.
* 이 기능은 ```Context```라 부른다.

#### Example
```kotlin
val key = "message"

val mono = Mono.just("Hello")
    .flatMap {
        Mono.deferContextual { contextView ->
            Mono.just("$it ${contextView.get<String>(key)}")
        }
    }
    .contextWrite {
        it.put(key, "World")
    }

StepVerifier.create(mono)
    .expectNext("Hello World")
    .verifyComplete()
```

### Tying a Context to a Flux and Writing
* ```Context```를 제대로 활용하려면, 특정 시퀀스에 연결해서 체인에 있는 모든 연산자에서 접근할 수 있어야 한다. 주의할 점은, ```Context```는 리액터에서 제공하기 때문에, ```Context```에 접근하는 연산자는 리액터의 네이티브 연산자만 사용해야 한다.
* 실제로 ```Context```는 각 체인에 있는 ```Subscriber```와 연결된다.
* ```Subscription``` 전파 메커니즘을 사용하기 때문에, 최종 ```subscribe```에서부터 시작해서 체인 위로 전달되어 연산자에서도 접근할 수 있는 것이다.
* ```Context```를 전달하려면 구독 시점에 ```contextWrite```연산자를 사용해야 한다.
* ```contextWrite```는 제공한 ```ContextView```와 다운스트림에서 받은 (```Context```는 체인 밑에서부터 위로 전파된다는 점을 기억하라)```Context```를 머지한다.
* 내부적으로는 ```putAll```을 호출해서 업스트림에 새 ```Context```를 전달한다.
