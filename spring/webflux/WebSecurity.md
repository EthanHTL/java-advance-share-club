# Web Security
- [WebFlux Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#jc-webflux)
- [WebFlux Testing Support](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#test-webflux)
- [CSRF Protection](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#csrf)
- [Security Response Headers](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#headers)
###view technologies
#### Thymeleaf
Spring WebFlux的集成,配置只需要一些bean声明,SpringResourceTemplateResolver,SpringWebFluxTemplateEngine,ThymeleafReactiveViewResolver,了解更多,查看[Thymeleaf + Spring](https://www.thymeleaf.org/documentation.html) 以及 WebFlux 集成[说明](http://forum.thymeleaf.org/Thymeleaf-3-0-8-JUST-PUBLISHED-td4030687.html)
#### FreeMarker
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.freeMarker();
    }

    // Configure FreeMarker...

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("classpath:/templates/freemarker");
        return configurer;
    }
}
```
#### Freemarker configuration
能够传递freeMarker setting 以及SharedVariables 给FreeMarker的Configuration对象(这将被Spring维护)-通过设置合适的属性-在FreeMarkerConfigurer bean, freemarkerSettings属性需要一个java.util.Properties对象,并且freemarkerVariables 属性需要一个map:
```text
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    // ...

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("xml_escape", new XmlEscape());

        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("classpath:/templates");
        configurer.setFreemarkerVariables(variables);
        return configurer;
    }
}
```
#### form 处理
spring提供了对jsp的类库支持(<spring:bind>.....) 主要是为了展示从后台返回来需要在前端表单展示的对象-这些可能是由于Validator验证失败的详细信息或者业务逻辑,Spring同样在freemarker中支持同样的功能,使用方便可选的宏生成表单输入元素!
#### bind Macros 
这些宏定义维护在spring-webflux.jar中,有些宏定义在spring template库中考虑内部使用,但是宏都没有作用域定义,对于调用代码和用户模板都是可见的,下面的部分集中仅仅在你的模板中直接调用需要的宏,如果你希望直接查看宏代码,这个文件叫做spring.ftl,在org.springframework.web.reactive.result.view.freemarker package下; \
### 其他
.....
