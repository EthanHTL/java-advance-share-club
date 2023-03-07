## Annotated Controllers
spring mvc提供了一个基于注解的程序设计模型(这里@Controller以及@RestController组件能够使用此注解展示请求映射 -> requestMappings),请求输入,异常处理以及更多,注解的控制器(controller)有非常灵活的方法签名以及不用扩展基类或者实现指定接口,下面是一个demo:
```java

@Controller
public class HelloController {

    @GetMapping("/hello")
    public String handle(Model model) {
        model.addAttribute("message", "Hello World!");
        return "index";
    }
}
```
前一个例子中，方法接受一个Model以及返回一个String类型的视图名,但是还有其他许多选项在后面进行描述; \
spring官网指南教程教你快速[上手](https://spring.io/guides)
##### 声明
@Controller bean的注入方式可以是通过SERVLET的WebApplicationContext进行注册,或者开启基于类路径扫描组件的方式,因为这些注解本质上都是@Component的模板注解,但是意味着这是一个Web组件;
```java
@Configuration
@ComponentScan("org.example.web")
public class WebConfig {

    // ...
}
```
等价于:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="org.example.web"/>

    <!-- ... -->

</beans>
```
@RestController  是一个组合注解(它是@Controller注解和@ResponseBody注解的组合,只是一个controller 的每一个方法都需要继承类级别的@ResponseBodyd注解),它直接向响应体中写出信息,而不是使用视图解析方案并使用Html模板渲染一个页面;
###### Aop 代理(推荐基于类代理)
在某些类中,你可能需要通过一个aop代理运行时装饰controller,一个例子就是如果你选择直接在controller上设置@Transactional注解,在这种情况下,controller比较特殊,我们推荐使用基于类的代理,通常默认选择就是controller,然而,如果一个控制器(controller)必须实现一个接口但是不是一个Spring Context回调(例如InitializingBean,*Aware以及其他),你需要显式配置基于类的代理,举个例子通过<tx:annotation-driven/> 你能够改变为<tx:annotation-driven proxy-target-class="true"/>并且@EnableTransactionManagement你能够改变为@EnableTransactionManagement(proxyTargetClass=true)
##### 请求映射
你能够使用@RequestMapping 注解去映射一个请求到控制器方法,他有各种各样的属性去匹配URL,http方法,请求参数，headers,以及media types,你能够使用它到类级别上去表达一个共享的映射或者在方法级别上限制一个特殊的方面映射; \
@RequestMapping的变种有很多
* GetMapping
* PostMapping
* PutMapping
* DeleteMapping
* PatchMapping\
快捷方式是提供自定义注解,可明确的,大多数控制器方法应该映射到一个特殊的Http方法(对照@RequestMapping,这个注解默认匹配所有的Http方法),同样@RequestMapping仍然需要在类级别上表达共享的映射;
```java
@RestController
@RequestMapping("/persons")
class PersonController {

    @GetMapping("/{id}")
    public Person getPerson(@PathVariable Long id) {
        // ...
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody Person person) {
        // ...
    }
}
```
##### URI 匹配模式
@RequestMapping 能够使用URL 模式进行映射
* PathPattern 与 URL 路径匹配的预解析模式也预解析为 PathContainer,设计为Web使用,使用此解析方案处理编码以及路径参数非常高效,并且高效匹配;

* AntPathMatcher 针对字符串路径匹配字符串模式,最开始是被Spring配置用来选择类路径上、文件系统、以及其他位置的资源，非常低效并且字符串路径输入是一个挑战(有效的处理编码以及URLs的其他问题) \
PathPattern 是一个web应用的推荐方案并且在Spring WebFlux中是唯一选择,在5.3之前,AntPathMatcher 是Spring mvc的唯一选择并且默认配置,然而PathPattern 能够在MVC Config中启动 \
PathPattern 支持AntPathMatcher的相同的模式语法,它还支持捕获模式,例如 {*spring},匹配0个或者多个路径碎片(已spring结尾的路径),PathPattern 也限制了**的使用(匹配多个路径碎片),这个写法仅仅能够出现在模式的末尾,在大多数模糊的情况下是抹除了 (当对给定的请求获取最近啊的匹配模式),对于更多模式语法请参考[PathPattern](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/javadoc-api/org/springframework/web/util/pattern/PathPattern.html)以及[AntPathMatcher](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/javadoc-api/org/springframework/util/AntPathMatcher.html) \
一些模式例子:
* "/resources/ima?e.png" 匹配路径碎片的一个字符,例如image / imaee.png 都是可以的
* "/resources/*.png" 匹配0个或者多个字符
* "/resources/**" 多个路径碎片
* "/projects/{project}/versions" 匹配一个路径碎片并且捕获它作为一个变量
* "/projects/{project:[a-z]+}/versions" 通过一个正则表达式捕获路径碎片并作为变量 \
捕获的变量可以通过@PathVarible进行访问:
```java
@GetMapping("/owners/{ownerId}/pets/{petId}")
public Pet findPet(@PathVariable Long ownerId, @PathVariable Long petId) {
    // ...
}
```
你也能够声明URI变量在类或者方法级别上
```java
@Controller
@RequestMapping("/owners/{ownerId}")
public class OwnerController {

    @GetMapping("/pets/{petId}")
    public Pet findPet(@PathVariable Long ownerId, @PathVariable Long petId) {
        // ...
    }
}
```
URI 变量将自动转换为 合适的类型变量,或者抛出一个TypeMismatchException异常,简单类型(int,long,Date 以及等等)默认 支持并且你能够注册你想要支持的任何数据类型,查看[Type Conversion](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-typeconversion)以及 [DataBinder](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-initbinder) \
你能够显式的设置命名URI 变量(例如@PathVariable("customId")),但是，如果名称相同并且您的代码是使用调试信息或 Java 8 上的 -parameters 编译器标志编译的，则您可以省略该细节 \
{varName:regex}语法声明了一个URI变量(使用普通表达式),例如URL "/spring-web-3.0.5.jar",根据以下方法抓取名称,版本、文件扩展名:
```java
@GetMapping("/{name:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{ext:\\.[a-z]+}")
public void handle(@PathVariable String name, @PathVariable String version, @PathVariable String ext) {
    // ...
}
```
URI路径模式也能够有一个内嵌的${...}占位符它能够在启动的时候通过PropertyPlaceHolderConfigurer进行解析(主要利用了局部变量、系统、环境、以及其他属性资源进行解析),你能够使用这个特性,例如: 根据外部化配置参数化基本URL;
##### 模式比较
当使用多个模式匹配一个URL时,最好的一个将会被选择,通过下面之一完成(依赖于解析的PathPattern是否启动使用或者禁止):
* [PathPattern.SPECIFICITY_COMPARATOR](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/javadoc-api/org/springframework/web/util/pattern/PathPattern.html#SPECIFICITY_COMPARATOR)
* [AntPathMatcher.getPatternComparator(String path)](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/javadoc-api/org/springframework/util/AntPathMatcher.html#getPatternComparator-java.lang.String-) \
两者都有助于对顶部有更具体的模式进行排序,一个模式具有较少的URL变量(计数1),单个通配符(计为1),双通配符(计数2),则该模式不太确定,给定相同的分数,选择较长的模式,相同长度的分数,URI变量比通配符更多的路径将被选择; \
默认映射模式(/**)从成绩中排除并且总是排在最后,同样前缀模式(例如 /public/**) 的优先级也比其他没有双通配符的模式低; \
对于更多完整信息,查看上面链接了解pattern Comparators;
##### 后缀匹配
从5.3开始mvc不在执行.*后缀模式匹配(当一个控制器映射到/person,同样会隐式映射到/person.*),结果就是路径扩展不在使用进行请求内容类型的判断来进行响应,例如 /person.pdf,/person.xml 以及其他; \
当浏览器过去常常发送难以一致解释的 Accept 标头时，以这种方式使用文件扩展名是必要的,现在不再需要必要,并且Accept请求头应该是更好的选择; \
随着时间推移,文件名扩展的使用证明这种方式的变种存在问题,当与 URI 变量、路径参数和 URI 编码的使用重叠时，它可能会导致歧义,例如基于URL的认证以及安全(查看下一部分获取更多)往往会变得更加困难; \
为了完全的禁用路径扩展的使用(在5.3之前),你可以这样做:
* useSuffixPatternMatching(false),查看[PathMatchConfigurer](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-config-path-matching)
* favorPathExtension(false),查看[ContentNegotiationConfigurer](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-config-content-negotiation) \
使用一种方式请求内容的类型而不是通过"Accept"请求头仍然是有用的,例如浏览器中的一个URL,一个安全的可选路径后缀是使用查询参数策略,如果你必须使用文件扩展,考虑限制它们并将它们显式注册(通过ContentNegotiationConfigurer的mediaTypes属性注册)
##### 后缀匹配以及RFD
 反射文件下载攻击类似与XSS(它依赖于请求输入 =》 例如查询参数以及URI变量)可能反映到响应中,然而除了插入脚本到html中,一个RFD攻击依赖浏览器开启执行一个下载并且信任此响应作为一个可执行脚本(当双击之后) \
 spring mvc中,@ResponseBody以及ResponseEntity方法存在危险,因为它们能够渲染不同的内容类型,这些客户端能够通过URL 路径扩展进行请求,禁用后缀模式匹配并且使用路径扩展进行内容协商风险更低但是不满足执行RFD攻击; \
 为了阻止RFD攻击,在渲染响应体之前,Spring mvc会增加一个Content-Disposition:inline;filename=f.txt的响应头去建议一个固定且安全的下载文件,它只会在URL路径包含了一个文件扩展才会触发此动作(文件扩展要么允许安全或者显式对内容协商进行注册的文件类型扩展),当 URL 直接输入浏览器时，它可能会产生副作用 \
 许多普通路径扩展默认是安全的,应用能够自定义HttpMessageConverter实现能够显式的注册文件扩展进行内容协商去避免对这些扩展产生一个Content-Disposition响应头,查看[Content Types](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-config-content-negotiation)获取更多,查看[CVE-2015-5211](https://pivotal.io/security/cve-2015-5211)的RFD相关的可选推荐信息; \
 ##### 自定义Media 类型
 能够限制请求的Content-type,例如:
 ```java
@PostMapping(path = "/pets", consumes = "application/json") 
public void addPet(@RequestBody Pet pet) {
    // ...
}
```
只需要使用consumes属性即可; \
consumes属性也支持取反表达式-例如: !text/plain 意味着任何Content-Type都接受,唯独不接受text/plain; \
也能够声明consumes属性在类级别上,不像大多数其他请求映射属性,然而在类级别上使用时,方法级别的consumes属性会覆盖类级别consumes属性而不是继承类级别声明; \
MediaType 提供了对大多数media types的使用约束,例如APPLICATION_JSON_VALUE以及APPLICATION_XML_VALUE;
##### producible Media Types
你能够限制请求映射基于Accept请求头并且要求controller的方法产生指定ContentType的内容,例如:
```java
@GetMapping(path = "/pets/{petId}", produces = "application/json") 
@ResponseBody
public Pet getPet(@PathVariable String petId) {
    // ...
}
```
使用produces属性按内容类型缩小映射范围 \
media类型能够指定一个字符集合,也支持取反表达式,例如!text/plain; \
你能够声明共享性质的produces属性在类级别上,不像其他请求映射属性,使用在类级别上时,方法级别的produces会覆盖而不是继承类级别的声明;

##### 参数、请求头
你能够限制请求映射-基于请求参数条件,你能够测试一个请求参数(myParam是否出现在请求中),对于参数缺失可以使用!myParam表达式,或者需要参数为指定值 myParam = myValue,例如:
```java
@GetMapping(path = "/pets/{petId}", params = "myParam=myValue") 
public void findPet(@PathVariable String petId) {
    // ...
}
```
你同样能够使用请求头条件,例如
```java
@GetMapping(path = "/pets", headers = "myHeader=myValue") 
public void findPet(@PathVariable String petId) {
    // ...
}
```
你能够通过header条件匹配Content-Type 以及 Accept请求头,但是更好使用consumes以及produces进行替换;
##### HTTP HEAD,OPTIONS
@GetMapping支持http Head 的请求映射,控制器方法不需要做任何改变,一个响应wrapper,应用在javax.servlet.http.HttpServlet中,确保Content-Length请求头控制写入的字节数量(实际上并没有写入) \
同样支持隐式映射以及支持Http HEAD,一个head请求会被处理(就好像它是一个GET请求),但并没有写入消息,只是计算字节数并设置Content-Length响应头 \
默认请求Http Options是通过设置Allow响应头来处理的(将它设置为指定的请求方法,mvc规定的 get,post,put,delete,head,patch)且必须包含匹配URL 模式; \
对于一个没有声明HTTP方法的@RequestMapping,这个allow header默认是设置为所有请求方法的方式,控制器方法能够总是声明这些支持的http 方法,例如通过指定的Http方法去指定: @GetMapping,而不是使用@RequestMapping \
你能够显式的映射@RequestMapping方法到HTTP HEAD 以及 HTTP OPTIONS,但是在普通的情况下这是不必要的;
##### 自定义注解
mvc 支持混合注解的使用来进行请求映射,这些注解都是使用@RequestMapping作为元注解并组合其他注解作为一个@RequestMapping属性限制的一个子集作用域注解来达到限制的目的或者其他目的; \
尽量使用@RequestMapping的指定Http方法版本,因为每一个请求都应该有具体的http方法类型 \
mvc 支持自定义@Request-mapping属性(通过自定义的请求匹配逻辑)进行覆盖,这是一种更加高级的选择，它需要一个RequestMappingHandlerMapping的子类去覆盖getCustomMethodCondition方法,这样您能够检测你自己的属性并返回你自己的RequestCondition;
##### 隐式注册
你能够编程式注册handler方法,如果为了复杂情况你可以使用动态注册,例如不同URLs下的相同处理器的不同实例,下面注册了一个handler方法:
```java
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
* 拦截目标处理器(handler)以及控制器的handler Mapping
* 准备请求映射元数据
* 获取handlerMethod
* 增加注册
####  处理器方法
@RequestMapping 处理器的方法有很灵活的签名并且能够选择可以从一系列支持的控制器方法参数和返回值中进行选择;
##### 方法参数
这张表描述了支持的控制器方法参数,响应式类型不对任何参数提供支持\
JDK8的 Optional支持作为一个方法参数且结合@RequestParam(包含了required属性的注解),等价于required=false;
* controller method argument   description
* WebRequest,NativeWebRequest  能够访问请求参数以及请求、会话属性，没有直接调用Servlet API;
* javax.servlet.ServletRequest 选择一个指定的请求或者响应类型,例如 ServletRequest,HttpServletRequest或者Spring的MultipartRequest,MultipartHttpServletRequest;
* javax.servlet.http.HttpSession 强制会话出现,因此,此参数决不能为空null,注意会话访问不是线程安全的,考虑设置RequestMappingHandlerAdapter实例的synchronizeOnSession标志为true,如果多个请求允许并发访问一个回话;
* javax.servlet.http.PushBuilder Servlet4.0发布了构建者API(为了编程式http/2资源推送),注意每一个Servlet规范,拦截的PushBuilder可能为空,如果客户端不支持Http/2特性;
* java.security.Principal 当前信任的用户,可能是一个知晓的Principal实现,注意这个参数不会过早解析,如果放置了此参数,那么可以在通过HttpServletRequest#getUserPrincipal默认解析之前通过自定义解析器进行解析,例如Spring Security Authentication实现了Principal并且能够通过能够HttpServletRequest#getUserPrincipal注入到参数上,除非你使用@AuthenticationPrincipal注解,这种情况下会导致自定义的Spring security参数解析器进行解析(通过Authentication#getPrincipal解析)
* HttpMethod 请求的http 方法
* Locale 当前请求的Locale,由可用的LocaleResolver推断(最好配置一个LocaleResolver或者LocaleContextResolver)
*TimeZone + ZoneId 与当前请求联系的time zone,由LocaleContextResolver决定
* InputStream,Reader 访问请求体(通过Servlet api暴露的)
* OutputStream Writer 访问通过Servlet api暴露的响应体
* @PathVariable 访问URI模板变量
* MatrixVariable 为了访问URI路径碎片中的key-value[键值对](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-matrix-variables)
* @RequestParam 为了访问Servlet 请求参数,包括上传文件. 参数变量能够转换为方法参数类型,注意@RequestParam的使用式可选的,对于简单参数值来说...,查看"Any other argument",此表末尾
* @RequestHeader 访问请求头,请求头能够转换为指定的类型
* CookieValue 访问cookie,cookie值能够转换为方法参数类型
* @RequestBody 访问Http请求体,body内容类型自动转换,通过HttpMessageConverter实现转换;
* HttpEntity<B>  访问请求头和请求体,请求体能够被HttpMessageConverter处理;
* RequestPart 访问multipart/form-data请求的一部分,同样通过HttpMessageConverter进行转换
* Map,Model,ModelMap 访问model(能够被html 控制器使用并暴露给模板作为渲染视图的一部分)
* RedirectAttributes 在重定向中使用的一些特殊属性(能够追加到查询参数字符串上)以及flash属性将会临时存储直到请求重定向之后
* ModelAttribute 为了访问Model上存在的属性(如果不存在则会实例化)并且使用了数据绑定以及认证,查看@ModelAttribute 以及Model、DataBinder; 注意@ModelAttribute的使用是可选的(例如,设置它的属性),查看"Any other argument",此表末尾
* Errors,BindingResult 访问验证错误以及命令对象数据绑定错误,是一个@ModelAttribute参数,或者来自@RequestBody、@RequestPart的验证的错误,你必须声明一个Errors或者BinderResult参数在认证的方法参数之后(紧接着验证参数)
* SessionStatus + class-level的@SessionAttributes  用于标记表单处理完成，触发清除通过类级 @SessionAttributes 注释声明的会话属性。有关更多详细信息，请参阅@SessionAttributes
* UriComponentsBuilder 为了预准备一个URL相关的当前请求的host,port,schema,context path,以及Servlet 映射的文字部分,详情查看URI链接;
* @SessionAttribute 访问一个session属性,对比model属性,它存储在session中(声明一个类级别的@SessionAttributes),那么方法结束之后@SessionAttribute会存储在会话中;
* @RequestAttribute 访问请求属性
* 任何其他参数 如果方法参数如果不能表中前面所提到的值并且它是一个简单类型(那么由BeanUtils#isSimpleProperty决定),会将它作为一个@ReqeustParam进行解析,否则会将它解析为@ModelAttribute
##### 返回参数
下表列出了controller支持返回的所有value类型,响应式类型支持所有返回类型;
* returnValue            description
* @ResponseBody   通过HttpMessageConverter实现进行转换并写入响应
* HttpEntity<B>,ResponseEntity<B> 此返回值包含了响应的全部内容(包括http headers以及body)能够通过HttpMessageConverter转换并写入响应;
* HttpHeaders 返回一个仅仅包含headers的响应
* String 能够通过ViewResolver解析的视图名称结合隐式的model使用 - 通过命令对象以及@ModelAttribute方法确定,处理器方法能够编程式丰富Model(通过声明一个Model参数)
* View 一个视图实例需要结合隐式model进行渲染-通过命令对象以及@ModelAttribute方法决定,这个处理器方法也能够编程式丰富model(通过声明一个Model参数)
* Map,Model 默认会将属性隐式添加到Model中,视图名称将通过RequestToViewNameTranslator进行决定;
* @ModelAttribute 标志着一个属性能够增加到@ModelAttribute上,视图名隐式的通过RequestToViewNameTranslator进行决定,@ModelAttribute是可选的
* ModelAndView对象 视图和model属性可以设置,还可以设置响应状态码
* void 方法能够返回void(或者null)将考虑已经完全处理响应了(如果它还有一个ServletResponse参数,OutputStream、@ResponseStatus注解),如果控制器有一个积极的ETag或者lastModified时间戳检测等价于前一种; 如果都不满足,void也指示了对于rest控制器没有响应体或者没有选择一个html控制的视图名
* DeferredResult<V> 生产前面返回值之一(从线程中异步)-例如: 由于事件或者回调,查看[Asynchronous Requests](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-async) 以及 DeferredResult;
* Callable<V> 在spring mvc管理的线程中异步返回一个响应值;
* ListenableFuture<T>,CompletionStage<V>,CompletableFuture<V> 同DeferredResult
* ResponseBodyEmitter,SseEmitter  异步的通过HttpMessageConverter写出响应,同样支持作为ResponseEntity的内容(body),查看[async request](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-async)以及 [http streaming](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-async-http-streaming)
* StreamingResponseBody 异步的写入到OutputStream中,同样支持作为ResponseEntity的body,查看异步请求以及http streaming;

* 响应式类型-Reactor,RxJava,或者通过ReactiveAdapterRegistry的其他类型 使用收集到列表的多值流（例如 Flux、Observable）替代 DeferredResult. 对于streaming 场景(例如: text/event-stream,application/json+stream),SseEmitter以及ResponseBodyEmitter的替代使用,ServletOutputStream 阻塞式IO执行在mvc的线程上并且确保写入完成,查看[Reactive Types](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-async-reactive-types)
* 如果都没有匹配上述的值并且他是一个字符串或者void 将被信任作为一个视图名(默认通过RequestToViewNameTranslator进行选择),如果提供的不是一个简单类型,通过BeanUtils#isSimpleProperty进行决定,简单值仍然不会解析;
##### 类型转换
有些注解的控制器方法参数基于String的请求输入(例如@RequestParam,@RequestHeader,@PathVariable,@MatrixVariable,@CookieValue)也需要类型转换(参数并不是字符串) \
默认情况,类型转换自动进行(通过配置的转换器),简单类型支持,你能够通过WebDataBinder自定义类型转换(查看获取更多)或者通过FormattingConversionService注册Formatters,查看[Spring Field Formatting](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/core.html#format) \
一个实际问题在类型转换中需要考虑,空值,如果需要的值为空,缺失了(往往null作为类型转换的结果),例如Long,UUID以及其他目标类型都会发生,如果你想要允许null被注入,要么使用required标志在参数注解上,或者声明一个@Nullable注解; \
注意: 5.3开始,null值如果允许handle method接受,那么给拥有required属性注解设置false,这是推荐做法,当然还有很多种其他做法,对于路径变量的缺失默认抛出MissingPathVariableException,其他Missing...Exception 在相似情况也会抛出;
##### Matrix变量
[RFC 3986](https://tools.ietf.org/html/rfc3986#section-3.3) 讨论了路径碎片中的name-value键值对,mvc中我们认为这些"matrix 变量"基于一个"旧的 post"通过 Tim Berners-Lee的理论???,但是它们认为是URI路径参数; \
Matrix 变量能够出现在路径碎片的任何位置,每一个变量通过;分割以及多个值通过,号分割,多个值能够被指定通过重复变量名也是可以的; \
如果URL一代包含matrix变量,这个请求映射到controller方法必须使用一个URI变量去标志变量内容并确保请求能够成功匹配(不依赖matrix 变量)
```java
@GetMapping("/pets/{petId}")
public void findPet(@PathVariable String petId, @MatrixVariable int q) {

    // petId == 42
    // q == 11
}
```
给定的所有路径碎片也许包含matrix变量,你也许有时需要放置变量模糊,那么以下方式能够解决:
```java
// GET /owners/42;q=11/pets/21;q=22

@GetMapping("/owners/{ownerId}/pets/{petId}")
public void findPet(
        @MatrixVariable(name="q", pathVar="ownerId") int q1,
        @MatrixVariable(name="q", pathVar="petId") int q2) {

    // q1 == 11
    // q2 == 22
}
```
matrix变量也能够作为可选的变量，也支持默认值,例如:
```java

// GET /pets/42

@GetMapping("/pets/{petId}")
public void findPet(@MatrixVariable(required=false, defaultValue="1") int q) {

    // q == 1
}
```
为了获取 所有的matrix变量,你能够使用MutiValueMap,以下例子:
```java
// GET /owners/42;q=11;r=12/pets/21;q=22;s=23

@GetMapping("/owners/{ownerId}/pets/{petId}")
public void findPet(
        @MatrixVariable MultiValueMap<String, String> matrixVars,
        @MatrixVariable(pathVar="petId") MultiValueMap<String, String> petMatrixVars) {

    // matrixVars: ["q" : [11,22], "r" : 12, "s" : 23]
    // petMatrixVars: ["q" : 22, "s" : 23]
}
```
注意你需要启动matrix变量的使用,在mvc配置中,你需要设置一个UrlPathHelper(removeSemicolonContent=false)=>路径匹配,在mvc 命名空间中你需要设置<mvc:annotation-driven enable-matrix-variables="true"/>
##### @RequestParam
```java
@Controller
@RequestMapping("/pets")
public class EditPetForm {

    // ...

    @GetMapping
    public String setupForm(@RequestParam("petId") int petId, Model model) { 
        Pet pet = this.clinic.loadPet(petId);
        model.addAttribute("pet", pet);
        return "petForm";
    }

    // ...

}
```
注意前台如果传递的数据是列表(Array),后台这边RequestParam("xxx参数[]") 来解析参数值,字符串",,,,”这种形式只需要RequestParam("xx参数"),当使用@RequestParam声明到Map<String,String>或者MultiValueMap<String,String>但没有在注解中指定参数名,那么此map将会收集每一个请求参数的参数值; \
注意@RequestParam使用是可选的,如果参数值普通类型,不会被参数解析器解析,默认当作@RequestParam信任

##### @RequestHeader
你能够使用@ReuqestHeader注解去绑定一个请求头
```text
Host                    localhost:8080
Accept                  text/html,application/xhtml+xml,application/xml;q=0.9
Accept-Language         fr,en-gb;q=0.7,en;q=0.3
Accept-Encoding         gzip,deflate
Accept-Charset          ISO-8859-1,utf-8;q=0.7,*;q=0.7
Keep-Alive              300
```
抓取请求头
```java
@GetMapping("/demo")
public void handle(
        @RequestHeader("Accept-Encoding") String encoding, 
        @RequestHeader("Keep-Alive") long keepAlive) { 
    //...
}
```
内建支持对于转换逗号分割的字符串为数组或者字符的集合或者类型转换系统已知的其他类型,例如@RequestHeader("Accept")的参数可以是String,也可以是String[],也可以是List<String>
##### @CookieValue 
```java
@GetMapping("/demo")
public void handle(@CookieValue("JSESSIONID") String cookie) { 
    //...
}
```
##### ModelAttribute
能够访问model中属性(如果model中不存在此属性会默认创建),如果匹配了servlet请求参数那么也可以是参数值,这意味着存在数据绑定,能够独立的解析并转换独立的查询参数以及表单字段,例如:
```java
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@ModelAttribute Pet pet) {
    // method logic...
}
```
流程:
* 如果已经被@ModelAttirbute 方法加入到model中,抓取此睡醒
* 抓取session中的属性(在类级别上列出的会话属性)
* 通过转换器获得，其中模型属性名称与请求值的名称匹配，例如路径变量或请求参数（参见下一个示例）
* 使用默认构造器实例化
* 通过一个首选构造器实例化(携带有参数的-能够匹配Servlet请求参数),参数名由JavaBeans@ConstructorProperties决定或者通过运行时包含的参数名(在字节码包含的)决定; \
其中之一就是使用@ModelAttribute方法提供它或者依靠框架创建model属性,有一个Converter<String,T>的转换器能够使用,当model属性名匹配了请求参数名(例如路径变量、请求参数)才进行使用,例如model属性为account匹配了URI路径变量 account,那么着就会注册一个Converter<String,Account>的转换器去加载Account(从数据存储中):
```java
@PutMapping("/accounts/{account}")
public String save(@ModelAttribute("account") Account account) {
    // ...
}
```
如果model属性实例已经包含了,数据绑定已经应用,WebDataBinder类能够匹配Servlet 请求参数名称(查询参数以及表单字段)到目标对象的字段名,匹配的字段在类型转换应用之后进行收集,这是必要的,对于数据绑定(验证)的更多信息,查看Validation,查看更多自定义数据绑定,查看DataBinder; \
数据绑定也能够导致错误,默认情况,BinderException 将抛出,然而在controller方法中获取这些错误,你需要加入一个BinderResult 参数到@ModelAttribute参数之后(因为它也会有数据验证、数据绑定),例如:
```java
@PutMapping("/accounts/{account}")
public String save(@ModelAttribute("account") Account account) {
    // ...
}
```
有些情况下,你也许想要访问model属性而不需要数据绑定,这种情况你可以直接拦截Model到控制器方法中直接访问,你也可以使用@ModelAttribute(binding=false),例如:
```java
@ModelAttribute
public AccountForm setUpForm() {
    return new AccountForm();
}

@ModelAttribute
public Account findAccount(@PathVariable String accountId) {
    return accountRepository.findOne(accountId);
}

@PostMapping("update")
public String update(@Valid AccountForm form, BindingResult result,
        @ModelAttribute(binding=false) Account account) { 
    // ...
}
```
你能够自动在数据绑定之后通过增加一个@Valid注解或者Spring的@Validated注解来进行验证(Bean 验证以及Spring 验证)，下面的例子:
```java
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@Valid @ModelAttribute("pet") Pet pet, BindingResult result) { 
    if (result.hasErrors()) {
        return "petForm";
    }
    // ...
}
```
注意@ModelAttribute是可选的(如果设置的是它自己的属性),默认情况下任何参数可能不是普通数据类型(通过BeanUtils#isSimpleProperty决定)并且如果他是@ModelAttribute属性,他不会被其他参数解析器解析(因为是值得信任的);
##### @SessionAttributes
此注解用来在SERVLET会话存储和请求之间使用,他是一个type级别的注解,能够被controller使用,通常指定存储的属性名称或者属性的类型(会透明的存储在会话中并且被请求访问)
```java
@Controller
@SessionAttributes("pet") 
public class EditPetForm {
    // ...
}
```
第一次请求,如果存在一个model属性为pet的数据将增加到会话中,它会保持到另一个控制器方法使用SessionStatus方法参数去清理此存储;
```java
@Controller
@SessionAttributes("pet") 
public class EditPetForm {

    // ...

    @PostMapping("/pets/{id}")
    public String handle(Pet pet, BindingResult errors, SessionStatus status) {
        if (errors.hasErrors) {
            // ...
        }
            status.setComplete(); 
            // ...
        }
    }
}
```
@SessionAttribute
访问一个已经存在的会话属性(全局管理的-在控制器之外的属性,例如通过过滤器加入的属性)以及它可能不存在,捏能够能够@SessionAttribute属性到方法参数上,例如:
```java
@RequestMapping("/")
public String handle(@SessionAttribute User user) { 
    // ...
}
```
对于需要增加或者移除会话属性，考虑拦截WebReqeust或者HttpSession到控制器方法中; \
对于临时存储的model属性到会话(要作为控制器工作流的一部分),考虑使用@SessionAttirbutes

##### @RequestAttribute
与@SessionAttribute相似,获取之前存在的请求属性,例如过滤器,处理器拦截器加入的;
```java
@GetMapping("/")
public String handle(@RequestAttribute Client client) { 
    // ...
}
```
##### 重定向属性
默认来说,所有的model属性考虑作为URL模板变量暴露在重定向URL中,保留上一次请求的属性，可能是基础数据或者集合或者数组,将会作为查询字符串追加; \
基础数据作为查询字符串很合适,但是在注解的controller中,模型能够包含可选的属性(为了渲染的目的,例如  下拉字段数据),为了避免这些数据追加到URI中,一个@RequestMapping方法能够声明一个RedirectAttributes属性来用它指定需要抓取的属性到RedirectView,如果方法做重定向,RedirectAttributes的内容将会被使用,model的内容将会被使用; \
RequestMappingHandlerAdapter提供了一个ignoreDefaultModelOnRedirect的标志,你能够使用它只是默认的Model内容不应该被使用,如果控制器发生重定向,相反控制器方法一ing该声明一个RedirectAttributes类型的参数,如果不这样做,就没有能够传递给RedirectView的属性,mvcNamespace ，java 配置都默认设置为false,主要为了向后兼容,对于新的应用来说,我们推荐设置为true; \
注意URI模板变量(来自保留的请求)将会Zion给生效,当扩展一个重定向URI时,你不需要显式的通过Model或者RedirectAttributes属性增加,例如:
```java
@PostMapping("/files/{path}")
public String upload(...) {
    // ...
    return "redirect:files/{path}";
}
```
传递数据到重定向目标的任何一种方式都需要使用flash属性,不像其他重定向属性,flash属性保存在httpSession中(所以不会出现URL中),查看Flash属性获取更多; \
##### Flash Attributes
flash属性总是对一个请求存储属性(打算在另一个请求中使用的属性),对于重定向来说这很有用,例如Post重定向到Get模式,flash属性将临时保存(在重定向之前,通常在会话中)去确保能够在重定向之后可用并立即删除; \
mvc有两种抽象支持flash 属性,FlashMap用于持有flash属性,当FlashMapManager用于存储、抓取管理FlashMap实例; \
Flash属性支持总是"开启" 以及不需要显式启动,如果不使用,他不会导致session的创建问题,在每一个请求上,这里总是一个输入性FlashMap(通过前一个请求传递的属性)以及一个输出性FlashMap(保存给后续的请求的属性),FlashMap实例都能够通过mvc的静态RequestContextUtils方法访问; \
注解的controller通常不需要直接使用FlashMap工作,相反@RequestMapping方法能够访问一个RedirectAttributes的类型属性用它增加flash属性来应付重定向场景,增加到RedirectAttributes的Flash属性将自动传播到output FlashMap,类似的,重定向之后,从Input的FlashMap将自动增加到目标URL提供的处理器 方法上的Model属性; \
注意: flash 属性的概念存在需要web框架中并且证明了有时候会保留并发问题,这是因为,因为定义flash属性存储在直到下一个请求来临之前,然而有些"next"请求也许没有打算接受，而是另一个异步请求(如轮询、资源请求),这种情况闪存属性过早被删除; \
为了减少这种问题的可能性,RedirectView自动票据了一个FlashMap实例(通过自动使用目标重定向 URL 的路径和查询参数),最终,默认的FlashMapManager匹配进入的请求进行匹配(然后在查询"input"FlashMap) \
这斌不能完全消除并发问题,但是它减少了在重定向中使用减少了问题,因此我们推荐在重定向场景使用flash属性;
##### Multipart
在MultipartResolver启动之后,Content-Type= multipart/form-data的post请求将解析且可以作为普通请求参数访问,下面有一个例子访问普通表单字段以及上传的文件:
```java
@Controller
public class FileUploadController {

    @PostMapping("/form")
    public String handleFormUpload(@RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            // store the bytes somewhere
            return "redirect:uploadSuccess";
        }
        return "redirect:uploadFailure";
    }
}
```
声明一个参数类型为List<MultipartFile>允许使用相同参数名上传多个文件 \
当@RequestParam注解声明在Map<String,MultipartFile>或者MultiValueMap<String,MultipartFile>(没有指定参数名-在注解上),那么这个map将会收集所有的上传文件,根据给定的参数名; \
servlet 3.0上传,你可以声明javax.servlet.http.Part代替Spring的MultipartFile,作为方法参数或者集合值类型; \
你也能够使用上传内容作为数据绑定到命令对象的一部分,例如表单字段和文件能够作为表单对象的字段,例如:
```java
class MyForm {

    private String name;

    private MultipartFile file;

    // ...
}

@Controller
public class FileUploadController {

    @PostMapping("/form")
    public String handleFormUpload(MyForm form, BindingResult errors) {
        if (!form.getFile().isEmpty()) {
            byte[] bytes = form.getFile().getBytes();
            // store the bytes somewhere
            return "redirect:uploadSuccess";
        }
        return "redirect:uploadFailure";
    }
}
```
文件上传请求也能够从一个非浏览器客户端提交(在restful服务场景中),例如通过json展示的文件:
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
你能够访问"meta-data"部分,通过@RequestParam作为一个字符串,但是你也可以从JSON反序列化(类似于@RequestBody),使用@RequestPart注解去访问一个通过httpMessageConverter转换过的上传内容:
```java
@PostMapping("/")
public String handle(@RequestPart("meta-data") MetaData metadata,
        @RequestPart("file-data") MultipartFile file) {
    // ...
}
```
你能够使用@RequestPart结合javax.validation.Valid或者Spring的@Validated注解使用,其中之一都能够触发标准bean验证,默认情况,验证失败会触发MethodArgumentNotValidException,默认响应错误码400,除此之外你可以处理验证错误(通过控制器方法添加Errors或者BindingResult)
```java
@PostMapping("/")
public String handle(@Valid @RequestPart("meta-data") MetaData metadata,
        BindingResult result) {
    // ...
}
```
##### @RequestBody
你能够使用@RequestBody注解获取请求体的解析并且反序列化数据为对象(通过HttpMessageConverter),例如:
```java
@PostMapping("/accounts")
public void handle(@RequestBody Account account) {
    // ...
}
```
你能够使用mvc config配置MessageConverters然后自定义消息转换; \
你能够结合验证相关的注解进行使用执行验证,默认异常为MethodArgumentNotValidException;
```java
@PostMapping("/accounts")
public void handle(@Valid @RequestBody Account account, BindingResult result) {
    // ...
}
```
#####HttpEntity
HttpEntity是一个或多或少与使用@RequestBody相似,基于一个容器对象-暴露请求头以及body:
```java
@PostMapping("/accounts")
public void handle(HttpEntity<Account> entity) {
    // ...
}
```
##### @ResponseBody
标志着会返回一个使用HttpMessageConverter进行序列化响应体;
```java
@GetMapping("/accounts/{id}")
@ResponseBody
public Account handle() {
    // ...
}
```
并且@ResponseBody支持类级别注解,会被所有的处理器方法继承,等效于@RestController; \
你能够将@ResponseBody结合响应式类型对象使用; \
你能够使用Mvc config配置自定义消息转换器进行消息转换; \
你能够通过JSON 序列化视图结合@ResponseBody方法使用;
##### ResponseEntity
类似与@ResponseBody,但是多了状态码和响应头:
```java
@GetMapping("/something")
public ResponseEntity<String> handle() {
    String body = ... ;
    String etag = ... ;
    return ResponseEntity.ok().eTag(etag).build(body);
}
```
mvc支持使用单个响应式值去异步生产ResponseEntity,增加或者单个以及多个响应式值到body中,允许以下异步响应的类型:
* ResponseEntity<Mono<T>> 或者 ResponseEntity<Flux<T>> 确保响应状态以及请求头能够立即知晓(当body在最后被异步提供时),使用Mono(代表body由0...1组成)或者如果它能够产生多个值应该使用Flux;
* Mono<ResponseEntity<T>> 提供了三个-响应状态、headers、body并异步提供,这允许响应状态和标头根据异步请求处理的结果而变化

##### jackson json
这一部分由Jackson Json库提供支持;
##### JSON 视图
spring mvc提供了Jackson‘s序列化视图的支持,能够序列化一个对象的子集,为了结合@ResponseBody或者ResponseEntity的控制器方法,你需要使用@JsonView注解去激活一个序列化视图类：
```java
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
@JsonView允许视图类集合,但是你只能够在每个controller方法上设置一个,如果你需要激活多个视图序列化,那么你应该使用组合接口; \
如果你想要上述编程式实现,你不应该声明@JsonView,通过MappingJacksonValue去包装返回值并且使用它去应用序列化视图;
```java
@RestController
public class UserController {

    @GetMapping("/user")
    public MappingJacksonValue getUser() {
        User user = new User("eric", "7!jd#h23");
        MappingJacksonValue value = new MappingJacksonValue(user);
        value.setSerializationView(User.WithoutPasswordView.class);
        return value;
    }
}
```
依赖视图解析的controller,你能够增加一个序列化视图类到model中,例如:
```java
@Controller
public class UserController extends AbstractController {

    @GetMapping("/user")
    public String getUser(Model model) {
        model.addAttribute("user", new User("eric", "7!jd#h23"));
        model.addAttribute(JsonView.class.getName(), User.WithoutPasswordView.class);
        return "userView";
    }
}
```
#### Model
你能够使用@ModelAttribute注解:
* 在@RequestMapping方法上的方法参数使用去创建或者访问一个来自Model中的属性并将它通过WebDataBinder绑定到请求上;
* 在@Controller或者@ControllerAdvice类中的方法级注解能够在任何@RequestMapping方法执行之前初始化model;
* @RequestMapping 方法上此返回值应该是一个model属性; \
这部分讨论了@ModelAttribute方法,前面列表的第二项描述过,一个controller能够有无数个@ModelAttribute方法,在相同controller的@RequestMapping方法执行之前这些方法会一一执行,@ModealAttribute方法能够通过@ControllerAdvice跨controller共享,查看ControllerAdvice获取更多; \
@ModelAttribute 方法有非常灵活的方法签名,它们支持相同的参数(例如: 和@RequestMapping方法参数相同),除了@ModelAttribute它本身之外或者和请求体正文相关的任何事;
```java
@ModelAttribute
public void populateModel(@RequestParam String number, Model model) {
    model.addAttribute(accountRepository.findAccount(number));
    // add more ...
}
```

```java
@ModelAttribute
public Account addAccount(@RequestParam String number) {
    return accountRepository.findAccount(number);
}
```
当名字没有显式指定,默认 名称是基于Object类型,在[Conventions](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/javadoc-api/org/springframework/core/Conventions.html)中描述;捏能够能够总是显式的通过覆盖addAttribute方法提供名称或者通过@ModelAttribute上的name属性设定名称(这种是对于将返回值作为@ModelAttribute); \
你能够使用@ModelAttribute 作为方法级别注解结合@RequestMapping 方法使用,在@RequestMapping方法的返回值将会被解释为一个model属性,这个通常是不需要的,就像在Html控制器中是默认行为,除非返回值是一个字符串(那么将解释为视图名),@ModelAttribute能够定制这个model属性名称:
```java
@GetMapping("/accounts/{id}")
@ModelAttribute("myAccount")
public Account handle() {
    // ...
    return account;
}
```
这种写法没必要
#### DataBinder
@Controller或者@ControllerAdvice能够有一个@InitBinder方法能够初始化一个WebDataBinder实例,他将返回:
* 绑定请求参数(例如表单参数或者请求参数)到model对象
* 转换字符串请求value值(例如 请求参数 、路径变量、headers、cookies以及其他)到控制器的目标方法参数上;
* 格式化model对象值作为一个string(当渲染html 表单时)  \

@InitBinder方法能够注册controller特定的java.beans.PropertyEditor或者Spring的Converter以及Formatter组件,除此之外你能够使用mvc配置注册Conveter以及Formatter类型到全局共享的FormattingConversionService \
@InitBinder 方法支持和@RequestMapping相同参数,除了@ModelAttribute(命令对象)参数之外;通常情况,它们声明一个WebDataBinder参数(为了注册)并且返回void类型:
```java
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
除此之外,当你使用一个基于Formatter的配置(通过FormattingConversionService),你能够重新使用相同的方式并且注册与特定于控制器相关的Formatter实现,如下展示:
```java
@Controller
public class FormController {

    @InitBinder 
    protected void initBinder(WebDataBinder binder) {
        binder.addCustomFormatter(new DateFormatter("yyyy-MM-dd"));
    }

    // ...
}
```
#### 异常处理
@Controller 以及 @ControllerAdvice都能够拥有@ExceptionHandler方法去处理控制器方法暴露的异常:
```java
@Controller
public class SimpleController {

    // ...

    @ExceptionHandler
    public ResponseEntity<String> handle(IOException ex) {
        // ...
    }
}
```
此异常也许能够针对顶级异常(传播的)(例如: IOException)或者正对一个内嵌的原因-通过一个包装异常(例如IOException被包装到IllegalStateException),从5.3开始,它能够匹配任意原因级别,而以前仅仅只考虑直接原因; \
为了匹配异常类型,最好声明目标异常作为方法参数,就如前一个例子所展示的那样,当多个异常方法匹配,一个顶级异常匹配通常优先级高于原因异常匹配,更加特殊来说,ExceptionDepthComparator将用来排序异常(基于它们自己的深度-根据抛出异常的类型),除此之外,此注解声明也许还会限制异常类型匹配,例如:
```java
@ExceptionHandler({FileSystemException.class, RemoteException.class})
public ResponseEntity<String> handle(IOException ex) {
    // ...
}
```
使用一个更加广泛的异常类型接受参数:
```java
@ExceptionHandler({FileSystemException.class, RemoteException.class})
public ResponseEntity<String> handle(Exception ex) {
    // ...
}
```
根异常匹配和原因异常匹配之间的区别可能奇怪:
在IOException变种展示更早之前,此方法通常会通过实际FileSystemException或者RemoteException实例作为参数进行调用,因为它们都继承于IOException,然而如果任何这样匹配的异常通过包装异常继续传播(例如它本身就是IOException),然后传递的异常实例就是包装异常; \
这个行为相似于handle(Exception)变种方法,这是一种在包装场景中执行包装异常的行为,使用实际匹配的异常能够通过ex.getCause获取到真正的原因,传递的异常实际是FileSystemException或者RemoteException实例(仅仅当他们作为顶级异常抛出时,可能会这样做) \
我们通常推荐你尽可能指定特殊类型作为参数签名,减少潜在的缺失匹配可能性(在root或者Cause异常类型之间),考虑细分多个匹配异常类型的方法进入@ExceptionHandler方法,每一个匹配异常类型都通过它自己的签名即可完成; \
在一个多@ControllerAdvice的安排中,我们推荐声明你的主要异常映射在一个@ControllerAdvice优先于其他异常映射的顺序,root异常优先于cause匹配,取决于在controller或者@ControllerAdvice中定义的方法之间的顺序,这意味着cause匹配(在高优先级中的@ControllerAdvice bean 更容易执行任何匹配),之后才是低优先级@ControllerAdvice \
最后但不是至少,@ExceptionHandler方法实现可以通过以原始形式重新抛出给定的异常实例来选择退出处理给定的异常实例,这是非常有用的(如果你仅仅对顶级匹配感兴趣或者使用特定上下文进行匹配却不能够静态的决定时),重抛一个异常能够通过解决链进行传播,就算给定的@ExceptionHandler 方法第一次没有匹配; \
在mvc中DispatcherServlet级别支持@ExceptionHandler,使用的是HandlerExceptionResolver进行处理;
##### 方法参数
@ExceptionHandler 方法支持以下参数:
```text
Method argument	Description
Exception type              For access to the raised exception.

HandlerMethod   访问抛出的异常

WebRequest, NativeWebRequest   访问请求参数以及会话属性没有直接使用Servlet api

javax.servlet.ServletRequest, javax.servlet.ServletResponse     选择一个指定的请求或者响应类型(例如ServletRequest或者HttpServletRequest或者Spring的MultipartRequest或者MultipartHttpServletRequest)

javax.servlet.http.HttpSession   会话对象,因此此参数不能为空,注意会话访问线程不安全,所以可以设置RequestMappingHandlerAdapter实例的synchronizeOnSession属性为true,如果允许会话并发访问;

java.security.Principal 当前认证的用户-可以为指定Principal实现类之一

HttpMethod http请求方法类型

java.util.Locale  当前请求的locale,LocaleResolver决定,例如配置LocaleResolver或者LocaleContextResolver

java.util.TimeZone, java.time.ZoneId   当前请求相联系的时区,由LocaleContextResolver解析;
java.io.OutputStream, java.io.Writer 访问响应体,servlet api暴露的对象
java.util.Map, org.springframework.ui.Model, org.springframework.ui.ModelMap 访问模型获取错误响应,总是空

RedirectAttributes 指定用于重定向的特殊属性 - 作为查询字符串追加并且flash属性将临时存储在当前请求,直到重定向之后删除;

@SessionAttribute 访问任意的会话属性,对比存储在会话的model属性(是类级别的@SessionAttributes的声明结果)

@RequestAttribute 访问请求属性
```

##### 返回值
@ExceptionHandler 方法支持以下返回值
```html
@ResponseBody    返回将通过HttpMessageConverter实例转换的值并且写入响应

HttpEntity<B>,ResponseEntity<B> 返回值标识完全响应内容(包括header 以及 body)能够通过HttpMessageConverter实例进行转换并且写入到响应中;

String 将被ViewResolver解析视图名(并且隐含一个Model共视图解析使用)-通过命令对象以及@ModelAttribute方法确定,处理器方法也能够编程式访问Model,仅仅声明Model参数

View 一个View实例能够被用来结合隐式model渲染视图-通过命令对象以及@ModelAttribute的方法决定,处理器方法能够编程式访问model(通过声明一个Model参数);

java.util.Map, org.springframework.ui.Model   这些属性将隐式增加到model中,并且视图名通过RequestToViewNameTranslator进行解析,推断;
@ModelAttribute 标识将它作为一个Model属性加入,viewName将会通过RequestToViewNameTranslator进行推测; 注意@ModelAttribute是可选的

ModelAndView 视图和model属性都是可用的,包括响应状态
void void,null考虑已经完全处理了响应(如果存在ServletResponse以及OutputStream流参数)或者一个@ResponseStatus注解,等价于控制器有一个积极的ETag或者lastModified 时间戳检测; 如果都不是,void 返回值表明没有响应体(对于rest controller或者对于html控制器来说没有默认的视图名称选择)
其他返回值,会通过BeanUtils#isSimpleProperty 判断是否为普通类型值,它会信任为一个Model值并加入到model中,如果他是一个简单的类型,它不会尝试解析;
```

##### REST API 异常
rest 服务的普通需求是包括一个错误详情(响应体中包含一个错误详情),spring 没有自动这样做,因为响应体中的错误详情的呈现是跟应用相关的,然而@RestController 也许使用@ExceptionHandler 方法(返回一个ResponseEntity)去设置响应的状态、以及响应体,因此这样的方法也能够声明在ControllerAdvice中进行全局应用; \
应用实现全局异常处理器(包含在响应体中的错误详情)应该考虑继承一个ResponseEntityExceptionHandler,它提供了一个在spring mvc中提供异常处理(并且提供了一个钩子去定制响应体),为了利用这个特性,创建一个ResponseEntityExceptionHandler的子类,通过@ControllerAdvice注解它,覆盖必要的方法并将它声明为一个spring bean;
#### controller advice
通常@ExceptionHandler ,@InitBinder 以及@ModelAttribute方法都应用在@Controller内部(或者类层次体系中)且声明它们,如果你想这样的方法更加全局化(跨controller),你应该声明它们到一个注解了@ControllerAdvice或者@RestControllerAdvice的springBean中; \
@ControllerAdvice使用@Component进行注释,这就意味着他能够作为一个spring bean注入(通过组件扫描),@RestControllerAdvice 是一个组合注解(通过@ControllerAdvice以及@ResponseBody进行注释的),这最终意味着@ExceptionHandler方法能够通过消息转换进行响应体渲染(对比视图解析或者模板渲染); \
一开始,@RequestMapping以及@ExceptionHandler方法的基础设施类会通过ControllerAdvice的spring bean检测并在运行时进行处理,全局@ExceptionHandler方法(来自 @ControllerAdvice)将在@Controller中局部的对应异常处理方法之后处理,作为对比,全局@ModelAttribute以及@InitBinder 方法将在局部之前进行应用; \
默认来说,@ControllerAdvice方法会应用在每一个请求上(也可以是所有控制器),但是你能够限制控制器的子集处理范围(通过使用注解的属性进行控制):
```java
// Target all Controllers annotated with @RestController
@ControllerAdvice(annotations = RestController.class)
public class ExampleAdvice1 {}

// Target all Controllers within specific packages
@ControllerAdvice("org.example.controllers")
public class ExampleAdvice2 {}

// Target all Controllers assignable to specific classes
@ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class})
public class ExampleAdvice3 {}
```
这些选择器将在运行时评估并且也许会影响性能(如果广泛使用);
