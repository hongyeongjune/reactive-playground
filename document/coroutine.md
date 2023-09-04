### Your first coroutine
> https://kotlinlang.org/docs/coroutines-basics.html#your-first-coroutine

* 코루틴은 개발자가 코드를 실행하는 도중 일시 중단하고 나중에 다시 이어서 실행할 수 있는 인스턴스이다.
* 코루틴은 코드 블록을 실행하면서 다른 코드와 동시에(concurrently) 실행된다는 점에서는 개념적으로 스레드와 비슷하다.
* 하지만, 코루틴은 특정 스레드에 묶이지 않는다.
* 코루틴은 한 스레드에서 실행을 일시 중단하고 다른 스레드에서 다시 실행할 수 있습니다. (즉, 코루틴 A는 B 스레드가 suspend 할 수 있고, C 스레드가 resume 할 수 있다.)

