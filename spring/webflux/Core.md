# webflux 配置
webflux java 配置声明了这些需要处理请求的组件,例如注解的controller以及函数式端点,并且它们提供了api扩展，定制configuration,这意味着你不需要理解这个底层的bean-通过java配置创建的,然而你想要理解它们,你能够查看WebFluxConfigurationSupport或者了解它们这些bean类型; \
对于大多数高级定制,在扩展api中是不可用的,你能够完全控制-通过[Advanced Configuration Mode](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-config-advanced-java)
### 启用WebFlux 配置
你能够使用@EnableWebFlux注解到java配置中
这默认会注册一些spring webflux的基础组件并且适配在类路径上出现的一些依赖;
#### webFlux config api
通过它进行扩展
#### Conversion,formatting
默认格式化器已经默认安装了很多,例如可以通过@NumberFormat或者@DateTimeFormat格式化字段 \
注册自定义..
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // ...
    }

}
```
spring webflux根据请求的Locale解析并格式化日期值,将对字符串的输入表单字段进行处理呈现为日期,对于date,time表单字段,浏览器使用了一个固定的格式-定义在html 规范中,这种情况可以尝试定制:
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }
}
```
查看FormatterRegistrar SPI 以及 FormattingConversionServiceFactoryBean 俩了解更多;
#### validation
... 配置校验器
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public Validator getValidator(); {
        // ...
    }

}
```
反之就是局部:
```text
@Controller
public class MyController {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(new FooValidator());
    }

}
```
为了使用标准java bean校验,可以需要注册一个LocalValidatorFactoryBean,创建并标记一个bean 为@Primary,避免和mvc配置中的bean产生冲突!
#### content type Resolvers
能够配置webflux 决定请求的媒体类型
默认情况 Accept会检查,你也能够根据查询参数策略进行检查
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureContentTypeResolver(RequestedContentTypeResolverBuilder builder) {
        // ...
    }
}
```
#### http 消息codecs
定制如何读取和写入请求或者响应

```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().maxInMemorySize(512 * 1024);
    }
}
```
ServerCodecConfigurer提供了大量默认的读取器以及写入器的集合,你能够增加更多,定制默认的或者完全替代默认的人; \
对于Jackson Json以及 XML，考虑使用[Jackson2ObjectMapperBuilder](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/javadoc-api/org/springframework/http/converter/json/Jackson2ObjectMapperBuilder.html),这会定制jackson的以下默认属性: 
- DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES is disabled.
- MapperFeature.DEFAULT_VIEW_INCLUSION is disabled. \
它会自定注册以下已知的模块(如果在类路径上检测到):
- jackson-datatype-joda: Support for Joda-Time types.
- jackson-datatype-jsr310: Support for Java 8 Date and Time API types.
- jackson-datatype-jdk8: Support for other Java 8 types, such as Optional.
- jackson-module-kotlin: Support for Kotlin classes and data classes.
#### 视图解析器
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // ...
    }
}
```
ViewResolverRegistry 拥有配置视图技术集成的快捷方式!...
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {


    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.freeMarker();
    }

    // Configure Freemarker...

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("classpath:/templates");
        return configurer;
    }
}
```
我能够加载任何一个ViewResolver实现:
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {


    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ViewResolver resolver = ... ;
        registry.viewResolver(resolver);
    }
}
```
为了支持内容协商并且渲染其他的格式(通过视图解析),你能够配置一个或者多个默认的视图(基于HttpMessageWriterView实现),它能够接收任意可选的Codecs(来自Spring-web)
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {


    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.freeMarker();

        Jackson2JsonEncoder encoder = new Jackson2JsonEncoder();
        registry.defaultViews(new HttpMessageWriterView(encoder));
    }

    // ...
}
```
#### 静态资源
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
            .addResourceLocations("/public", "classpath:/static/")
            .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
    }

}
```
这个资源处理器也支持ResourceResolver实现的链以及ResourceTransformer实现,者能够被用来创建一个和可选的资源工作的工作链；\
您可以根据从内容、固定应用程序版本或其他信息计算出的 MD5 哈希，将 VersionResourceResolver 用于版本化资源 URL。 ContentVersionStrategy (MD5 hash) 是一个不错的选择，但有一些值得注意的例外（例如与模块加载器一起使用的 JavaScript 资源）
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/public/")
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
    }

}
```
你能够使用ResourceUrlProvider 重写URLs并且应用解析器以及转换器的全部链(例如:  插入版本)webflux配置提供了一个ResourceUrlProvider 因此你能够在其他地方拦截它; \
和mvc不同,当它出现,webflux,没有一种方式透明的重写静态资源路径,因此这里没有视图技术能够利用解析器以及转换器的非阻塞链,当提供一些本地资源时,这个工作区直接使用ResourceUrlProvider(例如,通过自定义元素)并阻塞; \
注意当同时使用EncodeResourceResolver(例如: Gzip,Brotli编码的) 以及 VersionedResourceResolver,它们必须有序注册,确保基于内容的版本总是可靠的基于未编码的文件计算 \
WebJars 也被WebJarsResourceResolver支持,这将自动被注册(当org.webjars:webjars-locator-core)库存在类路径的时候,这个解析器能够重写URL到指定版本的jar并且能够针对进入的URL-没有版本 进行匹配,例如 /jquery/jquery.min.js 会匹配到 /jquery/1.2.0/jquery.min.js
#### 路径匹配
可以定制路径匹配相关的选择,对于独立选项的详细信息,查看PathMatchConfigurer获取更多
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
            .setUseCaseSensitiveMatch(true)
            .setUseTrailingSlashMatch(false)
            .addPathPrefix("/api",
                    HandlerTypePredicate.forAnnotation(RestController.class));
    }
}
```
Spring webflux 依靠RequestPath解析请求路径,用来访问路径碎片value,移除分号内容(例如路径或者matrix变量),这意味着不像mvc,你不需要指定是否解码请求路径或者是否移除分号内容-为了路径匹配的路径 \
Spring webflux 不支持后缀模式匹配,不像mvc,这里我们总是推荐移除这种方式;
#### websocketService
webflux java配置生米呢了一个WebSocketHandlerAdapter的bean,它提供了对于WebSocket handler的执行支持,这意味着仍然需要为了处理WebSocket 握手请求通过SimpleUrlHandlerMapping映射到webSocketHandler \
在某些情况下,创建WebSocketHandlerAdapter是必要的-并且提供一个WebSocketService服务,这允许配置WebSocket 服务器属性:
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public WebSocketService getWebSocketService() {
        TomcatRequestUpgradeStrategy strategy = new TomcatRequestUpgradeStrategy();
        strategy.setMaxSessionIdleTimeout(0L);
        return new HandshakeWebSocketService(strategy);
    }
}
```
#### 高级配置模式
@EnableWebFlux导入了DelegatingWebFluxConfiguration
- 提供了对于WebFlux应用支持的默认Spring配置
- 检测并代理到WebFluxConfigurer实现去定制配置 \
对于高级模式,你能够移除@EnableWebFlux并且直接从DelegatingWebFluxConfiguration扩展而不是实现WebFluxConfigurer
```text
@Configuration
public class WebConfig extends DelegatingWebFluxConfiguration {

    // ...
}
```
你能够在WebConfig保留存在的方法,但是你现在要从基础类中覆盖bean声明并且仍然可以有任意数量的WebMvcConfigurer实现在类路径上!
#### Http/2
http/2 被Reactor Netty,Tomcat,Undertow支持,但是考虑到服务器配置,了解更多查看
https://github.com/spring-projects/spring-framework/wiki/HTTP-2-support
