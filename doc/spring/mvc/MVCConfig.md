## MVC config
#### 启动mvc 配置
```java
@Configuration
@EnableWebMvc
public class WebConfig {
}
```
or
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/mvc
        https://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <mvc:annotation-driven/>

</beans>
```
前面的例子中已经注入了大量的mvc 基础设施类去适配可用的依赖(例如 JSON的重载 消息转换器, xml等等)
#### mvc config api
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    // Implement configuration methods...
}
```
xml形式在<mvc:annotation-driven>下的子元素进行配置
#### Type conversion
默认情况下fomatter,converter都安装了许多,并且还包含@NumberFormat以及@DateTimeFormat支持自定义字段,你也可以自定义注入一些formatter/ converters
```java

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // ...
    }
}
```
xml中稍微不一样
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/mvc
        https://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <mvc:annotation-driven conversion-service="conversionService"/>

    <bean id="conversionService"
            class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <property name="converters">
            <set>
                <bean class="org.example.MyConverter"/>
            </set>
        </property>
        <property name="formatters">
            <set>
                <bean class="org.example.MyFormatter"/>
                <bean class="org.example.MyAnnotationFormatterFactory"/>
            </set>
        </property>
        <property name="formatterRegistrars">
            <set>
                <bean class="org.example.MyFormatterRegistrar"/>
            </set>
        </property>
    </bean>

</beans>
```
默认mvc考虑请求locale(当解析、格式化日期数据时),可以自定义格式化形式
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }
}
```
注意: 查看 formatterRegistrar SPI 以及 FomattingConversionServiceFactoryBean 获取更多信息(如何、合适使用FormatterRegistrar实现)
#### 验证
类路径上出现bean 验证依赖(例如 hibernate 验证器),LocalValidatorFactoryBean 将作为一个全局的验证器注册(能够对@Valid 、 Validated 进行处理),如果你想自定义全局认证实例:
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public Validator getValidator() {
        // ...
    }
}
```
基于xml形式
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/mvc
        https://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <mvc:annotation-driven validator="globalValidator"/>

</beans>
```
注意的是能够局部注册Validator,例如
```java
@Controller
public class MyController {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(new FooValidator());
    }
}
```
如果您需要在某处注入 LocalValidatorFactoryBean，请创建一个 bean 并使用 @Primary 对其进行标记，以避免与 MVC 配置中声明的 bean 发生冲突
#### 拦截器
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
        registry.addInterceptor(new ThemeChangeInterceptor()).addPathPatterns("/**").excludePathPatterns("/admin/**");
        registry.addInterceptor(new SecurityInterceptor()).addPathPatterns("/secure/*");
    }
}
```
xml形式有所不同
```xml
<mvc:interceptors>
    <bean class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor"/>
    <mvc:interceptor>
        <mvc:mapping path="/**"/>
        <mvc:exclude-mapping path="/admin/**"/>
        <bean class="org.springframework.web.servlet.theme.ThemeChangeInterceptor"/>
    </mvc:interceptor>
    <mvc:interceptor>
        <mvc:mapping path="/secure/*"/>
        <bean class="org.example.SecurityInterceptor"/>
    </mvc:interceptor>
</mvc:interceptors>
```
#### Content Types
你能够配置spring mvc应该如何确定请求的media类型(从请求),例如Accept请求头,URL 路径扩展,查询参数,其他; \
默认仅仅检查Accept请求头 \
如果必须基于URL的content type 解析方案,考虑使用查询参数作为最佳策略而不是使用路径扩展,查看suffix match以及 后缀匹配以及RFD获取问题信息 \
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("json", MediaType.APPLICATION_JSON);
        configurer.mediaType("xml", MediaType.APPLICATION_XML);
    }
}
```
xml形式
```xml
<mvc:annotation-driven content-negotiation-manager="contentNegotiationManager"/>

<bean id="contentNegotiationManager" class="org.springframework.web.accept.ContentNegotiationManagerFactoryBean">
    <property name="mediaTypes">
        <value>
            json=application/json
            xml=application/xml
        </value>
    </property>
</bean>
```
#### 消息转换器
替换还是增加全由开发者决定
 configureMessageConverters() /  extendMessageConverters(),例如下面替换了默认的一个消息转换器:
 ```java
@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .indentOutput(true)
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .modulesToInstall(new ParameterNamesModule());
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
        converters.add(new MappingJackson2XmlHttpMessageConverter(builder.createXmlMapper(true).build()));
    }
}
```
上面的例子中进行了默认消息转换器的替换,Jackson2ObjectMpperBuilder用来创建普通配置(MappingJackson2HttpMessageConverter以及 MappingJackson2xmlHttpMessageConverter)独立启动,自定义日期格式,以及[jackson-module-parameter-names](https://github.com/FasterXML/jackson-module-parameter-names)注册,增加了访问参数名的支持(java 8特性) \
jackson默认属性:
* DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES is disabled.

* MapperFeature.DEFAULT_VIEW_INCLUSION is disabled. \
它自动注册以下已知模块(如果它们出现在类路径上)
* jackson-datatype-joda: Support for Joda-Time types.

* jackson-datatype-jsr310: Support for Java 8 Date and Time API types.

* jackson-datatype-jdk8: Support for other Java 8 types, such as Optional.

* jackson-module-kotlin: Support for Kotlin classes and data classes. \
注意: 除了 jackson-dataformat-xml 之外，使用 Jackson XML 支持启用缩进还需要 woodstox-core-asl 依赖项 \
其他的jackson模块也是必要的:
* jackson-datatype-money: Support for javax.money types (unofficial module).
  
* jackson-datatype-hibernate: Support for Hibernate-specific types and properties (including lazy-loading aspects). \
xml配置:
```xml
<mvc:annotation-driven>
    <mvc:message-converters>
        <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
            <property name="objectMapper" ref="objectMapper"/>
        </bean>
        <bean class="org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter">
            <property name="objectMapper" ref="xmlMapper"/>
        </bean>
    </mvc:message-converters>
</mvc:annotation-driven>

<bean id="objectMapper" class="org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean"
      p:indentOutput="true"
      p:simpleDateFormat="yyyy-MM-dd"
      p:modulesToInstall="com.fasterxml.jackson.module.paramnames.ParameterNamesModule"/>

<bean id="xmlMapper" parent="objectMapper" p:createXmlMapper="true"/>
```
#### 视图控制器
定义ParameterizableViewController的快捷方式(执行时立即转发给视图),你能够使用它进行静态资源映射(例如没有任何处理逻辑),直接映射URL到视图-生成响应 \
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
    }
}
```
xml配置
```xml
<mvc:view-controller path="/" view-name="home"/>
```
如果@RequstMapping 方法映射URL与视图控制器配置产生冲突,那么视图控制器配置不生效,因为基于注解的控制器URL匹配优先级更高(它足够指示了方法不允许405-METHOD_NOT_ALLOWED),415(UNSUPPORTED_MEDIA_TYPE),或者类似的响应能够设置给客户端去帮助调试,对于这种原因我们推荐避免分割URL处理在注解控制器以及视图控制器交叉处理;
#### 视图解析器
mvc 配置简单的注册了许多视图解析器,允许配置内容协商视图解析,通过使用JSP以及jackson作为默认JSON视图解析:
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.enableContentNegotiation(new MappingJackson2JsonView());
        registry.jsp();
    }
}
```
xml
```xml
<mvc:view-resolvers>
    <mvc:content-negotiation>
        <mvc:default-views>
            <bean class="org.springframework.web.servlet.view.json.MappingJackson2JsonView"/>
        </mvc:default-views>
    </mvc:content-negotiation>
    <mvc:jsp/>
</mvc:view-resolvers>
```
现在视图渲染技术也需要配置,例如FreeMarker或者脚本引擎;\
例如专用的元素配置视图工作:
```xml
<mvc:view-resolvers>
    <mvc:content-negotiation>
        <mvc:default-views>
            <bean class="org.springframework.web.servlet.view.json.MappingJackson2JsonView"/>
        </mvc:default-views>
    </mvc:content-negotiation>
    <mvc:freemarker cache="false"/>
</mvc:view-resolvers>

<mvc:freemarker-configurer>
    <mvc:template-loader-path location="/freemarker"/>
</mvc:freemarker-configurer>
```
java配置
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.enableContentNegotiation(new MappingJackson2JsonView());
        registry.freeMarker().cache(false);
    }

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("/freemarker");
        return configurer;
    }
}
```
#### 静态资源
提供了基于Resource定位的提供静态资源的方便方式: \
下一个例子设置了映射位置且增加了缓存配置: \
Resource#lastModified 推断Last-Modified是否修改了,支持Last-Modified请求头
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
            .addResourceLocations("/public", "classpath:/static/")
            .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));
    }
}
```
xml配置
```xml
<mvc:resources mapping="/resources/**"
    location="/public, classpath:/static/"
    cache-period="31556926" />
```
也可以使用ResourceResolver链处理支持或者ResourceTransformer实现,你能够用它创建一个优化且用于工作的工具链 \
您可以将 VersionResourceResolver 用于基于从内容、固定应用程序版本或其他计算出的 MD5 哈希的版本化资源 URL. 一个ContentVersionStrategy(MD5 hash)是一个好的选择- 除了一些值得注意的例外，例如与模块加载器一起使用的 JavaScript 资源 \
如何使用VersionResourceResolver
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/public/")
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
    }
}
```
xml配置
```xml
<mvc:resources mapping="/resources/**" location="/public/">
    <mvc:resource-chain resource-cache="true">
        <mvc:resolvers>
            <mvc:version-resolver>
                <mvc:content-version-strategy patterns="/**"/>
            </mvc:version-resolver>
        </mvc:resolvers>
    </mvc:resource-chain>
</mvc:resources>
```
然后，你可以使用ResourceUrlProvider来重写URL，并应用完整的解析器和转换器链--例如，插入版本。MVC配置提供了一个ResourceUrlProvider bean，这样它就可以被注入到其他地方。你也可以用ResourceUrlEncodingFilter使重写透明化，用于Thymeleaf、JSPs、FreeMarker和其他具有依赖HttpServletResponse#encodeURL的URL标签 \
请注意，当同时使用 EncodedResourceResolver（例如，用于提供 gzip 或 brotli 编码的资源）和 VersionResourceResolver 时，您必须按此顺序注册它们。这确保基于未编码的文件始终可靠地计算基于内容的版本 \
webjars 也支持(通过WebJarsResourceResolver自动注册资源,当org.webjars:webjars-locator-core库存在类路径上),此解析器能够重写URL去包括一个jar版本并针对没有版本的URL进行匹配,例如 /jquery/jquery.min.js 到 /jquery/1.2.0/jquery.min.js;
#### 默认servlet
mvc 允许DispatcherServlet 映射 /,同时可以将静态资源请求通过容器默认servlet处理,只需要配置一个DefaultServletHttpRequestHandler(并映射/**)以及低优先级的相对于其他URL的映射 \
这样会将所有资源请求转发给默认servlet,因此他必须作为URL 匹配的HandlerMapping的最后位置,作为兜底处理,xml形式使用<mvc:annotation-driven> 就是这样的情况,除此之外你也可以配置自定义的HandlerMapping实例,确保order属性设置足够低(比DefaultServletHttpRequestHandler低),默认值Integer.MAX_VALUE; \

java配置
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}
```
xml配置
```xml
<mvc:default-servlet-handler/>
```
servlet mapper 会使用RequestDispatcher派发给默认的Servlet,那么默认实现DefaultServletHttpRequestHandler会自动检测默认servlet(在一开始检测容器中),并且派发是通过servlet名称进行派发,不同的容器存在已知的Servlet名称,如果默认servlet被自定义,那么默认servlet名称是未知的(有可能),你需要手动显式提供默认servlet 名称:
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable("myCustomDefaultServlet");
    }
}
```
xml
```xml
<mvc:default-servlet-handler default-servlet-name="myCustomDefaultServlet"/>
```
#### 路径匹配
你能够定制路径匹配相关的选项以及URL的处理,PathMatchConfigurer查看配置的详细信息:
```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
            .setPatternParser(new PathPatternParser())
            .addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class));
    }

    private PathPatternParser patternParser() {
        // ...
    }
}
```
xml
```xml
<mvc:annotation-driven>
    <mvc:path-matching>
<!-- 尾部斜线-->
        trailing-slash="false" 
        path-helper="pathHelper"
        path-matcher="pathMatcher"/>
</mvc:annotation-driven>

<bean id="pathHelper" class="org.example.app.MyPathHelper"/>
<bean id="pathMatcher" class="org.example.app.MyPathMatcher"/>
```
#### 高级java 配置
@EnableWebMvc导入了DelegatingWebMvcConfiguration: 
* 提供了spring mvc应用的默认配置
* 检测并代理WebMvcConfigurer去定制配置 \
你能够移除@EnableWebMvc直接扩展DelegatingWebMvcConfiguration 去替代WebMvcConfigurer:
```java
@Configuration
public class WebConfig extends DelegatingWebMvcConfiguration {

    // ...
}
```
现在你仍然能够重写那些方法,并且仍然可以有许多WebMvcConfigurer;
#### 高级xml 配置
mvc namespace没有高级模式,如果你需要定制bean的属性,你不能够改变,要么使用后置处理器回调(关于ApplicationContext),例如:
```java
@Component
public class MyPostProcessor implements BeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
        // ...
    }
}
```
现在你可以通过组件扫描标签扫描后置处理器并运行时处理;<component-scan/>