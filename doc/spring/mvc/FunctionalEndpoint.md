## Functional Endpoint
spring web mvc包括了WebMvc.fn,一个轻量级的函数式编程模型(这些函数能够用于路由以及处理请求以及是对不可变的设计协商),基于注解的编程模型的替代方案,但是运行在同一个DispatcherServlet;
#### OverView
在webMvc.fn中,请求通过HandlerFunction处理,此函数携带了ServerRequest以及返回一个ServerResponse,请求和响应对象必须满足不可变的条件(JDK8能够友好访问http request以及响应),HandlerFunction是一个@RequestMapping方法函数体的等价物(在基于注解的编程模型中) \
进入的请求将通过RouterFunction路由到处理器函数,一个函数能够携带一个ServerRequest并且返回一个可选的HandlerFunction(或者,Optional<HandlerFunction>),当路由函数匹配之后,handler函数就会返回,否则是一个空的Optional,RouterFunction是@RequestMapping注解的一个等价物,但是路由函数主要不同是提供的不仅仅是数据,而且提供行为; \
RouterFunctions.route()提供了一个路由构建者(促进创建路由器),例如:
```java
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.*;
import static org.springframework.web.servlet.function.RouterFunctions.route;

PersonRepository repository = ...
PersonHandler handler = new PersonHandler(repository);

RouterFunction<ServerResponse> route = route()
    .GET("/person/{id}", accept(APPLICATION_JSON), handler::getPerson)
    .GET("/person", accept(APPLICATION_JSON), handler::listPeople)
    .POST("/person", handler::createPerson)
    .build();


public class PersonHandler {

    // ...

    public ServerResponse listPeople(ServerRequest request) {
        // ...
    }

    public ServerResponse createPerson(ServerRequest request) {
        // ...
    }

    public ServerResponse getPerson(ServerRequest request) {
        // ...
    }
}
```
如果你注册RouterFunction作为一个bean,你需要通过@Configuration暴露,他将自动被servlet检测,在[Runing a Server](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#webmvc-fn-running)解释
#### HandlerFunction
ServerRequest以及ServerResponse是一个不变的接口(JDK 8能够友好访问请求和响应),包括headers,body,method,状态码;
##### ServerRequest
它提供了访问HttpMethod,URI、Headers以及查询参数的功能,访问body通过body函数访问; \
```java
String string = request.body(String.class);
```
上述例子是访问一个String类型的请求体; \
下面的例子将body抓取到List<Person>,这里的Person对象从一个序列化的形式解码,例如JSON或者XML
```java
List<Person> people = request.body(new ParameterizedTypeReference<List<Person>>() {});
```
下面的例子展示了如何访问参数
```java
MultiValueMap<String, String> params = request.params();
```
##### ServerResponse
ServerResponse 提供了访问Http响应能力,因为他是不可变的,你能够使用一个build方法去创建它,你能够使用构建者去设置响应状态、增加响应头或者提供响应体,下面的例子展示了200状态码且JSON内容响应体:
```java
Person person = ...
ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(person);
```
下面的例子展示了201响应且包含了一个Location响应头以及no body:
```java
URI location = ...
ServerResponse.created(location).build();
```
你还可以使用异步结果作为body,例如CompletableFuture的形式,Publisher,或者被ReactiveAdapterRegistry支持的其他类型:
```java
Mono<Person> person = webClient.get().retrieve().bodyToMono(Person.class);
ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(person);
```
如果不仅仅是body,并且状态、headers(基于异步类型),你能够使用一个ServerResponse的静态async方法,它接受CompletableFuture<ServerResponse>,Publisher<ServerResponse>或者被ReactiveAdapterRegistry支持的其他异步类型:
```java
Mono<ServerResponse> asyncResponse = webClient.get().retrieve().bodyToMono(Person.class)
  .map(p -> ServerResponse.ok().header("Name", p.name()).body(p));
ServerResponse.async(asyncResponse);
```
服务端发送事件能够提供一个ServerResponse.sse方法提供,此构建者(通过此方法能够让你发送字符串或者作为JSON的对象):
```java
public RouterFunction<ServerResponse> sse() {
    return route(GET("/sse"), request -> ServerResponse.sse(sseBuilder -> {
                // Save the sseBuilder object somewhere..
            }));
}

// In some other thread, sending a String
sseBuilder.send("Hello world");

// Or an object, which will be transformed into JSON
Person person = ...
sseBuilder.send(person);

// Customize the event by using the other methods
sseBuilder.id("42")
        .event("sse event")
        .data(person);

// and done at some point
sseBuilder.complete();
```
##### Handler class
本质上handler function 就是lambda,于是
```java
HandlerFunction<ServerResponse> helloWorld =
  request -> ServerResponse.ok().body("Hello World");
```
为了方便,一个应用必然需要多个函数,一个多行lambda看起来非常乱,因此将handler function放入一个handler类中,非常相似@Controller(基于注解的应用),例如以下的类暴露一个响应式Person仓库:
```java
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class PersonHandler {

    private final PersonRepository repository;

    public PersonHandler(PersonRepository repository) {
        this.repository = repository;
    }

    public ServerResponse listPeople(ServerRequest request) { 
        List<Person> people = repository.allPeople();
        return ok().contentType(APPLICATION_JSON).body(people);
    }

    public ServerResponse createPerson(ServerRequest request) throws Exception { 
        Person person = request.body(Person.class);
        repository.savePerson(person);
        return ok().build();
    }

    public ServerResponse getPerson(ServerRequest request) { 
        int personId = Integer.parseInt(request.pathVariable("id"));
        Person person = repository.getPerson(personId);
        if (person != null) {
            return ok().contentType(APPLICATION_JSON).body(person);
        }
        else {
            return ServerResponse.notFound().build();
        }
    }

}
```
*  listPeople 是一个handler 函数返回所有的Person对象并返回JSON
* createPerson 是一个处理器函数存储一个新的Person对象(先从请求体中获取body)
* getPerson 是一个处理器函数(返回单个person),通过id标识为路径变量,我们从仓库中抓取Person并创建一个Json响应,如果他被发现,如果没有发现返回404
##### Validation
函数endpoint 能够使用spring的验证功能去对请求体进行校验,例如给定一个自定义spring 验证器实现:
```java
public class PersonHandler {

    private final Validator validator = new PersonValidator(); 

    // ...

    public ServerResponse createPerson(ServerRequest request) {
        Person person = request.body(Person.class);
        validate(person); 
        repository.savePerson(person);
        return ok().build();
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
handler也能够使用标准的bean 验证api(JSR-303)通过创建并注册一个全局的Validator实例(基于LocalValidatorFactoryBean),查看Spring 验证获取更多;
#### RouterFunction
有一个创建router函数的快捷工具类RouterFuncitons,例如RouterFunctions.route()提供了一个流式的构建者用于创建路由函数,然而RouterFunctions.route(RequestPredicate,HandlerFunction)是更直接的一种创建路由器的方式 \
通常推荐使用无参形式,因为它为典型的映射场景提供了方便的快捷方式，而无需难以发现的静态导入,对于实例来说,路由函数构建者提供了get(string,HandlerFunction)去创建一个映射到Get请求的映射,以及Post(String,HandlerFunction)处理post请求; \
除了http方法映射,路由构建者提供了一种方式去推断可选的条件(当映射一个请求时),每一个http方法它是一个重载的变种(它使用了RequestPredicate作为参数),尽管一些额外的约束能够表达;
##### Predicates
你能够写入子集的RequestPredicate,但是RequestPredicates工具类提供了通用使用实现,基于请求路径、http方法、content-Type等等其他方式,以下就是一个使用请求判断创建一个基于Accept请求头 约束的请求:
```java
RouterFunction<ServerResponse> route = RouterFunctions.route()
    .GET("/hello-world", accept(MediaType.TEXT_PLAIN),
        request -> ServerResponse.ok().body("Hello World")).build();
```
你也能够组合多个请求条件通过使用:
* RequestPredicate.and(RequestPredicate)-both 必须匹配
* RequestPredicate.or(RequestPredicate) - 或者匹配 \
许多predicates(来自RequestPredicates的)能够组合,例如RequestPredicates.get(String)从RequestPredicates.method(HttpMethod)以及RequestPredicates.path(String)的组合提供的,上面的例子展示了如何使用两个Request进行结合,作为构建者使用RequestPredicate.GET(内部使用),并且组合了accept条件;
#### Routes
路由器函数存在顺序执行: 如果第一个路由没有匹配,那么第二个继续评估,因此它能够在通用路由之前声明多个特殊路由,这是非常重要的,当注册的路由函数作为一个Spring bean,将在后面描述,注意这个行为不同于基于注解的编程模型,这里"大多数特殊"控制器方法将自动捆绑; \
当使用一个路由函数构建者,所有定义的路由将组合到一个RouteFunction(从build()返回),这里也有组合多个路由函数的其他方式:
* add(RouteFunction)-> 在RouterFunctions.route构建者上的方法
* RouterFunction.add(RouterFunction)
* RouterFunction.addRoute(RequestPredicate,HandlerFunction) RouterFunction.add和内嵌的RouterFunctions.route的快捷方式 \
下面是一个组合4个路由的demo:
```java
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.*;

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
##### 内嵌路由
对于存在公共条件的路由函数进行分组的通用手段,一个共享路径的实例,在上一个例子中,这个共享的predicate可能只有一个predicate匹配/person,被三个路由所使用,当使用注解时,你能够移除重复的路径通过使用type-level@RequestMapping注解去映射/person,在WebMvc.fn,路径predicate能够通过在router函数构建者上的path方法共享路径,上面的例子能够通过很少的代码进行优化-内嵌路由:
```java
RouterFunction<ServerResponse> route = route()
    .path("/person", builder -> builder 
        .GET("/{id}", accept(APPLICATION_JSON), handler::getPerson)
        .GET(accept(APPLICATION_JSON), handler::listPeople)
        .POST("/person", handler::createPerson))
    .build();
```
虽然基于路径的嵌套是最常见的，你能够通过nest方法去内嵌任何种类的predicate,上述包含了共享Accept-header条件形式的重复配置,我们可以继续优化,使用nest方法去集中accept:
```java
RouterFunction<ServerResponse> route = route()
    .path("/person", b1 -> b1
        .nest(accept(APPLICATION_JSON), b2 -> b2
            .GET("/{id}", handler::getPerson)
            .GET(handler::listPeople))
        .POST("/person", handler::createPerson))
    .build();
```
####  运行服务器
通常运行一个基于DispatcherHandler的路由函数配置可以通过mvc配置,将使用spring配置去声明组件(需要处理请求),mvc java配置声明了以下基础设施组件去支持函数式endpoints:
* RouterFunctionMapping: 检测一个或者多个RouterFunction<?> bean(在spring配置中),排序,通过RouterFunction.andOther进行合并并且路由请求到组合RouterFunction中
* HandlerFunctionAdapter 简单适配器(让DispatcherHandler执行一个处理器方法(handlerFunction)-映射到当前请求的处理器方法) \
前面的组件让函数式endpoints 完善了DispatcherServlet请求处理声明周期以及还（可能）与带注释的控制器并排运行,如果声明了,它也知道如何通过spring boot web starter启动函数式 端点; \
以下是启动一个server的demo:
```java
@Configuration
@EnableMvc
public class WebConfig implements WebMvcConfigurer {

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
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
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
#### 过滤 处理器函数
仍然能够使用过滤器函数调用before,after,filter方法在routing function builder上,包括注解,你能够实现类似的功能通过使用@ControllerAdvice、SERVLETFilter,或者两者同时配置,此过滤器将应用到所有路由(通过此route function builder构建的路由),这就意味着定义在内嵌路由中的过滤器不能够应用到顶级路由,例如:
```java
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
在router builder上的filter方法携带一个HandlerFilterFunction,此函数携带了ServerRequest以及HandlerFunction并且返回了一个ServerResponse.这个处理器函数参数表达了此过滤链的下一个元素,通常是应该路由的目标handler,如果应用了多个,他也可能是一个其他的过滤器; \
现在我们能够增加一个简单的安全过滤器到我们的路由中,假设我们有一个SecurityManager(它能够判断一个特殊的路径是否允许访问),例如:
```java
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
前面例子中证实了执行next.handle(ServerRequest)是可选的,我们只需要当可以访问的时候执行handler 函数; \
除了使用filter方法（在路由函数构建器中),它也可以应用一个过滤器到存在的路由函数,通过RouterFunction.filter(HandlerFilterFunction),对于函数式endpoint的跨域支持通过提供一个专用的CorsFilter即可;