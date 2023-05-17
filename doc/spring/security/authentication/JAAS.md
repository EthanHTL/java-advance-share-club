# Java Authentication and Authorization Service(JAAS) Provider
spring security 提供了一个包能够代理认证请求到 Java 认证 以及 授权服务(JAAS). 这部分讨论这个包的内容

## AbstractJaasAuthenticationProvider
此提供器是基础并提供了 JAAS AuthenticationProvider 实现 .. 子类必须实现一个创建 LoginContext的方法 .. 
这个AbstractJaasAuthenticationProvider 有大量的依赖能够注入进去 ..

## JAAS CallbackHandler
大多数JAAS LoginModule 实例需要某些类型的回调 .. 这些回调必须被用来获取来自用户的用户名以及密码 ...
在Spring Security 部署中,Spring Security负责对用户的交互(通过认证机制).. 因此,当authentication 请求代理传递给JAAS 的时候,
spring security的认证机制 已经完全填充好了的一个Authentication 对象(它包含了由JAAS LoginModule需要的所有信息) ..

因此,此JAAS包提供了两种默认的回调处理器 .. 
`JaasNameCallbackHandler` 以及 `JaasPasswordCallbackHandler`. 这些回调处理器的每一种都实现了 JaasAuthenticationCallbackHandler 。。
在大多数情况下,这些回调处理器能够被使用且无需理解内部机制 ..

为了完全控制这些回调行为 。。 
AbstractJaasAuthenticationProvider 内部包装了这些JaasAuthenticationCallbackHandler(通过一个 InternalCallbackHandler) ..
InternalCallbackHandler 是一个实际实现了JAAS 常见CallbackHandler接口的类 .
任何时候使用 JAAS LoginModule的情况下,它传递了一个应用上下文配置的InternalCallbackHandler接口 . 
如果LoginModule 通过InternalCallbackHandler 实例请求一个回调 ..
那么这个回调,最终将传递给被包装的 JaasAuthenticationCallbackHandler ...

## JAAS AuthorityGranter
JAAS 与主体一起工作, 就连角色在JAAS中也表示负责人(principal,不知道应该是什么意思) .. Spring security,另一方面,与Authentication工作 ..

每一个Authentication对象包含了一个principal 以及多个 GrantedAuthority 实例 .. 
为了便于这些不同概念之间进行映射 .. spring security的JAAS 包括了一个AuthorityGranter 接口 ..

一个AuthorityGranter 负责检测一个JAAS 主体并返回一组String 对象(代表分配个这个主体的权限) .. 
每一个权限都会返回一个权限字符串 ..  然后AbstractJaasAuthenticationProvider 创建了一个JaasGrantedAuthority(它实现了spring security的 GrantedAuthority接口) - 它包含了授权字符串 以及
AuthorityGranter传递的 JAAS 主体信息 .. 然后AbstractJaasAuthenticationProvider 抓取 JAAS 主体(通过首先 使用JAAS的LoginModule 成功的认证用户的凭证)并返回它返回的 LoginContext ..

通过做出LoginContext.getSubject().getPrincipals()调用, 每一个最终的主体将传递给 AbstractJaasAuthenticationProvider.setAuthorityGranters(List)属性设置的 每一个 AuthorityGranter..

spring security 并不会包含任何生产级的AuthorityGranter 实例 ..  因为每一个JAAS 主体都有特定实现的含义 ..

然而,有一个TestAuthorityGranter  能够在单元测试中使用去说明一个简单的AuthorityGranter 实现 ..

## DefaultJaasAuthenticationProvider
此提供器让一个JAAS配置对象能够注册为依赖 .. 此提供器将使用注入的JAAS 配置创建一个LoginContext ..
这意味着 此提供器没有绑定到任何特定的配置实现,不像 JaasAuthenticationProvider 那样绑定到 Configuration 的任何特定实现。

### InMemoryConfiguration
为了更容易的注入一个配置到 DefaultJaasAuthenticationProvider, 一个默认的命名为 InMemoryConfiguration的内存实现 可以提供 ..
这个实现构造器接受一个Map(这里的每一个key 代表着一个登录配置名称,并且值代表了AppConfigurationEntry实例的数组)..

InMemoryConfiguration 也支持一个默认的 AppConfigurationEntry对象数组将被使用(如果没有映射在提供的map中发现),了解详情请查看 [javadoc of InMemoryConfiguration](https://docs.spring.io/spring-security/site/docs/6.0.3/api/org/springframework/security/authentication/jaas/memory/InMemoryConfiguration.html) 

### DefaultJaasAuthenticationProvider 示例配置
然而Spring配置  对于 InMemoryConfiguration来说相比于标准的JAAS配置文件可能更加的啰嗦 .. 使用它结合DefaultJaasAuthenticationProvider比
JaasAuthenticationProvider更加灵活 .. 因为它不依赖于默认的Configuration 实现 ..

下面这个示例提供了一个使用`InMemoryConfiguration` 的 DefaultJaasAuthenticationProvider 实例 .. 注意到Configuration的自定义实现能够容易注入到
DefaultJaasAuthenticationProvider ..
```xml
<bean id="jaasAuthProvider"
class="org.springframework.security.authentication.jaas.DefaultJaasAuthenticationProvider">
<property name="configuration">
<bean class="org.springframework.security.authentication.jaas.memory.InMemoryConfiguration">
<constructor-arg>
	<map>
	<!--
	SPRINGSECURITY is the default loginContextName
	for AbstractJaasAuthenticationProvider
	-->
	<entry key="SPRINGSECURITY">
	<array>
	<bean class="javax.security.auth.login.AppConfigurationEntry">
		<constructor-arg value="sample.SampleLoginModule" />
		<constructor-arg>
		<util:constant static-field=
			"javax.security.auth.login.AppConfigurationEntry$LoginModuleControlFlag.REQUIRED"/>
		</constructor-arg>
		<constructor-arg>
		<map></map>
		</constructor-arg>
		</bean>
	</array>
	</entry>
	</map>
	</constructor-arg>
</bean>
</property>
<property name="authorityGranters">
<list>
	<!-- You will need to write your own implementation of AuthorityGranter -->
	<bean class="org.springframework.security.authentication.jaas.TestAuthorityGranter"/>
</list>
</property>
</bean>
```

## JaasAuthenticationProvider
此提供器假设默认的配置实例是 `[ConfigFile](https://docs.oracle.com/javase/8/docs/jre/api/security/jaas/spec/com/sun/security/auth/login/ConfigFile.html)` 的实例 ..

这是为了做出此假设是为了尝试更新配置 ..  此JaasAuthenticationProvider 然后使用默认的配置去创建LoginContext ..

假设我们有一个JAAS 登录配置文件,名为`/WEB-INF/login.conf` ,存在以下内容
```text
JAASTest {
	sample.SampleLoginModule required;
};
```
像所有的spring security bean一样,JaasAuthenticationProvider 通过应用上下文配置 .. 以下的定义对应JAAS 登录配置文件:
```xml
<bean id="jaasAuthenticationProvider"
class="org.springframework.security.authentication.jaas.JaasAuthenticationProvider">
<property name="loginConfig" value="/WEB-INF/login.conf"/>
<property name="loginContextName" value="JAASTest"/>
<property name="callbackHandlers">
<list>
<bean
	class="org.springframework.security.authentication.jaas.JaasNameCallbackHandler"/>
<bean
	class="org.springframework.security.authentication.jaas.JaasPasswordCallbackHandler"/>
</list>
</property>
<property name="authorityGranters">
	<list>
	<bean class="org.springframework.security.authentication.jaas.TestAuthorityGranter"/>
	</list>
</property>
</bean>
```

## 通过身份的方式运行
如果配置,JaasApiIntegrationFilter 尝试在JaasAuthenticationToken上作为Subject运行 .. 这意味着Subject 能够访问(使用以下方式)
```java
Subject subject = Subject.getSubject(AccessController.getContext());
```
你能够配置这个集成(通过使用 [jaas-api-provision](https://docs.spring.io/spring-security/reference/servlet/appendix/namespace/http.html#nsa-http-jaas-api-provision) 属性).. 这个特性是有用的,当集成一个遗留的或者外部api(依赖于填充JAAS 主体被填充的api) ..



## 总结
要会使用这个,先了解 java authentication / authorization api ... 否则根本不知道什么意思 ..








