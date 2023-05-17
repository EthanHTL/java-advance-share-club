# remember me
记住我或者持久化登录认证指的是Web 站点能够记住会话之间的一个实体的身份 .. 这通常通过发送cookie到浏览器完成 .. 在后续的会话期间cookie将会被检测并且能够发生自动登录 ..

spring security 提供了必要的回调函数(对于这些将要发生的操作 并且具有两种具体的记住我实现) ..

一种使用hash 去保留基于cookie 的 token的安全 .. 另一种使用数据库或者其他持久化方式去存储生成的 token ..

注意到两种实现同时 需要一个UserDetailsService .. 如果你使用了一个认证提供器但是没有使用UserDetailsService(例如LDAP provider),
那么它将不会工作,除非在应用上下文中存在UserDetailsService ..

## 简单的基于hash 的Token 方式
这种方式使用hash 去实现一个有用的记住我策略... 本质上,一个cookie 将会在交互式认证成功之后发送到浏览器..
首先它基于以下公式工作:
```text
base64(username + ":" + expirationTime + ":" + algorithmName + ":"
algorithmHex(username + ":" + expirationTime + ":" password + ":" + key))

username:          As identifiable to the UserDetailsService
password:          That matches the one in the retrieved UserDetails
expirationTime:    The date and time when the remember-me token expires, expressed in milliseconds
key:               A private key to prevent modification of the remember-me token
algorithmName:     The algorithm used to generate and to verify the remember-me token signature
```

上述的公式说明很简单,我们最需要关注的就是Key(私钥,用来阻止对记住我令牌的修改) ...

此token 仅在用户名 / 密码 / key没有发生改变 以及指定的周期内没有过期的情况下才认为token 是有效的 ...  尤其是,
它存在潜在的安全问题 .. 一个被捕获的记住我token 能够在任何用户代理上使用(直到token 过期).. 这和数字认证存在相同问题 ..

如果一个主体已经感知到token 已经被捕获,它能够轻易的改变它的密码 并立即让所有具有此问题的记住我 token 失效 .. 

如果需要更加健全的安全需要, 考虑后续的讨论的方式 ... 或者完全不使用记住我服务 ..

以下是一个xml配置方式启用记住我服务的一个示例
```xml
<http>
...
<remember-me key="myAppKey"/>
</http>
```
通常 UserDetailsService 会自动选择 .. 如果在应用上下文中存在多个,我们需要通过`user-service-ref` 属性去选择需要被使用的, 它的值是UserDetailsService bean的名称 ..


## 持久化token的方式
这种方式是基于 http://jaspan.com/improved_persistent_login_cookie_best_practice的文章且包含了一些小量的修改 ..而实现的

本质上,用户名将不会包括在cookie中,为了阻止去暴露一个有效的登录名称 .. 这在此文章的注释部分得到了讨论) ..

假设我们通过命名空间配置方式使用它,我们需要提供一个数据源引用
```xml
<http>
...
<remember-me data-source-ref="someDataSource"/>
</http>
```
那么这种方式需要包含一个 persistent_logins 表,创建使用以下的SQL(或者等价的)
```sql
create table persistent_logins (username varchar(64) not null,
								series varchar(64) primary key,
								token varchar(64) not null,
								last_used timestamp not null)
```
## remember-Me 接口和实现
它被UsernamePasswordAuthenticationFilter,并且实现通过回调AbstractAuthenticationProcessingFilter 父类逻辑,它也使用在BasicAuthenticationFilter中,这个钩子将执行
一个具体的RememberMeServices(在合适的时候),这个接口看起来像这样:
```java
Authentication autoLogin(HttpServletRequest request, HttpServletResponse response);

void loginFail(HttpServletRequest request, HttpServletResponse response);

void loginSuccess(HttpServletRequest request, HttpServletResponse response,
	Authentication successfulAuthentication);
```
请参阅javadoc 了解完整的讨论(那些函数做什么),尽管注意到这个阶段AbstractAuthenticationProcessingFilter仅仅能够调用
loginFail() 和 loginSuccess()方法, autoLogin() 方法通过RememberMeAuthenticationFilter 调用(无论SecurityContextHolder 是否包含Authentication) ..
因此，此接口为底层的 remember-me 实现提供了足够的身份验证相关事件通知,并且代理到实现上(当候选web 请求可能包含一个cookie且希望能够remembered ..) ..
这个设计允许任何数量的 remember-me 实现策略,我们可以了解到Spring Security提供了两种实现 ..


## TokenBasedRememberMeServices
最简单的一种实现就是基于Hash的Token方式,TokenBasedRememberMeServices 将生成一个RememberMeAuthenticationToken,它由
RememberMeAuthenticationProvider进行处理,一个key将会在这个认证提供器和 TokenBasedRememberMeServices共享,除此之外,
TokenBasedRememberMeServices 需要一个UserDetailsService(从这里抓取用户 / 密码)为了进行签名比较的目的,并且生成一个 RememberMeAuthenticationToken 去包含正确的
GrantedAuthoritys(信息) ..
某些分类的登出命令应该通过应用提供(如果是用户请求,应该无效),TokenBasedRememberMeServices 也实现了Spring Security的LogoutHandler接口,因此它能够被LogoutFilter使用去自动的清理cookie;

默认的算法使用的是SHA-256 去编码token 签名,为了验证token 签名,从algorithmName 抓取的算法将会被解析并使用 ..
如果没有此参数出现,那么默认的匹配算法将将会使用(那么就是sha-256),你可以制定一个完全不同的算法用来编码签名以及进行签名匹配 (签名 和签名匹配的算法可以不一样)..
```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http, RememberMeServices rememberMeServices) throws Exception {
	http
			.authorizeHttpRequests((authorize) -> authorize
					.anyRequest().authenticated()
			)
			.rememberMe((remember) -> remember
				.rememberMeServices(rememberMeServices)
			);
	return http.build();
}

@Bean
RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
	RememberMeTokenAlgorithm encodingAlgorithm = RememberMeTokenAlgorithm.SHA256;
	TokenBasedRememberMeServices rememberMe = new TokenBasedRememberMeServices(myKey, userDetailsService, encodingAlgorithm);
	rememberMe.setMatchingAlgorithm(RememberMeTokenAlgorithm.MD5);
	return rememberMe;
}
```

不要忘记添加你的RememberMeServices实现到UsernamePasswordAuthenticationFilter.setRememberMeServices(),包括RememberMeAuthenticationProvider 到
AuthenticationManager.setProviders()中,增加RememberMeAuthenticationFilter到FilterChainProxy(通常实现放置在UsernamePasswordAuthenticationFilter之后)
```xml
<bean id="rememberMeFilter" class=
"org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter">
<property name="rememberMeServices" ref="rememberMeServices"/>
<property name="authenticationManager" ref="theAuthenticationManager" />
</bean>

<bean id="rememberMeServices" class=
"org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices">
<property name="userDetailsService" ref="myUserDetailsService"/>
<property name="key" value="springRocks"/>
</bean>

<bean id="rememberMeAuthenticationProvider" class=
"org.springframework.security.authentication.RememberMeAuthenticationProvider">
<property name="key" value="springRocks"/>
</bean>
```

允许用户去安全的升级到不同的编码算法(然而仍然会验证旧的,如果没有算法名称出现).. 为了这样做,你可以自定义TokenBasedRememberMeServices 作为一个Bean 并在配置中使用它 ..

## 基于持久化Token的RememberMeServices
这个类能够以相同的方式作为TokenBasedRememberMeServices使用,但是需要额外配置 PersistenTokenRepository去存储token,这有两种标准的实现 ...
- InMemoryTokenRepositoryImpl 这仅仅只是为了测试使用
- JdbcTokenRepositoryImpl 将token存储在数据库中 ...







