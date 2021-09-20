# 函数式端点
spring webflux包括webFlux.fn,一个轻量级的函数式编程模型-这些函数用于路由以及处理请求并且设计为稳定的,它是基于注解的编程模型替代品(但是它运行在Reactive Core基础上foundation); \
### OverView
webflux.fn 中,请求通过HandlerFunction处理,一个函数调用ServerRequest并且返回一个延时的ServerResponse(ie. Mono<ServerResponse>). 请求和响应对象拥有不可变的约定-提供了jdk8的友好性访问请求和响应,HandlerFunction 等价于基于注解的编程模型的@RequestMapping方法的处理体; \
进入的请求将通过RouterFunction路由handler function,一个函数可以使用ServerRequest并返回一个延时的HandlerFunction(例如 Mono<HandlerFunction>),当路由函数匹配,一个处理器函数将会被返回,否则返回一个空的Mono,RouterFunction 等价于@RequestMapping注解,但是路由函数主要的不同就是不仅仅提供数据并且提供行为! \
RouterFunctions.route() 提供了一个路由构建器-创建路由器的能力
```text
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

PersonRepository repository = ...
PersonHandler handler = new PersonHandler(repository);

RouterFunction<ServerResponse> route = route()
    .GET("/person/{id}", accept(APPLICATION_JSON), handler::getPerson)
    .GET("/person", accept(APPLICATION_JSON), handler::listPeople)
    .POST("/person", handler::createPerson)
    .build();


public class PersonHandler {

    // ...

    public Mono<ServerResponse> listPeople(ServerRequest request) {
        // ...
    }

    public Mono<ServerResponse> createPerson(ServerRequest request) {
        // ...
    }

    public Mono<ServerResponse> getPerson(ServerRequest request) {
        // ...
    }
}
```
运行RouterFunction的方式就是包装它到一个HttpHandler并且安装它(通过内建的[Server adapters](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-httphandler))
- RouterFunctions.toHttpHandler(RouterFunction)
- RouterFunctions.toHttpHandler(RouterFunction,HandlerStrategies) \
大多数应用能够通过WebFlux java配置运行,可以查看[Running a Server](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-fn-running)
### HandlerFunction
ServerRequest 以及 ServerResponse是一个不可变的接口-提供了JDK8友好性访问HttpRequest以及响应的能力,请求以及响应针对流body-提供了[Reactive Stream](https://www.reactive-streams.org/)反压. 请求体可以使用Reactor Flux或者Mono呈现,响应体可以使用任何Reactive Stream Publisher呈现,包括Flux以及Mono,了解更多可以查看[Reactive Libraries](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-reactive-libraries)
#### ServerRequest
它提供了访问http method,URI,headers,query parameters的能力,通过body方法访问body体! \
下面的例子抓取请求体到Mono<String>:
```text
Mono<String> string = request.bodyToMono(String.class);
```
下面的例子抓取body到Flux<Person>或者(在Kotlin中的Flow<Person>),这里的Person对象将根据有些序列化形式解码,例如JSON或者XML:
```text
Flux<Person> people = request.bodyToFlux(Person.class);
```
前面的例子简短也可以使用更加通用的ServerRequest.body(BodyExtractor),这些接受BodyExtractor函数式策略接口. BodyExtractors工具类提供了访问大量实例的能力,例如前面的例子能够写成下面的样子:
```text
Mono<String> string = request.body(BodyExtractors.toMono(String.class));
Flux<Person> people = request.body(BodyExtractors.toFlux(Person.class));
```
下面的例子展示了如何访问表单数据:
```text
Mono<MultiValueMap<String, String>> map = request.formData();
```
下面的例子展示了如何访问multipart-data 作为一个map:
```text
Mono<MultiValueMap<String, Part>> map = request.multipartData();
```
下面的例子展示了如何访问multiparts,一次一个,在流式机制中:
```text
Flux<Part> parts = request.body(BodyExtractors.toParts());
```
#### ServerResponse
能够访问 http response,它不可变,能够使用build函数创建它,能够通过构建者设置响应状态,增加响应头,或者提供一个body,下面的例子创建了一个200(OK响应)包括JSON内容:
```text
Mono<Person> person = ...
ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(person, Person.class);
```
下面的例子展示了如何构建一个201(创建完毕的)响应并包括一个Location响应头-没有body:
```text
URI location = ...
ServerResponse.created(location).build();
```
依赖于编码器的使用,他可能传递一个提示参数去定制body如何序列化或者反序列化,例如,指定一个jackson JSON View:
```text
ServerResponse.ok().hint(Jackson2CodecSupport.JSON_VIEW_HINT, MyJacksonView.class).body(...);
```
#### 处理器(Handler) Classes
当你写handler函数作为一个lambda,以下例子如下:
```text
HandlerFunction<ServerResponse> helloWorld =
  request -> ServerResponse.ok().bodyValue("Hello World");
```
这是一个约定但是在一个应用中我们需要多个函数,以及多个内联lambda看起来更加乱(messy).因此对相关的处理器函数成组到一个处理器类中更加合适,这类似于@Controller的角色-在基于注解的应用中,例如下面的类暴露了一个响应式的Person仓库:
```text
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class PersonHandler {

    private final PersonRepository repository;

    public PersonHandler(PersonRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> listPeople(ServerRequest request) { 
        Flux<Person> people = repository.allPeople();
        return ok().contentType(APPLICATION_JSON).body(people, Person.class);
    }

    public Mono<ServerResponse> createPerson(ServerRequest request) { 
        Mono<Person> person = request.bodyToMono(Person.class);
        return ok().build(repository.savePerson(person));
    }

    public Mono<ServerResponse> getPerson(ServerRequest request) { 
        int personId = Integer.valueOf(request.pathVariable("id"));
        return repository.getPerson(personId)
            .flatMap(person -> ok().contentType(APPLICATION_JSON).bodyValue(person))
            .switchIfEmpty(ServerResponse.notFound().build());
    }
}
```
listPeople 是一个处理器函数返回了所有的Person对象-转换为JSON \
createPerson 是一个处理器函数存储了一个包含在请求体中的Person,注意PersonRepository.savePerson(Person)返回了Mono<Void>: 一个空的Mono 会提交完成的信号-当person已经从请求中读取并存储完成时. 因此我们可以使用build(Publisher<Void>)方法去发送一个响应(当个完成的信号时接受的,那就是当Person已经够被保存了!)\
getPerson 是一个处理器函数返回了一个单个person,通过id路径变量,我们从仓库中抓取并创建一个JSON响应,r如果他是一个变量. 如果它没有被发现,我们切换为switchIfEmpty(Mono<T>)返回一个404没有发现的响应!
#### 验证
一个函数式端点能够使用Spring 验证功能去验证请求体,例如使用自定义的Spring Validator实现校验一个Person
```text
public class PersonHandler {

    private final Validator validator = new PersonValidator(); 

    // ...

    public Mono<ServerResponse> createPerson(ServerRequest request) {
        Mono<Person> person = request.bodyToMono(Person.class).doOnNext(this::validate); 
        return ok().build(repository.savePerson(person));
    }

    private void validate(Person person) {
        Errors errors = new BeanPropertyBindingResult(person, "person");
        validator.validate(person, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString()); 
        }
    }
}
```
处理器也能够使用标准的JSR-303标准bean校验API(通过创建并注册一个全局的验证器实例-基于LocalValidatorFactoryBean)-查看[Spring Validation](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/core.html#validation-beanvalidation)
### 路由函数
路由函数路由请求到相关的HandlerFunction,通常不需要自己写路由函数,相反可以使用RouterFunctions工具类创建一个路由函数,RouterFunctions.route()提供一个fluent 构建器创建一个路由函数,另外RouterFunctions.route(RequestPredicate,HandlerFunction)去针对get请求创建一个映射通过POST(String,HandlerFunction)为Post创建! \
除了基于Http方法的映射,路由构建器提供了一个方式去引入额外的条件-当映射请求时,对于每一个http方法-这里都存在一个重载的变量能够获取RequestPredicate作为一个参数,用它表达额外的约束!
#### Predicates
能够提供自己的RequestPredicate,但是RequestPredicates工具类提供了大多数实现,基于请求路径,Http方法、content-type等等,下面的例子使用了一个请求predicate 创建一个基于Accept请求头的约束:
 ```text
RouterFunction<ServerResponse> route = RouterFunctions.route()
    .GET("/hello-world", accept(MediaType.TEXT_PLAIN),
        request -> ServerResponse.ok().bodyValue("Hello World")).build();
```
你能够组合多个请求predicate一起使用:
- RequestPredicate.and(RequestPredicate) 同时匹配
-RequestPredicate.or(RequestPredicate) 要么任意一个匹配 \
来自RequestPredicates的大多数predicate都是组合的，举个例子RequestPredicates.GET(String) 由RequestPredicates.method(HttpMethod)和RequestPredicates.path(String)组合,上面的例子展示了使用两个请求predicates,builder内部使用RequestPredicates.GET,和accept predicate组合使用! \
#### Routes
路由函数评估存在顺序,如果第一个没有匹配,第二个继续评估,因此它对于在通用路由之前声明特殊路由是非常重要的,当注册一个路由函数作为spring bean也是非常重要的,注意于基于注解的编程模型行为是不同,这里"most specific" 控制器方法自动捆绑-抓取! \
当使用路由函数构建器,所有定义的路由会组合到一个RouterFunction-根据build返回的,这里是另一个种方式组合多个路由函数的方式:
- RouterFunctions.route()构建者的add(RouterFunction)
- RouterFunction.add(...)
- RouterFunction.addRoute(RequestPredicate,HandlerFunction) RouterFunction.add的缩写包含了一个内嵌的RouterFunctions.route()\
下面的例子展示了如何组合一个路由:
```text
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

PersonRepository repository = ...
PersonHandler handler = new PersonHandler(repository);

RouterFunction<ServerResponse> otherRoute = ...

RouterFunction<ServerResponse> route = route()
    .GET("/person/{id}", accept(APPLICATION_JSON), handler::getPerson) 
    .GET("/person", accept(APPLICATION_JSON), handler::listPeople) 
    .POST("/person", handler::createPerson) 
    .add(otherRoute) 
    .build();
```
#### 内嵌路由
通常是对路由函数的分组-它们存在共享的predicate,一个共享路径的实例.在上面的demo中,共享的predicate 有一个能够匹配/person 的路径 条件,被三个路由使用,当使用注解的时候,你能够移除重复的通过设置类级别的@RequestMapping映射到/person,在WebFlux.fn中,路径predicates能够通过路由函数构建者的path方法共享,例如: 上面几行的demo可以使用内嵌路由改写:
```
RouterFunction<ServerResponse> route = route()
    .path("/person", builder -> builder 
        .GET("/{id}", accept(APPLICATION_JSON), handler::getPerson)
        .GET(accept(APPLICATION_JSON), handler::listPeople)
        .POST("/person", handler::createPerson))
    .build();
```
注意path的第二个参数是一个消费者(消费router builder) \
尽管基于路径的内嵌是更加常见的,你能够通过使用构建者之上的nest方法完成任何类型的predicate内嵌,上面仍然存在重复(重复的header predicate,可以变为内嵌共享的)可以改写,现在使用nest进行提升:
```text
RouterFunction<ServerResponse> route = route()
    .path("/person", b1 -> b1
        .nest(accept(APPLICATION_JSON), b2 -> b2
            .GET("/{id}", handler::getPerson)
            .GET(handler::listPeople))
        .POST("/person", handler::createPerson))
    .build();
```
#### 运行一个服务器
通过使用以下之一转换一个路由函数到HttpHandler即可:
 - RouterFunctions.toHttpHandler(RouterFunction)
 - RouterFunctions.toHttpHandler(RouterFunction,HandlerStrategies)
 \
 你能够使用这个拥有大量服务器适配器(根据特定于服务器的指令实现)的HttpHandler; \
 一个更加通常的选项,可以使用在SpringBoot中,通过WebFlux配置基于DispatcherHandler运行;
 使用Spring配置声明需要处理请求的组件,WebFluxJava配置声明以下的基础组件支持函数式端点:
 - RouterFunctionMapping 检测一个或者多个RouterFunction<?>bean,并排序通过RouterFunction.andOther合并他们,并且路由请求到组合的RouterFunction
 - HandlerFunctionAdapter: 简单的对Dispatcher执行HandlerFunction进行适配-处理一个映射的请求
 - ServerResponseResultHandler 处理一个HandlerFunction的执行结果,通过执行ServerResponse的writeTo方法 \
 前面的组件让函数式端点能够在DispatcherHandler 请求处理生命周期中并且底层能够和注解的控制器联合使用,如果任意之一声明,它也知道如何启动函数式端点-通过Spring boot webFlux starter; \
 下面的一个例子展示了WebFlux java配置(查看[DispatcherHandler](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-dispatcher-handler)怎样运行它)
 ```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Bean
    public RouterFunction<?> routerFunctionA() {
        // ...
    }

    @Bean
    public RouterFunction<?> routerFunctionB() {
        // ...
    }

    // ...

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // configure message conversion...
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // configure CORS...
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // configure view resolution for HTML rendering...
    }
}
```
### 过滤处理器方法
类似于拦截器,简单的可以通过使用@ControllerAdvice或者ServletFilter实现,这些过滤器会应用到所有的路由并通过构建者构建,这意味着定义在内嵌级别的路由的过滤器不会应用到全局路由,例如:
```text
RouterFunction<ServerResponse> route = route()
    .path("/person", b1 -> b1
        .nest(accept(APPLICATION_JSON), b2 -> b2
            .GET("/{id}", handler::getPerson)
            .GET(handler::listPeople)
            .before(request -> ServerRequest.from(request) 
                .header("X-RequestHeader", "Value")
                .build()))
        .POST("/person", handler::createPerson))
    .after((request, response) -> logResponse(response)) 
    .build();
```
before过滤器增加了一个自定义的请求头(仅仅应用在两个路由上) \
after.... \
在路由构建者上的filter会产生一个HandlerFilterFunction,这个函数需要一个ServerRequest以及HandlerFunction并返回一个ServerResponse,处理器方法参数出现在链中的下一个元素,这通常是被路由的目标handler,它也可以是其他过滤器(如果应用了多个过滤器) \
现在我们能够增加一个简单的安全过滤器到我们的路由上,假设我们有一个SecurityManager 它能够决定哪一个路由是允许的:
```text
SecurityManager securityManager = ...

RouterFunction<ServerResponse> route = route()
    .path("/person", b1 -> b1
        .nest(accept(APPLICATION_JSON), b2 -> b2
            .GET("/{id}", handler::getPerson)
            .GET(handler::listPeople))
        .POST("/person", handler::createPerson))
    .filter((request, next) -> {
        if (securityManager.allowAccessTo(request.path())) {
            return next.handle(request);
        }
        else {
            return ServerResponse.status(UNAUTHORIZED).build();
        }
    })
    .build();
```
前面的例子说明执行next.handle(ServerRequest)是可选的,我们仅仅让handler方法运行(仅仅当访问允许的时候) \
除了在路由方法构建者上使用filter方法,它也可以应用一个过滤器在已经存在的路由函数上,通过RouterFunction.filter(HandlerFilterFunction)处理 \
CORS对函数式端点的支持通过专用的CorsWebFilter处理