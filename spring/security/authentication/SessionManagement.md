# 会话管理
## Session Management
- 强制早期的会话创建
- 检测超时
- 并发会话控制
- 会话固定攻击保护
- SessionManagementFilter
- SessionAuthenticationStrategy
- Concurrency control
  - (为当前认证的用户以及它的会话)查询会话注册表

## 强制早期会话创建
有时早期会话创建是有价值的,这能够通过使用ForceEagerSessionCreationFilter 完成,
它能够如下配置:
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
        );
    return http.build();
}
```
## 检测超时
能够配置Spring security去检测一个无效会话的提交并重定向用户到合适的URL,这能够通过session-management 元素实现 ..
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .sessionManagement(session -> session
            .invalidSessionUrl("/invalidSession.htm")
        );
    return http.build();
}
```
注意如果你是用这个机制去保护会话超时,它可能会失败去报告一个错误(如果用户登出但是在不关闭浏览器的情况下重新登录) \
这是因为session cookie没有清理(.这是因为当您使会话无效时会话 cookie 不会被清除，即使用户已注销也会重新提交),
(这是由于浏览器会自动将cookie携带上),但是我们可以在登录的时候强制修改cookie内容,删除JSESSIONID
```java
JavaXML
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .logout(logout -> logout
            .deleteCookies("JSESSIONID")
        );
    return http.build();
}
```
不幸的是这部能够保证在所有的servlet容器工作,所以你需要在你的环境中测试 ...
注意如果你的application运行在代理之后,你也许能够移除session cookie(通过配置代理服务器),
例如使用Apache HTTPD的mod_headers,以下指令将会删除JSESSIONID cookie(通过显式的在登出请求中过期它,假设这个应用部署在 /tutorial下)
```java
<LocationMatch "/tutorial/logout">
Header always set Set-Cookie "JSESSIONID=;Path=/tutorial;Expires=Thu, 01 Jan 1970 00:00:00 GMT"
</LocationMatch>
```
直接让它过期 ...
## 并发会话控制
如果你希望对单个用户的登录能力进行约束,spring security 支持开箱即用的配置,只需要额外的配置,首先你需要增加以下的监听器到你的配置中保持spring security 关注会话声明周期事件 ..
```java
@Bean
public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
}
```
然后增加以下配置
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .sessionManagement(session -> session
            .maximumSessions(1)
        );
    return http.build();
}
```
这将会阻止用户同时多次登录,第二次登录将会导致第一个无效 ..通常你想要阻止第二个登录,仅仅需要配置以下
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .sessionManagement(session -> session
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true)
        );
    return http.build();
}
```
那么第二个登录将会被拦截,意味着用户将会转发到authentication-fauilure-url地址,如果是基于表单登录 ...
如果第二次认证采用非交互的机制,例如remember-me,那么将爆发一个 401未认证的错误(将会返回给客户端),如果你想要使用错误页面,
你能够为session-management元素增加一个 session-authentication-error-url ...
如果你使用自定义的认证过滤器进行基于表单的登录,那么你需要显式的配置并发会话控制支持 ..
更多详情查看[Session Management chapter](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html#session-mgmt);

## 会话固定攻击保护
会话固定攻击是一种潜在的风险(这里它可能由一个攻击者通过访问一个站点创建会话,然后伪装成其他用户使用相同会话登录),然后发送一个包含会话表示作为参数的连接 - 举个例子
,spring security 保护会针对这样的情况自动创建一个新的会话,而不是当用户登录的时候改变会话id ..
(也就是说,攻击者将这个会话id 利用了,然后其他人登录的时候,就携带了这个session-ID,如果后台系统是直接替换session-id到登录用户,那么会话固定攻击就生效了) ...
如果你不需要这个保护,或者它与其他的要求冲突,你能够控制这个行为,通过配置session-management的 session-fixation-protection属性
- none 不做任何事情(原来的会话将会保持)
- newSession (创建一个新的会话,不会复制拥有会话的数据,Spring security 相关的属性将会复制)
- migrateSession(创建新会话并复制所有会话属性到新的会话),这是servlet 3.0 以及旧的容器默认行为 ..
- changeSessionId(不创建新会话,相反使用由servlet容器提供的会话固定保护)
  (HttpServletRequest#changeSessionId()). 这个选项仅仅在Servlet3.1(JavaEE 7)以及新的容器支持 ...在旧的容器上指定将爆发异常 ..这是Servlet 3.1 以及新容器的默认行为 ..

当会话固定保护触发,他将会派发一个SessionFixationProtectionEvent 派发到应用上下文中,如果你使用changeSessionId,那么这个保护将会导致 javax.servlet.http.HttpSessionIdListener能够被通知到,
因此，如果您的代码同时监听这两个事件，请务必小心，查看会话管理章节了解更多信息 ..

## SessionManagementFilter
这个过滤器将会检测SecurityContextRepository的内容(根据当前SecurityContextHolder的当前内容)去决定一个用户是否在当前请求中已经被认证,通常使用非交互认证机制,例如预认证或者记住我 ..
如果仓库包含一个security context,那么这个过滤器将不会做任何事情 ... 如果不存在,那么thread-local SecurityContext将包含一个非匿名的Authentication 对象,然后这个过滤器假设它们已经被这个调用栈的其他之前过滤器验证过,然后将执行
配置的SessionAuthenticationStrategy ...
如果用户当前没有认证,那么这个过滤器将会检测是否是一个无效的sessionId 被请求了(因为超时,例如)并且执行配置的InvalidSessionStrategy(如果设置)...
这最常见的行为是仅仅重定向到固定的URL并且这封装在标准的SimpleRedirectInvalidSessionStrategy实现,后者被使用在通过命名空间配置一个无效的session URL 生效 ...

## 会话认证策略
它同时使用在SessionManagementFilter 以及 AbstractAuthenticationProcessingFilter之间,因此如果你使用了之定义的表单登录类别,举个例子你将需要同时拦截这两者,在这种情况下,一个通常的配置:
合并命名空间和自定义bean 看起来如下:
```java
<http>
<custom-filter position="FORM_LOGIN_FILTER" ref="myAuthFilter" />
<session-management session-authentication-strategy-ref="sas"/>
</http>

<beans:bean id="myAuthFilter" class=
"org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter">
	<beans:property name="sessionAuthenticationStrategy" ref="sas" />
	...
</beans:bean>

<beans:bean id="sas" class=
"org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy" />
```
请注意，如果您将 bean 存储在实现 HttpSessionBindingListener 的会话中，包括 Spring 会话范围的 bean，则使用默认值 SessionFixationProtectionStrategy 可能会导致问题。有关更多信息，请参阅此类的 Javadoc。

## 并发控制
... 不用多说,并发认证检查是通过ProviderManager进行处理的,但是这能够通过
ConcurrentSessionController进行注入 ... 后者将会检查如果用户已经超过了允许会话的最大值 ...
然而这种方式需要提前创建Http 会话(这是不友好的),在Spring Security 3中,用户首次被AuthenticationManager 认证并且一旦它成功认证,一个会话将会被创建并且检查将会生效(是否允许有其他的会话open)

为了实现并发会话支持,你需要增加以下内容到Web.xml
```java
<listener>
	<listener-class>
	org.springframework.security.web.session.HttpSessionEventPublisher
	</listener-class>
</listener>
```
但是对于spring boot而言,我们仅仅需要将它作为一个@Bean即可 ..
除此之外,你需要增加ConcurrentSessionFilter 到FilterChainProxy,这个过滤器需要两个参数,一个sessionRegistry,通常是SessionRegistryImpl,以及一个
sessionInformationExpiredStrategy,这定义了一个策略(当会话过期时如何处理) ...
以下是通过命名空间创建FilterChainProxy 以及其他默认bean的表现形式:
```xml
<http>
<custom-filter position="CONCURRENT_SESSION_FILTER" ref="concurrencyFilter" />
<custom-filter position="FORM_LOGIN_FILTER" ref="myAuthFilter" />

<session-management session-authentication-strategy-ref="sas"/>
</http>

<beans:bean id="redirectSessionInformationExpiredStrategy"
class="org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy">
<beans:constructor-arg name="invalidSessionUrl" value="/session-expired.htm" />
</beans:bean>

<beans:bean id="concurrencyFilter"
class="org.springframework.security.web.session.ConcurrentSessionFilter">
<beans:constructor-arg name="sessionRegistry" ref="sessionRegistry" />
<beans:constructor-arg name="sessionInformationExpiredStrategy" ref="redirectSessionInformationExpiredStrategy" />
</beans:bean>

<beans:bean id="myAuthFilter" class=
"org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter">
<beans:property name="sessionAuthenticationStrategy" ref="sas" />
<beans:property name="authenticationManager" ref="authenticationManager" />
</beans:bean>

<beans:bean id="sas" class="org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy">
<beans:constructor-arg>
	<beans:list>
	<beans:bean class="org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy">
		<beans:constructor-arg ref="sessionRegistry"/>
		<beans:property name="maximumSessions" value="1" />
		<beans:property name="exceptionIfMaximumExceeded" value="true" />
	</beans:bean>
	<beans:bean class="org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy">
	</beans:bean>
	<beans:bean class="org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy">
		<beans:constructor-arg ref="sessionRegistry"/>
	</beans:bean>
	</beans:list>
</beans:constructor-arg>
</beans:bean>

<beans:bean id="sessionRegistry"
	class="org.springframework.security.core.session.SessionRegistryImpl" />

```
增加监听器到web.xml 导致ApplicationEvent会派发到Spring ApplicationContext(每次HttpSession 开始或者结束的时候),这是一个条件,因此它允许SessionRegistryImpl能够被通知,当会话结束的时候 ...
没有它,一个用户将不能够再次登录(除非一旦它的会话允许期结束),即使他们退出另一个会话或超时,因为这个事件用于清理session注册信息 ..

## 为当前认证的用户以及它们的会话查询 SessionRegistry ...
设置并发控制,要么通过命名空间或者使用简单的bean 有副作用的提供对SessionRegistry 的引用,这能够直接在应用中使用,因此即使模拟不想要限制一个用户的会话个数,
你也许值得配置基础(无论如何),你能够配置maximumSession 属性为 - 1 允许不限制的会话 ..
如果你使用命名空间,你能够为内部创建的SessionRegistry 设置别名(通过 session-registry-alias),提供这个引用你能够注入到自己的bean中 ...
getAllPrincipals() 方法提供给你将返回当前所有认证用户的列表 ...
你能够查看一个用户的会话(通过调用 getAllSessions(Object principal,boolean includeExpiredSessions))方法,这将返回 SessionInformation对象列表 ..
你能够通过调用SessionInformation的expireNow()进行会话过期 .. 当这个用户返回到应用中,它将被阻止继续 ..
你也许会发现这些方法在管理端应用是有效的 ...

注意:
对于remember me;
不会检测到通过身份验证后执行重定向的机制（例如表单登录）进行SessionManagementFilter的身份验证，因为在身份验证请求期间不会调用过滤器。在这些情况下，会话管理功能必须单独处理。