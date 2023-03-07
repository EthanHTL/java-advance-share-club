## HTTP /2
servlet 4.0容器必须支持http/2,spring 5.0已经兼容了servlet 4.0api,从编程模型角度看,没有指定应用需要做什么,然而这里有一些服务器配置的考虑，对于更多信息,查看[http/2 wiki](https://github.com/spring-projects/spring-framework/wiki/HTTP-2-support) \
servlet api 暴露一个http/2相关的构造,你能够使用javax.servlet.http.PushBuilder去主动向客户端推送资源,并且它支持作为@RequestMapping的方法参数; \
#### Rest clients
这一部分描述了客户端访问REST endpoints的信息;
##### RestTemplate
RestTemplate 是一个异步客户端能够用来执行HTTP请求,它是一个原始的spring rest 客户端并且仅仅暴露一个简单的,模板方法api(底层是http client库) \
从5.0开始 RestTemplate 进入了维护模式,考虑使用WebClient提供更加模型化的api并同样支持异步、同步、streaming 场景;
##### webClient
webClient 是一个非阻塞式的,响应式客户端能够执行http请求,它在5.0开始引入并且提供了模型去替代RestTemplate,有效的支持异步和同步,同样也支持 streaming 场景; \
对比RestTemplate,WebClient支持以下：
* 非阻塞式IO
* Reactive Streams back pressure.
* 高并发(只需要少量的硬件资源)
* 函数式风格,流式api使用了java 8lambda表达式优点;
* 同步和异步交互
* 流式上传或者流式下载 \
查看 [WebClient](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web-reactive.html#webflux-client)获取更多