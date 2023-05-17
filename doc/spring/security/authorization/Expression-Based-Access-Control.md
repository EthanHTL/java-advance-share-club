# 基于表达式的访问控制

## 概述
spring security 使用spel 进行表达式支持, 并且你应该查看它如何工作(如果你对它感兴趣) ..
表达式使用一个'root 对象'作为评估上下文的一部分参与评估 ..

spring security 为web 和 method 安全使用不同的特定类作为 根对象去提供内置表达式并访问值,例如当前的身份(主体) ..

## 常见的内置表达式
表达式根对象的基类是 SecurityExpressionRoot, 提供了某些常见表达式(能够在web / method 安全上可用)

1. hasRole(role)
    
    注意事项:  role 不需要以ROLE_ 开始,它会自动幕后拼接,你能够定制这个行为,通过修改 DefaultWebSecurityExpressionHandler的 defaultRolePrefix的属性
2. hasAnyRole(...roles)

    同上,但是只要有一个角色存在即可
3. hasAuthority(authority)

    有指定的权限
4. hasAnyAuthority(... authorities)
    
    同上,但是只要有一个权限即可
5. principal
    
    访问代表当前用户的 主体对象
6. authentication

    访问从SecurityContext中拿到的当前 Authentication
7. permitAll

    总是评估为true
8. denyAll

    总是评估为false
9. isAnonymous()
    
    是否为匿名用户
10. isRememberMe()

    是否为记住我用户
11. isAuthenticated()
    
    用户是否为非匿名用户
12. isFullyAuthenticated()

    非匿名 以及非记住我用户
13. hasPermission(Object target,Object permission)

    给定的目标是否有给定的权限,例如 `hasPermission(domainObject,'read')`

    这个范围相比下面这一种更宽松 ..
14. hasPermission(Object targetId, String targetType, Object permission)

    如果用户能够使用给定的权限访问目标,例如: `hasPermission(1,'com.example.domain.Message','read')`

    这种是对acl 权限控制的支持(例如 那一条数据它具有读控制 / 那一条用户具有写控制)

## Web Security Expression
为了使用表达式去安全保护各种url,你需要设置 <http> 元素使用 use-expressions = true ..
spring security 期待 <intercept-url> 元素的 access 属性包含SpEL元素 .. 

每一个表达式应该评估为一个boolean, 决定是否可以访问 .. 下面是一个示例

```java
<http>
	<intercept-url pattern="/admin*"
		access="hasRole('admin') and hasIpAddress('192.168.1.0/24')"/>
	...
</http>
```

这个示例中保证只有admin 角色 以及匹配本地子网的 ip地址才可以访问 ... hasIpAddress 是一个特定于web 安全的表达式 ..
它定义在 WebSecurityExpressionRoot中, 它的实例作为表达式根对象 - 来评估web-access 表达式 ..

这个对象也能够暴露HttpServletRequest对象 - 到 `request` 属性上(因此你能够在表达式中直接执行请求(或者说调用请求的相关信息))

如果表达式被使用,那么WebExpressionVoter 被增加到将被命名空间(这里的命名空间指的是 上面的xml配置元素)使用的AccessDecisionManager .. 

因此,如果你不想要使用命名空间并且想要使用表达式,你需要增加这些到你的配置中 (例如java api 配置)..

### 在WebSecurity 表达式中应用 bean
如果你希望扩展可用表达式, 你能够很容易的引用 spring bean,举个例子,你能够如下示例展示,假设你有一个 名为 webSecurity的 bean,且包含了以下方法签名:
```java
public class WebSecurity {
		public boolean check(Authentication authentication, HttpServletRequest request) {
				...
		}
}
```
我们能够如下引用方法
```java
http
    .authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/user/**").access(new WebExpressionAuthorizationManager("@webSecurity.check(authentication,request)"))
        ...
    )
```

### 在WebSecurity 表达式中的路径变量
此时,我们能够很容易的引用在url中的路径变量, 例如,考虑一个restful 应用 - 他需要查询url 路径中的用户id(根据 /user{userId}) 即可得到 ..
你能够很容易的引用路径变量(在表达式中),例如如果你有一个bean(webSecurity)且具有以下签名:
```java
public class WebSecurity {
		public boolean checkUserId(Authentication authentication, int id) {
				...
		}
}
```
那么引用这个方法就如下所示:
```java
http
	.authorizeHttpRequests(authorize -> authorize
		.requestMatchers("/user/{userId}/**").access(new WebExpressionAuthorizationManager("@webSecurity.checkUserId(authentication,#userId)"))
		...
	);
```
在这个配置中,url 匹配传递的路径变量(并转换它)给checkUserId方法 ..

## Method Security Expressions
方法安全是稍微更加复杂一点,spring security 3.0引入了一些新的注解去允许对表达式进行广泛的支持 ..

## @Pre / @Post 注解

这里有4种注解可以支持表达式属性允许pre / post 调用的授权检查 并且也支持过滤提交的集合参数或者返回值 ..这里有 @PreAuthorize, @PreFilter, @PostAuthorize, and @PostFilter ..

通过`global-method-security` 命名空间元素即可启用 ..

### 通过 @PreAuthorize and @PostAuthorize 访问控制
最有用的是 @PreAuthorize ,表示一个方法是否应该执行 .. 
```java
@PreAuthorize("hasRole('USER')")
public void create(Contact contact);
```
这意味着用户必须要有ROLE_USER(用户角色)才允许访问 ..

很明显相同的事情可以通过传统配置以及一个对需要角色的简单配置属性得到 ..
```java
@PreAuthorize("hasPermission(#contact, 'admin')")
public void deletePermission(Contact contact, Sid recipient, Permission permission);
```

这里表示将方法参数作为表达式的一部分来决定是否当前用户存在 admin 权限(针对给定的contact),内置的hasPermission表达式 链接到spring security acl 模块(通过
应用上下文),能够通过将参数名作为表达式变量来访问 任何方法参数 ..

spring security 也能够以各种方式解析方法参数,spring security 使用DefaultSecurityParameterNameDiscoverer 去发现参数名 ..

默认情况,以下的选项将会针对方法进行尝试 ..
1. 如果spring security的@P 注解出现在方法的单个参数上,且给定的参数名称 ..
    
    例如在JDK8之前的jdk编译接口(这是有用的,因为之前的编译器无法知道参数的名称), 以下是使用@P注解的示例:
```java
import org.springframework.security.access.method.P;

...

@PreAuthorize("#c.name == authentication.name")
public void doSomething(@P("c") Contact contact);
```
在这个场景之后,这完全是通过 AnnotationParameterNameDiscoverer 发现的, 你能够定制这个特定注解的值的支持 ..

2. spring data的@Param注解出现在方法的参数上,等价于@P注解 ..

```java
import org.springframework.data.repository.query.Param;

...

@PreAuthorize("#n == authentication.name")
Contact findContactByName(@Param("n") String name);
```
3. 如果jdk8 通过 `-parameters`参数编译源代码 并且使用了spring 4+,那么标准的jdk 反射api 能够被用来发现参数名称,这对接口和类都工作 ..
4. 最后，如果代码是用调试符号编译的，那么参数名可以通过调试符号获取,这对于接口是不工作的,因为他们没有有关参数名称的调试信息 .. 对于接口,要么使用注解或者 jdk 偏好方式 ..

任何spel 函数在表达式中都是可用的,例如访问参数的属性 .. 例如如果你想要访问一个方法去访问仅仅允许(仅当用户名等于约定名称的时候)
```java
@PreAuthorize("#contact.name == authentication.name")
public void doSomething(Contact contact);
```
当然还使用其他内置表达式,例如authentication,Authentication 当前是存在security context中的 .. 也能够直接访问 principal 属性(通过principal表达式) ..

这个值将提供一个UserDetails实例 .. 因此你可能需要使用一些表达式访问属性(例如 principal.username / principal.enable)

### 使用 @PreFilter and @PostFilter 过滤
spring security 支持集合 / 数组 / map 以及流(stream)的过滤, 通过表达式做到 ..

通常最常见的是在一个方法的返回值上执行... 以下使用`@PostFilter` 注解
```java
@PreAuthorize("hasRole('USER')")
@PostFilter("hasPermission(filterObject, 'read') or hasPermission(filterObject, 'admin')")
public List<Contact> getAll();
```
当使用这个注解的时候,spring security 会迭代返回的集合或者map 并移除提供表达式评估为false的值,例如数组,将返回包含已经过滤后的元素返回 ..
`filterObject` 表示当前在集合中被过滤的当前对象,当使用Map,则表示当前的Map.Entry对象,那么我们可以在表达式中使用`filterObject.key` 或者 `filterObject.value`
访问它的属性 .. 我们也可以通过@PreFilter 在方法调用之前过滤 .. 尽管者很少使用 (但是对于在存在角色继承关系的情况下,我们可以依靠它做出数据权限) ..

假设如果超过多个参数都是集合类型,那么可以通过注解上的 filterTarget 来决定选择某一个集合 ..

注意到过滤很明显并不是调整你数据抓取查询的一个替代 .. 如果你想要过滤大型集合 并移除许多项,那么这是不高效的 ..

### 内置表达式
有关方法安全的内置表达式 只需要查看对应的根对象实现即可知道,那么`filterTarget` 表示 当前过滤的对象(不管是提交的集合还是返回值),然后`returnValue` 表达式表示返回值对象 ..
这些都比较简单,但是 `hasPermission` 表达式需要仔细查看 ..
### The PermissionEvaluator interface
`hasPermission` 表达式将会代理到 PermissionEvaluator 实例, 有意的桥接表达式系统和 spring security acl系统 .. 让你能够在域对象上指定授权约束 ..
基于抽象权限 .. 它没有显式的依赖于acl模块,因此你能够包装它(加入一些可能 需要的额外信息) ..

这个接口具有两个方法:
```java
boolean hasPermission(Authentication authentication, Object targetDomainObject,
							Object permission);

boolean hasPermission(Authentication authentication, Serializable targetId,
							String targetType, Object permission);
```
这些方法直接映射了表达式的可用版本 ...  除了第一个参数(Authentication 对象)不需要提供 ..
第一个将使用在域对象的情况中,访问能够控制(针对已经加载的对象). 然后如果表达式返回true,表示当前用户具有对给定对象的权限 ..

第二个版本将被使用在对象没有加载,但是它的标识符已知, 一个抽象的类型指定符号 (域对象的类型)也是必须的,这用于加载正确的acl权限 ..

这可能是传统的java 类型对象 又或者不是, 只要保持权限加载一致即可 ..

为了使用`hasPermission()` 表达式,你能够显式的配置 PermissionEvaluator 到应用上下文中,以下的示例展示你如何做:

```xml
<security:global-method-security pre-post-annotations="enabled">
<security:expression-handler ref="expressionHandler"/>
</security:global-method-security>

<bean id="expressionHandler" class=
"org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler">
	<property name="permissionEvaluator" ref="myPermissionEvaluator"/>
</bean>
```
这里的 myPermissionEvaluator 必须是实现了 PermissionEvaluator的bean, 通常它是来自acl 模块的实现,也就是aclPermissionEvaluator ..

查看 [Contacts](https://github.com/spring-projects/spring-security-samples/tree/main/servlet/xml/java/contacts) 了解示例应用配置获得更多信息 ..

## 方法安全源注解

使用元注解(为方法安全)能够让代码更具有可读性 .. 这特别方便,假设你发现了大量重复的相同复杂表达式(跨越你的代码库中),考虑
```java
@PreAuthorize("#contact.name == authentication.name")
```
替代到处重复, 通过元注解
```java
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("#contact.name == authentication.name")
public @interface ContactPermission {}
```
你能够使用元注解(对任何spring security method security annotations), 保持规范兼容,jsr-250 注解不支持元注解 ..
