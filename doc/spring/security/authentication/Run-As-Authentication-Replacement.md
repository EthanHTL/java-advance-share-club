# run-as Authentication Replacement
AbstractSecurityInterceptor 在安全对象回调阶段,能够临时的替换SecurityContext 以及 SecurityContextHolder中的Authentication 对象 ..
这仅仅发生在原始的Authentication object 已经成功的由AuthenticationManager 以及 AccessDecisionManager 处理之后 ..

那么RunAsManager 指示替代Authentication 对象 ..  - 如果可能,那么在SecurityInterceptorCallback 期间应该被使用 ..

在secure object callback 阶段,通过临时的替代Authentication 对象,这个secured invocation 能够调用其他对象(需要不同认证 / 授权凭证) ..

它也能够执行任何内部的安全检查(例如对特定的GrantedAuthority 对象做出检查) .. 因为Spring Security 提供了大量的帮助类能够自动的配置远程协议(基于
SecurityContextHolder的内容),这些run-as 替代在调用远程 web 服务的时候特别有用 ..

## 配置
Spring Security 提供了一个RunAsManager 接口
```java
Authentication buildRunAs(Authentication authentication, Object object,
	List<ConfigAttribute> config);

boolean supports(ConfigAttribute attribute);

boolean supports(Class clazz);

```
第一个方法返回了一个替代存在Authentication 对象的Authentication 对象(在此方法执行期间), 如果方法返回了null 它指示不需要发生任何替换 ..

第二个方法将被`AbstractSecurityInterceptor` 使用作为配置属性的启动验证的一部分 .. `support(Class)` 方法将被security 拦截器实现调用去
确保配置的RunAsManager 支持 security 拦截器提供的secure 对象类型 ..

Spring Security 提供了RunAsManager的一种具体实现 .. RunAsManagerImpl 类返回了一个替代RunAsUserToken(如果ConfigAttribute 以`RUN_AS` 开始),
如果任何这样的`ConfigAttribute` 被发现,那么这个替代的RunAsUserToken 包含了相同的principal / 凭证 以及授予给这个原始认证对象的权限 ..伴随着
一些新的SimpleGrantedAuthority(以RUN_AS_ 开始的ConfigAttribute 映射而来) .. 每一个新的 SimpleGrantedAuthority 以`ROLE_` 前缀开始 ..
后跟随的(紧跟随)是`RUN_AS` ConfigAttribute .. 举个例子`RUN_AS_SERVER` 最终在替代RunAsUserToken中包含了一个 `ROLE_RUN_AS_SERVER` 授予的授权 ..


替换RunAsUserToken 类似其他的Authentication 对象 .. 它需要能够被AuthenticationManager 认证 .. 可能通过代理到合适的AuthenticationProvider上 ..
`RunAsImplAuthenticationProvider` 执行这样的认证 .. 它接受任何 出现的RunAsUserToken是有效的 ..

为了确保恶意代码不能够创建RunAsUserToken 以及为了保证可接受性(通过 RunAsImplAuthenticationProvider 呈现它) ..
一个key的hash将存储在所有生成的 token中 ..

在bean上下文中`RunAsManagerImpl`  以及 `RunAsImplAuthenticationProvider` 使用相同的key ..
```xml
<bean id="runAsManager"
	class="org.springframework.security.access.intercept.RunAsManagerImpl">
<property name="key" value="my_run_as_password"/>
</bean>

<bean id="runAsAuthenticationProvider"
	class="org.springframework.security.access.intercept.RunAsImplAuthenticationProvider">
<property name="key" value="my_run_as_password"/>
</bean>
```

通过使用相同的key,每一个RunAsUserToken 能够被验证(因为它是由一个赞成的 RunAsManagerImpl所创建的) ..

在创建之后,RunAsUserToken是不可变的,由于安全原因 ...

## 总结

注意: 这里的secure 对象(很可能是一个过滤器 或者一个方法执行) .. 也就是在secure 对象回调阶段的过程中能够临时的替换 Authentication 对象 ..

也就是前面说的,例如调用其他web 服务时特别有用 ..

还有 AbstractSecurityInterceptor 是一个很重要的概念,在Spring Security中用来 考究一个安全对象是否能够被调用 ..

直白点就是,用户是否有权利调用此方法(等等处理) ..

总的来说,这个一般很少使用 ...













