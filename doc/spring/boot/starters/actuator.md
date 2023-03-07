# actuator 指南
## introduction
启用生产特性,例如actuator ..
### get started
```xml
 <dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```
或者使用gradle
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```
### 端点
此项目为我们提供了很多端点,都是基于伴生的应用中所收集、统计的 ..
本质上能够让我们监控或者与应用进行交互 ..
spring boot 包含了大量内置的端点 ,同时我们可以加入自己的 ..
同样每一个端点都可以启用并暴露(例如通过http / jmx) ...
但是内置的端点在可用时将会自动配置 ...
同样很多应用都选择通过http暴露,那么URL上会包含一个`/actuator`的前缀并紧跟端点的id ..,例如 \
health 端点将会暴露为`/actuator/health` .
内置的端点有专门的API 文档进行学习了解 ...
对于内置的端点这里就不再赘述查看相关的API 文档进行学习 . \
假设应用是一个Web应用(例如Spring mvc,Webflux,jersey),我们还有一些额外的端点可以使用 ..
- heapdump
    返回一个hprof 堆转储文件,需要HotSpot jvm才能做到这个事情 ..
- jolokia
    通过Http暴露JMX beans(当jolokia 在类路径上,对于webflux 不可用),需要一个依赖`jolokia-core` 
- logfile
    返回日志文件的内容(如果`logging.file.name` 或者`logging.file.path` 属性已经设置),支持使用Http的Range请求头去抓取日志文件的部分内容 ..
- prometheus
    暴露指标以一种能够被prometheus 服务器理解的格式 ..
    但是需要一个`micrometer-registry-prometheus` 依赖
### 2.1 启动端点
默认除了`shutdown` 端点都是启用的,为了配置一个端点的启用,那么可以使用management.endpoint.<id>.enable属性配置,这里的id表示端点的id,也可以说是简短的端点名\
例如,`shutdown` 启用就可以设置
```properties
management.endpoint.shutdown.enabled=true
```
如果您更喜欢端点启用是选择加入而不是选择退出，那么可以设置`management.endpoints.enable-by-default` 属性设置为`false` ,那么你可以选择让特定的端点启用 .. \
例如启用`info` 端点并禁用其他所有端点
```properties
management.endpoints.enabled-by-default=false
management.endpoint.info.enabled=true
```
注意: 禁用端点相当于完全从应用上下文中移除,如果你想要改变哪一个端点应该暴露,相反,应该使用`include` 以及`exclude` 属性 ..
### 2.2 暴露一个端点
由于一些端点也许包含了敏感的信息,小心思考是否应该暴露它们, 参考官网:  https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/production-ready-features.html#production-ready-endpoints-exposing-endpoints
为了暴露一个端点,我们可以使用`include` 以及 `exclude` 属性 \
例如:
`management.endpoints.jmx.exposure.exclude`设置为 info,health 则相当于jmx中不暴露info以及 health端点 ..
```properties
management.endpoints.jmx.exposure.include=health,info
```
也可以使用 * 选择所有的端点 .. 例如,在Http上暴露任何端点除了`env` 以及`beans` 端点 ... 使用以下属性
```properties
management.endpoints.web.exposure.include="*"
management.endpoints.web.exposure.exclude=env,beans
```
由于* 在yaml中有特殊的含义,因此你需要增加双引号(如果你想要包装 / 排除所有节点) .. \
如果你的应用是公开暴露的,强烈建议加固端点 ...也就是结合SpringSecurity 保障端点的安全 ...\
如果你想要实现端点的暴露自定义策略,可以注册一个 `EndpointFilter` bean ...

### 2.3 安全的http 端点
也就是我们可以保证端点的安全(通过和保护其他敏感url的相同方式去) .. 如果spring security 存在,那么端点默认就是被spring security的
内容协商策略保护 ..\
如果你想要为http 端点配置自定义的security ,例如紧急允许用户具有某一个角色才能够访问它们,那么spring boot 提供了某一些方便的`RequestMatcher` 对象你能够与spring security
结合使用 ... \
一个典型的spring security 配置可能看起来类似于如下示例:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.requestMatcher(EndpointRequest.toAnyEndpoint()).authorizeRequests((requests) ->
        requests.anyRequest().hasRole("ENDPOINT_ADMIN"));
    http.httpBasic();
    return http.build();
}
```
这个示例中使用`EndpointRequest.toAnyEndpoint()` 去匹配一个请求到任何端点并确保他们必须能够被有`ENDPOINT_ADMIN` 角色的用户访问 ...\
并且还包含了其他可以在EndpointRequest使用的各种匹配方法 ..,可以查看 端点的API 文档了解更多 ... \
如果你将应用部署到防火墙之后,你可能更希望actuator端点能够被访问而无需认证,那么你可以通过改变`management.endpoints.web.exposure.include` 属性来做到这样的事情 ...
```properties
management.endpoints.web.exposure.include=*
```
例如include中也可以填入希望暴露的端点来处理这样的事情 .. \
除此之外,如果Spring security 出现,你可能希望增加额外的安全配置,允许未认证的用户访问端点 ..
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.requestMatcher(EndpointRequest.toAnyEndpoint()).authorizeRequests((requests) ->
            requests.anyRequest().permitAll());
    return http.build();
}
```
上述示例中,配置仅仅应用到actuator 端点 ... 由于spring boot的安全配置会完全避让已经存在的任何 SecurityFilterChain bean ... 你将需要配置一个额外的
SecurityFilterChain bean(使用了角色的)运用到应用的其他部分 ..
### 2.4 配置端点
例如端点会自动的缓存没有携带任何参数的读操作的响应,为了配置一个端点缓存响应的时间,我们可以使用`cache.time-to-live` 属性去配置 . \
例如`beans` 端点的缓存时间控制 ..
```properties
management.endpoint.beans.cache.time-to-live=10s
```
这个前缀`management.endpoint.<name>` 作为唯一性标识哪一个端点被配置 ...
### 2.5 对于actuator web 端点的超媒体
对于actuator来说,为每一个web 暴露的端点都添加了链接(也就是对应端点的请求地址),这种页面称为发现页面 ...(discovery page),默认通过`/actuator`进行访问 ..\
当一个自定义管理上下文路径配置了,那么发现页面将自动从`/actuator` 进行移除并设置到管理上下文的根下 ... 例如管理上下文的路径是`/management` ,那么发现页面将通过`/management` 可用 \
当一个管理上下文的路径设置为`/` ,那么发现页面将被禁用为了阻止与其他映射冲突的可能性 ...
### 2.6 跨域支持
跨域资源共享(CORS)是一个[W3C 规范](https://www.w3.org/TR/cors/) 能够让我们在一种灵活的方式指定哪一类跨域请求是授权了的 .. 如果你使用Spring mvc或者 WebFlux,那么Actuator的web端点能够配置去支持 \
这些场景 .. \
CORS 支持默认是禁用的(仅仅通过`management.endpoints.web.cors.allowed-origins` 属性已经被设置),以下的配置允许接收来自`example.com`域的`GET` 和 `POST`请求:
```properties
management.endpoints.web.cors.allowed-origins=https://example.com
management.endpoints.web.cors.allowed-methods=GET,POST
```
查看[CorsEndpointProperties](https://github.com/spring-projects/spring-boot/tree/v2.4.13/spring-boot-project/spring-boot-actuator-autoconfigure/src/main/java/org/springframework/boot/actuate/autoconfigure/endpoint/web/CorsEndpointProperties.java) 了解完整的选项列表
### 2.7 实现自定义端点
如果你增加了一个被@Endpoint注解的@Bean,任何使用@ReadOperation 或者@WriteOperation或者 @DeleteOperation注解注释的方法能够自动的在JMX或者HTTp上进行暴露 ...\
暴露形式就是JMX / 或者 Jersey / Spring mvc / Spring WebFlux .. 如果Jersey和Spring MVC同时可用,那么 SpringMvc将会被使用 ..、
以下的示例暴露一个读操作(返回自定义对象)
```java
@ReadOperation
public CustomData getCustomData() {
    return new CustomData("test", 5);
}
```
你也能够通过@JmxEndpoint或者@WebEndpoint 编写特定于技术的端点,这些缎带你将限制为相关的技术,例如@WebEndpoint仅仅在Http上进行暴露而不在JMX中暴露 ..\
你也能够写特定于技术的扩展(通过使用@EndpointWebExtension 以及 @EndpointJmxExtension),这些注解能够让你提供特定于技术的操作去扩大存在的端点 .. \
最终,如果你需要访问特定于Web框架的功能,你能够实现Servlet或者Spring @Controller以及@RestController 端点,但是代价是无法通过JMX进行使用(或者完全使用不同框架时) ..\
也就是端点扩展依旧需要例如servlet/ spring mvc规范暴露出去 ... / 根据技术相关性 ..
#### 2.7.1 接收输入
一个端点上的操作可以通过它们的参数接收输入, 当通过web暴露shi,这些参数的值可以来自URL 的查询参数或者JSON 请求体 . \
当通过JMX时,这些参数将映射到MBean的操作上 ... 参数默认是必须的, 这意味着你能够通过`@javax.annotation.Nullable` 或者`@org.springframework.lang.Nullable` 注释它们为可选的 .. \
在JSON 请求体中的每一个根属性能够映射到端点的参数上,考虑以下JSON 请求body:
```json
{
  "name": "test",
  "counter": 42
}
```
者能够被用来执行一个写操作(携带了String name 以及 int counter参数),展示如下:
```json
@WriteOperation
public void updateCustomData(String name, int counter) {
    // injects "test" and 42
}
```
由于端点是技术无关的,仅仅只有简单的类型能够在方法签名中进行指定 ... 尤其是当声明单个定义了`name`以及`counter`属性的`CustomData` 类型的参数是不支持的 \
为了让输入能够映射到操作的方法参数,实现一个端点的Java代码应该通过通过`-parameters` 进行编译 ,如果通过kotlin代码实现(应该通过`java-parameters` 进行编译).. \
这将自动的触发(如果使用Spring boot的gradle 插件,或者使用Maven以及 spring-boot-starter-parent时).... ,那么我们可能需要看一下这个依赖中的pom配置 ...
##### 输入类型转换
参数将传递到端点的操作方法中,如果有必要,可以自动的转换到需要的类型.在调用一个方法之前,通过JMX或者Http请求接收的输入将会转换为需要的类型(使用ApplicationConversionService实例，同样任何的通过@EndpointConverter修饰的转换器Converter或者GenericConverter bean都是可以的)

#### 2.7.2 自定义Web端点
在@Endpoint / @WebEndpoint / @EndpointWebExtension将自动的暴露在Http上(通过jersey/ spring mvc/spring webflux),优先级spring webflux 更高 ..
##### web 端点请求条件
针对于基于web暴露的端点的每一个操作将会自动更生一个请求断言 ...
- 路径
  断言的路径通过端点的id以及微博暴露的端点的basepath 决定的,这个默认的base path 默认是`actuator`,例如,一个具有`sessions` id的端点将会使用`/actuator/sessions` 作为它的断言路径 \
  这个路径能够被深度自定义(通过@Selector注释操作的一个或者多个参数),例如增加到路径断言的参数将作为一个路径变量,这个变量的值将在端点的操作进行执行时传递给操作方法... \
  如果你想要捕捉所有保留的路径元素,你能够增加`@Selector(Match=ALL_REMAING)` 到最后的一个参数并且标记它是一个与`string[]` 转换兼容的类型
- http method
    断言的http方法是通过操作的类型进行决定的,例如 / GET/ POST /DELETE
- consumes
    也就是它仅仅消费对应类型的参数(通过MediaType决定)
    对于一个@WriteOperation(Http POST) 使用请求body, 他消费断言的条件是`application/vnd.spring.boot.actuator.v2+json,application/json` ,对于其他操作(消费条件是空的) ...
- produces
    本质上,也就是表明这个操作会产生什么样的结果(通过MediaType进行设定)
    断言的生产条件是通过@DeleteOperation,@ReadOperation,@WriteOperation 注解的`produces` 属性决定的,这个属性是可选的,如果他没有被使用,那么生产条件将自动决定... \
    如果这个操作方法返回`void` 或者`Void` 那么生产条件是空,如果这个操作方法返回`org.springframework.core.io.Resource` ,那么生产约定就是`application/octet-stream` ,对于所有其他的操作这个产生\
    条件(合约)是`application/vnd.spring-boot.actuator.v2+json,application/json` 
- Web 端点响应状态
    对于一个端点操作的默认响应状态依赖于操作类型(read / write /delete)以及(如果有,则操作返回的内容) ... \
    一个@ReadOperation 返回了一个值,那么响应状态将是200(OK),如果没有返回值,那么响应状态码则是404(未发现) ... \
    对于一个@WriteOperation / @DeleteOperation 返回了一个值,那么响应状态码将设置为200(OK),如果没有返回一个值(则标识没有内容204(No Content)) ..
    如果一个操作执行(却没有必要的参数，或者说使用的参数将不能够转换为一个需要的类型,那么这个方法将不会被调用并且响应状态码将设置为400(Bad Request))
- Web 端点Rang 请求
    一个Http 范围请求能够被用来请求一个Http 资源的一部分,当使用spring mvc / webflux,返回`org.springframework.core.io.Resource` 将自动的支持range请求 ...
    当使用Jersey的时候,Range请求暂时不支持 ...
- web端点安全
    在web端点上的一个操作或者特定于web的端点扩展能够接收当前的`java.security.Principal` 或者`org.springframework.boot.actuate.endpoint.SecurityContext` 作为方法参数 ..\
    前者通常结合@Nullable使用去提供不同的行为(例如认证和未认证用户),后者通常被用来执行授权检查(使用`isUserInRole(String)` 方法) ..
#### 2.7.3 servlet 端点
一个servlet能够暴露为一个端点(通过实现一个具有@ServletEndpoint注解的类即可,同样也需要实现`Supplier<EndpointServlet>`),servlet 端点提供了和Servlet 容器的深度集成,但是损失了可移植性 .. \
它们是有意的用来暴露一个存在的servlet作为端点,对于新的端点,@Endpoint以及@WebEndpoint注解应该是首选(无论什么时候) ...
#### 2.7.4 控制器端点(controller endpoints)
@ControllerEndpoint 以及 @RestControllerEndpoint 能够被用来实现仅仅通过spring mvc / spring webflux暴露的端点,方法通过spring mvc / webflux的标准注解进行映射(例如@RequestMapping / @GetMapping),\
使用端点的id作为路径的前缀 ... 控制器端点提供了深度集成(与spring 的web框架,同样损失了可移植性),`@Endpoint` 以及`@WebEndpoint` 注解应该首选(无论什么时候,或者尽可能)

### 2.8 健康信息
你能够使用健康信息来检查你运行服务的状态,它经常被监控软件使用去告知某个人(当生产系统挂掉之后),这个信息是通过`health`端点进行暴露的(依赖于`management.endpoint.health.show-details` 以及`management.endpoint.health.show-components` 属性) \
它们能够被配置为以下值:
- never 绝不展示详细
- when-authorized 仅仅对授权用户进行展示详情,角色通过`management.endpoint.health.roles` 配置 ..
- always 向所有用户展示详情

如果你希望保护你的应用并且希望使用`always` ,你的安全配置必须允许访问health 端点(对认证 / 或者未认证的用户) ... \
健康信息从HealthContributorRegistry 的上下文进行收集的(默认是定义在applicationContext中的所有`HealthContributor`实例) . Spring  boot 包括了大量自动配置HealthContributor,并且你也可以配置自己的 ..\
一个HealthContributor 可以是 `HealthIndicator` 或者`CompositeHealthContributor`.. 一个HealthIndicator 提供了实际的健康信息,包括`Status`. 一个`CompositeHealthContributor` 提供了其他`HealthContributor` 的组合信息 .. \
相互结合,贡献者的结构是一个树的结构去呈现整个系统的健康 ... \
默认情况,最终的系统健康是通过`StatusAggregator` 驱动的,它会排序每一个`HealthIndicator`的状态(基于一个有序的状态列表),在排序列表中的第一个状态被用来作为整体的健康状态,如果没有`HealthIndicator` 返回状态,那么StatusAggregator将返回整体的\
健康状态信息为`UNKNOWN` 状态 ... \
这个`HealthContributorRegistry` 能够被用来注册以及取消注册 health indicator ..(在运行时) ...
#### 2.8.1 自动配置的HealthIndicators
Spring boot 为我们自动配置了很多的HealthIndicator(当合适时将自动启用),你能够启用或者禁用选择的indicators(通过配置),`management.health.key.enabled` ,[key](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/production-ready-features.html#production-ready-health-indicators) 将在spring 官方文档中列出 . \  
那么可以通过`management.health.defaults.enabled` 设置整体禁用或者启用 .. \
额外的HealthIndicator可用但是默认没有启用
- livenessstate LivenessStateHealthIndicator 暴露'liveness - 活跃度'的应用必要状态
- readinessstate ReadinessStateHealthIndicator 暴露'Readiness - 准备度'的应用必要状态
#### 2.8.2 写自定义的HealthIndicators
为了提供自定义的health information,你能够注册Spring bean(实现了HealthIndicator接口的),你需要提供`health()` 方法的实现并返回`Health` 响应 ..\
这个响应必须包含一个状态并且能够可选的包括额外的详情能够被展示,下面的代码将展示示例HealthIndicator实现 ...
```java
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MyHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        int errorCode = check(); // perform some specific health check
        if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode).build();
        }
        return Health.up().build();
    }

}
```
给定HealthIndicator的标识符是bean的名字(没有HealthIndicator后缀),在前面的示例中,健康信息在名为'my'的条目是可用的 .. \
除了spring boot 预定义的Status 类型,你也可以让`Health` 返回自定义的`Status` (代表一个新的系统状态),在这些情况中,一个`StatusAggregator` 接口自定义实现也需要被提供,或者默认的实现已经通过`management.endpoint.health.status.order` 配置属性进行配置了 ...\
举个例子,假设一个具有`FATAL` 代码的Status已经使用在你的`HealthIndicator` 实现的其中之一,那么为了配置严格的顺序,那么需要增加以下属性到应用属性中:
```properties
management.endpoint.health.status.order=fatal,down,out-of-service,unknown,up
```
在响应中的HTTP status code反映了整体的健康状态,默认`OUT_OF_SERVICE` 以及 `DOWN` 映射到503,任何未映射的健康状态,包括UP,映射到200,你可能想要注册自定义的状态映射 \
如果你通过HTTP 访问health 端点,配置自定义映射默认是禁用的(对于DOWN / OUT_OF_SERVICE)来说,如果你想要保留默认的映射,它们必须显式的配置在任何自定义映射旁边,下面的属性映射 \
FATAL到503(服务不可用)并且保留对`DOWN` 和`OUT_OF_SERVICE`的默认映射 ...
```properties
management.endpoint.health.status.http-mapping.down=503
management.endpoint.health.status.http-mapping.fatal=503
management.endpoint.health.status.http-mapping.out-of-service=503
```
如果需要更多配置,那么你能够定义自己的HttpCodeStatusMapper bean .. \
以下的表展示了内置状态的默认状态映射
- down 服务不可用(503)
- out_of_service 服务不可用(503)
- up 默认没有映射,所以http 状态码200
- unknown 默认没有映射,所以http状态200

#### 2.8.3 响应式的健康指示器
对于响应式应用,例如使用spring webflux,`ReactiveHealthContributor` 提供了非阻塞约定(为了获取应用健康),类似于传统的`HealthContributor` ,健康信息通过`ReactiveHealthContributorRegistry` 的上下文进行收集(默认是定义在应用上下文中的HealthContributor, ReactiveHealthContributor实例) .. \
常规的HealthContributors 在弹性的调度器上执行时不需要针对响应式API 进行检查 ..
> 注意: 在响应式应用中,`ReactiveHealthContributorRegistry` 应该被用来注册或者取消注册指示器(在运行时),如果你不需要注册常规的`HealthContributor` ,你应该使用`ReactiveHealthContributor#adapt进行包装 ..

为了从响应式API 提供自定义的健康信息,需要注册实现了`ReactiveHealthIndicator` 接口的spring bean ...,例如:
```java
@Component
public class MyReactiveHealthIndicator implements ReactiveHealthIndicator {

    @Override
    public Mono<Health> health() {
        return doHealthCheck() //perform some specific health check that returns a Mono<Health>
            .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()));
    }

}
```
为了自动处理错误,请考虑从`AbstractReactiveHealthIndicator` 继承 ..
#### 2.8.4 自动配置的ReactiveHealthIndicators
[indicators](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/production-ready-features.html#reactive-health-indicators-autoconfigured)
如果有必要,响应式indicator将会 替换普通的,同样,任何未显式处理的`HealthIndicator`将会自动包装 ..
#### 2.8.5 健康组
有时候,组织健康指示器到分组能够在不同的目的中进行使用 .. \
为了创建一个健康指示器组(你能够使用`management.endpoint.health.group.<name>` 属性)并指定健康指示器id列表到`include` 或者`exclude` ,例如,为了创建一个仅仅包含数据库指示器的分组你仅仅只需要这样做:
```properties
management.endpoint.health.group.custom.include=db
```
你现在能够通过敲击`localhost:8080/actuator/health/custom` 进行结果检查 .. \
类似的,为了创建一个分组(排除了数据库指示器并包括了其他指示器),你能够定义如下:
```properties
management.endpoint.health.group.custom.exclude=db
```
默认分组将会继承于系统健康相同的`StatusAggregator` 以及 `HttpCodeStatusMapper` 配置,然而这些能够在每一个组基础上进行定义,它也可能去覆盖`show-details` 以及`roles` 属性(如果需要):
```properties
management.endpoint.health.group.custom.show-details=when-authorized
management.endpoint.health.group.custom.roles=admin
management.endpoint.health.group.custom.status.order=fatal,up
management.endpoint.health.group.custom.status.http-mapping.fatal=500
management.endpoint.health.group.custom.status.http-mapping.out-of-service=500
```
你能够使用`Qualifier("groupname")` ,如果你需要注册自定义的`StatusAggregator` 或者`HttpCodeStatusMapper` beans来与组使用..
### 2.9 k8s 探针
当应用部署到k8s中能够使用[容器探针](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes)来提供它们内部的状态 ...,依赖于你的k8s配置,kubelet 将能够调用这些探针并反应结果 .. \
spring boot 管理你的应用可用状态(开箱即用),具体查看spring boot 特性文档,如果它部署到k8s环境中,actuator将收集"活跃度,liveness" 以及 "准备就绪"的信息 - 从`ApplicationAvailability` 接口获取并且在专门的`Health indicators` 使用这些信息 \
例如LivenessStateHealthIndicator / ReadinessStateHealthIndicator .. 这些指示器能够展示在全局的健康端点进行展示("/actuator/health"),它们将暴露未单独的Http探针(通过health group) - "/actuator/health/liveness"
和"/actuator/health/readiness" .. \
你你能够这样配置你的k8s 基础设施(使用以下端点信息):
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: <actuator-port>
  failureThreshold: ...
  periodSeconds: ...

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: <actuator-port>
  failureThreshold: ...
  periodSeconds: ...
```
> `<actuator-port>` 应该设置为actuator端点的具体所在应用的端口,它可以是主 web服务器端口,也可以是单独的management 端口(如果management.server.port属性已经设置)

这些健康组将自动的启动(如果应用运行在k8s环境中),你能够在任何环境中启用它们(通过使用`management.endpoint.health.probes.enabled` 配置属性) .\
如果一个应用启动时间超过了配置的liveness 时间段,k8s使用"startupProbe"作为一个可能的解决方案. 这个'startupProbe' 是非必须的,因为这里的'readinessProbe'失败直到所有的startup 任务已经完成,查看[在应用的声明周期中探针的行为](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/production-ready-features.html#production-ready-kubernetes-probes-lifecycle) \
如果你的actuator端点是部署在独立的管理上下文中,那么则需要知道这些端点并没有使用和主应用相同的web基础设施(比如端点,连接协议,框架组件) ..在这种情况下,一个探针检查可能会成功(即使主应用可能没有没工作,例如它不能接收新的连接,而导致探针检查流程是Ok的) ...
#### 2.9.1 使用k8s探针检查外部的状态
actuator 配置了`liveness` 以及`readiness` 探针作为健康组,这意味着所有的健康组特性对他们来说可用,举个例子,配置额外的健康指示器:
```properties
management.endpoint.health.group.readiness.include=readinessState,customCheck
```
默认情况下,spring boot 并没有为这些组增加额外的健康指示器 .. \
活跃度(liveness)探针应该不依赖外部系统的健康检查. 如果用的活跃度被中断,k8s将会尝试重启应用实例来解决问题 .. 这意味着如果一个外部系统失败(例如数据库 / web api,外部缓存),k8s将可能重启所有的应用实例并创建级联的失败 ... \
对于'readiness' 探针,检查外部系统的选择必须由应用开发者小心的指定,例如: spring boot 并没有包含任何额外的健康检查(在readiness 探针中),如果一个应用实例的readiness 状态是unready,k8s将不会路由流量到这个实例,某些外部系统可能没有被应用实例共享,在这种情况下它们可能会很自然的包括在就绪探测中 .. 其他外部系统可能对于应用来说并不是必须的(应用可能有熔断器以及降级),在这种情况下完全不应该包括到这个探测阶段 ...
幸运的是,通常一个外部系统是被所有应用实例共享的,并且你必须做出判断,包括 它在readiness 探测中并且期待这个应用在外部系统挂掉时它停止服务,或者说移除它并处理堆栈更高层的错误,例如在调用方使用熔断器 ..(例如将它移除,熔断器能够得到错误信息,从而做出正确的响应) ...
> 如果一个应用的所有实例都是未准备,那么一个k8s服务(使用了type=ClusterIP 或者NodePort)将不能够接收任何进入的连接. 这里没有Http 错误响应(例如 503)， 因为这里没有连接 ..
一个使用了type=LoadBalancer的k8s服务可能或者可能不能接收连接,依赖于提供器(例如负载均衡后面已经没有可以支持的服务了,那么则不能接收连接,这是我的猜测,具体负载均衡的处理,k8s不是很清楚),一个使用了显式[Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)的服务将使用依赖于实现的方式进行响应 ,这个ingress 服务自身将可以决定怎样处理来自下游的"连接 拒绝",http 503在负载均衡和 ingress 情况下非常有可能出现 ..

同样,一个应用如果使用了k8s [自动缩放](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)(它可能对从负载均衡中取出的应用反应是不同的),依赖于自动缩放器的配置 ..
#### 2.9.2 应用生命周期和探针的状态
k8s探针支持的一个重要方面是它与应用生命周期是一致的,这是一个重要的不同(在AvailabilityState-在内存中,应用的内部状态 以及实际的探针暴露的状态之间),前者依赖于应用生命周期的阶段,探针可能不可用 ... \
spring boot 发布了在启动和关闭的应用事件,那么探针可以监听这些事件并暴露`AvailabilityState` 信息 ... \
以下展示了`AvailabilityState` 以及在不同阶段的Http连接器的状态 ... \
当spring boot 应用启动的时候: \

| startup phase  |  livenessState | readinessState | HTTP server | Notes  |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| starting  | broken  |  refusing_traffic  | not started | k8s 检查了 "liveness" 探针并且如果花费时间太长将重启应用 |
| started  | correct  |  refusing_traffic  | refuses requests | 这个应用上下文已经刷新,应用执行启动任务并且可能不能够接收流量 |
| ready    | correct  | accepting_traffic | accepts requests | 启动任务已经完成,这个应用可以接收流量 | 

当spring boot 应用关掉的时候: \


| shutdown phase | liveness state | readiness state | http server | notes |
| -------------- | -------------- | -------------- | -------------- | -------------- |
| running        | correct        | accepting-traffic | accepts requests | 关闭已经请求 |
| graceful shutdown | correct |   refusing_traffic | 新的请求拒绝 | 如果启用,优雅关闭会处理正在进行的请求|
| shutdown complete | N/A |  N/A | 服务关闭 | 应用上下文已经关闭并且应用已经停止|
> 检测[k8s 容器生命周期部分](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/deployment.html#cloud-deployment-kubernetes-container-lifecycle)了解有关k8s 部署的信息

### 2.10 应用信息
应用信息暴露了在应用上下文中的所有`InfoContributor` bean收集的各种信息. s
- 环境信息贡献器(EnvironmentInfoContributor)  将environment中国的key 暴露到 info key之下 ..
- GitInfoContributor   暴露有关git.properties的 git 信息(如果有)
- BuildInfoContributor 暴露构建信息(如果`META-INF/build-info.properties` 存在)
> 可以批量禁用它们,通过设置`management.info.defaults.enabled` 属性 ..

#### 2.10.2 自定义应用信息
我们可以定义通过`info` 端点暴露的信息(通过设置info.*的spring 属性),所有在Info key下面 key 将会映射为`Environment` 属性并暴露,举个例子,你可能增加了以下配置到`application.properties` 文件中 
```properties
info.app.encoding=UTF-8
info.app.java.source=11
info.app.java.target=11
```
除了硬编码这些值,我们可以在[构建时扩展这些信息属性](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/howto.html#howto-automatic-expansion),假设我们使用的是maven,那么我们可以重写之前的实例如下:
```properties
info.app.encoding=@project.build.sourceEncoding@
info.app.java.source=@java.version@
info.app.java.target=@java.version@
```
#### 2.10.3 git 提交信息
另一个info有用的特性是(它有能力推送、发布一些有关 git 源代码仓库的状态信息 - 当项目构建时),如果 `GitProperties` bean 可用,那么`info` 端点能够被用来暴露这些属性 ..
> `GitProperties` bean 是自动配置的,如果`git.properties`文件在类路径的根下可用, 查看[生成 git 信息](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/howto.html#howto-git-info)

默认来说,这个端点暴露了`git.branch` ,`git.commit.id` ,以及 `git.commit.time` 属性,如果存在 .. 如果你不想要端点响应中出现任何这些属性,你可以从git.properties文件中进行排除 ... ,如果你想要展示完全的git 信息(那就是git.properties的所有内容),使用`management.info.git.mode` 属性,例如:
```properties
management.info.git.mode=full
```
为了完全从info 端点中禁用git 提交信息, 设置 `management.info.git.enabled` 属性设置为false即可 ..
```properties
management.info.git.enabled=false
```
#### 2.10.4 构建信息
如果`BuildProperties` bean 可用,那么 info 端点能够发布你的构建的相关信息 .. 这个当`META-INF/build-info.properties` 文件在类路径上可用时自动配置
> 如果使用maven / gradle 插件,能够同时生成这个文件,查看 [生成构建信息](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/howto.html#howto-build-info)了解更多

#### 2.10.5 写一个自定义的infoContributors
为了提供自定义的应用信息,你可以注册一个实现了`InfoContributor` 接口的spring bean ...
例如: 以下实例贡献了具有单个值的example 项 
```java
import java.util.Collections;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class ExampleInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("example",
                Collections.singletonMap("key", "value"));
    }

}
```
当我们访问info端点时,你能够查看包含额外条目的响应 。
```json
{
    "example": {
        "key" : "value"
    }
}
```
###  3. 在Http 之上监控并管理
如果开发了一个web应用,Spring boot actuator 自动配置了所有启用的端点并在http 之上进行暴露,默认的转换是使用端点的`id` 并使用`/actuator` 作为url路径的前缀, 例如`health` 将暴露为`/actuator/health` ...
> actuator 原生与spring mvc / webflux /jersey 支持, 如果jersey 和spring mvc 同时可用,mvc具有更高的优先级 ..

> jackson 是必要的依赖,为了获取正确 的json 响应...

#### 3.1 自定义管理的端点路径
我们通过`management.endpoints.web.base-path` 属性设置管理端点的基本前缀
```properties
management.endpoints.web.base-path=/manage
```
那么模式将从`/actuator/{id}` 转变为`/manage/{id}` ...
> 除非管理端口已经通过不同的http 端口配置去暴露端点,否则 `management.endpoints.web.base-path`是相对于`server.servlet.context-path` 的(servlet web应用),或者`spring.webflux.base-path` 响应式web应用 ..
如果`management.server.port` 已经配置,那么`management.endpoints.web.base-path` 相对于`management.server.base-path`


如果你希望映射端点到不同的路径上,你可以使用`management.endpoints.web.path-mapping` 属性..
也就是改变id 到其他相对路径
```properties
management.endpoints.web.base-path=/
management.endpoints.web.path-mapping.health=healthcheck
```
#### 3.2 自定义管理的服务器端口
对于基于云部署来说,使用默认http 端口暴露管理端口是一种敏感选择 ..如果你的应用运行在你自己的数据中心内部,你也许偏向于通过不同的http 端口暴露端点 ...
通过设置`management.server.port` 属性改变http 端口
```properties
management.server.port=8081
```
> 在云环境中,应用仅仅接受来自8080端口的请求(tcp 和http路由),这是默认请求,如果你想要使用自定义的管理端口,这需要我们显式的配置应用的路由去转发流量到自定义端口 ..
也就是我们需要配置url 转发


#### 3.3 配置特定于管理的ssl
当自定义管理端口时,管理服务器也能够配置使用自己的SSL 证书(通过使用各种`management.server.ssl.*` 属性),举个例子,当应用使用https时能够让给管理服务器在http上可用,我们可以配置如下属性:
```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:store.jks
server.ssl.key-password=secret
management.server.port=8080
management.server.ssl.enabled=false
```
除此之外,我们还可以同时使用ssl(但是是不同的key store,如下)
```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:main.jks
server.ssl.key-password=secret
management.server.port=8080
management.server.ssl.enabled=true
management.server.ssl.key-store=classpath:management.jks
management.server.ssl.key-password=secret
```
#### 3.4 自定义管理服务器地址
通过设置`management.server.address` 属性来自定义管理端点的地址, 这是有用的(例如你仅仅想要监听一个内部的或者面向操作的网络,或者仅仅监听来自`localhost`的连接) ...
> 你可以监听不同的地址(仅仅当端口不同于主服务端口时)

例如,禁止远程连接管理连接
```properties
management.server.port=8081
management.server.address=127.0.0.1
```
#### 3.5 禁用Http 端点
如果你不想要暴露端点在http之上,你可以设置管理端口为-1,其次还可以设置暴露排除
```properties
management.server.port=-1
```
或者
```properties
management.endpoints.web.exposure.exclude=*
```
排除在http上所有暴露端点

### 4. 基于jmx监控或者管理
java management extensions(jmx) 提供了一种标准的机制去监控并管理应用 .. 默认来说,这个特性并没有启用并且能够打开(通过设置spring.jmx.enabled等于true),spring boot 暴露了管理端点作为jmx MBeans(默认是在`org.springframework.boot` 名之下)

#### 4.1 自定义 MBean Names
MBean的名称通常是根据端点的id进行生成的,举个例子,`health` 端点暴露为`org.springframework.boot:type=Endpoint,name=Health`  \
如果你的应用中包含了超过一个的spring 应用上下文,你也许可能会发现命名冲突,为了解决这个问题,你能够设置`spring.jmx.unique-names` 属性为true,那么MBean名称总是独一无二的 .. \
你能够定义在JMX域下面那些端点应该暴露,例如:
```properties
spring.jmx.unique-names=true
management.endpoints.jmx.domain=com.example.myapp
```
#### 4.2 禁用jmx 端点
如果你不想要在jmx上暴露端点,你可以设置`management.endpoints.jmx.exposure.exclude` 等于*，例如:
```properties
management.endpoints.jmx.exposure.exclude=*
```
由于*在yaml中具有特殊的含义
```yaml
management:
  endpoints:
    jmx:
      exposure:
        exclude: "*"
```
所以需要加上双引号
#### 4.3 Using Jolokia for JMX over HTTP
jolokia是一个jmx-http 桥接器(提供了一种访问jmx bean的替代方式),为了使用过jolokia,包括依赖`org.jolokia:jolokia-core`,例如如果是maven
```xml
<dependency>
    <groupId>org.jolokia</groupId>
    <artifactId>jolokia-core</artifactId>
</dependency>
```
jolokia端点能够通过增加jolokia或者* 到`management.endpoints.web.exposure.include` 属性进行暴露,你能够在管理http 服务器上通过使用`/actuator/jolokia` 访问 ..
> 这个jolokia 端点暴露的是 jolokia的servlet作为一个 actuator端点 ..,因此,他是特定于servlet的环境(例如spring mvc / jersey),这个端点在webflux中是不可使用的 ..

##### 4.3.1 自定义jolokia
jolokia有大量的配置,传统上,你可能需要配置servlet参数,通过spring boot,你能够使用application.properties做这个事情,为了这样做,使用`management.endpoint.jolokia.config` 作为前缀的参数,例如:
```properties
management:
  endpoint:
    jolokia:
      config:
        debug: true
```
#### 4.3.2 禁用jolokia
如果使用jolokia,但是不想要spring boot配置它,设置`management.endpoint.jolokia.enabled` 属性为false,如下
```properties
management:
  endpoint:
    jolokia:
      enabled: false
```
### 5. 日志器
Spring boot actuator包括了一种能力去浏览并在运行时配置应用的日志级别 .. 你能够浏览要么完整的列表或者独立的日志器配置 ...
它由显式配置的日志记录级别以及日志框架为其提供的有效日志记录级别组成。
- trace
- debug
- info
- warn
- error
- fatal
- off
- null

其中null 指示这里没有显式的配置 ...
#### 5.1 配置一个日志器
为了配置一个给定的日志器,展示如下:
```json
{
    "configuredLevel": "DEBUG"
}
```
> 为了重置日志器的特定级别(相反使用默认的配置),你能够传递`null`值作为`configuredLevel` ...


### 6. 指标
spring boot actuator 提供了依赖管理以及 [Micrometer](https://micrometer.io/)的自动配置 ... 支持各种监控系统的应用指标门面(担当),包括:
[详情参考官方文档](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/production-ready-features.html#production-ready-metrics)
> 为了学习Micrometer的能力,需要参考它的[参考文档](https://micrometer.io/docs),尤其是[概念](https://micrometer.io/docs/concepts)部分 ...

#### 6.1 开始
spring boot 自动配置了一个组合的MeterRegistry 并且增加了一个注册器为组合在每一个在类路径上发现的所受支持的实现 ...在你的运行时类路径上有一个`micrometer-registry-{system}`依赖足够让spring boot 配置这个(registry)注册机 .. \
大多数注册共享相同的特性,你可以禁用一个特定的注册机(即使Micrometer 注册机实现在类路径上),例如,禁用Datadog:
```properties
management.metrics.export.datadog.enabled=false
```
您还可以禁用所有注册表，除非注册表特定属性另有说明，如以下示例所示：
```properties
management.metrics.export.defaults.enabled=false
```
spring boot 同样增加任何自动配置的注册到全局静态的组合注册机(Metrics类上的),除非你显式的告诉它不那么做:
```properties
management.metrics.use-global-registry=false
```
你能够注册任意数量的MeterRegistryCustomizer bean去深度配置注册机,例如应用常见的tag,在任何meters往这个注册机注册之前
```java
@Bean
MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry -> registry.config().commonTags("region", "us-east-1");
}
```
你还可以自定义特殊的注册机实现(通过指定泛型) ..
```java
@Bean
MeterRegistryCustomizer<GraphiteMeterRegistry> graphiteMetricsNamingConvention() {
    return registry -> registry.config().namingConvention(MY_CUSTOM_CONVENTION);
}
```
Spring boot 同样配置了[内置的指令](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/production-ready-features.html#production-ready-metrics-meter)能够通过配置或者专用给的注解标志器进行控制 ..

#### 6.2 支持的监控系统
##### 6.2.1 AppOptics
默认来说,AppOptics 注册机会推送指标到`api.appoptics.com/v1/measurements`-周期性的,为了暴露指标到Saas [AppOptics](https://micrometer.io/docs/registry/appOptics),你的API token必须提供:
```properties
management.metrics.export.appoptics.api-token=YOUR_TOKEN
```
##### 6.2.2 Atlas
默认来说,指标将会暴露到运行在你本地运行的[Atlas](https://micrometer.io/docs/registry/atlas),这个[Atlas server](https://github.com/Netflix/atlas)能够使用属性进行提供:
```properties
management.metrics.export.atlas.uri=https://atlas.example.com:7101/api/v1/publish
```
##### 6.2.3 datadog
Datadog 注册机将定期的推送指标到 [datadoghq](https://www.datadoghq.com/),我们需要配置启用它
```properties
management.metrics.export.datadog.api-key=YOUR_KEY
```
需要提供api-key ...
你也能够改变发送到datadog的指标的周期
```properties
management.metrics.export.datadog.step=30s
```
##### 6.2.4 Dynatrace
周期性发送指标,需要填写api token, 设备id,url 必须提供
```properties
management.metrics.export.dynatrace.api-token=YOUR_TOKEN
management.metrics.export.dynatrace.device-id=YOUR_DEVICE_ID
management.metrics.export.dynatrace.uri=YOUR_URI
```
以及发送频率
```properties
management.metrics.export.dynatrace.step=30s
```
##### 6.2.5. Elastic
默认,这个指标将发送到本地运行的[Elastic](https://micrometer.io/docs/registry/elastic),我们需要使用以下属性提供Elastic 服务器的地址
```properties
management.metrics.export.elastic.host=https://elastic.example.com:8086
```
##### 6.2.6. Ganglia
同上,我们需要提供地址
```properties
management.metrics.export.ganglia.host=ganglia.example.com
management.metrics.export.ganglia.port=9649
```
##### 6.2.7. Graphite
同上,指定
```properties
management.metrics.export.graphite.host=graphite.example.com
management.metrics.export.graphite.port=9004
```
Micrometer 提供了一个默认的 HierarchicalNameMapper，它控制维度仪表 ID 如何映射到[平面层次结构名称](https://micrometer.io/docs/registry/graphite#_hierarchical_name_mapping)。
> 为了控制或者说接管这个行为,定义自己的GraphiteMeterRegistry 并提供你自己的HierarchicalNameMapper ..
一个自动配置的GraphiteConfig 以及 Clock bean将会提供(除非你定义了自己的)..

```java
@Bean
public GraphiteMeterRegistry graphiteMeterRegistry(GraphiteConfig config, Clock clock) {
    return new GraphiteMeterRegistry(config, clock, MY_HIERARCHICAL_MAPPER);
}
```
##### 6.2.8. Humio
By default, the Humio registry pushes metrics to [cloud.humio.com](https://cloud.humio.com/) periodically. To export metrics to SaaS [Humio](https://micrometer.io/docs/registry/humio), your API token must be provided:
```properties
management.metrics.export.humio.api-token=YOUR_TOKEN
```
还可以配置一个或者多个标签来识别数据源从而知道那些指标将被发送
```properties
management.metrics.export.humio.tags.alpha=a
management.metrics.export.humio.tags.bravo=b
```
##### 6.2.10 JMX
Micrometer 提供了体系的映射到 [JMX](https://micrometer.io/docs/registry/jmx),主要是作为一种便利并且可移植的方式去本地查看指标,那么默认指标会暴露为`metrics` JMX 域,那么这个域也能够被配置
```properties
management.metrics.export.jmx.domain=com.example.app.metrics
```
Micrometer provides a default HierarchicalNameMapper that governs how a dimensional meter id is mapped to flat hierarchical names.
> 为了接管/接收这个行为,我们可以定义`JmxMeterRegistry` 并提供自己的`HierarchicalNameMapper` . 一个自动配置的JmxConfig以及 CLock bean 将会自动提供,除非你定义自己的


```java
@Bean
public JmxMeterRegistry jmxMeterRegistry(JmxConfig config, Clock clock) {
    return new JmxMeterRegistry(config, clock, MY_HIERARCHICAL_MAPPER);
}
```

##### 6.2.13. Prometheus
[Prometheus](https://micrometer.io/docs/registry/prometheus) Prometheus 期望抓取或轮询单个应用程序实例以获取指标。
Spring boot 本身提供了一个actuator端点(`/actuator/prometheus`) 去标识了具有合适的格式的[Prometheus scrape](https://prometheus.io/) ..
> 这个端点默认是没有使用的,并且需要暴露 ..

这里有一个示例scrape_config 增加到 prometheus.yml
```yaml
scrape_configs:
  - job_name: 'spring'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['HOST:PORT']
```
对于短暂或者批量的job(任务),存在时间不足以被抓取,[Prometheus Pushgateway](https://github.com/prometheus/pushgateway)支持被使用暴露它们的指标到 Prometheus ..,为了启用Prometheus Pushgateway 支持,需要增加以下的依赖到项目中 ...
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_pushgateway</artifactId>
</dependency>
```
当这个依赖出现在类路径上的时候并且`management.metrics.export.prometheus.pushgateway.enabled` 设置为true,那么 PrometheusPushGatewayManager bean将会自动配置,这能够用来管理推送指标到 Prometheus Pushgateway ... \
PrometheusPushGatewayManager 能够使用 management.metrics.export.prometheus.pushgateway下的属性进行调整,对于高级配置,你可以提供自定义的PrometheusPushGatewayManager  bean ...

#### 6.3 支持的指标
spring boot 注册了以下的核心指标(当可能):
- jvm 指标 使用情况报告:
    - 各种内存以及缓存池
    - 垃圾回收相关的统计
    - 线程使用情况
    - 加载以及卸载类的数量
- cpu 指标
- 文件描述符指标
- jetty 指标
- kafka 消费者/生产者/流指标
- log4j2 指标: 在每一个级别上记录到Log4j2的事件的数量
- Logback指标: 同上,只不过是记录到Logback的..
- Uptime 指标: 报告正常运行时间的标准和代表应用程序绝对启动时间的固定标准
- Tomcat 指标(server.tomcat.mbeanregistry.enabled 必须设置为true,为了让所有的tomicat 指标能够被注册)
- [Spring Integration](https://docs.spring.io/spring-integration/docs/5.4.12/reference/html/system-management.html#micrometer-integration) metrics

##### 6.3.1 Spring mvc 指标
自动配置启用了由spring mvc处理的请求仪表,当management.metrics.web.server.request.autotime.enabled is true,这个仪表会贯穿所有的请求,除此之外,当设置为false,你能够通过@Timed到请求处理方法上启用仪表...
```java
@RestController
@Timed 
public class MyController {

    @GetMapping("/api/people")
    @Timed(extraTags = { "region", "us-east-1" }) 
    @Timed(value = "all.people", longTask = true) 
    public List<Person> listPeople() { ... }

}
```
1. 控制器类启用了在控制器中每一个请求处理器上进行计时 ..
2. 为独立的端点启用计时数据统计,这是不必要的,如果你在类上存在,但是这能够被用来深度定制这个特定端点的秒表(计时器)?
3. 这个方法上标记了`longTask=true` 启动了一个长的任务计时器(为这个方法),长任务计时器需要一个单独的指标名称,能够和短任务定时器叠加在一起 ...

默认来说,指标使用名称生成,那么http.server.requests ..这个名称能够被自定义(通过设置management.metrics.web.server.request.metric-name 属性) \
默认情况下,springmvc 相关的指标都被使用以下信息进行标记 \

| tag | 描述|
|-----| -----|
| exception | 当处理请求时发生了异常的异常的简单类名|
| method | 请求的方法,get / post |
| outcome | 基于响应的状态码的请求输出,xx 标识 INFORMATIONAL,2xx是 SUCCESS,3xx是REDRECTION,4xx CLIENT_ERROR,5xx SERVER_ERROR .. |
| status | 响应的http 状态码,例如 200 / 500 |
| uri | 变量替换之前的URL 请求模板,如果可能,例如 `/api/person/{id}` |

为了增加到默认的tag,提供一个或者多个实现了WebMvcTagsContributor的 @Bean,替代默认的tag,你可以提供一个实现了WebMvcTagsProvider的@Bean ...

##### 6.3.2 spring webflux 指标 
同上 ...
只是实现的类是WebFlux开头而不是WebMvc ...

##### 6.3.3 Jersey Server Metrics
一般不同,用再说 ...
例如jitisi 视频的控制中心就使用了jersey ... 但是我们可以通过 spring mvc 进行转换 ...
当Micrometer的`micrometer-jersey2` 模块出现在类路径上,那么浙江自动的启用请求的仪表(由jersey JAX-RS实现处理的请求),当设置management.metrics.web.server.request.autotime.enabled  = true,
这个仪表将贯穿所有请求,当设置为false,通过增加@Timed 到请求处理的方法之上来启用仪表 ..
其他的同spring mvc / webflux,为了定制tag,可以提供一个实现了JerseyTagsProvider的@Bean ..
##### 6.3.4 Http Client Metrics
Spring boot actuator 同时管理RestTemplate 以及 WebClient的仪表,对此,你可以注入一个自动配置的构建起并且使用它们来创建实例
- RestTemplateBuilder for RestTemplate
- WebClient.Builder for WebClient

我们可以应用自定义器去定制仪表, 例如 MetricsRestTemplateCustomizer , MetricsWebClientCustomizer ... \
同理,指标名称为 http.client.requests, 也可以配置,通过 management.metrics.web.client.request.metric-name 属性进行之定义 ..
默认由一个测量的客户端生成的指标包含了以下的信息 \

| tag | 描述|
|---- | ----- |
|clientName | url的主机部分|
| method | 请求方法 get /post |
|outcome | 请求的输出,基于响应的状态码,1xx Informational,2xx success,3xx redirection, 4xx client_error,5xx server_error,否则 unknown ..|
| status | 响应的Http 状态码,如果必要,例如 200 / 500, 或者i /o问题的情况下(IO_ERROR),否则 CLIENT_ERROR |
| uri | 在变量替换之前的请求的url 模板,如果可能,例如`/api/person/{id}` |

为了定制tag,依赖于你客户端的选择,你可以提供一个实现了RestTemplateExchangeTagsProvider or WebClientExchangeTagsProvider的 bean,这里包含了大量的静态方法在RestTemplateExchangeTags and WebClientExchangeTags中
##### 6.3.5 缓存指标
能够自动配置所有Cache可用的检测(cache为前缀)指标,Cache检测是标准话的指标的基本集合,除此之外,特定缓存的指标也是可用的 ..\
如下的缓存库是支持的:
- Caffeine
- EhCache2
- Hazelcast
- 任何遵循JCache(JSR-107)的实现

指标通过缓存的名称进行标签分类,通过CacheManager bean的bean 名称衍生 ..
> 仅仅在一开始配置并约束到这个注册机的缓存 ... 对于没有在缓存配置中定义的缓存,例如运行时创建的cache或者在启动阶段之后编程式注入的,需要显式的注册 ...
一个CacheMetricsRegistrar  能够用来处理这些东西 ..

##### 6.3.6 数据库指标
启用了有关DataSource对象相关的指标(前缀 jdbc.connections), 数据库检测结果包含了当前激活,空闲，最大允许和最小允许的连接数(当前连接池中) ... \
指标同样通过数据库的bean 名称进行衍生进行分类 ...
> 默认情况下 Spring boot 提供了所有支持的数据源的元数据,你能够增加额外的 DataSourcePoolMetadataProvider beans(如果你喜爱的数据库并没有开箱即用的被支持),查看
DataSourcePoolMetadataProvidersConfiguration  了解示例 ..


同样例如Hikari特定的指标暴露在`hikaricp` 前缀下,每一个指标通过池的名称进行分类(也能够通过spring.datasource.name进行通知)

##### 6.3.7 hibernate 指标
对于Hibernate 也有EntityManagerFactory示例的一些统计启用能自动配置检测,并且指标名为`hibernate` .. \
指标也是通过EntityManagerFactory的bean 名称进行衍生 且分类 ..
为了启用统计,标准的jpa 属性`hibernate.generate_statistics` 必须设置为true,你能够在自动配置的EntityManagerFactory上配置 ..
```properties
spring.jpa.properties[hibernate.generate_statistics]=true
```
##### 6.3.8. RabbitMQ Metrics
自动配置任何所有RabbitMQ 连接工厂的检测信息(指标名为 rabbitmq) ...
##### 6.3.9 kafka 指标
自动配置将注册一个 MicrometerConsumerListener  以及 MicrometerProducerListener (根据自动配置的消费者工厂以及生产者工厂),它将注册一个KafkaStreamsMicrometerListener 为了StreamsBuilderFactoryBean ...
有关详情参考 Spring kafka文档的 [Micrometer Native Metrics](https://docs.spring.io/spring-kafka/docs/2.6.12/reference/html/#micrometer-native)部分 ...
##### 6.3.10. Jetty Metrics
自动配置将绑定Jetty的ThreadPoll的指标(使用 Micrometer的 JettyServerThreadPoolMetrics) ..
#### 6.4 注册自定义的指标
为了注册自定义的指标,注册MeterRegistry 到你的组件中,例如:
````java
class Dictionary {

    private final List<String> words = new CopyOnWriteArrayList<>();

    Dictionary(MeterRegistry registry) {
        registry.gaugeCollectionSize("dictionary.size", Tags.empty(), this.words);
    }

    // …

}
````
如果你的指标依赖于其他bean,强烈建议你使用MeterBinder去注册它们,例如
```java
@Bean
MeterBinder queueSize(Queue queue) {
    return (registry) -> Gauge.builder("queueSize", queue::size).register(registry);
}
```
使用MeterBinder确保正确的依赖关系配置并且bean 时可用的(当指标的值被抓取时,可以直接依赖对应的bean),一个MeterBinder实现非常有用 - 如果您发现您重复检测一组跨组件或应用程序的指标，则 MeterBinder 实现也很有用。
> 默认情况,来自所有MeterBinder bean的指标讲过自动的绑定到spring管理的MeterRegistry ...


#### 6.5 自定义独立的指标
如果你需要应用自定义到特定的Meter示例上,你能够使用 io.micrometer.core.instrument.config.MeterFilter 接口,例如如果你想要重命名`mytag.region` 到 `mytag.area` tag(对于所有以`com.example` 开始的meter ids),你能够如下做法:
```java
@Bean
public MeterFilter renameRegionTagMeterFilter() {
    return MeterFilter.renameTag("com.example", "mytag.region", "mytag.area");
}
```
> 默认情况,所有的MeterFilter beans能够自动的限定到spring管理的MeterRegistry,确保使用这个注册机注册你自己的指标并且而不是调用了Metrics上的任何静态方法 ..
因为那些事全局注册机使用的,而不是spring 管理的 ..
##### 6.5.1 常用标签
常见标签通常被用来进行在操作环境上的维度深化(例如主机 / 实例 / region / stack等等),常见标签能够应用到所有的meters并且能够如下配置:
```properties
management.metrics.tags.region=us-east-1
management.metrics.tags.stack=prod
```
上面的实例标识为所有的meters增加了具有`us-east-1` 以及 `prod` 值的标签 ...(region / stack)
> 常见标签的顺序是重要的,如果你使用Graphite,因为常见标签的顺序不能够被保证,使用这种方式(Graphite用户需要通知定义自定义的MeterFilter替代 ..)


##### 6.5.2 每一个meter 属性
除了MeterFilter bean之外,我们可以提供有限的自定义集合(基于使用属性的在每一个meter上),每一个meter 自定义应用到任何以跟定名称开始的所有 meter ids ... \
例如,以下示例禁用了任何meters(以example.remote开始的id)
```properties
management.metrics.enable.example.remote=false
```
以下属性允许每一个meter自定义 \

| property | 描述|
|---- | ---- |
|management.metrics.enable | 是否禁用某一些指标 |
|management.metrics.distribution.percentiles-histogram | 是否发布适合计算可聚合（跨维度）百分位数近似值的直方图。 |
| management.metrics.distribution.minimum-expected-value, management.metrics.distribution.maximum-expected-value |通过限制预期值的范围发布较少的直方图桶。 |
| management.metrics.distribution.percentiles | 发布在您的应用程序中计算的百分位值 |
| management.metrics.distribution.slo | 发布一个累积直方图，其中包含由您的服务级别目标定义的存储桶。 |

对于percentiles-histogram, percentiles and slo的概念,参考 the ["Histograms and percentiles"](https://micrometer.io/docs/concepts#_histograms_and_percentiles) section of the micrometer documentation.

#### 6.6 指标端点
spring boot 提供了metrics 端点能够被用来诊断由应用收集的指标检查, 这个端点默认不是可用的,需要暴露 ..
例如`/actuator/metrics` 会展示所有必要的meter 名称,你可能深入查看它们特定meter的信息(通过提供它们的名称作为选择器),例如`/actuator/metrics/jvm.memory.max` ..
> 这个名称应该匹配在代码中使用的名称，不是可能存在命名转换格式之后的名称（例如prometheus可能有一个蛇形名称转换,例如 jvm.memory.max它可以展示为 jvm_memory_max,但是我们仍然应该使用'jvm.memory.max'作为选择器,去检测 metrics端点下特定的meter指标 ..

你能够增加任意数量的(tag=KEY:VALUE)查询参数到URL的尾部以维度深入了解meter,例如 /actuator/metrics/jvm.memory.max?tag=area:nonheap ..
> 这报告的测量值是所有匹配这个meter名称以及应用的任何tag的所有meter的统计总和 .. 如上所述,这个返回的"Value" 统计是"Code Cache","Compressed Class Space",队的"Metaspace"区域最大内存占用量之和 ... 如果我们仅仅想查看"metaspace"的最大值,可以额增加额外的tag=id:Metaspace,例如 \
/actuator/metrics/jvm.memory.max?tag=area:nonheap&tag=id:Metaspace ....


### 7 审查
一旦spring security 启用,spring boot actuator 有一个灵活的审查框架能够发布事件(默认,例如认证 / 失败 / 访问拒绝等异常都会进行事件发布),这个特性非常有用(对于暴露以及基于认证失败实现一个无锁协议) ..
审查能够通过提供一个 AuditEventRepository  bean 进行启用,为了方便,Spring 提供了一个InMemoryAuditEventRepository, InMemoryAuditEventRepository它具有有限的能力并且我们推荐仅仅在开发环境中使用,对于生产环境考虑创建一个自己的可替换的AuditEventRepository  实现 .. \
spring boot针对spring security自动配置类是AuditAutoConfiguration(主要是针对认证 / 授权事件进行数据输出 ),AuditEventsEndpointAutoConfiguration,后者是为了配置一个可访问端点 ...

#### 7.1 自定义审查
为了自定义发布的安全事件,提供自己的 AbstractAuthenticationAuditListener  和 AbstractAuthorizationAuditListener 实现 ...
你能够使用审查服务(用来处理业务事件),为了这样做,需要注册一个 AuditEventRepository  bean到自己的组件中并直接使用或者发布一个 AuditApplicationEvent(通过 spring的ApplicationEventPublisher ,也可以通过 ApplicationEventPublisherAware去获得这个派发器 bean)

### 8 http tracing
http 跟踪通过添加一个 HttpTraceRepository 就能够启用, spring boot 提供了 InMemoryHttpTraceRepository  可以存储针对最近100个请求/响应交换的跟踪,但是这个仅仅用于开发,生产环境应该使用生产级别的跟踪或者观察解决方案,例如ziplin或者 spring cloud sleuth,这个推荐的 ..
除此之外,你可以创建你自己的HttpTraceRepository 来实现自己的需要 ... \
httprace 端点能够被用来获取请求 /响应交换(在HttpTraceRepository中存储的)

##### 8.1 自定义http 跟踪
我们可以先启用并暴露,management.trace.http.include ,同样,开启自定义,注册自己的 HttpExchangeTracer 实现 ..


### 9 处理监控
在spring-boot项目,我们能发现两种类去创建文件（为了处理监控)
- ApplicationPidFileWriter 创建一个包含应用的pid文件(默认在应用目录下包含一个文件名为 application.pid) ..
- WebServerPortFileWriter 创建了一个文件 /或者一系列文件(包含了运行web服务器的端口列表,默认是在应用的目录下有一个application.port的文件名的文件)
默认,这些写入器够被激活,能够被启用 ..
- [通过扩展配置](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/production-ready-features.html#production-ready-process-monitoring-configuration)
- [编程式](https://docs.spring.io/spring-boot/docs/2.4.13/reference/html/production-ready-features.html#production-ready-process-monitoring-programmatically)

#### 9.1 扩展配置
在META-INF/spring.factories文件中,你能够激活队写入一个PID文件的监听器列表
```properties
org.springframework.context.ApplicationListener=\
org.springframework.boot.context.ApplicationPidFileWriter,\
org.springframework.boot.web.context.WebServerPortFileWriter
```

#### 9.2 编程式
通过SpringApplication.addListeners()方法激活一个监听器并且传递合适的Writer对象 ...
这个方法让你能够自定义文件名称以及Writer构建器中的路径 ...

### 10 cloud foundry 支持
spring boot的actuator模块包括了大量可选的支持(能够激活),当我们部署到一个兼容性的Cloud Foundry实例上时,这个 `/cloudfoundryapplication ` 路径提供了一个额外的安全的路由到所有@Endpoint bean的方式.. \
这个扩展支持让Cloud Foundry 管理UI(例如web 应用-你能够用来查看部署的应用)被Spring boot actuator 信息增强 .. 例如一个应用状态页面也许能够包括完整的健康信息而不是仅仅是运行还是停止状态 ...
> /cloudfoundryapplication 路径不应该直接被普通用户直接访问,为了使用这个端点,一个有效的UAA token 必须在请求中进行传递 ..

#### 10.1 禁用扩展的cloud foundry actuator 支持
如果你想要完全的禁用`/cloudfoundryapplication 端点,你可以增加以下配置到 `application.properties` 中
```properties
management.cloudfoundry.enabled=false
```
#### 10.2 cloud Foundry 自签名证书
默认来说,默认情况下，`/cloudfoundryapplication`  端点的安全验证会对各种 Cloud Foundry 服务进行 SSL 调用,如果你的cloud foundry UAA 或者Cloud 控制器服务使用自签名证书,你需要设置以下属性
```properties
management.cloudfoundry.skip-ssl-validation=true
```
因为自签名证书没有人能够证明它们的可靠性 ..
#### 10.3 自定义上下文路径
如果服务器的上下文路径已经被配置,而不是`/` ,那么cloud foundry 端点将不可用(在应用的根下),例如server.servlet.context-path=/app,那么cloud foundry 将变成/app/cloudfoundryapplication/* ..
如果你期望cloud foundry 端点总是在/cloudfoundryapplication/*可用,无论服务器的上下文路径,你需要显式的配置到应用中,这个配置依赖于使用的web服务器:
例如tomcat
```java
@Bean
public TomcatServletWebServerFactory servletWebServerFactory() {
    return new TomcatServletWebServerFactory() {

        @Override
        protected void prepareContext(Host host, ServletContextInitializer[] initializers) {
            super.prepareContext(host, initializers);
            StandardContext child = new StandardContext();
            child.addLifecycleListener(new Tomcat.FixContextListener());
            child.setPath("/cloudfoundryapplication");
            ServletContainerInitializer initializer = getServletContextInitializer(getContextPath());
            child.addServletContainerInitializer(initializer, Collections.emptySet());
            child.setCrossContext(true);
            host.addChild(child);
        }

    };
}

private ServletContainerInitializer getServletContextInitializer(String contextPath) {
    return (c, context) -> {
        Servlet servlet = new GenericServlet() {

            @Override
            public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
                ServletContext context = req.getServletContext().getContext(contextPath);
                context.getRequestDispatcher("/cloudfoundryapplication").forward(req, res);
            }

        };
        context.addServlet("cloudfoundry", servlet).addMapping("/*");
    };
}
```
