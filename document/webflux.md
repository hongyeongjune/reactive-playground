### Spring WebFlux 의 요청 흐름
> ![image](https://github.com/hongyeongjune/reactive-playground/assets/39120763/faff9a77-f345-418f-8509-ded110c5ef34)

1. 최초에 클라이언트로부터 요청이 들어오면 Netty 등의 서버 엔진을 거쳐 HttpHandler 가 들어오는 요청을 전달받는다. HttpHandler 는 Netty 이외의 다양한 서버 엔진을 지원하는 서버 API 를 사용할 수 있도록 서버 API 를 추상화해 주는 역할을 하는데, 각 서버 엔진마다 주어지는 ServerHttpRequest 와 ServerHttpResponse 를 포함하는 ServerWebExchange 를 생성한 후, WebFilter 체인으로 전달한다.
2. ServerWebExchange 는 WebFliter 체인에서 전처리 과정을 거친 후, WebHandler 인터페이스의 구현체인 DispatcherHandler 에 전달된다.
3. Spring MVC 의 DispatcherServlet 과 유사한 역할을 하는 DispatcherHandler 에서는 HandlerMapping List 를 원본 Flux 의 소스로 전달받는다.
4. ServerWebExchange 를 처리할 핸들러를 조회한다.
5. 조회한 핸들러의 호출을 HandlerAdapter 에게 위임한다.
6. HandlerAdapter 는 ServerWebExchange 를 처리할 핸들러를 호출한다.
7. Controller 또는 HandlerFunction 형태의 핸들러에서 요청을 처리한 후, 응답 데이터를 리턴한다.
8. 핸들러로부터 리턴받은 응답 데이터를 처리할 HandlerResultHandler 를 조회한다.
9. 조회한 HandlerResultHandler 가 응답 데이터를 적절하게 처리한 후, response 로 리턴한다.

### Spring WebFlux 의 핵심 컴포넌트
#### HttpHandler
* 다른 유형의 HTTP 서버 API 로 request 와 response 를 처리하기 위해 추상화된 단 하나의 메서드만 가진다.
* HttpHandler 의 구현체인 HttpWebHandlerAdapter 클래스는 handle() 메서드의 파라미터로 전달받은 ServerHttpRequest 와 ServerHttpResponse 로 ServerWebExchange 를 생성한 후에 WebHandler 를 호출하는 역할을 한다.

```java
public interface HttpHandler {
	Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response);
}
```

```java
public class HttpWebHandlerAdapter extends WebHandlerDecorator implements HttpHandler {
  ...
  @Override
	public Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response) {
		if (this.forwardedHeaderTransformer != null) {
			try {
				request = this.forwardedHeaderTransformer.apply(request);
			}
			catch (Throwable ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to apply forwarded headers to " + formatRequest(request), ex);
				}
				response.setStatusCode(HttpStatus.BAD_REQUEST);
				return response.setComplete();
			}
		}
		ServerWebExchange exchange = createExchange(request, response);

		LogFormatUtils.traceDebug(logger, traceOn ->
				exchange.getLogPrefix() + formatRequest(exchange.getRequest()) +
						(traceOn ? ", headers=" + formatHeaders(exchange.getRequest().getHeaders()) : ""));

		return getDelegate().handle(exchange)
				.doOnSuccess(aVoid -> logResponse(exchange))
				.onErrorResume(ex -> handleUnresolvedError(exchange, ex))
				.then(cleanupMultipart(exchange))
				.then(Mono.defer(response::setComplete));
	}
  ...
}
```

#### WebFilter
* Spring MVC 의 서블릿 필터처럼 핸들러가 요청을 처리하기전에 전처리 작업을 할 수 있도록 해준다.
* filter 메서드로 정의되어 있으며 파라미터로 전달받은 WebFliterChain 을 통해 필터 체인을 형성하여 원하는 만큼의 WebFliter 를 추가할 수 있다.

```java
public interface WebFilter {
	Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain);
}
```

### DispatcherHandler
* WebHandler 인터페이스의 구현체로서 Spring MVC 에서 Front Controller 패턴이 적용된 DispatcherServlet 처럼 중앙에서 먼저 요청을 전달받은 후에 다른 컴포넌트에 요청 처리를 위임한다.
* DispatcherHandler 자체가 Spring Bean 으로 등록되도록 설계되었으며, AppicationContext 에서 HandlerMapping, HandlerAdapter, HandlerResultHandler 등의 요청 처리를 위한 위임 컴포넌트를 검색한다.

```java
public class DispatcherHandler implements WebHandler, PreFlightRequestHandler, ApplicationContextAware {
  @Nullable
	private List<HandlerMapping> handlerMappings;

	@Nullable
	private List<HandlerAdapter> handlerAdapters;

	@Nullable
	private List<HandlerResultHandler> resultHandlers;

  ...
  protected void initStrategies(ApplicationContext context) {
		Map<String, HandlerMapping> mappingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				context, HandlerMapping.class, true, false);

		ArrayList<HandlerMapping> mappings = new ArrayList<>(mappingBeans.values());
		AnnotationAwareOrderComparator.sort(mappings);
		this.handlerMappings = Collections.unmodifiableList(mappings);

		Map<String, HandlerAdapter> adapterBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				context, HandlerAdapter.class, true, false);

		this.handlerAdapters = new ArrayList<>(adapterBeans.values());
		AnnotationAwareOrderComparator.sort(this.handlerAdapters);

		Map<String, HandlerResultHandler> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				context, HandlerResultHandler.class, true, false);

		this.resultHandlers = new ArrayList<>(beans.values());
		AnnotationAwareOrderComparator.sort(this.resultHandlers);
	}
  ...
  @Override
	public Mono<Void> handle(ServerWebExchange exchange) {
		if (this.handlerMappings == null) {
			return createNotFoundError();
		}
		if (CorsUtils.isPreFlightRequest(exchange.getRequest())) {
			return handlePreFlight(exchange);
		}
		return Flux.fromIterable(this.handlerMappings)
				.concatMap(mapping -> mapping.getHandler(exchange))
				.next()
				.switchIfEmpty(createNotFoundError())
				.onErrorResume(ex -> handleDispatchError(exchange, ex))
				.flatMap(handler -> handleRequestWith(exchange, handler));
	}
  ...
  private Mono<HandlerResult> invokeHandler(ServerWebExchange exchange, Object handler) {
    if (this.handlerAdapters != null) {
      for (HandlerAdapter handlerAdapter : this.handlerAdapters) {
        if (handlerAdapter.supports(handler)) {
          return handlerAdapter.handle(exchange, handler);
        }
      }
    }
    return Mono.error(new IllegalStateException("No HandlerAdapter: " + handler));
  }
  ...
  private Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
		Mono<Void> resultMono = doHandleResult(exchange, result, "Handler " + result.getHandler());
		if (result.getExceptionHandler() != null) {
			resultMono = resultMono.onErrorResume(ex ->
					result.getExceptionHandler().handleError(exchange, ex).flatMap(result2 ->
							doHandleResult(exchange, result2, "Exception handler " +
									result2.getHandler() + ", error=\"" + ex.getMessage() + "\"")));
		}
		return resultMono;
	}
  ...
  private HandlerResultHandler getResultHandler(HandlerResult handlerResult) {
		if (this.resultHandlers != null) {
			for (HandlerResultHandler resultHandler : this.resultHandlers) {
				if (resultHandler.supports(handlerResult)) {
					return resultHandler;
				}
			}
		}
		throw new IllegalStateException("No HandlerResultHandler for " + handlerResult.getReturnValue());
	}
  ...
}
```
