# CORS
Cross Origin Resource Sharing 是一个w3c规范-被大多数浏览器实现(让你可以指定那些跨域请求是被认证的),而不是使用一些安全性较低以及很少有用的基于IFRAME或者JSONP的工作区! \
CROS 辨别预请求、简单请求、实际请求,了解更多,查看https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS \
spring webFlux HandlerMapping 实现提供了对CORS的内置支持,在成功映射请求到handler之后,HandlerMapping会对给定的请求以及处理器检查CORS配置以及其他更详细的动作,预请求会直接处理,简单和实际CORS请求将会被拦截,验证,是否必须的CORS响应头被设置! \
为了启用跨域请求(Origin 请求头必须出现,可以是不同host),你需要显式声明跨域配置,如果没有发现,与请求将被拒绝,没有CORS响应头会被增加到简单以及实际的跨域请求的响应中,因此浏览器会拦截它们; \
可以使用基于URL pattern的CorsConfiguration映射,对每一个HandlerMapping进行独立配置,大多数情况,使用webflux java 配置声明这些映射,这导致单个、全局映射传递给所有的HandlerMapping实现; \
你能够合并全局的Cors配置(在HandlerMapping级别进行更加细腻的控制),注解的控制器能够使用@CrossOrigin(其他处理器也能够实现CorsConfigurationSource) \
合并全局以及局部配置通常是可选的,举个例子所有全局以及所有局部的origins. 对于那些属性这里仅仅有单个值能够被接受,例如allowCredentials以及maxAge,局部可以覆盖全局值,查看[CorsConfiguration#combine(CorsConfiguration)](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/javadoc-api/org/springframework/web/cors/CorsConfiguration.html#combine-org.springframework.web.cors.CorsConfiguration-)获取更多! \
为了学习从资源或者更多高级定制:
- CorsConfiguration
- CorsProcessor 以及 DefaultCorsProcessor
-AbstractHandlerMapping
### @CorsOrigin
可以使用allowOriginPatterns  设置origins的正则
### global configuration
除了细腻配置,可以定义一些全局CORS配置,能够在每一个HandlerMapping上个独立设置基于URL的CorsConfiguration mappings,大多数应用,使用 webflux java配置做这个事情! \
默认的全局配置启动如下:
- all origins
- all headers
- get / head / post 方法 \
allowedCredentials 没有默认启用,因为会建立一个信任的级别(暴露用户的敏感信息-例如 CSRF token)只有在合适的时候才使用,allowOrigins 必须设置一个或者多个指定的domain,不能是 "*"
### Cors WebFilter
CorsFilter和Spring Security集成,Spring Security对跨域有内置支持;
```text
@Bean
CorsWebFilter corsFilter() {

    CorsConfiguration config = new CorsConfiguration();

    // Possibly...
    // config.applyPermitDefaultValues()

    config.setAllowCredentials(true);
    config.addAllowedOrigin("https://domain1.com");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsWebFilter(source);
}
```
