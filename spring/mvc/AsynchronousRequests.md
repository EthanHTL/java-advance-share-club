## Asynchronous Requests
mvc 存在一个和Servlet3.0的异步请求处理扩展集成:
* DeferredResult 以及 Callable (在controller中方法的返回值类型) 并对单个异步返回值提供基本支持
* 控制器能够流式发布多次值,包括SSE以及 原始数据(raw data)
* 控制器能够使用响应式客户端并且返回响应式类型来响应处理;

#### DeferredResult
异步请求特性一旦在Servlet容器中启动,控制器方法能够包装任何一个支持返回DeferredResult的控制器方法:
```java
@GetMapping("/quotes")
@ResponseBody
public DeferredResult<String> quotes() {
    DeferredResult<String> deferredResult = new DeferredResult<String>();
    // Save the deferredResult somewhere..
    return deferredResult;
}

// From some other thread...
deferredResult.setResult(result);
```
控制器能够异步的生产返回值,从一个不同的线程返回,例如响应一个外部事件(JMS 消息),一个调度任务或者其他事件;
#### Callable
也支持对Callable进行包装 \
```java
@PostMapping
public Callable<String> processUpload(final MultipartFile file) {

    return new Callable<String>() {
        public String call() throws Exception {
            // ...
            return "someView";
        }
    };
}
```
这个返回值能够通过配置的TaskExecutor运行给定的任务 \
异步请求处理的步骤:
* 一个ServletRequest 能够通过调用request.startAsync(). 主要影响是Servlet(其他任何过滤器也一样)会退出,但是响应仍然保持打开、为了后面处理完毕;
* 调用request.startAsync 返回一个AsyncContext,这样你能够使用它进行更深层次的控制(进行异步处理),例如: 它提供 了一个dispatch方法，它相似于Servlet api 转发,除了它允许应用程序在 Servlet 容器线程上恢复请求处理
* ServletRequest 提供了访问当前DispatcherType的能力,你能够使用它区分初始化请求处理、异步派发、转发或者其他派发类型; \
DeferredResult的处理流程:
* 控制器返回了一个DefferredResultt 将它保存在一个内存队列中,或者列表中,它能够被访问; 
* Spring mvc调用request.startAsync()
* 然后,DispatcherServlet以及所有配置的过滤器全部从请求处理线程退出,但是响应仍然保持打开
* 应用程序从其他线程设置DeferredResult,mvc派方法这个请求回到Servlet 容器
* DispatcherServlet再次处理,并重新调用异步处理生产返回值; \
Callable 处理流程: \
* 控制器返回了一个Callable
spring mvc 调用request.startAsync()并提交了一个Callable到TaskExecutor然后在单独的线程进行处理;
* 然后,DispatcherServlet 以及所有过滤器退出Servlet容器线程,但是响应仍然打开
* 最终Callable 生产了一个结果,并且Spring Mvc 派发了请求回到Servlet容器去完成处理
* DispatcherServlet再次执行,并且重新处理并异步返回生产值(从Callable获取) \
对于更多背景以及上下文信息,可以阅读spring blog文章并了解异步请求处理支持; \
#### 异常处理
当你使用DeferredResult,你能够选择是否调用setResult或者setErrorResult来设置异常,不管什么情况,mvc 会派发请求回到Servlet 容器去完成处理,要么就像控制器方法返回的给定值、或者就是通过给定的异常生产的结果,这个异常能够通过普通的异常处理机制,例如执行@ExceptionHandler; \
当使用Callable,类似于逻辑处理发生,主要不同就是从Callable返回或者由它抛出的异常是最终结果;
#### 拦截
HandlerInterceptor实例能够是一个AsyncHandlerInterceptor,能够接受一个afterConcurrentHandlingStarted 回调(标识初始化请求开始异步处理,替代后置处理以及afterCompletion) \
HandlerInterceptor 实现能够注册一个CallableProcessingInterceptor 或者一个 DeferredResultProcessingInterceptor,为了更加深度的集成异步请求的生命周期(例如: 处理超时),查看此拦截器了解更多 \
DeferredResult 提供了 onTimeout(Runnable)以及onCompletion(Runnable)回调,查看java doc获取更多,Callable能够取代WebAsyncTask(能够暴露例如超时、完成回调的额外方法)
##### 对比WebFlux
Servlet api最开始建立是为了通过过滤器-Servlet链进行单次处理,异步请求处理: Servlet 3.0开始增加,让应用能够退出Filter-Servlet 链但是响应能够继续打开(为了后续处理),这样Spring mvc 异步处理支持完全建立在此机制上,当一个控制器返回了DeferredResult,那么Filter-Servlet 链将退出,并且Servlet 容器线程将释放,之后,当DeferredResult设置完毕之后,一个ASYNC 派发(到相同的URL)进行处理,当控制器方法再次映射过后、相反并不执行它,DeferredResult将直接使用(就像controller返回的一样)重新处理; \
作为对比,Spring webflux 没有完全建立在Servlet api上,它也可以异步请求处理,因为它是基于异步设计的,异步处理内置于所有框架锲约中,在请求处理的各个阶段都会得到内部支持 \
从编程模型方面来说,spring mvc以及spring webflux 都支持异步请求以及响应式类型作为控制器方法的返回值. mvc 甚至支持http streaming,包括响应式背压(reactive back pressure). 然而独立写入到响应仍然会阻塞(并且正在独立的线程执行),不像WebFlux,完全依赖于非阻塞式IO并且不需要额外的线程(每一次写入) \
另一个根本区别是 Spring MVC 不支持控制器方法参数中的异步或反应类型（例如，@RequestBody、@RequestPart 等），也没有明确支持异步和反应类型作为模型属性。 Spring WebFlux 确实支持所有这些 \
#### http streaming
你能够使用DeferredResult以及Callable进行单次异步的返回值,如果你想生产多个异步值并且将这些写入一个响应中,这一部分教你如何做:
##### Objects
你能够使用ResponseBodyEmitter 提交数据来生产流中的对象,每一个对象都会通过HttpMessageConverter序列化并写入响应，例如:
```java
@GetMapping("/events")
public ResponseBodyEmitter handle() {
    ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    // Save the emitter somewhere..
    return emitter;
}

// In some other thread
emitter.send("Hello once");

// and again later on
emitter.send("Hello again");

// and done at some point
emitter.complete();
```
你能够使用ResponseBodyEmitter 作为ResponseEntity的body,定制响应状态以及响应的响应头\
当emitter 抛出了一个IOException,如果远程客户端消失了,应用没有责任去清理此连接并且不应该执行emitter.complete或者emitter.completeWithError,相反servlet 容器会自动初始化一个AsyncListener进行错误通知,spring mvc中使用completeWithError,最后会执行一个ASYNC 派发到应用中,然后spring mvc通过配置的异常解析器解析异常并完成请求; \
##### SSE
SseEmitter(一个ResponseBodyEmitter的实现)提供了Server-sent-Events,从服务端发送的事件根据w3C sse规范格式化,为了从控制器中产生一个SSE流,返回一个SseEmitter即可:
```java
@GetMapping(path="/events", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter handle() {
    SseEmitter emitter = new SseEmitter();
    // Save the emitter somewhere..
    return emitter;
}

// In some other thread
emitter.send("Hello once");

// and again later on
emitter.send("Hello again");

// and done at some point
emitter.complete();
```
SSE目前是浏览器流处理的主要选择,但是IE不支持Server-Sent Events,考虑使用Spring的WebSocket messaging和SockJS fallback 传输(包括SSE),这些都被浏览器广泛支持;
##### Raw Data
有些时候,绕过消息转缓缓并直接向响应流OutputStream输出数据,例如文件下载,你能够使用StreamingResponseBody返回对应类型数据:
```java
@GetMapping("/download")
public StreamingResponseBody handle() {
    return new StreamingResponseBody() {
        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            // write...
        }
    };
}
```
你能够使用StreamingResponseBody 作为ResponseEntity的body去定制响应的状态以及响应头;
#### 响应式类型
mvc支持响应式客户端库的使用到controller中(也可以阅读webflux部分的Reactive libraries),暴露spring-webflux的WebClient以及其他内容,例如Spring Data reactive data repository,这些场景之下,它能够非常方便的从控制器方法返回响应式类型 \
响应式返回类型会被处理:
* 单值promise能够适配,类似于DeferredResult,例如包括Mono(Reactor)或者Single(RxJava)
* 一个多值流(通过流媒体形式 application/x-ndjson 或则 text/event-stream)也能够适配,类似于使用ResponseBodyEmitter 或者SseEmitter,例如包括Flux(Reactor)或者Observable(RxJava),应用能够返回Flux<ServerSentEvent> 或者 Observable<ServerSentEvent>
* 一个任意媒体类型(application/json)的多值流也能够适配,类似于使用DeferredResult<List<?>> \
mvc 支持reactor 以及RxJava (通过ReactiveAdapterRegistry-来自spring-core),这些让它能够适配来自多个响应式库; \
对于流式传输到响应，支持反应式背压，但对响应的写入仍然是阻塞的，并且通过配置的 TaskExecutor 在单独的线程上运行，以避免阻塞上游源（例如从 WebClient 返回的 Flux）。默认情况下，SimpleAsyncTaskExecutor 用于阻塞写入，但这不适合在负载下使用。如果您计划使用反应式流式传输，则应使用 MVC 配置来配置任务执行器

#### 取消连接
servlet api没有提供任何通知(当远程客户端断开),因此当使用流式传输响应,不管你通过SseEmitter或者响应式类型,它们会周期性发送数据,因此写入失败(如果客户端断开). 发送可能是空内容形式SSE event或者任意的其他数据,且应该理解为心跳并忽略; \
除此之外,考虑使用 web 消息解决方案(例如 WebSocket上的STOMP 协议或者 WebSocket 结合SockJs使用),因为它本身有内置的心跳机制;
#### 配置
异步请求处理特性必须在Servlet 容器级别开启,mvc 配置暴露了一个异步请求的几个的选项;

##### Servlet 容器
过滤器以及Servlet声明都有一个asyncSupported标志,你需要 设置为true来启用异步处理,除此之外,过滤器mappings 应该声明处理ASYNC (javax.servlet.DispatcherType) \
在java配置中,当你使用AbstractAnnotationConfigDispatcherServletInitializer去初始化Servlet 容器,会自动完成 \
在web.xml配置中,你需要增加<async-supported>true</async-supported> 到DispatcherServlet 以及过滤器声明(增加<dispatcher>ASYNC</dispatcher>)到filterMappings中
##### spring mvc
mvc配置暴露了以下跟异步请求相关的处理:
* 使用configureAsyncSupport回调 - webMvcConfigurer
* xml配置 使用<mvc:annotation-driven>的<async-support>支持 \
你也能够配置以下内容:
*异步请求的超时时间,如果不设置取决于底层的Servlet容器
* AsyncTaskExecutor 用来阻塞写操作(当通过响应式流式传输响应以及执行从控制器方法返回的Callable实例),我们非常推荐配置此属性(如果你是响应式流式传输响应或者有控制器方法返回callable),默认是一个SimpleAsyncTaskExecutor;
* DeferredResultProcessingInterceptor 实现以及CallableProcessingInterceptor实现 \
注意你能够设置DeferredResult、ResponseBodyEmitter以及SseEmitter的超时时间,对于callable您能够使用WebAsyncTask提供超时时间;

