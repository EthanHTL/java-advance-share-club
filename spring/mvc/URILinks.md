## URI Links
这一部分描述了各种各样的属性(能够在spring框架中使用URI工作)
#### UriComponents
UriComponentBuilder 提供了构建URI的帮助(通过URI模板以及变量进行构建),例如:
```java
UriComponents uriComponents = UriComponentsBuilder
        .fromUriString("https://example.com/hotels/{hotel}")  
        .queryParam("q", "{q}")  
        .encode() 
        .build(); 

URI uri = uriComponents.expand("Westin", "123").toUri();  
```
主要使用通过字符串模板进行变量填充 \
前面的例子能够更加简短:
```java
URI uri = UriComponentsBuilder
        .fromUriString("https://example.com/hotels/{hotel}")
        .queryParam("q", "{q}")
        .encode()
        .buildAndExpand("Westin", "123")
        .toUri();
```
你能够通过直接获取一个URI(隐式编码):
```java
URI uri = UriComponentsBuilder
        .fromUriString("https://example.com/hotels/{hotel}")
        .queryParam("q", "{q}")
        .build("Westin", "123");
```
你能够通过一个URI模板直接构建
```java
URI uri = UriComponentsBuilder
        .fromUriString("https://example.com/hotels/{hotel}?q={q}")
        .build("Westin", "123");
```
#### UriBuilder
UriComponentBuilder是UriBuilder的实现者,你能够创建一个UriBuilder,最后通过UriBuilderFactory返回,UriBuilderFactory以及UriBuilder提供了一个增强型的机制去从URI模板构建URI,基于共享配置,例如一个基本URL,编码首选项、以及其他信息; \
你也能够配置RestTemplate以及WebClient=>通过UriBuilderFactory去定制URI的准备 \
DefaultUriBuilderFactory是UriBuilderFactory的默认实现能够使用UriComponentsBuilder并暴露共享的配置选项\
例如展示了如何配置一个RestTemplate:
```java
// import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

String baseUrl = "https://example.org";
DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
factory.setEncodingMode(EncodingMode.TEMPLATE_AND_VALUES);

RestTemplate restTemplate = new RestTemplate();
restTemplate.setUriTemplateHandler(factory);
```
配置一个WebClient
```java
// import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

String baseUrl = "https://example.org";
DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
factory.setEncodingMode(EncodingMode.TEMPLATE_AND_VALUES);

WebClient client = WebClient.builder().uriBuilderFactory(factory).build();
```
除此之外,你能够直接使用DefaultUriBuilderFactory,类似于UriComponentBuilder,除了静态工厂方法之外,它是一个实际的实例能够持有配置以及首选项:
```java
String baseUrl = "https://example.com";
DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);

URI uri = uriBuilderFactory.uriString("/hotels/{hotel}")
        .queryParam("q", "{q}")
        .build("Westin", "123");
```
#### URI Encoding
UriComponentsBuilder 暴露了两种编码选项:
* UriComponentsBuilder#encode 首先预先编译URI模板以及扩展时严格编码URI变量
* UriComponents#encode 在URI变量扩展完毕之后编码URI组件 \
两个选项都会替换非ascii字符以及遗留，非法字符(使用转义八位字节),第一个选项还替换出现在 URI 变量中的具有保留含义的字符 \
考虑";",这是一个遗留在路径中的字符(且具有保留意义),第一个选项会使用%3B替换在URI变量中的";"但是不在URI模板中的";",对比下来,第二个选项绝不会替换";",因此他会作为一个遗留字符在路径中; \
大多数情况下,第一个选项想要获取一个期待的结果,因为它信任URI变量作为一个不透明的值将会被完全编码,第二个选项也是有用的(如果URI变量内部需要包含保留字符串),第二个选项在(当完全没有扩展URI变量,它也会编码任何事情)--当根本不扩展 URI 变量时，因为这也会对任何看起来像 URI 变量的东西进行编码 \
使用了第一个选择的demo:
```java
URI uri = UriComponentsBuilder.fromPath("/hotel list/{city}")
        .queryParam("q", "{q}")
        .encode()
        .buildAndExpand("New York", "foo+bar")
        .toUri();

// Result is "/hotel%20list/New%20York?q=foo%2Bbar"
```
你也能够继续改进: 编码隐式构建
```java
URI uri = UriComponentsBuilder.fromPath("/hotel list/{city}")
        .queryParam("q", "{q}")
        .build("New York", "foo+bar");
```
还可以改进:
```java
URI uri = UriComponentsBuilder.fromUriString("/hotel list/{city}?q={q}")
        .build("New York", "foo+bar");
```
WebClient以及RestTemplate扩展并且编码URI模板-内部使用(通过UriBuilderFactory策略),你也能够同时配置自定义策略:
```java
String baseUrl = "https://example.com";
DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl)
factory.setEncodingMode(EncodingMode.TEMPLATE_AND_VALUES);

// Customize the RestTemplate..
RestTemplate restTemplate = new RestTemplate();
restTemplate.setUriTemplateHandler(factory);

// Customize the WebClient..
WebClient client = WebClient.builder().uriBuilderFactory(factory).build();
```
DefaultUriBuilderFactory实现使用UriComponentsBuilder 进行内部扩展并且编码URI 模板,作为一个工厂,他提供了一个配置合适编码的方式,基于以下编码模式:
* TEMPLATE_AND_VALUES: 使用UriComponentsBuilder#encode,标识第一种编码策略,先预编译URI模板,然后对扩展URI变量进行严格编码
* VALUES_ONLY 不编码URI模板,相反,应用严格的编码URI变量上,通过UriUtils.encodeUriVariables(在扩展URI模板的变量之前,对变量进行编码)
* URI_COMPONENT 使用UriComponents#encode() 第二个编码选择,在URI变量扩展之后进行编码;
* NONE 不做任何编码 \
RestTemplate 设置的是EncodingMode.URI_COMPONENT (由于历史原因以及向后兼容),WebClient依靠DefaultUriBuilderFactory的默认值,5.0 默认EncodingMode.URI_COMPONENT 5.1 为EncodingMode.TEMPLATE_AND_VALUES

#### 相关的Servlet请求
你能够使用ServletUriComponentsBuilder 创建相对于当前请求的URI,例如:
```java
HttpServletRequest request = ...

// Re-uses host, scheme, port, path and query string...

ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request)
        .replaceQueryParam("accountId", "{id}").build()
        .expand("123")
        .encode();
```
你也能够创建相对于当前上下文路径的URI,例如:
```java
// Re-uses host, port and context path...

ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromContextPath(request)
        .path("/accounts").build()
```
你也能够创建 相对于Servlet的URI(例如 /main/*),例如:
```java
// Re-uses host, port, context path, and Servlet prefix...

ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromServletMapping(request)
        .path("/accounts").build()
```
注意:从5.1开始,ServletUriComponentBuilder 将忽略来自Forwarded以及X-Forwarded-*请求头信息,这些指定客户端发起地址,考虑使用ForwardedHeaderFilter去抓取并使用或者抛弃这些headers;
#### Links to Controllers
mvc提供了一个机制去准备链接到控制器方法的值,举个例子,下面的mvc控制器允许执行链接创建:
```java
@Controller
@RequestMapping("/hotels/{hotel}")
public class BookingController {

    @GetMapping("/bookings/{booking}")
    public ModelAndView getBooking(@PathVariable Long booking) {
        // ...
    }
}
```
你能够通过参考方法名称预定义link,例如:
```java
UriComponents uriComponents = MvcUriComponentsBuilder
    .fromMethodName(BookingController.class, "getBooking", 21).buildAndExpand(42);

URI uri = uriComponents.encode().toUri();
```
在前面的一个例子中,我们提供了一个实际的方法参数值(21)将被用于作为一个路径变量并且将插入URL中,因此我们提供一个值 42,将会继续填充剩余的URI变量,例如从顶级type-level请求mapping继承的hotel变量,如果方法还有更多参数,我们能够使用null填充URL不需要的参数值,仅仅@PathVariable以及@RequestParam参数与构建URL相关; \
这里有一个替代MvcUriComponentsBuilder的方式,例如: 捏能够使用一个技术模拟类似的测试(通过代理去避免通过名称去引用控制器方法),如下所示:(此例子假设有MvcUriComponentsBuilder.on的静态导入：
```java
UriComponents uriComponents = MvcUriComponentsBuilder
    .fromMethodCall(on(BookingController.class).getBooking(21)).buildAndExpand(42);

URI uri = uriComponents.encode().toUri();
```
注意:  控制方法签名存在限制(当他们支持通过fromMethodCall创建link时),除了需要合适的参数签名之外，还存在一个技术限制(在返回值类型上)--换句话说,通常是一个运行时代理执行link的构建,因此返回值必须不能被final修饰,除此之外,视图名返回的公共字符串类型不会工作,你应该使用ModelAndView或者简单Object(带有字符串返回值)进行代替; \
前面的例子使用了一个MvcUriComponentBuilder的静态方法,本质上,它们依赖与ServletUriComponentsBuilder从scheme、host、port、contextpath,以及当前请求的servlet 路径进行预准备,大多数情况下正常工作,有些时候,它不能够满足，例如你也许在请求上下文之外(然后你想要批量预处理links)或者由于你需要插入一个路径前缀(因此一个locale前缀可能会从请求路径中移除并且需要重新插入到links) \
对于这种情况,你需要使用一个静态的fromXxx 重载方法去访问一个UriComponentsBuilder去使用一个基本URL,除此之外,你能够创建一个MvcUriComponentsBuilder(拥有基本URL)的实例并且可以使用基于实例的withXxx方法,例如,下面展示了withMethodCall的使用:
```java
UriComponentsBuilder base = ServletUriComponentsBuilder.fromCurrentContextPath().path("/en");
MvcUriComponentsBuilder builder = MvcUriComponentsBuilder.relativeTo(base);
builder.withMethodCall(on(BookingController.class).getBooking(21)).buildAndExpand(42);

URI uri = uriComponents.encode().toUri();
```
####  Link 到 Views
例如在freeMarker,Themeleaf,或者jsp中,你能够绑定link到注解的controller,通过隐式或者显式的赋值名称对每一个请求mapping进行link构建
```java
@RequestMapping("/people/{id}/addresses")
public class PersonAddressController {

    @RequestMapping("/{country}")
    public HttpEntity<PersonAddress> getAddress(@PathVariable String country) { ... }
}
```
对比view:
```jsp
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
...
<a href="${s:mvcUrl('PAC#getAddress').arg(0,'US').buildAndExpand('123')}">Get Address</a>
```
前面的例子中依靠mvcUrl函数(声明在Spring tag库中)->META-INF/spring.tld,但是他是非常容易的定义你自己的函数或者准备一个类似的函数; \
他如何工作,一开始  每一个@RequestMapping 被赋值一个默认的名称(通过HandlerMethodMappingNamingStrategy),默认实现使用类的首字母以及方法名,例如ThingController中的getThing将形成"TC#getThing",如果命名冲突,你能够使用@RequestMapping(name="..")去设置一个显式名称或者实现你自己的HandlerMethodMappingStrategy; 