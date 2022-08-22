# remember me
## 简单的基于hash 的Token 方式
首先它基于以下公式工作:
```text
base64(username + ":" + expirationTime + ":" +
md5Hex(username + ":" + expirationTime + ":" password + ":" + key))

username:          As identifiable to the UserDetailsService
password:          That matches the one in the retrieved UserDetails
expirationTime:    The date and time when the remember-me token expires, expressed in milliseconds
key:               A private key to prevent modification of the remember-me token
```

也就是这个 remember-me token 会随着,cookie一同发送,那么能够让用户自动登录,但是有一个问题是它仅仅在一段时间内有效,并且在token 失效之后,它能够被任何用户代理使用
这和使用digest 认证具有相同的问题,但是对于token 泄露的情况下,可以直接改变用户密码就能够使得remember-me token 失效 ..
在更加安全的情况下你可以应该使用另一种方式或者不启用remember me 服务 ..

## 持久化token的方式
这种方式是基于 http://jaspan.com/improved_persistent_login_cookie_best_practice的文章且包含了一些小量的修改 ..而实现的
那么这种方式需要包含一个 persistent_logins 表,创建使用以下的SQL(或者等价的)
```sql
create table persistent_logins (username varchar(64) not null,
								series varchar(64) primary key,
								token varchar(64) not null,
								last_used timestamp not null)
```
## remember-Me 接口和实现
它使用UsernamePasswordAuthenticationFilter,并且实现通过回调AbstractAuthenticationProcessingFilter 父类逻辑,他也使用在BasicAuthenticationFilter中,这个钩子将执行
一个具体的RememberMeServices(在合适的时候),这个接口看起来像这样:
```java
Authentication autoLogin(HttpServletRequest request, HttpServletResponse response);

void loginFail(HttpServletRequest request, HttpServletResponse response);

void loginSuccess(HttpServletRequest request, HttpServletResponse response,
	Authentication successfulAuthentication);
```
请参阅javadoc 了解完整的讨论(那些函数做什么),尽管注意到这个阶段AbstractAuthenticationProcessingFilter仅仅能够调用
loginFail() 和 loginSuccess()方法, autoLogin() 方法通过RememberMeAuthenticationFilter 调用(无论SecurityContextHolder 并没有包含Authentication) ..
因此，此接口为底层的 remember-me 实现提供了足够的身份验证相关事件通知,并且代理到实现上(当候选web 请求可能包含一个cookie且希望能够remembered ..) ..
这个设计允许任何数量的 remember-me 实现策略,我们可以了解到Spring Security提供了两种实现 ..


## TokenBasedRememberMeServices
最简单的一种实现就是基于Hash的Token方式,TokenBasedRememberMeServices 将生成一个RememberMeAuthenticationToken,它由
RememberMeAuthenticationProvider进行处理,一个key将会在这个认证提供器和 TokenBasedRememberMeServices共享,除此之外,
TokenBasedRememberMeServices 需要一个UserDetailsService(从这里抓取用户 / 密码)为了进行签名比较的目的,并且生成一个 RememberMeAuthenticationToken 去包含正确的
GrantedAuthoritys(信息) ..
某些分类的登出命令应该通过应用提供(如果是用户请求,应该无效),TokenBasedRememberMeServices 也实现了Spring Security的LogoutHandler接口,因此它能够被LogoutFilter使用去自动的清理cookie;

不要忘记添加你的RememberMeServices实现到UsernamePasswordAuthenticationFilter.setRememberMeServices(),包括RememberMeAuthenticationProvider 到
AuthenticationManager.setProviders()中,增加RememberMeAuthenticationFilter到FilterChainProxy(通常实现放置在UsernamePasswordAuthenticationFilter之后)

## 基于持久化Token的RememberMeServices
这个类能够以相同的方式作为TokenBasedRememberMeServices使用,但是需要额外配置 PersistenTokenRepository去存储token,这有两种标准的实现 ...
- InMemoryTokenRepositoryImpl 这仅仅只是为了测试使用
- JdbcTokenRepositoryImpl 将token存储在数据库中 ...