## 过滤器
spring-web模块提供了一些有用的过滤器
* 表单数据
* forwareded headers
* shallow etag
* cors
#### 表单数据
浏览器能够通过get或者post提交表单数据,但是非浏览器客户端能够使用httpPut、Patch、delete提供,servlet api 确保ServletRequest.getParameter*()方法去支持仅仅通过HttpPost访问表单字段; \
spring-web模块提供了FormContentFilter去拦截PUT、Patch、Delete请求(content-type=application/x-www-form-urlencoded),从请求体中读取表单数据并且包装到ServletRequest中确保表单数据能够通过ServletRequest.getParameter*()家族方法获取有效
#### forwarded headers
请求有可能通过代理(例如 load balancers)的主机、端口以及协议(scheme)而改变,这使得从客户端的角度创建指向正确主机、端口和方案的链接成为一项挑战 \
[RFC 7239](https://tools.ietf.org/html/rfc7239) 定义了一个Forwarded 请求头使得代理能够提供原始请求的信息,这是一个非标准的请求头,包括了X-Forwarded-Host、X-Forwarded-Port,X-Forwarded-Proto,X-Forwarded-Ssl以及X-Forwarded-Prefix\
ForwardedHeaderFilter 是一个Servlet 过滤器能够a修改请求为了改变主机、端口、以及协议-基于Forwarded请求头,然后删除b的这些表头以消除进一步的影响,此过滤器依赖封装的请求，所以它应该放在其他过滤器之前,例如RequestContextFilter应该通过修改过的请求进行工作而不是原始请求; \
这里也存在对于forwarded请求的一些安全考虑(应用程序不知道这些请求头是代理添加的),可能猜想为恶毒的客户端,这就是为什么应该配置信任边界处的代理以删除来自外部的不受信任的转发标头,你应该配置设置了removeOnly=true的ForwardedHeaderFilter,这种情况下它会移除而且不打算使用这些请求头; \
为了支持异步请求以及错误派发,此过滤器应该使用DispatcherType.ASYNC 以及 DispatcherType.ERROR,如果使用spring的abstractAnnotationDispatcherServletInitializer(查看servlet 配置),那么所有的过滤器将会对所有的调度类型自动进行注册,然而如果使用web.xml进行注册过滤器或者在SpringBoot中通过FilterRegistrationBean 注册,确保包括了DispatcherType.ASYNC以及DispatcherType.ERROR ->  除开DispatcherType.REQUEST

#### shallow ETag
浅度的ETag,ShallowEtagHeaderFilter 过滤器创建了一个"shallow" ETag (通过缓存写入到响应的内容以及据此计算一个MD5 hash),当下一次客户端发送,它执行相同动作,但是他也会比较计算值(针对请求头If-None-Match),如果两者相等,则返回304(表示未修改!) \
这个策略节省了网络带宽但不是CPU,对于每个请求来说响应必须计算完成,其他的策略在控制器级别上,在前边已经描述过,能够避免计算,查看[HTTP Caching](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-caching) \
此过滤器有一个writeWeakETag参数能够配置此过滤器去写入一个弱的ETags,与此相似 -> W/"02a2d595e6ed9a0b24f027f2b63b134d6"(定义在[RFC 7232 Section2.3](https://tools.ietf.org/html/rfc7232#section-2.3)) \
为了支持[asynchronous requests](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-ann-async),此过滤器必须使用DispatcherType.ASYNC进行映射,因此过滤器能够延迟并成功的生成一个ETag(到最后一次的异步派发的结尾),如果你使用了Spring的AbstractAnnotationConfigDispatcherServletInitializer(查看 servlet config),所有过滤器能够对所有的派发类型进行自动注册,如果通过web.xml进行注册或者在boot通过FilterRegistrationBean进行注册需要包括DispatcherType.ASYNC;
#### CORS
SpringMVC 提供了对CORS配置的细腻化支持(通过在controller配置注解即可实现跨域),然而和Spring security结合使用的时候,我们建议CORS过滤器要在spring security过滤链之前;
查看[CORS](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-cors) 以及[CORS Filter](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/web.html#mvc-cors-filter)获取详细信息;