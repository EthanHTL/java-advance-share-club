## CORS跨域
spring mvc 能够让你处理跨域CORS(cross origin resource sharing)
#### 介绍
由于安全原因,浏览器会阻止同域之外的ajax请求调用,例如,你有一个自己的银行账户在tab另一个在evil.com,来自evil.com脚本不应该能够发送ajax请求到你的银行api(通过你的凭证信息),例如从你的账户中取款; \
跨域资源共享是w3c标准被大多数浏览器实现让你能够指定那些类型的跨域请求能够被处理,而不是使用安全性较低或者很少使用的基于IFrame或者JSONP进行工作处理;
#### 处理
跨域标准分为预请求、简单请求、实际请求,为了学习cors如何工作,你可以阅读[文章](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS),也可以是其他或者查看规范获取更多信息; \
mvc HandlerMapping 实现提供了对CORS内置支持,在成功映射一个请求到处理器之后,HandlerMapping实现会检测跨域配置(对给定请求、处理器以及更多动作),预检测请求会直接执行,简单请求和 实际的CORS请求将会被拦截、验证、并且会有必须的CORS响应头被设置 \
为了能够启动跨域请求(Origin 请求头 必须出现并且标志请求的主机),你需要显式声明跨域配置,否则预请求会被拦截,没有跨域请求头会增加到简单的以及实际跨域请求响应头 上,结果就是浏览器会拒绝他们; \
每一个HanlderMapping 能够独立的配置-结合URL 模式的CorsConfiguration配置映射,大多数情况使用mvc java配置还活着xml配置声明这些映射,这回导致一个全局的映射会通过所有的HandlerMappings实例; \
能够在HandlerMapping级别合并全局跨域配置(更加细腻化的控制),handler-level cors配置,例如控制器方法上能够使用方法级@CorsOrigin注解解决跨域(或者实现CorsConfigurationSource解决跨域) \
合并全局以及局部配置的规则通常是相叠加,例如所有的全局、所有的局部源头,对于这些属性(这里仅仅接受一个值),例如 allowCredentials 以及 maxAge,局部值覆盖全局值,查看CorsConfiguration#combine(CorsConfiguration)获取更多信息; \
注意: 为了学习来自资源或者更多高级自定义,查看以下代码
* CorsConfiguration
* CorsProcessor,DefaultCorsProcessor
* AbstractHandlerMapping
#### @CrossOrigin
```java
@RestController
@RequestMapping("/account")
public class AccountController {

    @CrossOrigin
    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```
默认情况允许所有origins,允许所有headers,允许所有http 方法类型 \
allowCredentials默认没有启动,因此建立一个信任级别的暴露敏感用户信息(例如cookies以及CSRF tokens尤为重要)并且应该在合适的时候进行使用,当它启动要么allowOrigins必须设置为一个或者多个特殊域(但是不能够指定为"*")或者可选的allowOriginPattern属性也可以用来匹配动态的origins集合; \
maxAge 30分钟,@CrossOrigin支持类级别设置,会被所有方法继承 \
```java
@CrossOrigin(origins = "https://domain2.com", maxAge = 3600)
@RestController
@RequestMapping("/account")
public class AccountController {

    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```
你能够同时使用在类或者方法上
```java
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/account")
public class AccountController {

    @CrossOrigin("https://domain2.com")
    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```
####  全局配置
除了controller方法级别的细腻控制,你可以全局配置,你能够设置基于URL的CorsConfiguration映射(独立于HandlerMapping),大多数应用都会这样做, mvc java配置或者xml \
* 允许所有origins
* 允许所有headers
* GET,Head,POST请求 \
allowCredentials 默认没有启动,因为这建立了一个暴露敏感用户特定信息（例如 cookie 和 CSRF 令牌）的信任级别，并且只应在适当的情况下使用,allowOrigins不能设置为"*" 也可以使用allowOriginPattern属性设置一个模式匹配动态origins; \
maxAge 30分钟
##### 基于java配置
CorsRegistry回调 
```java
@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {

        registry.addMapping("/api/**")
                .allowedOrigins("https://domain2.com")
                .allowedMethods("PUT", "DELETE")
                .allowedHeaders("header1", "header2", "header3")
                .exposedHeaders("header1", "header2")
                .allowCredentials(true).maxAge(3600)

        // Add more mappings...
    }
}
```
XML 配置,需要使用<mvc:cors>
```java
<mvc:cors>

    <mvc:mapping path="/api/**"
        allowed-origins="https://domain1.com, https://domain2.com"
        allowed-methods="GET, PUT"
        allowed-headers="header1, header2, header3"
        exposed-headers="header1, header2" allow-credentials="true"
        max-age="123" />

    <mvc:mapping path="/resources/**"
        allowed-origins="https://domain1.com" />

</mvc:cors>
```
#### 跨域过滤器
内置跨域过滤器,如果和Spring security一起用,需要注意Spring Security本身对跨域存在内置支持; \
为了配置这个过滤器,通过CorsConfigurationSource作为构造器参数:
```java
CorsConfiguration config = new CorsConfiguration();

// Possibly...
// config.applyPermitDefaultValues()

config.setAllowCredentials(true);
config.addAllowedOrigin("https://domain1.com");
config.addAllowedHeader("*");
config.addAllowedMethod("*");

UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
source.registerCorsConfiguration("/**", config);

CorsFilter filter = new CorsFilter(source);
```
#### Web 安全
spring security提供了保护web项目的安全性,查看spring security参考文档，包括:
* spring mvc security
* spring mvc test support
* csrf protection
* security response headers \
[HDIV](https://hdiv.org/)是一个可以和spring mvc集成的其他web安全框架;
           
