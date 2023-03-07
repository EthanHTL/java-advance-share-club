# Java 以及XML 混合方式实现Spring IOC 容器
####### Import注解,导入其他配置,从而实现通过一个入口装载所有bean
```java
@Configuration
public class ConfigA {

    @Bean
    public A a() {
        return new A();
    }
}

@Configuration
@Import(ConfigA.class)
public class ConfigB {

    @Bean
    public B b() {
        return new B();
    }
}
```
spring 4.3开始已经可以通过@Import导入一个@Component注解的类,对比AnnotationConfigApplicationContext.register方法,这非常有用,你能够避免组件扫描,通过使用少量配置的@Configuration 类作为入口去显式定义所有组件!

####### 在导入的@Bean定义上拦截依赖
在很多情况下,一个bean的依赖交叉于另一个配置类,当使用xml的时候,这不是一个问题,因为没有编译器可执行,你也能够声明ref="someBean"以及在容器初始化的过程之外相信spring会很好的工作,当使用@Configuration 类时候,java 编译器在配置模型中替换约束,限制,在这样的情况下引用其他bean需要受到java语法限制;
幸好,恰巧,解决这个问题简单,已经提到过@Bean方法可以有无数个参数去描述它的依赖选项,考虑一个demo:
```java
@Configuration
public class ServiceConfig {

    @Bean
    public TransferService transferService(AccountRepository accountRepository) {
        return new TransferServiceImpl(accountRepository);
    }
}

@Configuration
public class RepositoryConfig {

    @Bean
    public AccountRepository accountRepository(DataSource dataSource) {
        return new JdbcAccountRepository(dataSource);
    }
}

@Configuration
@Import({ServiceConfig.class, RepositoryConfig.class})
public class SystemTestConfig {

    @Bean
    public DataSource dataSource() {
        // return new DataSource
    }
}

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
    // everything wires up across configuration classes...
    TransferService transferService = ctx.getBean(TransferService.class);
    transferService.transfer(100.00, "A123", "C456");
}
```
还可以使用另一种方式通过使用autowired 或者@Value,请记住,确保你的依赖注入足够合理,比如不希望过早初始化,比如autowired失效,transacational失效,这些都是由于bean实例化过早,没有被对应的后置处理器增强,例如在AutowiredAnnotationBeanPostProcessor.之前初始化了一些@Bean,其次使用静态工厂方法注册后置处理器是最好的方式,不会初始化@Configuration配置类,就不会初始化其中的@Bean方法的bean;
```java
@Configuration
public class ServiceConfig {

    @Autowired
    private AccountRepository accountRepository;

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl(accountRepository);
    }
}

@Configuration
public class RepositoryConfig {

    private final DataSource dataSource;

    public RepositoryConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public AccountRepository accountRepository() {
        return new JdbcAccountRepository(dataSource);
    }
}

@Configuration
@Import({ServiceConfig.class, RepositoryConfig.class})
public class SystemTestConfig {

    @Bean
    public DataSource dataSource() {
        // return new DataSource
    }
}

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
    // everything wires up across configuration classes...
    TransferService transferService = ctx.getBean(TransferService.class);
    transferService.transfer(100.00, "A123", "C456");
}
```
spring4.3以后支持构造器注入,注意这里并不需要指定@Autowired,如果目标只定义一个构造器!
####### 全类型修饰导入的bean更容易导航
有些时候autowired只是让注入变得可行而已,但是有时可能会变得歧义，那么声明@Bean的具体类型是更加合适的!
其次还可以使用基于接口或者基于抽象类的@Configuration配置,这样能够使得配置低耦合,不会与特定的类型高耦合!
例如:
```java
@Configuration
public class ServiceConfig {

    @Autowired
    private RepositoryConfig repositoryConfig;

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl(repositoryConfig.accountRepository());
    }
}

@Configuration
public interface RepositoryConfig {

    @Bean
    AccountRepository accountRepository();
}

@Configuration
public class DefaultRepositoryConfig implements RepositoryConfig {

    @Bean
    public AccountRepository accountRepository() {
        return new JdbcAccountRepository(...);
    }
}
```
前面提到可以使用@Import标签逃避组件扫描,所以这里可恶意直接通过@Import导入ServiceConfig以及DefaultRepositoryConfig(这是具体的依赖项),这样的话,ServiceConfig和具体的DefaultRepositoryConfig低耦合!
####### 使用@Conditional 条件注入@Configuration 或者某一个Bean
例如@Profile注解,其本质也是通过一个实现了Conditional的接口的类来实现根据环境加载bean,ProfileCondition专注于profile上的value值!
####### 合并xml以及java配置
那么使用@ImportResource能够做到这样的策略!
AnnotationConfigApplicationContext 结合@ImportResource使用,或者ClassPathXmlApplicationContext结合注解扫描标签使用!
当没有使用组件扫描的时候,同样可以将bean作为一个bean element,但是需要开启<context:annotation-config/> 进行注解处理!
####### 环境抽象
在容器中分为两个关键点: 一个profiles 一个 properties
profile不用多数,执行一个运行环境,指定环境的所有bean definition都会自动注册到容器中,与profile角色相关的environment对象决定哪一个环境应该激活!
properties在每一个系统中都是非常重要的角色,他们可能来源有很多种,例如properties file,jvm 系统参数,system environment属性,jndi,servlet 上下文参数,以及ad-hoc 属性对象,以及map对象等等!这些属性可以被用户配置在组件、服务中,能够被解析,使用更加方便！
######## bean definition profiles
其本质就是可以在容器中根据不同环境注册不同的bean,环境一词意味着可能面对不同的用户,包含了许多种情况
* 例如在开发环境中使用针对于内存型数据库来工作,而不是在进行QA或生产时从JNDI查找相同的数据源
* 仅仅在部署一个应用到性能环境中注册一个监视基础设施;
* 仅仅在A消费者注册自定义的bean自定义实现,而不是在B中注册!
* 考虑一个测试应用中需要一个数据源,配置可能如下:
```java
@Bean
public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("my-schema.sql")
        .addScript("my-test-data.sql")
        .build();
}
```
如果转换到生产环境,直接通过生产环境应用服务器的JNDI文件夹注入了数据源,那么可能配置如下:
```java
@Bean(destroyMethod="")
public DataSource dataSource() throws Exception {
    Context ctx = new InitialContext();
    return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
}
```
如何去切换成为了一个问题,当然在spring中,这种解决方式有很多种,比如通常情况下,使用一个import 元素(<import/>)然后通过占位符例如${runEnvrionment},根据当前运行环境的属性值来解析配置文件的路径从而进行导入正确的配置文件!<br/>
但是spring提供了另一种简单的方式:
bean definition profiles进行处理
1) 使用@Profile
例如:
```java
@Configuration
@Profile("development")
public class StandaloneDataConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .addScript("classpath:com/bank/config/sql/test-data.sql")
            .build();
    }
}
```
or 生产环境下
```java
@Configuration
@Profile("production")
public class JndiDataConfig {

    @Bean(destroyMethod="")
    public DataSource dataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
    }
}
```
注意:
在@Bean方法中,你可以使用程序化的JNDI 查询,或者使用spring的JndiTemplate/JndiLocatorDelegate 去帮助查询或者通过前面例子中展示的生产环境下通过JNDI查询、加载数据库的使用方式(通过InitialContext),但是那个方式没有使用JndiObjectFactoryBean,那么这里需要里强制使用FactoryBean作为返回类型；<br/>
当然@Profile的value可以使用表达式实现复杂的环境指定
例如:
1) ! 标识非
2) & 标识 and
3) | 标识或者

如果同时使用& | 那么切记共同条件需要将加上(),例如
```xml
production & us-east | eu-central 
```
无效
```xml
production & (us-east | eu-central)
```
有效
对于在@Configuration或者@Component上使用的@Profile 会传播,如果此类上还使用了@Import导入了其他类的资源,否则该类以及@Import联系的资源都会跳过加载!<br/>
profile也能够用在@Bean上,这样通过不同的环境加载不同的bean,但是这里有一些特殊情况,例如如果工厂方法有着重载,那么所有的重载方法都需要有具体的profile,如果格式不一致(就是有些有Profile,有些没有),那么只有第一个声明了@Profile的工厂方法是有效的,其次对于构造器重载也是同理!<br/>
否则通过唯一的java方法名,通过@Bean的name属性去标识返回的类型是一样的,这种方式也是可以的,然后通过添加@Profile去添加不同环境下的@Bean;
####### xml bean definition profiles
对于xml,profile存在于<beans>的属性上
```xml
<beans profile="development"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xsi:schemaLocation="...">

    <jdbc:embedded-database id="dataSource">
        <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
        <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
    </jdbc:embedded-database>
</beans>
<beans profile="production"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
</beans>
```
重写方式如上,
当然也可以写在同一个xml中:
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <!-- other bean definitions -->

    <beans profile="development">
        <jdbc:embedded-database id="dataSource">
            <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
            <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
        </jdbc:embedded-database>
    </beans>

    <beans profile="production">
        <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
    </beans>
</beans>
```
在beans中使用beans,spring.xsd提供了约束,为了提高灵活性,而不会使其混乱!
例如:
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <!-- other bean definitions -->

    <beans profile="production">
        <beans profile="us-east">
            <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
        </beans>
    </beans>
</beans>
```
由于xml形式不支持profile的表达式,但是同样是支持否定!,and,或者这三种形式,上面的形式就展示了
production和us-east同时激活,那么就导入此bean,注意这里使用嵌套元素而不是&;
####### 激活一个profile
这非常简单,
ctx.getEnvironment().setActiveProfiles("development");
同样也可以使用spring.profiles.active属性,那么这个属性可以通过jvm,系统变量,servlet 上下文参数指定,甚至使用JNDI ,在测试环境中(这里指的是集成测试,可以使用@ActiveProfiles注解)
####### 默认profile
可以通过setDefaultProfiles()设置默认环境,也可以使用spring.profiles.default

###### PropertySource 抽象
spring的环境抽象提供了一个搜索属性功能(能够在可配置的属性资源层级中查找);
```java
ApplicationContext ctx = new GenericApplicationContext();
Environment env = ctx.getEnvironment();
boolean containsMyProperty = env.containsProperty("my-property");
System.out.println("Does my environment contain the 'my-property' property? " + containsMyProperty);
```
通过这种方式去询问当前环境是否包含对应的属性,spring会在PropertySource对象集合中查找它,例如A propertySource可能是一个简单的k-v键值对集合属性资源,并且spring的StandardEnvironment 已经配置了两个PropertySource对象,一个是jvm系统属性资源,另一个是系统环境属性资源!<br/>
注意: 默认属性资源都是StandardEnvironment所管理,在单机应用中使用;StandardServletEnvironment 将会搜集可选的默认属性资源例如servlet config以及servlet context parameters;
它也能够可选的启用JndiPropertySource;
最终,如果使用的是StandardEnvironment,如果你查找的是一个my-property,如果存在jvm属性或者系统环境变量属性,那么将返回true!<br/>
由于查询是层级式的,所以系统属性具有更高的优先级,并且属性不会合并只会被优先级高的属性覆盖!<br/>
覆盖级别如下:
1) ServletConfig parameters (if applicable — for example, in case of a DispatcherServlet context)

2) ServletContext parameters (web.xml context-param entries)

3) JNDI environment variables (java:comp/env/ entries)

4) JVM system properties (-D command-line arguments)

5) JVM system environment (operating system environment variables)

由于这是可配置的,所以你可以自定义属性(集成到搜索中),你可以实现并实例化自己的PropertySource并增加它到PropertySources,例如:
```java
ConfigurableApplicationContext ctx = new GenericApplicationContext();
MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
sources.addFirst(new MyPropertySource());
```
请注意,在这里我们的属性具有更高的优先级!<br/>
MutablePropertySources暴露大量的方法能够允许你精确收集property sources属性!

####### @PropertySource 加入属性
这个注解提供了一种方便并且声明式的机制去增加一个PropertySource到Spring环境中,假如你有一个app.properties,你可以此注解去导入一个@PropertySource
```java
@Configuration
@PropertySource("classpath:/com/myco/app.properties")
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setName(env.getProperty("testbean.name"));
        return testBean;
    }
}
```
这样返回的testBean 返回名字就是属性名!
同时此注解可以使用${}解析已经存在于环境中的属性值,然后以此加载其他propertySource资源;
```java
@Configuration
@PropertySource("classpath:/com/${my.placeholder:default/path}/app.properties")
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setName(env.getProperty("testbean.name"));
        return testBean;
    }
}
```
当然注意到这里,如果my.placeholder不存在,那么将会使用默认值default/path,如果没有默认值,将爆出IllegalArgumentException ;<br/>
注意:
@PropertySource是可重复的,根据java 8的规范, 但是所有的@PropertySource需要声明在相同的级别上,要么在@Configuration配置类上,或者作为一个元素据注解标注在一个自定义注解上!
直接使用以及混合元数据注解不推荐,直接使用可能更有效!
####### 语句中的占位符解析
由于历史原因,占位符能够被jvm属性或者操作系统属性进行解析,但是目前已经发生变化,因为Environment 抽象已经通过容器进行集成,通过它能够非常容易的进行占位符的路由解析,也许你想要配置你想要的解析程序,你能够改变属性查询的优先级问题,或者直接移除jvm属性或者操作系统环境变量属性(这里仅仅只是不让这些属性加载到容器中),
你也可进行属性混合,例如:
前面提到切换不同的环境加载不同的配置,可以使用import,那么这就是一个例子:
```xml
<beans>
    <import resource="com/bank/service/${customer}-config.xml"/>
</beans>
```
这里可以根据属性去加载不同的bean配置!
####### 注册一个loadTimeWeaver
LoadTimeWeaver 用来动态翻译(转换)类并将他们加载到jvm中;
为了启用load-time weaving,你可以使用@EnableLoadTimeWeaving 到@Configuration中,例如:
```java
@Configuration
@EnableLoadTimeWeaving
public class AppConfig {
}
```
在xml中,使用方式类似:
```xml
<beans>
    <context:load-time-weaver/>
</beans>
```
一旦为applicationContext中配置了loadtimeWeaving,如果容器中有一个bean实现了LoadTimeWeaverAware接口,从而接收一个引用load-time weaver实例,这非常有用,在合并spring JPA支持时,需要load-time weaving 去对JPA 类进行比秒的转换,参考LocalContainerEntityManagerFactoryBean 了解更多;
####### 容器的可选能力
org.springframework.beans.factory包提供了管理并收集bean的一些基本功能,包括了程序化的方式,org.springframework.context存在一个ApplicationContxt接口,它扩展了Beanfactory接口,除此之外还扩展了其他面向于应用框架风格的接口提供丰富的功能,许多人使用完全声明式applicationContext,甚至不需要程序化的创建它,相反可以使用ContextLoader去自动加载实例化一个ApplicationContext作为一个普通JavaEE web应用程序启动的一部分!<br/>
为了增强BeanFactory面向框架风格的功能,context包提供了以下功能:
1) 访问国际化消息,通过MessageSource接口
2) 访问资源,例如URLs以及files,通过ResourceLoader 接口
3) 事件发布,实现了ApplicationListener接口的bean,通过ApplicationEventPublisher接口进行使用;
4) 加载层级上下文,让每一个专注于自己的那一层,例如web层的应用上下文,并通过HierarchicalBeanFactory接口进行使用!

##### 使用过MessageSource进行国际化
applicationContext继承了MessageSource接口,提供了国际化的能力,spring也提供了HierarchicalMessageSource接口,它能够层级化解析消息,这些接口提供了Spring影响消息解析的基础,提供的方法如下:
1) String getMessage(String code, Object[] args, String default, Locale loc) 这将从MessageSource中抓取一个消息,当没有消息从指定的地区发现时会使用默认的消息,任何一个参数都是一个可替换的值,MessageFormat通过标准库进行提供!
2) String getMessage(String code, Object[] args, Locale loc) 本质上和前一个方法相同,但是不同点在于没有默认message,会爆发异常NoSuchMessageException
3) String getMessage(MessageSourceResolvable resolvable, Locale locale)此方法包含了一个MessageSourceResolvable,包含了所有的属性<br/>

当一个applicationContext被加载了,会自动 在上下文中搜索MessageSource,这个bean的名称必须是messageSource,前面提到的所有方法最终都会代理到这个messageSource,如果没有发现,applicationContext会尝试从父级容器去寻找相同名称的bean,如果它找到了,那就会使用这个bean,作为MessageSource,如果ApplicationContext不能发现这个message的资源，那么默认会使用 一个DelegatingMessageSource,只是为了能够调用前面接口所定义的方法!<br/>
spring提供了三种MessageSource实现,ResourceBundleMessageSource,ReloadableResourceBundleMessageSource以及StaticMessageSource,它们都实现了HierarchicalMessageSource为了能够内嵌消息传递,这个staticMessageSource很少使用,并且提供了程序化的方式去增加消息到source上,例如:
```java
<beans>
    <bean id="messageSource"
            class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basenames">
            <list>
                <value>format</value>
                <value>exceptions</value>
                <value>windows</value>
            </list>
        </property>
    </bean>
</beans>
```
这个例子假设你有三个资源束,分别叫做format,exceptions,windows定义在类路径上.
任意一个请求去处理一个消息时通过基于jdk的方式去通过ResourceBundle对象解析消息,假设你的资源束文件如下:
```properties
   # in format.properties
    message=Alligators rock!
```
```properties
   # in exceptions.properties
    argument.required=The {0} argument is required.
```
下面展示如何在程序中使用MessageSource的功能!
```java
public static void main(String[] args) {
    MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
    String message = resources.getMessage("message", null, "Default", Locale.ENGLISH);
    System.out.println(message);
}
```
最终打印Alligators rock!<br/>
为了总结,MessageSource是被定义到beans.xml中的,它存在于你类路径下的根目录,messageSource bean definition 通过它的basenames 属性引用了大量的资源束,这在前面的例子中有体现;
由于将这三个文件作为一个列表设置到了basenames属性中,那么如果文件存在于类路径根目录,那么它们将被分别的被调用!<br/>
例如下面一个例子展示了如何通过将参数传递给消息查找,这个参数已经被转换为一个String 对象并插入到已经寻找的消息的占位符上!
```xml
<beans>

    <!-- this MessageSource is being used in a web application -->
    <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basename" value="exceptions"/>
    </bean>

    <!-- lets inject the above MessageSource into this POJO -->
    <bean id="example" class="com.something.Example">
        <property name="messages" ref="messageSource"/>
    </bean>

</beans>
```
```java
public class Example {

    private MessageSource messages;

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public void execute() {
        String message = this.messages.getMessage("argument.required",
            new Object [] {"userDao"}, "Required", Locale.ENGLISH);
        System.out.println(message);
    }
}
```
最后execute的结果就是The userDao argument is required.那么可以知道传递的参数就是为了替换占位符;
除了国际化(i18n),spring 有着各种各样的MessageSource实现作为标准的JDK 资源束(ResourceBundle)遵循相同的locale解析方案以及回滚规则,前面的example可以做出改变,如果你想针对(en-GB)地区解析消息,你能够(分别的)创建一个名称叫做format_en_GB.properties的文件,exceptions_en_GB.properties以及windows_en_GB.properties<br/>
通常来说,地区解析是被当前的应用的周围环境所管理,如何通过手动指定来解析对应地区的消息:
```properties
# in exceptions_en_GB.properties
argument.required=Ebagum lad, the ''{0}'' argument is required, I say, required.
```
```java
public static void main(final String[] args) {
    MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
    String message = resources.getMessage("argument.required",
        new Object [] {"userDao"}, "Required", Locale.UK);
    System.out.println(message);
}
```
最终结果:Ebagum lad, the 'userDao' argument is required, I say, required.<br/>
注意:  MessageSourceAware可以获得一个MessageSource的引用,当MessageSource被创建的时候,此接口能够获得通知;<br/>
除了ResourceBundleMessageSource之外,spring提供了一个ReloadableResourceBundleMessageSource这个变种支持相同的资源束文件格式,但是它比标准的JDK 标准ResourceBundleMessageSource实现更加灵活,除此之外,它允许从任意的一个spring 资源位置(不仅仅是类路径)读取文件并且支持对bundle属性文件的重载(可能会进行缓存),查看ReloadableResourceBundleMessageSource查看更多;
##### 标准的、自定义的事件
事件处理在applicationContext中是一个特性,通过applicationEvent类以及ApplicationListener接口进行操作,如果实现了事件监听器并且配置到当前上下文,一旦一个ApplicationEvent事件发送到ApplicationContext中,那么事件监听器将会得到通知,最终,本质上来说这是一个标准的观察者设计模式;<br/>
从spring4.2开始,事件架构有重大提升以及提供了一个基于注解的模式同样能够拥有发布事件的能力(这个对象不需要集成ApplicationEvent),当这个对象被发布,将自动包装此对象!<br/>
内置事件:
1) ContextRefreshedEvent 当applicationContext被初始化或者刷新了就进行发布(例如ConfigurableApplicationContext 的refresh方法调用),这里初始化的标识所有的bean已经加载完成,后置处理器检测完成且激活,单例被提前实例化完成,并且ApplicationContext对象已经能够进行使用,同样上下文没有关闭,一个refresh能够触发多次,前提是所选的ApplicationContext实际上支持这种"热"刷新,举个例子,XmlWebApplicationContext支持热刷新,但是GenericApplicationContext不支持;
2) ContextStoppedEvent 当applicationContext通过调用ConfigurableApplicationContext接口的stop方法停止,这里stopped意味着所有的LifeCycle bean 接受了一个显式的停止信号,停止了同样可以使用start()调用启动;
3) ContextStartedEvent 当调用ConfigurableApplicationContext的start方法并在应用容器启动完成时发布,这里的started意味着所有的LifeCycle bean 接收一个显示的启动信号,通常,这个信号是为了在显式停止之后重新启动bean,但是在容器启动时,当组件没有配置自动开始,那么这也能够触发(例如: 尚未在初始化时启动的组件);
4) ContextClosedEvent
    当ApplicationContext通过调用ConfigurableApplicationContext.close方法并在容器关闭的时候发布,或者通过jvm的关闭回调钩子触发,这里的close意味着单例将会被摧毁,一般容器关闭,生命周期走到尽头他不能够刷新或者重新启动(refresh or restarted);
5) RequestHandledEvent
    一个基于web的事件告诉所有bean,http请求已经服务完成,这个事件在请求完成之后进行发布,这个事件仅仅在SpringMvc的时候才是有效的;
6) ServletRequestHandledEvent
    一个RequestHandledEvent子类(但是拥有servlet相关上下文的信息)<br/>
发布事件很简单,不用多说,例如:
```java
public class EmailService implements ApplicationEventPublisherAware {

    private List<String> blockedList;
    private ApplicationEventPublisher publisher;

    public void setBlockedList(List<String> blockedList) {
        this.blockedList = blockedList;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void sendEmail(String address, String content) {
        if (blockedList.contains(address)) {
            publisher.publishEvent(new BlockedListEvent(this, address, content));
            return;
        }
        // send email...
    }
}
```
由于实现了applicationEventPublisherAware接口,那么就拥有了发布事件的能力,这个事件将在应用上下文中进行传播;<br/>
接收处理事件也很简单:
```java
public class BlockedListNotifier implements ApplicationListener<BlockedListEvent> {

    private String notificationAddress;

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
    }

    public void onApplicationEvent(BlockedListEvent event) {
        // notify appropriate parties via notificationAddress...
    }
}
```
需要实现ApplicationListener并泛型参数指定监听那种事件,这意味着类型安全,避免了向下转型,你能够随意的注册你的事件监听器,并且此种类型监听器是同步的,意味着发布事件在进行处理的过程中是同步发生的,直到所有的事件监听器处理完毕,这样同步和单线程方式的好处是: 当一个监听器监听到事件,他会在时间发布器的事务上下文进行处理,如果对于事件发布有其他的策略,可以参考ApplicationEventMulticaster接口以及SimpleApplicationEventMulticaster获取更多信息;
例如xml形式的处理:
```xml
<bean id="emailService" class="example.EmailService">
    <property name="blockedList">
        <list>
            <value>known.spammer@example.org</value>
            <value>known.hacker@example.org</value>
            <value>john.doe@example.org</value>
        </list>
    </property>
</bean>

<bean id="blockedListNotifier" class="example.BlockedListNotifier">
    <property name="notificationAddress" value="blockedlist@example.org"/>
</bean>
```
邮件服务可以给上述配置的bean definition三个邮箱发送消息,但是通过blockedListNotifier配置应该给谁发送消息,那么最后获取到事件,就会触发发送邮件!<br/>
<b>spring 事件处理机制是设计来对当前相同上下文的bean之间的交互,对于更加复杂的企业级集成需要,单独管理的spring integration项目对著名的spring 编程模型的轻量级、面向模式、事件驱动架构的构建提供了完整支持</b>
####### 基于注解形式的事件监听器
使用@EventListener进行事件接收!
```java
public class BlockedListNotifier {

    private String notificationAddress;

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
    }

    @EventListener
    public void processBlockedListEvent(BlockedListEvent event) {
        // notify appropriate parties via notificationAddress...
    }
}
```
只需要放置在特殊方法上,不针对于类;只要实际事件类型在其实现层次结构中解析通用参数，也可以通过通用类型来缩小事件类型;
还有一种一个方法监听多个事件的形式:
```java
@EventListener({ContextStartedEvent.class, ContextRefreshedEvent.class})
public void handleContextStart() {
    // ...
}
```
并且也支持条件执行,使用此注解的condition属性(能够使用spEL表达式),例如:
```java
@EventListener(condition = "#blEvent.content == 'my-event'")
public void processBlockedListEvent(BlockedListEvent blEvent) {
    // notify appropriate parties via notificationAddress...
}
```
这样当blEvent的内容是一个my-event的时候,才进行执行!
对于可以使用的SPEL表达元数据,如下所述:
1) Event  实际的ApplicationEvent  使用形式  #root.event or event
2) arguments array   当前方法上的参数数组   #root.args 或者args 也可以使用arg[0]获取某一个特定的参数
3) argument name  方法参数的名称,由于某些原因,名称可能不可用(有些时候,编译字节码没有调试信息),单例的参数使用#a<#arg>语法获取,<#arg>语法是参数的下标,从0开始  #blEvent or #a0 或者使用 #p0 or #p<#arg><br/>
如果你需要在事件处理完毕之后,发布一个事件作为处理完成的结果,那么可以修改此方法的返回类型,那么将会发布返回类型事件!
```java
@EventListener
public ListUpdateEvent handleBlockedListEvent(BlockedListEvent event) {
// notify appropriate parties via notificationAddress and
// then publish a ListUpdateEvent...
}
```
注意: <b>异步事件监听器不支持
</b>
当然您还可以返回一个需要发布事件的集合!
####### Asynchronous Listeners
处理异步事件,只需要在加上一个@Async注解!
```java
@EventListener
@Async
public void processBlockedListEvent(BlockedListEvent event) {
    // BlockedListEvent is processed in a separate thread
}
```
但是有以下限制:
1) 如果抛出异常,不会传播到调用者,查看AsyncUncaughtExceptionHandler查看详情;
2) 异步事件监听器不支持将处理结果作为一个事件再进行发布,如需发布,手动applicationEventPublisher进行发布
####### 监听器的执行顺序
只需要使用@Order注解进行排序即可
```java
@EventListener
@Order(42)
public void processBlockedListEvent(BlockedListEvent event) {
    // notify appropriate parties via notificationAddress...
}
```
####### Generic Events
很多情况下某一种事件下有着多种小情况,这个时候使用泛型事件就很合理,
例如为了处理EntityCreatedEvent事件,比如Person实体创建的时候,
```java
@EventListener
public void onPersonCreated(EntityCreatedEvent<Person> event) {
    // ...
}
```
由于类型擦除，只有在触发的事件解析了事件侦听器所依据的通用参数（即类似PersonCreatedEvent的类扩展EntityCreatedEvent <Person> {…}）时，此方法才起作用。
再某些情况下,统一的结构形式非常枯燥无味,这种情况下,你可以实现ResolvableTypeProvider 去指示框架在运行时环境提供类型,例如:
```java
public class EntityCreatedEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {

    public EntityCreatedEvent(T entity) {
        super(entity);
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getSource()));
    }
}
```
这样能够进行运行时类型解析;
这样不仅能够工作在ApplicationEvent并且任意的对象作为事件发送也会被处理!

####### 方便的访问低成本的资源

为了最佳使用以及理解applicationContext,你应该熟悉Resource抽象,一个应用上下文也是一个ResourceLoader,它能够加载资源对象,一个资源对象其实就是一个JDK的URL类的丰富版本,事实上Resource其实就是包装了一个URL的对象,一个Resource能够包含一个low-level资源(一种透明的机制),包括类路径、文件系统位置、能够通过标准的URL位置标识的资源、或者其他变种；如果资源路径是一个简单的路径（没有包含任何前缀），那么文件资源的前缀由上下文决定;<br/>
你能够配置一个bean部署到应用上下文中,但是这个类需要实现ResourceLoaderAware接口,因为在应用上下文初始化过程中会自动进行回调同时将它作为一个Resource进行传递,如果你需要访问静态资源,你可以暴露一个Resource的属性,它们同样和其他Properties一样能够被注入,你能够指定这些Resource属性作为一个简单的字符串路径并且依赖于自动转换技术将这些文本转换到实际的Resource对象，当Resource被部署的时候!<br/>
这个资源路径可以应用在ApplicationContext的构造器参数上(类型是一个字符串),简单形式中,它会根据指定的上下文实现而合适的配置并信任,例如ClassPathXmlApplicationContext 会配置并信任一个类路径,这里和前面所说的没有配置指定前缀的Resource一致,由当前上下文决定资源的路径,你也能够指定前缀来标识资源的位置,无论上下文是那种类型!
##### 应用启动堆栈
应用上下文为了管理spring应用的生命周期并围绕组件提供了丰富的编程模型,因此复杂的应用能够拥有等价的复杂组件图视以及启动阶段!<br/>
通过指定的指标堆栈(追踪)应用启动步骤能够帮助你理解当前启动阶段用了多少时间,并且也能够作为一种方式去理解整个应用上下文的生命周期;<br/>
AbstractApplicationContext（及其子类）通过ApplicationStartup进行检测，该应用程序收集有关各个启动阶段的StartupStep数据：
1) 应用上下文生命周期(包扫描,配置类管理)
2) beans 生命周期(实例化,智能初始化,后置处理)
3) application event 处理<br/>
例如:
```java
// create a startup step and start recording
StartupStep scanPackages = this.getApplicationStartup().start("spring.context.base-packages.scan");
// add tagging information to the current step
scanPackages.tag("packages", () -> Arrays.toString(basePackages));
// perform the actual phase we're instrumenting
this.scanner.scan(basePackages);
// end the current step
scanPackages.end();
```
通过调用start开启一个步骤,然后添加步骤标签,最后end结束,统计了运行时间等信息!<br/>
一个阶段有多个步骤,每个阶段只记录一次,这些记录的信息能够被收集,展示,分析(通过特殊的工具),对于完成的存在的启动步骤,可以查看[dedicated appendix section](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#application-startup-steps)<br/>
默认的ApplicationStartup实现是一个 no-op 变种(非循环),有着最小的开销,默认情况下是没有任何指标(在应用启动的时候)被收集,spring 框架附带了java flight recorder的实现来跟踪启动步骤进行展示: FlightRecorderApplicationStartup,
为了使用这个变量,你必须在创建好它之后立即注册到应用上下文中!<br/>
开发者能够使用ApplicationStartup基础设施(如果它们提供了自己的AbstractApplicationContext子类,或者它们希望搜集更多精确的信息)<br/>
注意: <b>ApplicationStartup仅仅在应用启动时进行使用,核心容器也能使用),他仅仅是一种基本指标信息的展示,绝不是java profiler(Java  解析器)以及像<br/>
[Micrometer](https://micrometer.io/)
之类的指标库替代品!</b>
##### 对于web应用程序的applicationContext实例化
前面提到ContextLoader能够作为Web应用程序启动的一部分,所以除了用这种方式,也能够使用编程式触发容器实例化,首先使用contextLoader:
```java
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>/WEB-INF/daoContext.xml /WEB-INF/applicationContext.xml</param-value>
</context-param>

<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```
还记得环境抽象吗? 能够扫描当前servletContext的上下文参数,所以spring依赖于contextConfigLocation参数来发现启动引导配置文件,默认情况下如果不存在就是/WEB-INF/applicationContext.xml,如果存在那么也可以是多个,通过逗号,分号,空格进行分割,在应用上下文搜索资源文件时将会使用,同时也可以使用Ant-style 匹配模式,例如/WEB-INF/*Context.xml 这个意思不说也懂!
####### 将spring应用程序部署为javaEE RAR文件
将应用上下文部署为javaEE rar也是可行的,包装上下文以及所有必须的bean类以及类库到Javaee RAR部署单元中,这等价于引导一个单独的ApplicationContext(仅仅在Java EE环境托管)它能够访问JavaEE 服务器的能力,RAR 部署是一种更加原始的在无War文件方式的部署场景,事实上,一个没有任何HTTP入口点的WAR文件，该文件仅用于引导Java EE环境中的Spring ApplicationContext;<br/>
RAR 部署是一个理想的没有http 端点入口的应用上下文(但是能够存在消息端点以及定时任务)等等,在当前上下文的beans能够使用application 服务资源(例如JTA事务管理器以及JNDI绑定的JDBC数据源实例以及JMS ConnectionFactory实例以及能够注册平台JMX server)的特性,全部可以通过spring 标准的事务管理以及JNDI,JMX支持的能力代理,应用组件能够和应用服务器的JCA workManager交互(但是是通过Spring的TaskExecutor抽象进行的);
查看[SpringContextResourceAdapter](https://docs.spring.io/spring-framework/docs/5.3.7/javadoc-api/org/springframework/jca/context/SpringContextResourceAdapter.html)查看(执行在RAR部署中的配置详细)<br/>
下面有一个简单的部署流程:
1) 打包所有的应用类到RAR文件中(这是一种的标准JAR文件,但是是一种不同的文件扩展),所有需要的库Jar都需要放在RAR 归档的根目录下,增加一个MEATA-INF/ra.xml部署文件(查看[SpringContextResourceAdapter](https://docs.spring.io/spring-framework/docs/5.3.7/javadoc-api/org/springframework/jca/context/SpringContextResourceAdapter.html)))了解更多,以及一个配置bean definitions 文件入口(通常是 META-INF/applicationContext.xml)
2) 将此RAR文件部署到应用服务器部署目录中;<br/>
注意:
<b>一个rar部署单元通常是自我包含的,它们不需要暴露组件到外面,就算是同一个应用中的其他模块也不需要,和基于RAR的applicationContext交互通常发生在通过JMS目标它和其他模块共享,一个基于rar的applicationContext也是如此,例如定时调度的某些jobs或者需要对文件系统的某些新创建的文件做出响应,如果它需要从外面同步访问,它可以暴露出RMI端点,能够被其他应用模块(当前机器上)使用</b>
###### beanFactory
BeanFactory api 提供基本能力(ioc),它指定了大多和spring其他模块集成以及和第三方框架集成的使用情况,默认DefaultListableBeanFactory实现是一个在高水平GenericApplicationContext容器的关键代理;<br/>
BeanFactory以及相关的接口(例如BeanFactoryAware,InitializeBean,DispoableBean)是一个非常重要的集成点,对于其他框架来说;它并不需要任何注解或者反射,它允许非常有效的容器和组件之间进行交互,应用级别的bean 也许能够使用相同的回调接口(但是更喜欢使用声明式的依赖注入进行替代),或者也可以通过注解或者编程式配置!<br/>
注意核心BeanFactory api 级别以及DefaultListableBeanFactory实现并没有对配置格式以及人组件注解进行规定,所有的行为都来自扩展(例如XmlBeanDefinitionReader以及AutowiredAnnotationBeanPostProcessor)并操作共享的BeanDefinition对象作为一个核心的元数据表现层,这也是为什么spring容器如此灵活以及扩展性极强的本质!
####### beanfactory 以及applicationContext之间的关系
BeanFactory和ApplicationContext容器级别之间的区别以及对引导的影响,大多数情况下应该使用applicationContext,除非你又更好的理由,通常GenericApplicationContext或者子类AnnotationConfigApplicationContext是一个普通实现(对于自定义引导),spring核心容器的主要入口(对于大多数目的):加载配置文件,触发类路径扫描,程序化注入bean 定义以及注解的类,以及在5.0之后注入功能性bean 定义;<br/>
因为applicationContext包含了BeanFactory的所有功能,它比BeanFactory更加推荐,期待需要完全控制bean处理,在application中(通常是GenericApplicationContext实现),一些种类的bean能够通过形式进行检测(例如通过bean name,或者bean type,甚至通过post-processor),普通的DefaultListableBeanFactory不能识别任何特殊的bean;<br/>
对于许多扩展的容器特性,例如注解处理以及aop代理,这个BeanPostProcessor扩展点是本质,如果你仅仅使用一个简单的DefaultListableBeanFactory,那么post-processor不能够被检测以及被激活,这个情况就会变得很疑惑,因为实际上你的bean配置没有错误,相反在这种情况下你能够需要容器通过额外的启动进行完全引导!<br/>
ioc容器和ApplicationContext的特性:
<table>
<thead>
<tr>
<th>Feature</th>
<th>BeanFactory</th>
<th>ApplicationContext</th>
</tr>
</thead>
<tbody>
<tr>
<td>Bean instantiation/wiring</td>
<td>Yes</td>
<td>Yes</td>
</tr>
<tr>
<td>Integrated lifecycle management</td><td>No</td><td>Yes</td>
</tr>
<tr>
<td>Automatic BeanPostProcessor registration</td><td>No</td><td>Yes</td>
</tr>
<tr><td>Automatic BeanFactoryPostProcessor registration</td><td>No</td><td>Yes</td>
</tr>
<tr><td>Convenient MessageSource access (for internalization)</td><td>No</td><td>Yes</td>
</tr>
<tr>
<td>Built-in ApplicationEvent publication mechanism</td><td>No</td><td>Yes</td>
</tr>
</tbody>
</table>
所以从上面可以发现使用应用上下文必然要轻松很多!<br/>
当然你同样可以使用DefaultListableBeanFactory注册后置处理器并使用它!<br/>

```java
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
// populate the factory with bean definitions

// now register any needed BeanPostProcessor instances
factory.addBeanPostProcessor(new AutowiredAnnotationBeanPostProcessor());
factory.addBeanPostProcessor(new MyBeanPostProcessor());

// now start using the factory
```
仅仅需要调用postProcessBeanFactory即可激活后置处理器!
```java
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
reader.loadBeanDefinitions(new FileSystemResource("beans.xml"));

// bring in some property values from a Properties file
PropertySourcesPlaceholderConfigurer cfg = new PropertySourcesPlaceholderConfigurer();
cfg.setLocation(new FileSystemResource("jdbc.properties"));

// now actually do the replacement
cfg.postProcessBeanFactory(factory);
```
像这样形式太啰嗦了,应用上下文已经封装好了一切,能够在幕后配置好这一切,特别是在企业级应用启动时需要使用后置处理器等扩展功能就很关键!<br/>
前面说到,默认自定义扩展使用AnnotationConfigApplicationContext ,因为它有着所有的普通注解的后置处理器并可能在配置注解之后引入其他后置处理器,例如 @EnableTransactionManagement,作为spring注解配置模型的抽象层,这些后置处理器的概念都是容器上下文的内部细节!


