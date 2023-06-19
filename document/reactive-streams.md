#### 리액티브 선언문 설계 원칙
![images](https://www.reactivemanifesto.org/images/reactive-traits.svg)

* MEANS(수단)
    * 비동기 메세지 기반의 통신을 통해서 구성요소들 간의 느슨한 결합, 격리성, 위치 투명성 보장
* FORM (형성)
    * Elastic(탄력성)
        * 시스템의 작업량이 변화하더라도 일정한 응답을 유지하는 것
    * Resilient(회복성)
        * 시스템에 장애가 발생하더라도 응답성을 유지하는 것
* VALUE(가치)
    * 비동기 메세지 기반 통신을 바탕으로 한 회복성과 예측 가능한 규모 확장 알고리즘을 통해 시스템의 처리량을 자동으로 확장하고 축소하는 탄력성을 확보함으로써 즉각적으로 응답 가능한 시스템을 구축할 수 있도록 하는 것

#### Reactive Streams Component
컴포넌트|설명
---|---
Publisher|데이터를 생성하고 통지(발행, 게시 방출)하는 역할을 한다.
Subscriber|구독한 Publisher로부터 통지(발행, 게시, 방출)된 데이터를 전달받아서 처리하는 역할을 한다.
Subscription|Publisher에 요청할 데이터의 개수를 지정하고, 데이터의 구독을 취소하는 역할을 한다.
Processor|Publisher와 Subscriber의 기능을 모두 가지고 있다. 즉, Subscriber로서 다른 Publisher를 구독할 수 있고, Publisher로서 다른 Subscriber가 구독할 수 있다.

#### Reactive Streams Flow
![images](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*L5vvRN1vQZRHrkrgBO4tPg.png)

1. Subscriber 는 전달받을 데이터를 구독한다. (subscribe)
2. Publisher 는 데이터를 발행할 준비가 되었음을 Subscriber 에 알린다. (onSubscribe - subscription())
3. Publisher 가 데이터를 통지할 준비가 되었다는 알림을 받은 Subscriber 는 전달받기를 원하는 데이터의 개수를 Publisher 에게 요청한다. (Subscription.request)
4. Publisher 는 Subscriber 로부터 요청받은 만큼의 데이터를 통지한다. (onNext)
5. Publisher 와 Subscriber 간에 데이터 통지, 데이터 수신, 데이터 요청의 과정을 반복하다가 모든 데이터를 통지하게 되면 마지막으로 데이터가 전송되었음을 알린다. (onComplete) 만약 중간에 에러가 발생하면 에러를 날린다. (onError)

#### Publisher
```java
public interface Publisher<T> {
    // 파라미터로 전달받은 Subscriber 를 등록하는 역할
    public void subscribe(Subscriber<? super T> s);
}
```

#### Subscriber
```java
public interface Subscriber<T> {
    // 구독 시작 시점에 Publisher에게 요청할 데이터의 개수를 지정하거나 구독을 해지하는 처리를 하는 역할
    public void onSubscribe(Subscription s);
    // Publisher가 통지한 데이터를 처리하는 역할
    public void onNext(T t);
    // Publisher가 데이터 통지를 위한 처리 과정에서 에러가 발생했을 때 해당 에러를 처리하는 역할
    public void onError(Throwable t);
    // Publisher가 데이터 통지를 완료했음을 알릴 때 호출되는 메서드
    public void onComplete();
}
```

#### Subscription
```java
public interface Subscription {
    // Publisher에게 데이터의 개수를 요청
    public void request(long n);
    // 구독 해지
    public void cancel();
}
```

#### Processor
```java
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```

#### Signal
* Publisher 와 Subscriber 간에 주고받는 상호작용
* ex. onSubscribe, onNext, onComplete, onError, request, cancel 

#### Demand
* Subscriber 가 Publisher 에게 요청하는 데이터 즉, Publisher 가 아직 Subscriber 에게 전달하지 않은 Subscriber 가 요청한 데이터

#### Emit
* Publisher 가 Subscriber 에 데이터를 전달하는 것

#### Sequence
* Publisher 가 emit 하는 데이터의 연속적인 흐름을 정의해 놓은 것
* 즉, 아래 코드처럼 Flux 를 통해서 데이터를 생성, emit 하고 filter 메서드를 통해서 필터링한 후 map 메서드를 통해 변환하는 이러한 과정 자체를 Sequence 라고 한다.
```java
public class ReactiveStreams {
    public static void main(String[] args) {
        Flux.just(1,2,3,4,5,6)
            .filter(n -> n % 2 == 0)
            .map(n -> n * 2)
            .subscribe(System.out::println);
    }
}
```

#### Operator
* just, filter, map 같은 메서드들을 Operator 라 부른다.

### 참조
> 스프링으로 시작하는 리액티브 프로그래밍 - Spring WebFlux 를 이용한 Non-Blocking 애플리케이션 구현