# 安全对象实现
spring security 是针对安全对象来进行授权的,现在我们来了解安全对象实现 ..

## aop alliance(MethodInvocation) Security Interceptor
从spring security 2.0开始, 加固MethodInvocation 实例需要大量的模版配置 ..现在对于方法安全更加推荐的方式是使用命名空间配置

那么这种方式方法安全基础设施将自动的为我们配置 ... 因此你不需要知道有关实现类 ..

这里仅仅提供一些类的快速概览 ...

方法安全是通过MethodSecurityInterceptor 强制的,这保护MethodInvocation 实例 .. 依赖于配置的方式,一个拦截器也许特定于单个bean 或者再多个bean 中共享 ..

拦截器使用MethodSecurityMetadataSource 实例去获取配置属性(然后应用到特定的方法调用中) ..
MapBasedMethodSecurityMetadataSource 被用来存储配置属性(以方法名作为key,这也能通配符处理) 并且这会被内部使用- 当属性定义在应用上下文中(通过
<intercept-methods> 或者<protect-point> 元素定义), 其他实现被用来处理基于注解的配置 ..

但是xml的配置方式我们无需关注,直接基于java bean方式配置可能更好 ..

## Explicit MethodSecurityInterceptor Configuration
我们能够在应用上下文中配置一个 MethodSecurityInterceptor(使用spring aop的代理机制)
```xml
<bean id="bankManagerSecurity" class=
	"org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor">
<property name="authenticationManager" ref="authenticationManager"/>
<property name="accessDecisionManager" ref="accessDecisionManager"/>
<property name="afterInvocationManager" ref="afterInvocationManager"/>
<property name="securityMetadataSource">
	<sec:method-security-metadata-source>
	<sec:protect method="com.mycompany.BankManager.delete*" access="ROLE_SUPERVISOR"/>
	<sec:protect method="com.mycompany.BankManager.getBalance" access="ROLE_TELLER,ROLE_SUPERVISOR"/>
	</sec:method-security-metadata-source>
</property>
</bean>
```

## AspectJ (JoinPoint) Security Interceptor
Aspect 安全拦截器非常类似于aop alliance 安全拦截器 .. 

此拦截器命名为 AspectJSecurityInterceptor, 不像aop alliance 安全拦截器,这依赖于 spring 应用上下文去通过代理编织安全上下文 ..

此拦截器通过Aspectj 编译器编织 .. 在同一个应用中存在多个类型的安全拦截器是不常见的 ..  AspectJSecurityInterceptor
能够被用来进行域对象实例安全 并且 aop Alliance MethodSecurityInterceptor 能够用来进行服务层的安全 ..

示例配置:
```xml
<bean id="bankManagerSecurity" class=
	"org.springframework.security.access.intercept.aspectj.AspectJMethodSecurityInterceptor">
<property name="authenticationManager" ref="authenticationManager"/>
<property name="accessDecisionManager" ref="accessDecisionManager"/>
<property name="afterInvocationManager" ref="afterInvocationManager"/>
<property name="securityMetadataSource">
	<sec:method-security-metadata-source>
	<sec:protect method="com.mycompany.BankManager.delete*" access="ROLE_SUPERVISOR"/>
	<sec:protect method="com.mycompany.BankManager.getBalance" access="ROLE_TELLER,ROLE_SUPERVISOR"/>
	</sec:method-security-metadata-source>
</property>
</bean>
```
两种拦截器都共享相同的 securityMetadataSource,因为SecurityMetadataSource 与`java.lang.reflect.Method` 实例协同工作而不是和 Aop 特定库的类协同工作 ..

你的访问决定能够访问相关的 aop 特定库调用(例如 MethodInvocation 或者JoinPoint) 并且能够考虑大量的可选条件(例如方法参数) - 当做出访问决定时 ..

下一部分,我们需要定义一个AspectJ 切面
```java
package org.springframework.security.samples.aspectj;

import org.springframework.security.access.intercept.aspectj.AspectJSecurityInterceptor;
import org.springframework.security.access.intercept.aspectj.AspectJCallback;
import org.springframework.beans.factory.InitializingBean;

public aspect DomainObjectInstanceSecurityAspect implements InitializingBean {

	private AspectJSecurityInterceptor securityInterceptor;

	pointcut domainObjectInstanceExecution(): target(PersistableEntity)
		&& execution(public * *(..)) && !within(DomainObjectInstanceSecurityAspect);

	Object around(): domainObjectInstanceExecution() {
		if (this.securityInterceptor == null) {
			return proceed();
		}

		AspectJCallback callback = new AspectJCallback() {
			public Object proceedWithObject() {
				return proceed();
			}
		};

		return this.securityInterceptor.invoke(thisJoinPoint, callback);
	}

	public AspectJSecurityInterceptor getSecurityInterceptor() {
		return securityInterceptor;
	}

	public void setSecurityInterceptor(AspectJSecurityInterceptor securityInterceptor) {
		this.securityInterceptor = securityInterceptor;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.securityInterceptor == null)
			throw new IllegalArgumentException("securityInterceptor required");
		}
	}
}
```
在前面的示例中,要求security 拦截器将应用到每一个 PersistableEntity的实例上,它是一个没有展示的抽象类(你能够使用其他类或者切入点表达式) .. 对于
那些好奇心强的人来说,AspectJCallback 需要的,因为`proceed()` 语句仅仅在 `around()` 方法体中有特殊含义 ..,AspectJSecurityInterceptor 调用
匿名的AspectJCallback 类(当它想要目标对象继续执行时) ..

你需要配置spring 去加载切面(并使用AspectJSecurityInterceptor进行编制),下面的示例中展示了 bean声明来实现:
```xml
 <bean id="domainObjectInstanceSecurityAspect"
       class="security.samples.aspectj.DomainObjectInstanceSecurityAspect"
       factory-method="aspectOf">
    <property name="securityInterceptor" ref="bankManagerSecurity"/>
</bean>
```
现在你能够在应用中的任何地方创建你的bean(使用你觉得适合的任何方式,例如 new Person()),并且安全拦截器都会被应用 ..