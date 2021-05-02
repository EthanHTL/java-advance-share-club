#容器扩展
默认情况并不需要你实现子类ApplicationContext,建议使用指定接口进行可插拔式的增强;

## 通过使用BeanPostProcessor自定义Beans
实现BeanPostProcessor接口定义的回调函数能够干什么? 可以提供自定义的实例化逻辑,依赖解析逻辑,以及其他;如果想在spring容器完成实例化、配置、初始化之后实现自定义逻辑,你可以加入多个自定义BeanPostProcessor实现;<br/>
多个实例BeanPostProcessor之下,可以制定运行顺序（通过设置order）,可以通过实现Ordered接口设置此属性,如果你写了一个自己的后置处理器,你可以考虑实现Ordered接口,查看[ BeanPostProcessor](https://docs.spring.io/spring-framework/docs/5.3.6/javadoc-api/org/springframework/beans/factory/config/BeanPostProcessor.html)
以及[Ordered](https://docs.spring.io/spring-framework/docs/5.3.6/javadoc-api/org/springframework/core/Ordered.html)查看更多,也可以查看程序化注入BeanPostProcessor实例!<br/>
后置处理器在bean上操作,意味着每当创建一个bean那么将会通过beanPostProcessor进行操作!<br/>
每个容器中的后置处理器都是有作用域的,这仅仅和使用的容器层次有关,在容器中创建的后置处理器只会存在容器中,换句话说,当前容器创建的bean不可能使用另一个容器的bean进行处理,即使两个容器都是相同体系的一部分!<br/>
如果为了改变bean 定义,那么你需要使用的是BeanFactoryPostProcessor,在[Customizing Configuration Metadata with a BeanFactoryPostProcessor.](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-extension-factory-postprocessors)进行描述!

##后置处理器详解
beanpostProcessor存在两个回调方法,并且它会在容器初始化(例如InitializeBean的回调方法或者init 方法)前后进行回调处理,此后置处理器可以对bean进行任何动作,包括完全忽略回调,获取将bean包装为一个代理,有些spring aop基础设施类会实现BeanPostProcessor来提供代理包装逻辑!<br/>
spring会自动检测这些后置处理器并容器创建之前注册它们,记得前面说过当使用工厂方法在@Configuration类中加入一个后置处理器,尽量返回@BeanPostProcessor接口类型或者返回实现类的真实类型,否则可能无法通过类型检测,后置处理器需要运用在bean创建之时,所以必须提前初始化好,早期的类型检测是必要的!<br/>
1) 程序化注册后置处理器
推荐applicationContext自动检测后置处理器,但是可以在ConfigurableBeanFactory 使用addBeanPostProcessor 增加后置处理器,当需要在执行条件逻辑之前注册或者跨越容器体系复制后置处理器非常有用,程序化增加后置处理器不需要遵循Ordered接口,注册的顺序就标识了执行顺序,值得注意的是使用这种方式注入的后置处理器在自动检测之前,不管是否进行了显式顺序设置!
## 后置处理器以及AOP 自动代理
bean后置处理器的所有引用都会在启动的时候提前实例化,是应用上下文指定 的启动阶段一部分,所有的后置处理器将被注册(有序)并应用在所有后续的bean上,因为AOP自动代理是作为BeanPostProcessor本身实现的，所以BeanPostProcessor实例或它们直接引用的bean都不适合进行自动代理，因此没有编织的方面,对于这样的bean来说,你可能会得到一个消息,这是因为bean实例化太早,无法进行aop自动代理
```text
Bean someBean is not eligible for getting processed by all BeanPostProcessor interfaces (for example: not eligible for auto-proxying).
```
如果你的后置处理器和一个需要的bean使用自动装配或者@Resource进行了连接,这个时候可能会导致自动装配对候选匹配发现了不期待的bean,因此它们不适合自动代理或者其他的后置处理
如果您有一个用@Resource注释的依赖项，其中字段或设置器名称不直接与bean的声明名称相对应，并且不使用name属性，那么Spring将访问其他bean以按类型匹配它们。
这里也可以使用Groovy脚本注入bean,动态脚本支持查看[ Dynamic Language Support](https://docs.spring.io/spring-framework/docs/current/reference/html/languages.html#dynamic-language)<br/>
例如:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:lang="http://www.springframework.org/schema/lang"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/lang
        https://www.springframework.org/schema/lang/spring-lang.xsd">

    <lang:groovy id="messenger"
            script-source="classpath:org/springframework/scripting/groovy/Messenger.groovy">
        <lang:property name="message" value="Fiona Apple Is Just So Dreamy."/>
    </lang:groovy>

    <!--
    when the above bean (messenger) is instantiated, this custom
    BeanPostProcessor implementation will output the fact to the system console
    -->
    <bean class="scripting.InstantiationTracingBeanPostProcessor"/>

</beans>
```
然后可以通过容器获取这个messenger bean

### AutowiredAnnotationBeanPostProcessor
该实现随Spring发行版一起提供，并自动装配带注释的字段，setter方法和任意配置方法
### BeanFactoryPostProcessor 定制配置元数据
这是第二个扩展点,此接口和后置处理器类似,主要不同就是它定制配置元数据,对于此接口可以实现Ordered接口来标识运行的顺序<br/>
注意如果需要改变已经通过元数据创建的实例,你应该使用BeanPostProcessor 而不是使用此处理器,从技术上讲，可以在BeanFactoryPostProcessor中使用Bean实例（例如，通过使用BeanFactory.getBean() ），但这样做会导致过早的Bean实例化，从而违反了标准容器的生命周期，并且可能会造成副作用,例如可能不会被后置处理器处理,其他的性质和后置处理器没什么不同,例如当前容器创建的bean肯定只能通过当前容器的后置处理器处理!<br/>
后置处理器将自动被使用,并且spring提供了大量的预处理后置处理器来改变bean定义配置,例如PropertyOverrideConfigurer and PropertySourcesPlaceholderConfigurer,你也能够注入自定义的bean工厂后置处理器;<br/>
与后置处理器一样,你通常不会给懒加载设置BeanFactoryPostProcessors,如果没有bean引用Bean(Factory)PostProcessor,那么处理器将不会实例化,所以懒加载标志将被忽略,即使设置了<beans>的 default-lazy-init = true也会导致所有的Bean(Factory)PostProcessor被实例化!
### PropertySourcesPlaceholderConfigurer 进行类名替换(属性替换)
```xml
<bean class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">
    <property name="locations" value="classpath:com/something/jdbc.properties"/>
</bean>

<bean id="dataSource" destroy-method="close"
        class="org.apache.commons.dbcp.BasicDataSource">
    <property name="driverClassName" value="${jdbc.driverClassName}"/>
    <property name="url" value="${jdbc.url}"/>
    <property name="username" value="${jdbc.username}"/>
    <property name="password" value="${jdbc.password}"/>
</bean>
```
例如这段代码非常常见,使用PropertySourcesPlaceholderConfigurer导入资源进行属性名替换,它遵循Ant或者log4j以及JSP EL表达式风格!<br/>
随着spring2.5的推广，能够使用专用的(dedicate)标签导入属性占位符的资源,可以是逗号分隔的列表
```xml
<context:property-placeholder location="classpath:com/something/jdbc.properties"/>
```
并且如果它不能够发现指定的properties文件,那么它会检查spring环境中的属性以及普通的java 系统属性!<br/>
除此之外,你可以使用
```xml
<bean class="org.springframework.beans.factory.config.PropertySourcesPlaceholderConfigurer">
    <property name="locations">
        <value>classpath:com/something/strategy.properties</value>
    </property>
    <property name="properties">
        <value>custom.strategy.class=com.something.DefaultStrategy</value>
    </property>
</bean>

```
这种形式,通过设置properties来添加需要的属性,属性的替换发生在非懒加载的执行上下文的preInstantiateSingletons阶段进行,如果解析失败,bean创建会失败!
### PropertyOverrideConfigurer
也是一个bean工厂后置处理器,类似于前面的PropertySourcesPlaceholderConfigurer,但是不同于后者的是属性可以有默认值,如果说覆盖properties文件没有对应的某些bean 属性的条目,那么会使用默认的上下文定义;
请注意，bean定义不知道会被覆盖，因此从XML定义文件中不能立即看出正在使用覆盖配置器。如果有多个PropertyOverrideConfigurer实例为同一个bean属性定义了不同的值，则由于覆盖机制，最后一个实例将获胜,同时对于组合路径覆盖也是可以的,其实就是嵌套数据覆盖,比如:
tom.fred.bob.sammy=123,但是路径中间不能够为空！<br/>
指定的替代值始终是文字值。它们不会转换为Bean引用。当XML bean定义中的原始值指定bean引用时，此约定也适用,spring2.5开始可以使用指定的元素进行配置,如:
```xml
<context:property-override location="classpath:override.properties"/>
```
#### 自定义通过FactoryBean实例化的逻辑
能够实现org.springframework.beans.factory.FactoryBean接口作为对象的工厂,这个接口是通过插件形式加入到spring的容器实例化逻辑中,如果你有一个复杂的初始化代码能够比xml繁琐格式在java中更容易表达,那么你可以创建一个自己的FactoryBean,在这个类中写入你的复杂的初始化代码,然后将此类加入到容器中!<br/>
该FactoryBean 接口提供了三个方法:
1) 返回从工厂中创建的对象的实例,该实例可以是共享的,取决于该工厂是否返回单例或原型
2) 是否为单例
3) Class getObjectType() getObject返回的对象类型,如果null标识类型不知道!<br/>
此概念和接口在spring 中大量使用,并且在spring中使用getBean("myBean"),比如myBean那么就是该工厂生产的一个对象实例,如果加上&那么将获取的是工厂对象本身,如getBean("&myBean")!

