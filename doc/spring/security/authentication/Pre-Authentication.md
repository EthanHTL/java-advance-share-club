# 预认证场景

示例包括 X.509、Siteminder 和运行应用程序的 Java EE 容器的身份验证,当使用预认证的时候,Spring Security 能够:
1. 通过请求识别用户
2. 获取用户的权限

这些详情依赖于外部认证机制 .. 一个用户可能通过它的证书信息(例如x.509的情况下)识别,或者通过在Siteminder的情况下通过http 请求 header 进行识别 ..
如果涉及到容器认证,那么用户通过调用进入的Http请求的getUserPrincipal()方法即可识别. ..

在某些情况下,一个外部机制也许提供了有关用户的角色以及权限信息 .. 然而,在其他情况下,你必须从单独的来源中获取权限(例如UserDetailsService) ...

也就是说一个身份信息包含了用户信息,又或许由外部机制提供的角色和权限,又或是单独权限系统中的权限信息 ..

## 预认证框架类
因为大多数的预认证机制遵循相同的模式,spring security 具有一组类 - 提供了一个内部框架(实现了预认证的认证提供器) ..
这移除了重复 并让新的实现能够以结构化的方式增加 ..

无需从头开始编写任何事情 ... 你不需要了解这些类(如果你想要使用类似于[x.509 认证](https://docs.spring.io/spring-security/reference/servlet/authentication/x509.html#servlet-x509)),
因为它已经包含了一个命名空间的配置选项(这能够简单使用并从它开始) .. 如果你需要使用显式的bean 配置或者 计划编写自己的实现 .. 你需要理解提供的实现如何工作 ..
你能够在`org.springframework.security.web.authentication.preauth` 包下发现这些类 .. 这里仅仅是一个概述 ..

## AbstractPreAuthenticatedProcessingFilter
此类将会检查securityContext的当前上下文, 如果为空,尝试从http 请求中抓取用户信息并提交给 authenticationManager ... 子类可以覆盖以下方法去获取这些信息
```java
protected abstract Object getPreAuthenticatedPrincipal(HttpServletRequest request);

protected abstract Object getPreAuthenticatedCredentials(HttpServletRequest request);
```
调用这些方法之后,这个过滤器将创建一个PreAuthenticatedAuthenticationToken (这包含了返回的数据 并提交进行认证),
通过`authentication` ,这意味着我们想要进一步处理,或者是加载用户权限 .. 通过遵循标准的spring security 认证架构..

正如其他的spring security filters, 预认证过滤器有一个authenticationDetailsSource 属性,这默认将会创建一个 WebAuthenticationDetails 对象去存储额外的信息 ..

例如会话标识符 以及ip地址来源(这些将 包含在Authentication 对象的details属性中).. 对于用户的角色信息能够从预认证机制中获取的情况下,
此数据也能够存储在此属性中,通过让此详情实现GrantedAuthoritiesContainer 接口 .. 这启用了认证提供器 去读取权限(这些可能外部分配给此用户的权限) ..


## J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource
如果一个过滤器配置了authenticationDetailsSource, 如果是这个类的实例,那么权限信息能够通过调用`isUserInRole(String role)` 方法 - 针对每一个 预先决定好的
`可映射的 角色信息` 来确定 .. 这个类能够从一个配置的MappableAttributesRetriever去获取角色列表 .. 

可能的实现包括在应用上下文中硬编码一个列表 或者从`web.xml` 文件中从`<security-role>` 中获取角色信息 ...

预认证示例应用使用后者 ..
这是一个额外的阶段(角色或者属性 映射到 spring security GrantedAuthority对象 能够通过 Attributes2GrantedAuthoritiesMapper)，默认将仅仅增加通用的`ROLE_` 前缀到名称上 ..
但是我们可以完全控制这个行为 ..

## PreAuthenticatedAuthenticationProvider
预先认证的提供者除了为用户加载UserDetails对象外，几乎没有其他事情可做 它通过代理到 AuthenticationUserDetailsService 来做这些事情 ...

后者能够类似于标准的UserDetailsService,但是它传递一个 Authentication 对象,而不是一个用户名称 ..
```java
public interface AuthenticationUserDetailsService {
	UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException;
}
```

这个接口也能够有其他使用,但是,通过预认证,他允许访问打包在authentication 对象中的权限 .. PreAuthenticatedGrantedAuthoritiesUserDetailsService
类是这样做的,也就是说默认实现就是它, 除此之外,它也许可以代理到标准的UserDetailsService, 通过UserDetailsByNameServiceWrapper 实现 ..

## Http403ForbiddenEntryPoint

AuthenticationEntryPoint 负责启动未认证用户的认证处理流程 (当他们尝试访问一个受保护的资源) ..

然而,在预认证的情况下,这不会应用 ... 你能够仅仅配置 ExceptionTranslationFilter（通过使用此类实例配置), 如果你与其他认证机制合并使用 ..
如果用户被AbstractPreAuthenticatedProcessingFilter 拒绝之后将会调用, 最终是一个空 authentication, 此认证入口总是返回一个403-forbidden 响应码 ..


## Concrete Implementations
X.509 认证将会在独立的章节进行解释 ... 这里我们查看某些支持其他预认证的场景 ..

### 请求头 认证(Siteminder)
一个外部认证系统也许将提供信息给应用(通过在http请求上设置指定的请求头).. 一个已知的示例就是Siteminder .. 它将通过`SM_USER` 请求头传递用户名称 ...
这个机制被RequestHeaderAuthenticationFilter 类支持 .. 这能够从请求头中抓取用户名,默认使用SM_USER的名称作为header的名称 .. 

> 当使用一个类似于这样的系统时,框架完全不会执行未认证检查 .. 并且它是尤其重要的(外部系统正确的配置 并且需要能够保护所有对应用程序的访问).. 如果一个攻击者能够在它原始的请求中伪造这些header
> 而没有能够被发现,这可能导致攻击者能够选择它想要的用户名 ..

#### 示例配置
```xml
<security:http>
<!-- Additional http configuration omitted -->
<security:custom-filter position="PRE_AUTH_FILTER" ref="siteminderFilter" />
</security:http>

<bean id="siteminderFilter" class="org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter">
<property name="principalRequestHeader" value="SM_USER"/>
<property name="authenticationManager" ref="authenticationManager" />
</bean>

<bean id="preauthAuthProvider" class="org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider">
<property name="preAuthenticatedUserDetailsService">
	<bean id="userDetailsServiceWrapper"
		class="org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper">
	<property name="userDetailsService" ref="userDetailsService"/>
	</bean>
</property>
</bean>

<security:authentication-manager alias="authenticationManager">
<security:authentication-provider ref="preauthAuthProvider" />
</security:authentication-manager>
```
这个示例配置中也假设配置了一个UserDetailsService 到配置中用来加载用户的角色 ...

### Java EE Container Authentication
J2eePreAuthenticatedProcessingFilter 能够从HttpServletRequest的userPrincipal属性中获取用户的名称 ...
此过滤器的使用通常将合并JavaEE角色使用,在J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource 中存在描述 ..

[示例](https://github.com/spring-projects/spring-security/tree/5.4.x/samples/xml/preauth) 在代码中使用了这种方式,能够从github上拉取并查看此应用的上下文文件(如果你感兴趣) ..









