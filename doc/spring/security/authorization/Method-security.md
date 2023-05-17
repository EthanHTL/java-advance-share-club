# 方法安全
从2.0版本开始,spring security 本质上已经优化了支持(增加安全到 服务层的方法上) .. 
它提供了对jsr-250注解安全的支持 以及 框架原生的@Secured注解支持 .. 从3.0开始,我们能够使用基于表达式的注解 ..
你能够应用安全到单个bean上,通过使用`intercept-methods` 元素去装饰bean 声明,或者能够保护多个bean(使用AspectJ 风格切入点跨整个服务层) ..

## 启用方法安全
从spring security 5.6开始,能够通过@EnableMethodSecurity 注解到任何@Configuration 实例上 ..

从各个方面优化了 @EnableGlobalMethodSecurity

1. 使用 AuthorizationManager 替代了 元数据源,配置属性,decision managers 以及投票者,能够简化重用 以及自定义 ..
2. 偏好基于bean的配置,而不是继承GlobalMethodSecurityConfiguration 来自定义 bean ..
3. 内置使用原生的spring aop, 移除抽象并允许你使用spring aop 构建块来自定义 ..
4. 检查冲突的注解确保 非歧义安全配置
5. 遵循JSR-250
6. 启用了@PreAuthorize, @PostAuthorize, @PreFilter, and @PostFilter 注解 ..

对于更早的版本,了解类似的支持([@EnableGlobalMethodSecurity](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html#jc-enable-global-method-security))

例如,以下的示例启用了Spring Security的 @PreAuthorize 注解
```java
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
	// ...
}
```
增加一个注解到类或者接口的方法上能够相应的限制对方法的访问 ..

spring Security的原生注解支持为方法定义一组属性 .. 这些能够传递给 DefaultAuthorizationMethodInterceptorChain 去做出最终决定 ..
```java
public interface BankService {
	@PreAuthorize("hasRole('USER')")
	Account readAccount(Long id);

	@PreAuthorize("hasRole('USER')")
	List<Account> findAccounts();

	@PreAuthorize("hasRole('TELLER')")
	Account post(Account account, Double amount);
}
```
当然还可以启用spring security的`@Secured` 注解
```java
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class MethodSecurityConfig {
	// ...
}
```
或者jsr-250
```java
@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
public class MethodSecurityConfig {
	// ...
}
```

### 自定义授权
Spring Security’s @PreAuthorize, @PostAuthorize, @PreFilter, and @PostFilter ship with rich expression-based support.
如果需要自定义表达式的处理,可以暴露一个 MethodSecurityExpressionHandler ,例如:
```java
@Bean
static MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
	DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
	handler.setTrustResolver(myCustomTrustResolver);
	return handler;
}
```

> 使用静态方法暴露MethodSecurityExpressionHandler, 能够确保spring 在初始化 spring security的方法安全配置类之前发布它 ..

同样对于基于角色的授权,spring security增加了默认的`ROLE_` 前缀,当评估`hasRole` 类似的表达式被使用 ..

你能够配置授权规则去使用不同的前缀(通过暴露一个GrantedAuthorityDefaults bean),例如:
```java
@Bean
static GrantedAuthorityDefaults grantedAuthorityDefaults() {
	return new GrantedAuthorityDefaults("MYPREFIX_");
}
```

### 自定义授权管理器
方法授权合并了method-before / method-after 授权

> 如果授权失败,则抛出AccessDeniedException

为了重新创建@EnableMethodSecurity 默认做的事情 ..

Full Pre-post Method Security Configuration
```java
@Configuration
@EnableMethodSecurity(prePostEnabled = false)
class MethodSecurityConfig {
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	Advisor preFilterAuthorizationMethodInterceptor() {
		return new PreFilterAuthorizationMethodInterceptor();
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	Advisor preAuthorizeAuthorizationMethodInterceptor() {
		return AuthorizationManagerBeforeMethodInterceptor.preAuthorize();
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	Advisor postAuthorizeAuthorizationMethodInterceptor() {
		return AuthorizationManagerAfterMethodInterceptor.postAuthorize();
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	Advisor postFilterAuthorizationMethodInterceptor() {
		return new PostFilterAuthorizationMethodInterceptor();
	}
}
```
注意spring security的方法安全内置使用 spring aop, 拦截器基于指定的顺序执行 ..
这能够通过调用拦截器实例上的`setOrder` 方法来设置 ..
```java
@Bean
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
Advisor postFilterAuthorizationMethodInterceptor() {
	PostFilterAuthorizationMethodInterceptor interceptor = new PostFilterAuthorizationMethodInterceptor();
	interceptor.setOrder(AuthorizationInterceptorOrders.POST_AUTHORIZE.getOrder() - 1);
	return interceptor;
}
```
如果你仅仅支持 `@PreAuthorize` 
```java
@Configuration
@EnableMethodSecurity(prePostEnabled = false)
class MethodSecurityConfig {
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	Advisor preAuthorize() {
		return AuthorizationManagerBeforeMethodInterceptor.preAuthorize();
	}
}
```
或者你可以自定义支持before-method的AuthorizationManager(将它装饰增加到拦截器列表中) ..
在这种情况下,你讲需要告诉spring security(AuthorizationManager) 以及 授权管理器需要应用到的类或者方法 ..

然后,你能够配置spring Security 去在@PreAuthorize  以及 @PostAuthorize 之间调度 AuthorizationManager ..

```java
@Configuration
@EnableMethodSecurity
class MethodSecurityConfig {
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public Advisor customAuthorize() {
		JdkRegexpMethodPointcut pattern = new JdkRegexpMethodPointcut();
		pattern.setPattern("org.mycompany.myapp.service.*");
		AuthorizationManager<MethodInvocation> rule = AuthorityAuthorizationManager.isAuthenticated();
		AuthorizationManagerBeforeMethodInterceptor interceptor = new AuthorizationManagerBeforeMethodInterceptor(pattern, rule);
		interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE_ADVISOR_ORDER.getOrder() + 1);
		return interceptor;
    }
}
```
> 你能够在spring security 方法拦截器之间使用在 AuthorizationInterceptorsOrder中指定的顺序常量来放置拦截器 ..

队友after-method 授权是完全相同的 .. 后者通常关注与分析返回值来验证访问 ..

例如,你可能有一个方法确保请求的账户名实际是是属于登录用户的
```java
public interface BankService {

	@PreAuthorize("hasRole('USER')")
	@PostAuthorize("returnObject.owner == authentication.name")
	Account readAccount(Long id);
}
```
你能够应用自己的 AuthorizationMethodInterceptor 去定制如何访问返回值进行评估 ..
举个例子,如果具有自定义注解,你可以这样配置:
```java
@Configuration
@EnableMethodSecurity
class MethodSecurityConfig {
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor customAuthorize(AuthorizationManager<MethodInvocationResult> rules) {
        AnnotationMatchingPointcut pattern = new AnnotationMatchingPointcut(MySecurityAnnotation.class);
        AuthorizationManagerAfterMethodInterceptor interceptor = new AuthorizationManagerAfterMethodInterceptor(pattern, rules);
        interceptor.setOrder(AuthorizationInterceptorsOrder.POST_AUTHORIZE_ADVISOR_ORDER.getOrder() + 1);
        return interceptor;
    }
}
```
它将在 @PostAuthorize 拦截器之后进行调度 ..

## EnableGlobalMethodSecurity
我们能够启用基于注解安全 通过 @EnableGlobalMethodSecurity 注解(到任何配置(@Configuration)实例上)

以下的方式启用spring security的@Secured 注解 ..
```java
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class MethodSecurityConfig {
// ...
}
```
最终spring security 会将 配置属性集合传递给AccessDecisionManager 做出最终决定
```java
public interface BankService {

    // 表示是否为 匿名
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
public Account readAccount(Long id);

// 是否为匿名
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
public Account[] findAccounts();

@Secured("ROLE_TELLER")
public Account post(Account account, double amount);
}
```
能够启用JSR-250注解
```java
@Configuration
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class MethodSecurityConfig {
// ...
}
```
这里是基于标准的,能够应用简单的基于角色的约束,但是并不具有spring security原生注解的功能 .. 
为了使用新的基于表达式的语法,开启Pre / post 
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
// ...
}
```
等价的代码是
```java
public interface BankService {

@PreAuthorize("isAnonymous()")
public Account readAccount(Long id);

@PreAuthorize("isAnonymous()")
public Account[] findAccounts();

@PreAuthorize("hasAuthority('ROLE_TELLER')")
public Account post(Account account, double amount);
}
```

## GlobalMethodSecurityConfiguration
有些时候你能需要执行比@EnableGlobalMethodSecurity注解结构更加复杂的操作 .

对此,你能够扩展 GlobalMethodSecurityConfiguration,确保 @EnableGlobalMethodSecurity注解出现在子类上 ..

例如,如果你想提供一个自定义的 MethodSecurityExpressionHandler 处理器 ..
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		// ... create and return custom MethodSecurityExpressionHandler ...
		return expressionHandler;
	}
}
```

### The <global-method-security> Element
此元素可以启用基于注解的安全(通过给元素设置合适的属性) 并分组安全切入点声明 跨越整个应用上下文应用 ..

你应该仅仅声明一个 <global-method-security> 元素。

下面启用了Spring Security的@Secured 支持
```java
<global-method-security secured-annotations="enabled" />
```

记住全局方法安全 背后使用AccessDecisionManager 进行活动处理 ..

启用JSR-250注解的支持
```java
<global-method-security jsr250-annotations="enabled" />
```
这是基于标准的,并且允许简单的基于角色的约束应用,但是没有spring security元素注解的能力


启用基于表达式的语法支持
```java
<global-method-security pre-post-annotations="enabled" />
```
Expression-based annotations are a good choice if you need to define simple rules that go beyond checking the role names against the user’s list of authorities.

> 注意: 被注释的方法仅仅会对声明为spring bean的实例生效(启用了方法安全的相同应用上下文),如果你想要保护不是由spring创建的实例(启用Aspectj)
> 注意,在同一个应用中可以有多种注解类型,但是一个类型应该仅仅应用到一个接口或者类上,否则这个行为无法很好的定义 ..
> 
> 如果两个注解在特定的方法上发现,只有一个会应用 ...

## Adding Security Pointcuts by using protect-pointcut

protect-pointcut非常有用,能够让你应用安全到许多bean上(通过简单的声明) ..
考虑一个示例:
```java
<global-method-security>
<protect-pointcut expression="execution(* com.mycompany.*Service.*(..))"
	access="ROLE_USER"/>
</global-method-security>
```
这样只要类名中以Service结尾的(以及以com.mycompany包中的类) 将被保护(它们也需要声明为应用上下文的bean) ..
同样,访问它们被限制需要具有ROLE_USER 角色 .. 

如同URL 匹配,最特定的匹配必须出现在切入点列表中的第一位,因为只有第一个匹配的表达式将使用 ..

Security 注解的优先级比切入点高 ..

也就是执行顺序相比于这些AccessDecisionManager更高 ..