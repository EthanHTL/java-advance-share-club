# WebClient
spring webflux 包括了一个客户端去执行http请求,webClient是一个函数式的,流式的api(基于Reactor),查看[Reactive Libraries](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-reactive-libraries),它能够启用异步逻辑组合的声明而不需要线程处理或者并发,它完全异步,支持流式,并且依赖于相同的Codecs(支持编码、解码请求以及响应内容-在服务器端) \
webClient 需要一个Http 客户端库去执行请求,以下是内置支持:
- Reactor Netty
- Jetty Reactive HttpClient
- Apache HttpComponent
- 通过ClientHttpConnector加强的其他组件
### 配置
通过以下静态工厂方法创建一个WebClient:
 - WebClient.create
 - WebClient.create(String baseUrl) \
 你也能够使用WebClient.builder使用更多选择
 - uriBuilderFactory: 定制UriBuilderFactory 去作为一个基础URL
 - defaultUriVariables 当扩展URI模板时使用的默认值
 - defaultHeader 每一个请求的header
 - defaultCookie 每一个请求的cookie
 - defaultRequest 定制每一个请求的消费者Consumer
 - filter 每一个请求的过滤器
 - exchangeStrategies http 消息reader/writer的定制
 - clientConnector httpClient 库的设置 \
 举个例子:
 ```text
WebClient client = WebClient.builder()
        .codecs(configurer -> ... )
        .build();
```
一旦建立,WebClient不可变,然而你能够克隆他并建立一个修改copy的信息:
```text
WebClient client1 = WebClient.builder()
        .filter(filterA).filter(filterB).build();

WebClient client2 = client1.mutate()
        .filter(filterC).filter(filterD).build();

// client1 has filterA, filterB

// client2 has filterA, filterB, filterC, filterD
```
### MaxInMemorySize
编码器可以对内存中缓存的数据进行限制去避免应用内存问题,默认256kb,如果他不够,你能够获得以下错误:
```org.springframework.core.io.buffer.DataBufferLimitException: Exceeded limit on max bytes to buffer
```
改变:
```text
WebClient webClient = WebClient.builder()
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
        .build();   
```
### Reactor Netty
定制Reactor Netty配置,提供一个预先配置的HttpClient
```text
HttpClient httpClient = HttpClient.create().secure(sslSpec -> ...);

WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
```
#### 资源
默认情况下，HttpClient 参与到 reactor.netty.http.HttpResources 中保存的全局 Reactor Netty 资源，包括事件循环线程和连接池。这是推荐的模式，因为固定的共享资源是事件循环并发的首选。在这种模式下，全局资源保持活动状态，直到进程退出 \
如果服务器处理超时,通常不需要显式关闭,但是如果服务器能够开启或者关闭(在进程中)-例如Spring mvc应用部署为一个war,你能够声明一个Spring管理的bean,名为ReactorResourceFactory(globalResources =true)确保Reactor Netty全局资源是关闭的-当Spring ApplicationContext关闭之后:
```text
@Bean
public ReactorResourceFactory reactorResourceFactory() {
    return new ReactorResourceFactory();
}
```
你能够选择不参与到全局Reactor Netty资源，然而在这种模式中,责任(burden)确保所有的Reactor Netty客户端以及服务器实例使用共享资源:
```text
@Bean
public ReactorResourceFactory resourceFactory() {
    ReactorResourceFactory factory = new ReactorResourceFactory();
    factory.setUseGlobalResources(false); 
    return factory;
}

@Bean
public WebClient webClient() {

    Function<HttpClient, HttpClient> mapper = client -> {
        // Further customizations...
    };

    ClientHttpConnector connector =
            new ReactorClientHttpConnector(resourceFactory(), mapper); 

    return WebClient.builder().clientConnector(connector).build(); 
}
```
1.创建一个全局的独立资源
2.使用ReactorClientHttpConnector构造器构造
3.将connector加入到WebClient.Builder中
#### Timeouts
配置超时
```text
import io.netty.channel.ChannelOption;

HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

WebClient webClient = WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
```
配置读写超时:
```text
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

HttpClient httpClient = HttpClient.create()
        .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(10))
                .addHandlerLast(new WriteTimeoutHandler(10)));

// Create WebClient...
```
配置响应超时-对所有请求:
```text
HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(2));

// Create WebClient...
```
对单个指定的请求配置响应超时:
```text
WebClient.create().get()
        .uri("https://example.org/path")
        .httpRequest(httpRequest -> {
            HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
            reactorRequest.responseTimeout(Duration.ofSeconds(2));
        })
        .retrieve()
        .bodyToMono(String.class);
```
### Jetty
如何定制jetty HttpClient配置:
```text
HttpClient httpClient = new HttpClient();
httpClient.setCookieStore(...);

WebClient webClient = WebClient.builder()
        .clientConnector(new JettyClientHttpConnector(httpClient))
        .build();
```
默认来说,HttpClient创建了它自己的资源(Executor,ByteBufferPool,Scheduler),这仍然会激活直到进程退出或者stop调用; \
你能够在多个jetty客户端(以及服务器)实例之间共享资源并确保这些资源被关闭-在ApplicationContext关闭的时候-声明一个Spring管理的JettyResourceFactory bean做到这个事情!
```text
@Bean
public JettyResourceFactory resourceFactory() {
    return new JettyResourceFactory();
}

@Bean
public WebClient webClient() {

    HttpClient httpClient = new HttpClient();
    // Further customizations...

    ClientHttpConnector connector =
            new JettyClientHttpConnector(httpClient, resourceFactory()); 

    return WebClient.builder().clientConnector(connector).build(); 
}
```
### HttpComponents
如何定制HttpComponents HttpClient配置:
```text
HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom();
clientBuilder.setDefaultRequestConfig(...);
CloseableHttpAsyncClient client = clientBuilder.build();
ClientHttpConnector connector = new HttpComponentsClientHttpConnector(client);

WebClient webClient = WebClient.builder().clientConnector(connector).build();
```
### 抓取
retrieve方法能够被用来声明如何抓取响应:
```text
WebClient client = WebClient.create("https://example.org");

Mono<ResponseEntity<Person>> result = client.get()
        .uri("/persons/{id}", id).accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .toEntity(Person.class);
```
或者获取body
```text
WebClient client = WebClient.create("https://example.org");

Mono<Person> result = client.get()
        .uri("/persons/{id}", id).accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(Person.class);
```
或者获取解码对象的流
```text
Flux<Quote> result = client.get()
        .uri("/quotes").accept(MediaType.TEXT_EVENT_STREAM)
        .retrieve()
        .bodyToFlux(Quote.class);
```
默认俩说,4xx or 5xx response 导致一个WebClientResponseException,包括特定于Http状态的子类,为了定制处理错误响应,使用onStatus处理器:
```text
Mono<Person> result = client.get()
        .uri("/persons/{id}", id).accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(HttpStatus::is4xxClientError, response -> ...)
        .onStatus(HttpStatus::is5xxServerError, response -> ...)
        .bodyToMono(Person.class);
```
### Exchange
exchangeToMono 以及 exchangeToFlux方法(或者 awaitExchange{}以及 exchangeToFlow{}在kotlin)是非常有用的(对于需要更多控制的情况),例如解码响应依赖于不同的响应码:
```text
Mono<Object> entityMono = client.get()
        .uri("/persons/1")
        .accept(MediaType.APPLICATION_JSON)
        .exchangeToMono(response -> {
            if (response.statusCode().equals(HttpStatus.OK)) {
                return response.bodyToMono(Person.class);
            }
            else if (response.statusCode().is4xxClientError()) {
                // Suppress error status code
                return response.bodyToMono(ErrorContainer.class);
            }
            else {
                // Turn to error
                return response.createException().flatMap(Mono::error);
            }
        });
```
当使用上面的代码,在返回Mono/Flux完成之后,响应体被检查并且如果它没有被消费-它会释放阻止内存以及连接泄漏,因此响应不能够在后续下游解码,但是可以提供一个函数声明如何解码响应(如果有需要)
### 请求体
请求体能够编码来自被ReactiveAdapterRegistry处理的任何异步类型,例如Mono或者kotlin中的Deferred:
```text
Mono<Person> personMono = ... ;

Mono<Void> result = client.post()
        .uri("/persons/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .body(personMono, Person.class)
        .retrieve()
        .bodyToMono(Void.class);
```
你能够有一个编码的对象的流:
```text
Flux<Person> personFlux = ... ;

Mono<Void> result = client.post()
        .uri("/persons/{id}", id)
        .contentType(MediaType.APPLICATION_STREAM_JSON)
        .body(personFlux, Person.class)
        .retrieve()
        .bodyToMono(Void.class);
```
此外,如果你有一个实际的值,你能够使用bodyValue快捷方法,如下:
```text
Person person = ... ;

Mono<Void> result = client.post()
        .uri("/persons/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(person)
        .retrieve()
        .bodyToMono(Void.class);
```
### 表单数据
为了发送表单数据你能够提供一个MultiValueMap<String,String>作为body,注意内容自动通过FormHttpMessageWriter声明为application/x-www-form-urlencoded如下:
```text
MultiValueMap<String, String> formData = ... ;

Mono<Void> result = client.post()
        .uri("/path", id)
        .bodyValue(formData)
        .retrieve()
        .bodyToMono(Void.class);
```
你能够应用行内表单数据通过BodyInserters
```text
import static org.springframework.web.reactive.function.BodyInserters.*;

Mono<Void> result = client.post()
        .uri("/path", id)
        .body(fromFormData("k1", "v1").with("k2", "v2"))
        .retrieve()
        .bodyToMono(Void.class);
```
### Multipart Data
为了发送multipart 数据,你需要提供一个MultiValueMap<String,?> 这些值或者要么是一个Object实例呈现为内容的一部分或者HttpEntity实例呈现part的内容以及请求体,MultipartBodyBuilder提供了一个遍历的api去准备一个multipart 请求,下列的例子展示如何创建一个MultipartValueMap<String,?>
```text
MultipartBodyBuilder builder = new MultipartBodyBuilder();
builder.part("fieldPart", "fieldValue");
builder.part("filePart1", new FileSystemResource("...logo.png"));
builder.part("jsonPart", new Person("Jason"));
builder.part("myPart", part); // Part from a server request

MultiValueMap<String, HttpEntity<?>> parts = builder.build();
```
在大多数请求,你不需要对每一个part指定一个Content-type,这个内容类型会自动的根据HttpMessageWriter推断并选择如何序列化,在Resource的情况下,基于文件扩展选择,如果有必要你能够提供MediaType去对每一个part使用,通过构建者的一个part方法可以做到;\
一旦MultiValueMap构建好,有一个容易的方式传递它给WebClient-通过body方法:
```text
MultipartBodyBuilder builder = ...;

Mono<Void> result = client.post()
        .uri("/path", id)
        .body(builder.build())
        .retrieve()
        .bodyToMono(Void.class);
```
如果MultiValueMap包含了至少一个不是String的值,这可能也有普通的表单数据(例如application/x-www-form-urlencoded),你不需要设置Content-Type到multipart/form-data,当使用MultipartBodyBuilder的情况下,它会确保使用一个HttpEntity wrapper; \
当使用MultipartBodyBuilder,你需要提供Multipart内容,行内模式,通过BodyInserters:
```text
import static org.springframework.web.reactive.function.BodyInserters.*;

Mono<Void> result = client.post()
        .uri("/path", id)
        .body(fromMultipartData("fieldPart", "value").with("filePart", resource))
        .retrieve()
        .bodyToMono(Void.class);
```
### Filters
你能够注册一个客户端过滤器(ExchangeFilterFunction)-通过WebClient.Builder有序的拦截并修改请求:
```text
WebClient client = WebClient.builder()
        .filter((request, next) -> {

            ClientRequest filtered = ClientRequest.from(request)
                    .header("foo", "bar")
                    .build();

            return next.exchange(filtered);
        })
        .build();
```
对于跨域修改的场景下非常有用,例如认证
```text
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

WebClient client = WebClient.builder()
        .filter(basicAuthentication("user", "password"))
        .build();
```
过滤器能够通过一个可改变的存在WebClient实例进行增加或者删除,导致一个新的WebClient实例能够返回-不会影响以前的:
```text
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

WebClient client = webClient.mutate()
        .filters(filterList -> {
            filterList.add(0, basicAuthentication("user", "password"));
        })
        .build();
```
WebClient 是一个围绕过滤器链的外观，后跟一个 ExchangeFunction。它提供了一个工作流来发出请求、与更高级别的对象进行编码，并有助于确保始终使用响应内容。当过滤器以某种方式处理响应时，必须格外小心以始终使用其内容或以其他方式将其向下游传播到 WebClient，这将确保相同。下面是一个处理 UNAUTHORIZED 状态代码但确保任何响应内容（无论是否预期）都被释放的过滤器
```text
public ExchangeFilterFunction renewTokenFilter() {
    return (request, next) -> next.exchange(request).flatMap(response -> {
        if (response.statusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
            return response.releaseBody()
                    .then(renewToken())
                    .flatMap(token -> {
                        ClientRequest newRequest = ClientRequest.from(request).build();
                        return next.exchange(newRequest);
                    });
        } else {
            return Mono.just(response);
        }
    });
}
```
#### Attributes
能够增加一些属性到请求中,这非常方便如果需要传递一些信息-通过过滤器链以及对给定的请求影响过滤器的行为:
```text
WebClient client = WebClient.builder()
        .filter((request, next) -> {
            Optional<Object> usr = request.attribute("myAttribute");
            // ...
        })
        .build();

client.get().uri("https://example.org/")
        .attribute("myAttribute", "...")
        .retrieve()
        .bodyToMono(Void.class);

    }
```
注意你能够在WebClient.Builder全局配置一个defaultRequest回调让你能够插入属性到所有请求,者能够使用在例如Spring mvc应用中收集基于ThreadLocal 数据收集请求数据
#### Content
属性提供了一个方便的形式传递信息到过滤器链中但是它仅仅影响当前请求,如果你想要传递一些信息传播到额外的请求(这些请求是内嵌的),例如 通过flatMap或者执行之后,或者通过concatMap,那么你需要使用Reactor Context; \
Reactor Context 需要在响应链末尾被收集为了应用所有操作:
```text
WebClient client = WebClient.builder()
        .filter((request, next) ->
                Mono.deferContextual(contextView -> {
                    String value = contextView.get("foo");
                    // ...
                }))
        .build();

client.get().uri("https://example.org/")
        .retrieve()
        .bodyToMono(String.class)
        .flatMap(body -> {
                // perform nested request (context propagates automatically)...
        })
        .contextWrite(context -> context.put("foo", ...));
```
#### 同步使用
WebClient也能够同步使用:
```text
Person person = client.get().uri("/person/{id}", i).retrieve()
    .bodyToMono(Person.class)
    .block();

List<Person> persons = client.get().uri("/persons").retrieve()
    .bodyToFlux(Person.class)
    .collectList()
    .block();

```
然而如果多次调用,在每个响应上独立这是非常有用的,并且等待合并的结果:
```text
Mono<Person> personMono = client.get().uri("/person/{id}", personId)
        .retrieve().bodyToMono(Person.class);

Mono<List<Hobby>> hobbiesMono = client.get().uri("/person/{id}/hobbies", personId)
        .retrieve().bodyToFlux(Hobby.class).collectList();

Map<String, Object> data = Mono.zip(personMono, hobbiesMono, (person, hobbies) -> {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("person", person);
            map.put("hobbies", hobbies);
            return map;
        })
        .block();
```
类似于CompeletableFuture,上面是一个简单的合并例子,这里有大量的其他的模式以及拉取集合一个响应式的管道去产生多次远程调用,底层可以有些是内嵌的，内部依赖,没有阻塞直到最后结束!\
Flux或者Mono,你应该绝壁会阻塞在Spring mvc或者Spring webFlux控制器中,简单的从控制器方法返回响应式类型即可,相同的主体应用到Kotlin携程以及Spring WebFlux,仅仅使用消费函数或者在控制器方法中返回Flow;
#### Testing
为了测试WebClient,你能够使用一个Mock web 服务器,例如[OkHttpMockWebServer](https://github.com/square/okhttp#mockwebserver),为了查看使用的demo,可以检查在Springg 框架测试场景中的https://github.com/spring-projects/spring-framework/tree/main/spring-webflux/src/test/java/org/springframework/web/reactive/function/client/WebClientIntegrationTests.java 或者在OkHttp仓库中的[static-server](https://github.com/square/okhttp/tree/master/samples/static-server)


