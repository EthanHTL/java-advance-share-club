# DispatcherHandler
spring webflux,类似Spring mvc,围绕前端控制器模式设计,这里存在一个核心的WebHandler,这个DispatcherHandler,提供了一个共享的算法来进行请求处理,当然实际执行的工作是可配置的,可代理组件的. 这个模型是灵活的支持不同(diverse)工作流! \
DispatcherHandler 发现代理组件(从Spring配置中,类似于Spring Mvc),它也设计为作为一个spring bean并且实现了ApplicationContextAware 为了在运行的时候访问上下文,如果DispatcherHandler已经声明了bean名称为webHandler的bean,最终会被WebHttpHandlerBuilder发现,这些将会连同请求处理链保存起来,这些在WebHandler api中有描述! \
Spring configuration 在webFlux应用中通常包含:
- DispatcherHandler(bean 名称为 webHandler)
- webFilter 以及 WebExceptionHandler beans
- DispatcherHandler [Special beans](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-special-bean-types)
- 其他 \
这个配置是通过给定一个WebHttpHandlerBuilder去构建处理链,如下所示:
```text
ApplicationContext context = ...
HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();
```
这导致HttpHandler 在一个[服务器适配器](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-httphandler)中准备好使用！
### Special Bean Types
DispatcherHandler 代理指定的bean来处理请求以及渲染合适的响应,通过"special beans",我们明白Spring管理的对象Object实例实现了WebFlux框架的合约,这些通常往往是内置约定,但是你能够定制它们的属性、扩展它们或者替换它们! \
下面的列表展示了会被DispatcherHandler检测的special bean,注意它们有些其他的bean会在底层被检测(查看[Special  bean types](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-httphandler)-在WebHandler api中的[special bean types](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-web-handler-api-special-beans)) \
bean type              explanation \
HandlerMapping 映射一个请求到处理器,这个映射基于某些条件,详细信息需要查看HandlerMapping的实现-注解的controller或者简单的URL 模式映射,以及其他,主要HandlerMapping实现是一个RequestMappingHandlerMapping 用来处理@RequestMapping注解的方法,RouterFunctionMapping针对函数式端点路由,以及SimpleUrlHandlerMapping针对显示的URL路径模式注册以及WebHandler实例到指定的WebHandler处理!  \
HandlerAdapter 帮助DipatcherHandler执行已经映射的一个处理器方法,不管实际处理器如何执行,举个例子执行一个注解的controller需要解析注解,HandlerAdapter 保护-(shield)屏蔽DispatcherHandler的细节! \
HandlerResultHandler 处理处理器执行的结果并且终结响应,查看[Result Handling](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-resulthandling)
#### WebFlux Config
应用能够声明一个骨架bean(在Web Handler API 以及 DispatcherHandler中列出的那些)-他们需要用来处理请求,然而大多数情况,WebFlux Config式最好开始的起点,他声明了一些需要的bean并且提供了高级的配置回调api进行定制它! \
Spring Boot依赖WebFlux配置去配置Spring WebFlux并且也提供了许多额外的方便的选择!
### Processing
DispatcherHandler 处理请求如下:
- 每一个HandlerMapping 询问去发现匹配一个handler,第一个匹配将会被使用!
- 如果处理器发现,他将通过适合的HandlerAdapter进行运行,这将暴露执行中的返回值作为一个HandlerResult;
- HandlerResult会通过合适的HandlerResultHandler去完成处理-通过直接写入响应或者通过视图进行渲染!
### Result Handling
处理一个来至处理器执行的结果,通过HandlerAdapter进行执行，将包装为一个HandlerResult,连同有些可选的上下文,并传递给第一个声称(Claims)支持它的 HandlerResultHandler,下面的表展示了可用的HandlerResultHandler实现,所有都已经在WebFlux配置中声明! \
Result Handler Type     Return Values   Default Order \
ResponseEntityResultHandler |  ResponseEntity,typically from @Controller instantces | 0
ServerResponseResultHandler | ServerResponse,通常是函数式端点的结果  |  0
ResponseBodyResultHandler | 处理来至@ResponseBody的方法的返回值或者@RestController类的返回值 |  100\
ViewResolutionResultHandler | CharSequence,View,Model,Map,Rendering,或者其他的对象-能够作为一个模型属性的对象[View Resolution](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-viewresolution) | Integer.MAX_VALUE

### 异常
HandlerResult能够暴露一个函数通过某些特定于处理器的机制处理错误,这些错误函数叫做:
- 如果处理器(@Controller)执行失败
- 通过HandlerResultHandler处理失败
这个错误函数能够改变这个响应(例如 一个错误状态码),尽管一个错误信号发生在响应式对象从处理器中返回-且产生了任意的数据项 \
@ExceptionHandler方法在@Controller是被支持的,作为对比,在Spring mvc中具有相同的支持是通过HandlerExceptionResolver处理,这通常应该无关紧要,然而,记住,在webflux中,你不能够使用@ControllerAdvice去处理一个异常(在一个处理器已经选择之前发生的异常!) \
查看在注解的controller部分的[Managing Exceptions](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-ann-controller-exceptions)或者在WebHandler API部分的[异常](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-exception-handler)
### 视图解析
视图解析启用了通过一个html模板的形式以及模型数据(没有让你使用某一种视图技术)而实现渲染,在SpringWebFlux中,视图解析是受支持的(通过一个指定的HandlerResultHandler-使用了ViewResolver实例去映射一个字符串-作为逻辑视图名称-指向一个View实例),这个View会被用来渲染响应！
#### 处理
传递给ViewResolutionResultHandler的HandlerResult包含了从执行器返回的返回值以及在请求执行过程中添加的一些属性,这些返回值将会依次处理:
* String,CharSequence: 一个逻辑视图名将被解析为View,通过已经配置好的ViewResolver实现处理
* void: 选择一个静态视图名称-依据请求路径,最小化领导者以及去掉尾部的斜杠,并解析为视图,当一个视图名没有被提供，那么会执行相同的动作(例如 模型属性被返回了)或者一个异步返回的值(例如 Mono -完成为空)
* Rendering: 视图解析场景的API,在IDE中通过代码提示探索这一部分功能;
* ModelMap:额外的模型属性能够被增加到当前请求的模型中
* 其他数据: 例如其他返回值(除了简单类型,会通过BeanUtils#isSimpleProperty进行检测)他将作为一个可信任的属性添加到Model中,这个属性名会通过约定从类名进行衍生,除非处理器方法上的@ModelAttribute注解出现(约定了属性名!) \
模型能够包含异步,响应式类型(举个例子,来至Reactor或者RxJava),在渲染之前,AbstractView解析这些模型属性到具体的值并更新模型,单个值的Reactive 类型会被解析成单个值或者没有值(如果为空),单多个值的响应式类型(例如Flux<T>)会被收集并解析为Flux<T> \
为了配置视图解析,只需要简单的增加一个ViewResolutionResultHandler bean到Spring 配置中,WebFlux配置提供了专用的配置API进行视图解析! \
查看视图技术学习如何和SpringWebFlux[view Technologies](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-view)集成!

#### Redirecting
前缀"redirect:"可以出现在视图名中让你执行一个重定向,UrlBasedViewResolver(以及子类组织)这个作为一个指令-如果需要重定向,其余部分是重定向的URL; \
网络影响是相同的就像控制器controller返回了一个RedirectView或者Rendering.redirectTo("abc").build(),但是现在controller它本身能够根据逻辑视图名称进行操作!一个视图名称例如: redirect: /some/resource是相对于当前应用的,当一个...重定向互联网!
#### 内容协商
ViewResolutionResultHandler支持内容协商,它比较被每一个选择的View支持的媒体类型和请求的媒体类型,第一个View支持请求的媒体类型的视图将会被使用! \
为了支持例如像JSON、XML之类的媒体类型,SpringFlux提供了HttpMessageWriterView,这是一个特殊的View-通过[HttpMessageWriter](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-codecs)渲染,通常,它能够通过WebFlux配置作为这些作为默认的视图,默认的视图总是被选择并且被使用(如果它匹配这个请求的媒体类型)
### 注解的Controller
没有什么说的
```text
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String handle() {
        return "Hello WebFlux";
    }
}
```
### 请求映射
@RequestMapping 注解被使用来映射一个请求到控制器方法上,现在同Mvc一样它有针对于特定于请求方法的注解!
### URL PATTERNS
这些都可以用在请求路径中
- ? 匹配一个字符 /pages/t?xt.html
- * 匹配0个或者多个字符 /resource/*.png 
- ** 匹配直到路径结尾  "/resources/**/file.png" 无效
- {name: [a-z]+} 可以使用正则表达式匹配路径参数
- {*path} 匹配一个或者多个路径碎片直到路径结尾,取结果为路径变量 /resources/{*file} 匹配 /resources/images/file.png的结果是 /images/file.png \
URL变量会自动的转换为合适的类型或者抛出一个TypeMismatchException,简单类型(int,long,Date,等等)默认支持也可以注册需要支持的其他数据类型-查看[Type Conversion](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-ann-typeconversion) 以及 DataBinder \
路径变量正则匹配demo: 
例如 /spring-web-3.0.5.jar 使用以下表达式捕获
```text
@GetMapping("/{name:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{ext:\\.[a-z]+}")
public void handle(@PathVariable String version, @PathVariable String ext) {
    // ...
}
```
URL 路径模式也可以使用内嵌的${}进行占位符替换(如果在程序一开始启动时都通过PropertyPlaceHolderConfigurer注册了本地、系统、环境、其他属性资源信息),你能够使用这些东西,例如:  在某些扩展配置中参数化一个基础的URL \
注意: Spring WebFlux 使用PathPattern 以及 PathPatternParser 进行URL路径匹配支持,两个类都位于spring-web并且明确的是设计来在运行时在web应用中匹配大量的URL 路径模式; \
Spring WebFlux 不支持 后缀模式匹配 ,不像Spring Mvc,这里有一个映射,例如/person 也能够匹配/person.* 对于一个基于URL的内容协商,如果需要,我们推荐使用查询参数，这更简单,更加明显,并且减少针对于URL路径的攻击!
#### 模式比对
当多个模式匹配到一个URL,它们必须匹配最佳模式,由PathPattern.SPECIFICITY_COMPARATOR,它寻找模式更加特殊! \
每一个模式都会计算一个得分,基于URL变量以及通配符的数量,当URL变量分数低于通配符，总分数越低就更容易胜出,如果两个模式具有相同的分数,更长的路径更容易选择！ \
捕获所有的模式{例如 ** {*VarName}} 不作为计算分数的一部分,它们总是排序到最后,如果两个模式同时捕获,最长的选择!
#### 消费Media Types
能够基于请求的Content-Type 减小请求mapping的访范围
```text
@PostMapping(path = "/pets", consumes = "application/json")
public void addPet(@RequestBody Pet pet) {
    // ...
}
```
这个consumes属性支持取反表达式 例如  !text/plain 意味着内容类型不是text/plain \
能够声明此属性到类级别,不像其他请求mapping属性,当使用在类级别上,一个方法级别的consumes 属性会覆盖不会继承类级别的声明!
#### Producible Media types
能够减小请求mapping的范围通过设定accept请求头的内容类型
```text
]@GetMapping(path = "/pets/{petId}", produces = "application/json")
 @ResponseBody
 public Pet getPet(@PathVariable String petId) {
     // ...
 }
```
同样支持取反表达式
同样支持类级别,行为同consumes
#### 参数以及请求头
例如测试请求参数是否出现,或者缺席,或者指定值
myParam / !myParam / myParam = myValue
```text
@GetMapping(path = "/pets/{petId}", params = "myParam=myValue") 
public void findPet(@PathVariable String petId) {
    // ...
}
```
也能够检查Header是否存在或者等于什么值
```text
@GetMapping(path = "/pets", headers = "myHeader=myValue") 
public void findPet(@PathVariable String petId) {
    // ...
}
```
myHeader  = myValue
#### 自定义注解
支持用户自定义注解并使用,spring webflux同样支持自定义请求映射属性结合自定义请求匹配逻辑,这是更高级的选择需要继承RequestMappingHandlerMapping 并且覆盖getCustomMethodCondition方法,那么你能够检查自定义属性并且返回自己的RequestCondition! \
#### 显示的注册
能够动态注册处理器方法,这对于动态注册或者高级请求可能很有用,例如在不同的URL上使用相同的处理器的不同实例:
```text
@Configuration
public class MyConfig {

    @Autowired
    public void setHandlerMapping(RequestMappingHandlerMapping mapping, UserHandler handler) 
            throws NoSuchMethodException {

        RequestMappingInfo info = RequestMappingInfo
                .paths("/user/{id}").methods(RequestMethod.GET).build(); 

        Method method = UserHandler.class.getMethod("getUser", Long.class); 

        mapping.registerMapping(info, handler, method); 
    }

}
```
### 处理器方法
@RequestMapping 处理器方法有灵活的签名并且能够从受支持的控制器方法(包含参数以及返回值)中进行选择 \
#### 方法参数
响应式类型必须支持(Reactor,RxJava以及其他)-当参数需要阻塞式IO(例如 读取请求体)能够被解析,当不需要阻塞参数时响应式类型是不期待的! \
JDK 1.8 Optional支持作为一个方法参数(等价于required属性)-例如 @RequestParam/@RequestHeader 等价于 required =false
- ServerWebExchange  访问完整的ServerWebExchange-请求以及响应,请求以及会话属性、checkNotModified 方法等其他的容器
- ServerHttpRequest,ServerHttpResponse 访问请求和响应
- WebSession 访问会话,这不会强制开启一个新的会话,除非属性增加过,支持响应式类型!
- java.security.Principal 当前认证的用户-可能是指定的Principal实现,支持响应式类型
- HttpMethod 请求的Http方法
- Locale 当前请求的Locale,由LocaleResolver解析,事实上,是配置的LocaleResolver/LocaleContextResolver
- PathVariable
- MatrixVariable 访问URL路径碎片(k-v键值对)[Matrix Variable](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-ann-matrix-variables)
- RequestParam
- RequestHeader
- CookieValue 访问cookies,cookie values 将转换为声明的方法参数
- RequestBody 将body内容通过HttpMessageReader实例转换为声明的方法参数类型,支持响应式类型
 - HttpEntity<B> 访问请求头和body,body通过HttpMessageReader实例转换，支持响应式类型
 -RequestPart multipart/form-data 表单提交请求的一部分数据,支持响应式类型,查看[Multipart content](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-multipart-forms) 以及 [Multipart Data](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-multipart)
 - Map,Model,ModelMap 访问model实例-实例渲染时将它作为视图渲染的一部分
 - ModelAttribute 使用数据绑定以及校验验证访问model中存在的属性(如果实例不存在),查看@ModelAttribute,Model以及DataBinder,注意@ModelAttribute的使用可选,举个例子如果它设置了属性
 - Errors/ BindingResult 访问校验错误信息,以及对于命令对象的数据绑定,例如@ModelAttribute参数,Errors或者BindingResult必须立即声明在一个验证的方法参数之后,例如@Valid / Validated
 - SessionStatus 以及 class-level @SessionAttributes  用于标记表单处理完成，触发清除通过类级 @SessionAttributes 注释声明的会话属性。有关更多详细信息，请参阅@SessionAttributes。
 - UriComponentsBuilder 预备一个相对于当前请求的host,port,scheme以及上下文路径的URL,查看[URL Links](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-uri-building)
 - @SessionAttribute 访问会话属性(特定于@SessionAttributes定义的一些属性)-对比model属性-它主要存储在会话中(根据类级别的@SessionAttributes所声明的一些会话属性声明)
 - RequestAttribute 访问请求的属性
 任何其他参数，如果上述的参数都没有匹配,默认它将作为一个@RequestParam解析,如果他是一个简单类型,这个判定由BeanUtils.isSimpleProperty决定,否则作为一个@ModelAttribute属性!
 #### Return Values
 支持响应式类型Reactor/RxJava等其他类型
 - ResponseBody 通过HttpMessageWriter实例进行编码且写入响应
 - HttpEntity<B> ResponseEntity<B> 返回值指定了所有响应，包括Http请求头,body(通过HttpMessageWriter实例编码完成的)并写入响应
 - HttpHeaders 响应中返回请求头且无响应body
 - String 被ViewResolver解析的逻辑视图名,处理器方法也能够通过声明一个Model参数填充(enrich)属性!
 - View 视图实例(用来渲染视图的对象-与隐式的Model结合使用)
 - Map/Model 返回的属性将会添加到Model中,视图名隐式的通过请求路径决定
 - @ModelAttribute 将增加这个属性到Model中,视图名基于请求路径选择!
 - Rendering 视图渲染场景以及模型的一个API
 - void void 可能是Mono<Void>,可能异步,返回类型或则null /将考虑请求完全处理完毕(如果他是一个ServerHttpResponse,ServerWebExchange参数或者@ResponseStatus注解),同样考虑完全处理,如果控制器有一个正向的ETag或者lastModified时间戳检查!
 - Flux<ServerSentEvent>,Observable<ServerSentEvent>或者其他响应式类型 提交服务器发送事件,ServerSentEventwrapper能够省略,如果仅仅只有数据需要写入(然而必须是text/event-stream请求或者通过produces属性声明在mapping为此类型的请求)
 - 任何其他返回值,同样的如果上述都不匹配,将默认视为视图名,如果它是一个String或者void(默认的视图名将进行选择),或者作为一个Model属性添加到model中,除非它是一个简单类型,通过BeanUtils#isSimpleProperty见检查,这种情况下它仍可能无法解析!
 #### 类型转换
 控制器方法参数会自动进行类型转换,如果需要自定义转换类型支持,可以通过WebDataBinder定制或者使用FormattingConversionService注册Formatters[Spring field Formatting](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/core.html#format)
 \
 一个常见问题就是类型转换会信任空值,可能为空null,对于包装类型,UUID其他目标对象类型同样,如果值为null可以被注册,可以添加一个required属性到参数注解上,或者声明参数为@Nullable!
 #### matrix variable
 [RFC 3986](https://tools.ietf.org/html/rfc3986#section-3.3) 讨论了路径碎片中的name-value,在spring webflux中,我们参考了"matrix variables"-基于一个"old post" ,但是它们仍然会作为一个URL路径参数引用! \
 一般来说,matrix variable形式是通过分号(semicolon)分割变量,kv通过(=),多个项通过(comma分割)
 "/cars;color=red,green;year=2012"
 当然也支持"color=red;color=green;color=blue". \
 和mvc不同,webflux如果在URL出现了matrix variables不影响请求mapping,换句话说,你不需要使用一个URL变量去掩饰(mask)变量类容,如果你想要在控制器方法中使用matrix 方法,你需要增加一个URL变量到路径碎片上(对期待的matrix变量才需要这样做)
 
 ```text
// GET /pets/42;q=11;r=22

@GetMapping("/pets/{petId}")
public void findPet(@PathVariable String petId, @MatrixVariable int q) {

    // petId == 42
    // q == 11
}
```
能够为matrix变量设置可选或者指定默认值
```text
@GetMapping("/pets/{petId}")
public void findPet(@MatrixVariable(required=false, defaultValue="1") int q) {

    // q == 1
}
```
对于多个matrix 变量可以使用MultiValueMap
```text
// GET /owners/42;q=11;r=12/pets/21;q=22;s=23

@GetMapping("/owners/{ownerId}/pets/{petId}")
public void findPet(
        @MatrixVariable MultiValueMap<String, String> matrixVars,
        @MatrixVariable(pathVar="petId") MultiValueMap<String, String> petMatrixVars) {

    // matrixVars: ["q" : [11,22], "r" : 12, "s" : 23]
    // petMatrixVars: ["q" : 22, "s" : 23]
}
```
#### request param
servlet api "请求参数"概念合并查询参数,表单数据以及multipart为一种,然而webflux中,每一个都需要通过ServerWebExchange独立访问,当@RequestParam仅仅绑定到查询参数,你能够使用数据绑定应用到查询参数、表单数据、以及multiparts到一个[command object](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-ann-modelattrib-method-args) \
此注解可以声明在Map<String,String> 或者MultiValueMap<String,String>上，收集所有的查询参数! \
@RequestParam 是可选的,required or @Nullable
或者Optional
#### RequestHeader
例如:
```text
Host                    localhost:8080
Accept                  text/html,application/xhtml+xml,application/xml;q=0.9
Accept-Language         fr,en-gb;q=0.7,en;q=0.3
Accept-Encoding         gzip,deflate
Accept-Charset          ISO-8859-1,utf-8;q=0.7,*;q=0.7
Keep-Alive              300
```
获取Accept-Encoding and Keep-Alive headers
```text
@GetMapping("/demo")
public void handle(
        @RequestHeader("Accept-Encoding") String encoding, 
        @RequestHeader("Keep-Alive") long keepAlive) { 
    //...
}
```
同样的支持注解Map<String,String> 或者MultiValueMap<String,String> 或者HttpHeaders参数,这会收集所有的请求头参数！\
注意: 对于逗号分割的字符串转换为数组或者字符串集合或者当前约定系统已知的其他类型,例如一个方法参数注解为@RequestHeader("Accept"),或许它可以是String,也可以是String[] 或者List<Sting>
#### cookieValue
...
#### ModelAttribute
能够使用这个注解放置在方法参数上获取Model中的属性(如果不存在则实例化),这个属性能够覆盖查询参数以及表单字段-如果name相同,这意味着数据绑定以及会保存处理解析和转换独立的查询参数以及表单字段-数据绑定,绑定Pet的实例demo如下:
```text
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@ModelAttribute Pet pet) { } 
```
实例流程:
- 从model 获取
- 通过@SessionAttribute获取
- 执行默认构造器
- 主构造器的执行(包含某些参数-匹配查询参数或者表单字段),参数名通过JavaBeans @ConstructorProperties或者运行时在字节码中包含的参数名! \
model属性实例化填充之后,数据绑定使用,WebExchangeDataBinder类匹配查询参数以及表单字段的名称 -在目标Object之上,如果有必要在类型转换之后将匹配的字段进行收集,对于更多的数据绑定以及验证,查看[Validation](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/core.html#validation),对于自定义数据绑定,查看DataBinder; \
数据绑定可能会导致错误,默认来说会抛出一个WebExchangeBindException,但是 可以在控制器方法中加入一个BindingResult 到@ModelAttribute参数之后去捕获错误(因为ModelAttribute也涉及到属性绑定...)
```text
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@ModelAttribute("pet") Pet pet, BindingResult result) { 
    if (result.hasErrors()) {
        return "petForm";
    }
    // ...
}
```
还可以
```text
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@Valid @ModelAttribute("pet") Pet pet, BindingResult result) { 
    if (result.hasErrors()) {
        return "petForm";
    }
    // ...
}
```
可以查看[BEAN Validation](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/core.html#validation-beanvalidation) 以及 [Spring validation](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/core.html#validation) \
在Spring webflux中,不同于mvc,在model中支持响应式类型-例如Mono<Account> 或者 io.reactivex.Single<Account>,你能够声明一个@ModelAttribute参数在一个没有响应式类型wrapper的参数上,它将合理解析,然而,可以使用BindingResult,但是必须在@ModelAttribute参数之前,如果没有响应式类型wrapper,除此之外,能够通过响应式类型处理错误
```text
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public Mono<String> processSubmit(@Valid @ModelAttribute("pet") Mono<Pet> petMono) {
    return petMono
        .flatMap(pet -> {
            // ...
        })
        .onErrorResume(ex -> {
            // ...
        });
}
```
注意@ModelAttribute的使用是可选的,例如  给它设置属性,默认来说,任何参数不一定是简单类型,并且可能不被参数解析器信任就像它已经被@ModelAttribute注释!
#### sessionAttributes
获取在WebSession中的model属性,基于类级别的注解(可能对于某些处理器需要),通常model属性或者model属性的类型应该被透明的存储在会话中为了后续请求访问!
```text
@Controller
@SessionAttributes("pet") 
public class EditPetForm {
    // ...
}
```
在第一个请求中,pet model属性会增加到model中,它会自动的提示并保存到WebSession中,它保存直到另一个处理器方法使用了SessionStatus的方法参数去清理存储!
```text
@Controller
@SessionAttributes("pet") 
public class EditPetForm {

    // ...

    @PostMapping("/pets/{id}")
    public String handle(Pet pet, BindingResult errors, SessionStatus status) { 
        if (errors.hasErrors()) {
            // ...
        }
            status.setComplete();
            // ...
        }
    }
}
```
#### sessionAttribute
可能需要访问预先存在的会话属性(被全局管理的)-例如在控制器之外,过滤器(并且可能存在或者不存在),你能够使用@SessionAttribute注解在某个方法参数上
```text
@GetMapping("/")
public String handle(@SessionAttribute User user) { 
    // ...
}
```
对于需要增加或者删除会话属性,考虑注册一个WebSession到控制器方法中 \
对于model属性的临时存储在会话中作为控制器工作流的一部分,考虑使用SessionAttributes
#### requestAttribute
类似于@SessionAttribute,能使用这个注解访问预先存在的请求属性(被更早的创建的,例如过滤器)
```text
@GetMapping("/")
public String handle(@RequestAttribute Client client) { 
    // ...
}
```
#### Multipart Content
在[Multipart Data](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-multipart)被解释过,ServerWebExchange提供了访问multipart 内容的能力,最好的处理文件上传的形式(例如,从浏览器)在控制器-通过数据绑定到一个command object,例如: 
```text
class MyForm {

    private String name;

    private MultipartFile file;

    // ...

}

@Controller
public class FileUploadController {

    @PostMapping("/form")
    public String handleFormUpload(MyForm form, BindingResult errors) {
        // ...
    }

}
```
@ModelAttribute用来数据绑定command对象!
由于如果请求参数无法解析MyForm,会将它作为一个ModelAttribute属性,可能就涉及到Data Binding; \
你能够在restful服务场景下通过一个非浏览器客户端提交multipart 请求,例如 使用一个伴随JSON的文件: 
```text
POST /someUrl
Content-Type: multipart/mixed

--edt7Tfrdusa7r3lNQc79vXuhIIMlatb7PQg7Vp
Content-Disposition: form-data; name="meta-data"
Content-Type: application/json; charset=UTF-8
Content-Transfer-Encoding: 8bit

{
    "name": "value"
}
--edt7Tfrdusa7r3lNQc79vXuhIIMlatb7PQg7Vp
Content-Disposition: form-data; name="file-data"; filename="file.properties"
Content-Type: text/xml
Content-Transfer-Encoding: 8bit
... File Data ...
```
能够使用@RequestPart独立访问其中的一部分
```text
@PostMapping("/")
public String handle(@RequestPart("meta-data") Part metadata, 
        @RequestPart("file-data") FilePart file) { 
    // ...
}
```
Using @RequestPart to get the metadata.
Using @RequestPart to get the file. \
为了反序列化这个原始内容(例如: 通过@RequestBody),也可以声明一个具体的目标对象,代替Part:
```text
@PostMapping("/")
public String handle(@RequestPart("meta-data") MetaData metadata) { 
    // ...
}
```
你能够使用@RequestPart结合验证,这会进行标准的bean校验,验证错误可能会抛出WebExchangeBindException,导致400响应,这个异常包含了一个BindingResult(包含了错误详细信息以及能够在控制器方法中通过声明一个异步包装器以及使用错误相关的操作符处理)
```text
@PostMapping("/")
public String handle(@Valid @RequestPart("meta-data") Mono<MetaData> metadata) {
    // use one of the onError* operators...
}
```
为了访问所有multipart数据作为MultiValueMap,可以使用@RequestBody
```text
@PostMapping("/")
public String handle(@RequestBody Mono<MultiValueMap<String, Part>> parts) { 
    // ...
}
```
为了后续访问multipart 数据,在流式机制中,你能够使用@RequestBody结合Flux<Part>或者(在kotlin中使用Flow<Part>)替代
```text
@PostMapping("/")
public String handle(@RequestBody Flux<Part> parts) { 
    // ...
}
```
#### RequestBody
.....
唯一不同于mvc,就是支持响应式类型以及完全非阻塞式读取以及(客户端到服务器的流)
```text
@PostMapping("/accounts")
public void handle(@RequestBody Mono<Account> account) {
    // ...
}
```
你能够使用WebFlux的配置的[HTTP message codecs](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-config-message-codecs)相关选择配置或者定制消息readers \
它同样可以支持结合Valid进行校验,验证错误会抛出WebExchangeBindException,导致400响应.....
可以通过异步wrapper声明此类型的参数并采用错误操作符处理
```text
@PostMapping("/accounts")
public void handle(@Valid @RequestBody Mono<Account> account) {
    // use one of the onError* operators...
}
```
#### HttpEntity
它更多或者更少的标识去使用@RequestBody,但是它基于容器对象暴露请求头和请求体:
```text
@PostMapping("/accounts")
public void handle(HttpEntity<Account> entity) {
    // ...
}
```
#### ResponseBody
..... 通过HttpMessageWriter渲染!
它也支持响应式类型,这意味着它能够返回Reactor或者RxJava类型-拥有异步的值 他能会产生-渲染响应,更多信息，查看[Streaming](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-codecs-streaming)以及 JSON渲染[JSON rendering](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-codecs-jackson)\
你能够使用webFlux的配置关于Http message codecs的相关选项配置或者定制消息写入!
#### ResponseEntity
类似于@ResponseBody,但是拥有响应头以及status
```
@GetMapping("/something")
public ResponseEntity<String> handle() {
    String body = ... ;
    String etag = ... ;
    return ResponseEntity.ok().eTag(etag).build(body);
}
```
webflux 支持使用单个值的reactive类型去异步产生ResponseEntity,and/ or 单个或者多个值的reactive 类型产生body,使用ResponseEntity异步响应的变种:
- ResponseEntity<Mono<T>>或者 ResponseEntity<Flux<T>> 确定响应码以及响应头(立即-当在后续的一个点被异步提供之后),使用Mono-如果body由-..1个值组成,如果需要产生多个值使用Flux;
- Mongo<ResponseEntity<T>> 提供了响应码、响应头、响应体-异步产生,允许响应码以及响应头完全依赖于异步请求处理的输出!
- Mono<ResponseEntity<Mono<T>>>或者Mono<ResponseEntity<Flux<T>>> 也有可能,首先提供响应码以及响应头(第一次异步提供这些)然后在是响应体,也是异步的!
#### Jackson JSON
....
#### JSON Views
spring webflux对jackson的序列化视图提供内建支持,这允许渲染所有字段的一个子集,可以和@ResponseBody或者ResponseEntity控制器方法结合使用,你能够使用Jackson的@JsonView注解去集火一个序列化视图类
```
@RestController
public class UserController {

    @GetMapping("/user")
    @JsonView(User.WithoutPasswordView.class)
    public User getUser() {
        return new User("eric", "7!jd#h23");
    }
}

public class User {

    public interface WithoutPasswordView {};
    public interface WithPasswordView extends WithoutPasswordView {};

    private String username;
    private String password;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @JsonView(WithoutPasswordView.class)
    public String getUsername() {
        return this.username;
    }

    @JsonView(WithPasswordView.class)
    public String getPassword() {
        return this.password;
    }
}
```
#### Model
    On a method argument in @RequestMapping methods to create or access an Object from the model and to bind it to the request through a WebDataBinder.

    As a method-level annotation in @Controller or @ControllerAdvice classes, helping to initialize the model prior to any @RequestMapping method invocation.

    On a @RequestMapping method to mark its return value as a model attribute.

    This section discusses @ModelAttribute methods, or the second item from the preceding list. A controller can have any number of @ModelAttribute methods. All such methods are invoked before @RequestMapping methods in the same controller. A @ModelAttribute method can also be shared across controllers through @ControllerAdvice. See the section on Controller Advice for more details.

    @ModelAttribute methods have flexible method signatures. They support many of the same arguments as @RequestMapping methods (except for @ModelAttribute itself and anything related to the request body).
	When a name is not explicitly specified, a default name is chosen based on the type, as explained in the javadoc for Conventions. You can always assign an explicit name by using the overloaded addAttribute method or through the name attribute on @ModelAttribute (for a return value).

除此之外它支持响应式类型，例如Mono<Account>或者 io.reactivex.Single<Account>,例如异步模型属性高也能够透明的被解析(以及模型刷新)到它们实际的值(在@RequestMapping执行的时候),提供了一个@ModelAttribute声明一个参数且不需要wrapper的示例如下:
```text
@ModelAttribute
public void addAccount(@RequestParam String number) {
    Mono<Account> accountMono = accountRepository.findAccount(number);
    model.addAttribute("account", accountMono);
}

@PostMapping("/accounts")
public String handle(@ModelAttribute Account account, BindingResult errors) {
    // ...
}
```
除此之外,任何一个Model属性能够有一个响应式类型wrapper去解析它们实际的value(以及model更新)-仅仅优先于视图渲染之前\
除此之外,将处理器结果作为model属性,这默认是不需要的,@ModelAttribute能够帮助定制模型属性的名称
```text
@GetMapping("/accounts/{id}")
@ModelAttribute("myAccount")
public Account handle() {
    // ...
    return account;
}
```
### DataBinder
@Controller控制器方法以及@ControllerAdvice
可以有一个@InitBinder方法 去初始化一个定制的WebDataBinder
- 可以用来绑定请求参数(表单数据、查询参数)到model对象
- 转换基于String的请求参数(例如请求参数、路径变量、请求头、cookies,其他)到控制器方法参数的目标类型
- 格式化model对象值作为String值(当渲染html形式的时候) \
@InitBinder方法能够注册特定于控制器的PropertyEditor 或者Converter以及 Formatter组件,除此之外你能够使用[WebFlux java 配置](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-config-conversion) 注册Converter 以及Formatter类型到共享的FormattingConversionService;\
@InitBinder方法支持多个相同参数-在@RequestMapping上,除了@ModelAttribute的command object(参数),通常来说它们会声明一个@WebDataBinder参数,用来注册,并且返回值为空
```text
@Controller
public class FormController {

    @InitBinder 
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

    // ...
}
```
除此之外,当使用通过共享FormattingConversionService配置的基础的Formatter,你能够重用相同的方式并且注册一个特定于控制器的Formatter实例
```text
@Controller
public class FormController {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addCustomFormatter(new DateFormatter("yyyy-MM-dd")); 
    }

    // ...
}
```
### 管理异常
通过@ControllerAdvice管理
```text
@Controller
public class SimpleController {

    // ...

    @ExceptionHandler 
    public ResponseEntity<String> handle(IOException ex) {
        // ...
    }
}
```
声明的异常处理器最好是处理目标异常类型,通过注册声明能够减少异常类型的匹配,我们通常推荐尽可能在参数签名中声明并且声明一个主异常映射到@ControllerAdvice中(可以使用合适的顺序指定!)-查看[mvc section](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-exceptionhandler)获取更多 \
在webflux中支持@ExceptionHandler 可以包含和@RequestMapping相同的参数以及返回值,包含请求体的异常以及@ModelAttribute相关的方法参数! \
在Spring webflux中对@ExceptionHandler的支持通过@RequestMapping methods 的HandlerAdapter
#### REST API exceptions
对rest 服务一个普通的需求就是在响应体中包括错误详细信息,spring框架不会自动做,因为错误详细在响应体中的呈现特定于应用,然而@RestController能够使用一个@EexceptionHandler(包含ResponseEntity的返回值去设置状态以及响应的body),例如这些方法也能够声明在@ControllerAdvice中全局使用! \
注意spring webflux 不等价于 SpringMvc的ResponseEntityExceptionHandler,因为WebFlux仅仅抛出ResponseStatusException(或者它的子类)并且它不需要转换为Http状态码!
### controller Advice
.... 例如减少它处理的范围,定义一些全局共享的方法,包括方法的一些执行顺序,查看mvc了解!

