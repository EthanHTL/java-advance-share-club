### Bean 作用域
当创建bean definition,可以创建一个配方来通过bean definition创建实际实例,这个想法非常重要,因为这意味着和类一样,能够通过单个配方创建多个实例!
能够控制各种依赖以及配置数据(能够从特殊的bean 定义中加载到另一个对象中),并且从一个bean definition中控制对象的作用域,这个方法是非常有效的以及灵活的,可以选择对象的作用域(通过配置而不是在Java类级别上关注作用域),bean能够存在一个或者多个作用域!
spring支持6个作用域,其中在使用web-aware的应用上下文来说是有4个必要的,你能够创建自定义作用域!
支持的作用域如下:
1) singleton 单例(每个spring Ioc容器通过一个bean definition定义一个单例对象)
2) prototype 单个bean 定义存在很多的对象实例
3) request 单个bean 定义的作用域控制到单个http请求的生命周期中,意味着每一个http请求拥有自己的实例,在web-aware(感知) 的spring applicationContext下才有效!
4) session 作用域存在于一个http session,对于web感知容器有效!
5) websocket websocket的生命周期作为单个bean的作用域,同样对于web感知的上下文支持
6) application 整个servletContext的生命周期<br/>
在spring3.0之后,一个必要的thread 作用域是必要的但是默认不会自动注入,对于更多信息，可以查看[simpleThreadScope](SimpleThreadScope),对于想要创建自定义作用域可以查看[Using Custom Scope](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-scopes-custom-using);
   
#### 单例
spring中的单例和设计模式中的单例概念并不一样,设计模式的单例是当类加载器加载特定类的时候硬编码一个实例,而spring 单例是针对于每一个Bean来说的,是通过指定的Bean definition定义的单例Bean,单例默认为spring容器的作用域模式;
#### 原型
默认情况下,建议有状态bean使用原型Bean,无状态bean使用单例!
默认情况下,spring不会维护原型bean,尽管会调用初始化方法,但是销毁方法将不会调用!如果需要释放这些昂贵的资源,建议使用自定义[ bean post-processor](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-extension-bpp)
有些时候原型bean等价于new 操作符,生命周期回调通过客户端管理!
#### 两者区别
当你使用一个拥有原型bean依赖的单例bean,当该单例实例化的时候会得到感知,然后能够注册原型bean到一个单例bean中!
如果需要在运行时重复多次注入原型bean到一个单例中,可以使用方法注入!
#### web 感知作用域
request,session,application,websocket作用域在web感知应用中是必要的,例如xmlWebApplicationContext,如果在普通的ioc容器上使用这些scope,例如在ClassPathXmlApplicationContext上使用,如果存在位置作用域会抛出异常IllegalStateException 
#### 初始化配置
为了支持request...等这些级别的作用域,某些次要初始化配置也是必须要的(在定义你的bean之前)[对于标准的作用域例如singleton以及prototype初始化步骤是不需要的]

可以在spring Web MVC中访问这些作用域bean,非常有效的是,每个请求将被spring DispatcherServlet进行处理,没有必须的步骤,这个servlet已经暴露了所有相关的状态!<br/>
如果使用的是servlet 2.5的web 容器,请求将在spring DispatcherServlet之外进行处理(举个例子,当使用JSF或者struts),你需要注册org.springframework.web.context.request.RequestContextListener ServletRequestListener,
对于servlet3.0,能够完全通过WebApplicationInitializer接口控制,此外,对于旧容器,可以增加以下申明到应用的web.xml文件中:
```xml
<web-app>
    ...
    <listener>
        <listener-class>
            org.springframework.web.context.request.RequestContextListener
        </listener-class>
    </listener>
    ...
</web-app>
```
此外,如果监听器启动存在疑问,考虑使用spring的RequestContextFilter,过滤器映射依赖于web应用配置,可以合适的改变它!
```xml
<web-app>
    ...
    <filter>
        <filter-name>requestContextFilter</filter-name>
        <filter-class>org.springframework.web.filter.RequestContextFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>requestContextFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ...
</web-app>
```
实际上DispatcherServlet, RequestContextListener, and RequestContextFilter做的是相同的事情,将http请求对象绑定到为此请求服务的Thread,这使得在请求和会话范围内的Bean可以在调用链的更下游使用。
#### 请求作用域
作用域为单个http request,当请求结束之后,对象将被抛弃!
基于注解的形式
@RequestScope
#### 会话作用域
同请求作用域,但是作用域大小不一样!
如果使用基于注解的形式,可以使用@SessionScope
#### 应用作用域
整个应用将只存在一个单例,作用域ServletContext级别上并且将作为一个普通的ServletContext 属性而已!
请注意这是servletContext级别上的,而不是spring应用级别上的,所以可能一个applicationContext存在多个servletContext,并且实际已经暴露为servletContext属性!
但使用基于注解的形式组件时,@ApplicationScope可以标识一个 application作用域

#### 将scope对象作为依赖
spring容器不仅仅可以管理你的依赖,而且可以合作者,如果你想要注入一个http request 作用域的bean到另一个更长存活的bean中,你需要选择注入一个aop 代理去替换此作用域bean,你需要通过相同的接口去暴露一个代理对象作为此作用域对象(并且它能够从相关的作用域获取真实的业务对象,例如一个http 请求并且在真实对象上委派方法)<br/>
您还可以在范围为单例的bean之间使用<aop：scoped-proxy />，然后引用通过可序列化的中间代理进行，因此能够在反序列化时重新获得目标单例bean<br/>
如果在原型bean上使用代理,将会导致每次在代理上调用方法会创建一个新的实例对象, 前一个将会丢弃!<br/>
不仅仅可以通过生命周期安全的方式在更短的作用域中访问,可以申明一个一个注入点(构造器参数或者setter 参数或者自动装配字段)作为一个ObjectFactory<MyTargetBean>,允许通过getObject去抓取当前真实目标对象(在每次需要的时候),不需要拥有此实例或者存储它!<br/>
作为扩展的变体，您可以声明ObjectProvider <MyTargetBean>，它提供了几个附加的访问变体，包括getIfAvailable和getIfUnique<br/>
当前被调用的Provider是JSR-330的变种,一般被Provider<MyTargetBean>使用,每次会使用相关的get方法进行抓取尝试,可以查看[JSR-330了解更多](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-standard-annotations)!
<br/>
通过一个例子展示为什么要这样做并了解幕后原理:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <!-- an HTTP Session-scoped bean exposed as a proxy -->
    <bean id="userPreferences" class="com.something.UserPreferences" scope="session">
        <!-- instructs the container to proxy the surrounding bean -->
<!--        标识容器将会通过代理代理此对象!-->
        <aop:scoped-proxy/>
    </bean>

    <!-- a singleton-scoped bean injected with a proxy to the above bean -->
    <bean id="userService" class="com.something.SimpleUserService">
        <!-- a reference to the proxied userPreferences bean -->
        <property name="userPreferences" ref="userPreferences"/>
    </bean>
</beans>
```
从上述例子我们可以发现<aop:scoped-proxy/>能够代理对象(查看 [Choosing the Type of Proxy to Create and XML ](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-scopes-other-injection-proxies)
[Schema-based configuration](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-scopes-other-injection-proxies)了解)
为什么在request、session、以及自定义作用域级别需要这个<aop:scoped-proxy> ,下面给出一个例子和前面的bean 定义做出对比,这个例子将逐步完善;
```xml
<bean id="userPreferences" class="com.something.UserPreferences" scope="session"/>

<bean id="userManager" class="com.something.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```
这个例子将http session作用域相关的bean作为依赖注入到另一个生命周期更长的单例中[本质上并不需要将一个生命周期短的注入到领域给更长的,只需要进行实例代理即可特定与作用域bean合作]，userManger将不会感知到userPreferences是一个代理,并且在执行方法的时候,实际上是在代理对象上调用,代理对象调用真实对象执行此方法!<br/>
最终完善版本是:
```xml
<bean id="userPreferences" class="com.something.UserPreferences" scope="session">
    <aop:scoped-proxy/>
</bean>

<bean id="userManager" class="com.something.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```
#### 选择创建的代理类型
默认情况下,使用CGLIB进行代理,并且只能够执行公共方法调用,它们不会委派到实际的作用域目标对象!
除此之外,可以使用JDK动态代理,通过在<aop:scoped-proxy>的属性proxy-target-class设置为false即可,基于接口意味着你需要实现对应的接口并且所有的合作者中注入的作用域bean中的引用需要通过接口完成
```xml
<!-- DefaultUserPreferences implements the UserPreferences interface -->
<bean id="userPreferences" class="com.stuff.DefaultUserPreferences" scope="session">
    <aop:scoped-proxy proxy-target-class="false"/>
</bean>

<bean id="userManager" class="com.stuff.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```
对于基于类代理和方法代理的详细信息,查看[ Proxying Mechanisms](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop-proxying)

#### 自定义作用域
bean作用域机制是可以扩展的,你能够定义你自己的作用域或者重新定义存在的作用域,但是尽量不要重定义已经存在的作用域,这是一种不好的形式;
1) 创建一个自定义作用域,需要实现org.springframework.beans.factory.config.Scope,通过[scope doc](https://docs.spring.io/spring-framework/docs/5.3.6/javadoc-api/org/springframework/beans/factory/config/Scope.html)查看更多;<br/>
scope存在4个方法,获取对象以及删除对象、摧毁对象!
2) 比如session 作用域实现,返回一个session作用域bean(如果不存在,将返回一个新实例,在未来将会和session绑定),以下方法将从作用域中返回指定对象!
```java
Object get(String name, ObjectFactory<?> objectFactory)
```
3) 为了移除一个与作用域相关的bean,可以使用以下方法
```java
Object remove(String name)
```
4) 当指定的对象的作用域发生摧毁的时候,注册的一个回调将会触发,然后执行逻辑;
```java
   void registerDestructionCallback(String name, Runnable destructionCallback)
```
5) 获取会话身份,对于每一个作用域来说都是不同的,对于session作用域来说会获取的是sessionId
```java
String getConversationId()
```
#### 使用自定义作用域
使用必须让spring 感知到你的自定义作用域
通过
```java
void registerScope(String scopeName, Scope scope);
```
进行注册,此方法声明在ConfigurableBeanFactory,但是必要的BeanFactory属性被大多数applicationContext实现,第一个参数标识作用域的唯一标识名,第二个就是实际的作用域实现！<br/>
例如你可以这样做:
```java
Scope threadScope = new SimpleThreadScope();
beanFactory.registerScope("thread", threadScope);
```
然后通过在bean中使用scope属性进行设置!<br/>
注册方式不局限于程序化注册,也可以使用xml声明式注册,通过使用CustomScopeConfigurer进行自定义作用域注册
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="thread">
                    <bean class="org.springframework.context.support.SimpleThreadScope"/>
                </entry>
            </map>
        </property>
    </bean>

    <bean id="thing2" class="x.y.Thing2" scope="thread">
        <property name="name" value="Rick"/>
        <aop:scoped-proxy/>
    </bean>

    <bean id="thing1" class="x.y.Thing1">
        <property name="thing2" ref="thing2"/>
    </bean>

</beans>
```
当你在FactoryBean中使用<aop:scoped-proxy/>,代理对象是FactoryBean,而不是getObject()的返回的对象!
如果还不清楚<aop:scoped-proxy> 干什么的,其实就是将真实对象变成代理对象！
