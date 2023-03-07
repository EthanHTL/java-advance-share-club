## Testing
spring-test框架提供测试支持 
* servlet api mock: servlet api的模拟实现与测试控制器、filter、其他web组件的约定
* TestContext  框架: 支持加载spring 配置(通过JUnit以及 TestNG测试),包括有效的加载配置缓存(跨域测试方法以及通过MockServletContext加载一个WebApplicationContext),查看
[TestContext Framework](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/testing.html#testcontext-framework)获取更多 \
* spring mvc test: 一个框架 也成为 mockMvc,主要是为了测试通过DispatcherServlet的注解控制器(支持注解),spirng mvc 基础设施完成(但是没有任何http Server),查看 [spring mvc test](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/testing.html#spring-mvc-test-framework)获取更多
* client side rest: spring-test 提供了MockRestServiceServer(你能够使用它作为一个模拟的服务器用来测试客户端代码-通过内部使用RestTemplate),查看[client rest tests](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/testing.html#spring-mvc-test-client)获取更多
* WebTestClient: 构建webFlux应用的测试,也能够进行端到端集成测试,到任何一个服务器、在一个http连接之上,他是非阻塞式,相依ing是客户端并且同样适合测试异步、流式场景;