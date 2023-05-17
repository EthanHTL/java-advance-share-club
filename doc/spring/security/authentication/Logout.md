# 处理登出
当使用`HttpSecurity` bean的时候,登出的能力将自动的使用 .. 默认是接受一个`/logout` URL 用来记录用户的退出
- 无效 httpSession
- 清理掉已经配置的任何RememberMe 认证
- 清理SecurityContextHolder
- 清理SecurityContextRepository
- 重定向到`/login?logout`

类似于配置登录能力,然而,这里也存在一些各种不同的选择 用来更深的定制你的退出需求:
```java
public SecurityFilterChain filterChain(HttpSecurity http) {
    http
        .logout(logout -> logout
            .logoutUrl("/my/logout")
            .logoutSuccessUrl("/my/index")
            .logoutSuccessHandler(logoutSuccessHandler)
            .invalidateHttpSession(true)
            .addLogoutHandler(logoutHandler)
            .deleteCookies(cookieNamesToClear)
        )
        ...
}
```

1. logoutUrl 表示触发登出的url(如果csrf 保护启用 - 默认启用),那么请求必须是一个POST,了解更多信息查看 java源码 ..
2.  是否清理HttpSession, 这是默认启用的 .. 配置SecurityContextLogoutHandler 之上覆盖了这些 .. 
3. 清理cookie(这是增加一个 CookieClearingLogoutHandler的快捷方式) ..

> 注意:
> Logout 能够通过使用XML命名空间符号 配置,查看 [logout elements](https://docs.spring.io/spring-security/reference/servlet/appendix/namespace/http.html#nsa-logout) 了解更多 ..
> 对于更多的常见场景,这些处理器在使用流式 api的时候,涵盖在这些api之下 ..

## Logout XML Configuration
`logout` 元素增加了登出支持(通过导航到一个特定的url),默认的登出url 是`/logout` ,但是你能够设置它为其他形式(例如通过设置`logout-url` 属性),
你能够在命名空间附录中发现其他可用的属性 ...

## LogoutHandler
通常,此处理器负责登出过程中的收尾处理 . 执行必要的资源清理动作,因此,他们不应该抛出异常,spring security 提供了各种实现:
- [PersistentTokenBasedRememberMeServices](https://docs.spring.io/spring-security/site/docs/6.0.3/api/org/springframework/security/web/authentication/rememberme/PersistentTokenBasedRememberMeServices.html)
    
    基于持久化的  remember me 服务的 token 清理 
- TokenBasedRememberMeServices

    同上,但是可能是自定义实现
- [CookieClearingLogoutHandler](https://docs.spring.io/spring-security/site/docs/6.0.3/api/org/springframework/security/web/authentication/logout/CookieClearingLogoutHandler.html) 

    cookie 清理
- [CsrfLogoutHandler](https://docs.spring.io/spring-security/site/docs/6.0.3/api/org/springframework/security/web/csrf/CsrfLogoutHandler.html)

    csrf 令牌清理
- [SecurityContextLogoutHandler](https://docs.spring.io/spring-security/site/docs/6.0.3/api/org/springframework/security/web/authentication/logout/SecurityContextLogoutHandler.html)

    清理安全上下文
- [HeaderWriterLogoutHandler](https://docs.spring.io/spring-security/site/docs/6.0.3/api/org/springframework/security/web/authentication/logout/HeaderWriterLogoutHandler.html)
    
    输出一些header

除了直接提供LogoutHandler实现,流式api也能够提供快捷方式(在背后提供相关的LogoutHandler 实现) ..
例如`deleteCookies` 将让你能够指定一个或者多个在登出成功是需要移除的cookie .. 这是增加CookieClearingLogoutHandler 的快捷方式 ..


## LogoutSuccessHandler
这个不用多说,就是登出成功之后需要做什么 ..

例如重定向或者转发到其他目的地 .. 但是此登出处理器可以抛出异常 ..

spring 提供了以下实现:
- SimpleUrlLogoutSuccessHandler
- HttpStatusReturningLogoutSuccessHandler

正如前面所提到,你不需要直接指定 SimpleUrlLogoutSuccessHandler .. 相比之下可以通过流式api 设置(logoutSuccessUrl()) ..
在背后设置 SimpleUrlLogoutSuccessHandler .. 这将在登出之后重定向到指定的 url(默认是 `/login?logout` )

HttpStatusReturningLogoutSuccessHandler 能够在rest api 类型场景下感兴趣... 除了重定向到一个url(在成功退出之后), 那么`LogoutSuccessHandler` 
让你提供了一个简单的Http 状态码能够返回 .. 如果没有配置,则返回默认状态码 200

## 更多的Logout相关的参考
- [Properly Clearing Authentication When Explicit Save is Enabled](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html#properly-clearing-authentication)
- [Logout Handling](https://docs.spring.io/spring-security/reference/servlet/authentication/logout.html#ns-logout)
- [Testing Logout](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/logout.html#test-logout)
- [`HttpServletRequest.logout()`](https://docs.spring.io/spring-security/reference/servlet/integrations/servlet-api.html#servletapi-logout) 
- [记住我的接口以及实现](https://docs.spring.io/spring-security/reference/servlet/authentication/rememberme.html#remember-me-impls) 
- CSRF 注意事项下的 [Logging Out](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#servlet-considerations-csrf-logout)
- Documentation for the [logout element](https://docs.spring.io/spring-security/reference/servlet/appendix/namespace/http.html#nsa-logout)  in the Spring Security XML Namespace section






