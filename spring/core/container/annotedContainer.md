# 基于容器的注解配置
spring 提供了许多注解进行配置,例如@Required @Named @Inject(但是需要加入javax.inject的包),以及@Autowired(和自动装配有一样的功能,并且能更加细腻化的配置),详细信息查看[相关部分](relevant section)<br/>
<font color=red>注解的注入在xml注入之前,所以xml的bean注入可能会覆盖之前的配置!</font><br/>
在xml配置中,可以通过 <context:annotation-config/> 开启注解相关的配置支持(它主要是为了注入注解处理的后置处理器,当然你可以单独注册)!<br/>
1) ConfigurationClassPostProcessor
2) AutowiredAnnotationBeanPostProcessor
3) CommonAnnotationBeanPostProcessor
4) PersistenceAnnotationBeanPostProcessor
5) EventListenerMethodProcessor
#### @Required
可以应用在setter上标识是一个必须的属性,如果没有就报空异常,spring5.1不在建议使用这个,对于必须的我们建议使用它作为构造器参数,(或者通过InitializingBean.afterPropertiesSet()进行自定义配置@PostConstruct 或者bean 属性setter)
#### @Autowired
被@AutoWired的注解的构造器将被优先使用,其次可以使用在setter上、字段上!
对于通过xml配置或者基于类路径扫描的组件类型是可以提前知道的,但是对于@Bean工厂方法将必须指定类型来足够表达它,同样也可以运用到数组字段上,例如:
```java
public class MovieRecommender {

    @Autowired
    private MovieCatalog[] movieCatalogs;

    // ...
}

public class MovieRecommender {

    private Set<MovieCatalog> movieCatalogs;

    @Autowired
    public void setMovieCatalogs(Set<MovieCatalog> movieCatalogs) {
        this.movieCatalogs = movieCatalogs;
    }

    // ...
}
```
通过对bean实现Ordered接口或者@Order注解或者标准的@Priority注解来指定存储的顺序,否则顺序就是注入对象到容器中的顺序!<br/>
你能够在@Been方法上使用@order或者类级别上,顺序能够被感知到并且不会影响单例的启动顺序,通过依赖关系和@Dependson共同决定!
由于@Priority 不能运用在任何方法上,那么针对于@Bean方法来说可以通过@Order以及@Primary来决定优先级!

```java
public class MovieRecommender {

    private Map<String, MovieCatalog> movieCatalogs;

    @Autowired
    public void setMovieCatalogs(Map<String, MovieCatalog> movieCatalogs) {
        this.movieCatalogs = movieCatalogs;
    }

    // ...
}
```
注入需要注意的是key是相关bean的名称,值是对应类型的对象!<br/>
例如你可以通过设置autowired的required的属性为false标识它不是必须的!
同时在构造方法上,如果存在多个被注解的构造方法,那么只能有一个方法为required =true的标识默认,主要的方法,如果仅仅只有一个构造方法,那么即使不加上autowired,那么也将使用这个构造函数,同时支持使用optional 包裹目标依赖,如果不存在,那么将注入一个空的!<br/>
spring 5.0支持使用@Nullable来表示允许为空!
### 使用Qualifier 或者使用泛型形式进行类型选择
```java
@Autowired
private Store<String> s1; // <String> qualifier, injects the stringStore bean

@Autowired
private Store<Integer> s2; // <Integer> qualifier, injects the integerStore bean
@Configuration
public class MyConfiguration {

    @Bean
    public StringStore stringStore() {
        return new StringStore();
    }

    @Bean
    public IntegerStore integerStore() {
        return new IntegerStore();
    }
}
```
对于列表,map,数组都是一样的!
#### 使用自定义CustomAutowireConfigurer
即使它本身没有被@Quailfier注释,也能够通过CustomAutowireConfigurer加入自定义修饰符!
```java
<bean id="customAutowireConfigurer"
        class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer">
    <property name="customQualifierTypes">
        <set>
            <value>example.CustomQualifier</value>
        </set>
    </property>
</bean>
```
#### AutowireCandidateResolver决定自动装配候选:
1) 每一个bean definition 的autowire-candidate决定,
2) 在beans上的default-autowire-candidates属性决定
3) @Qualifier以及注册的自定义限定符!
对于多个bean定义来说,拥有prmary 属性为true的bean definition 将会选择!
#### @Resource
将通过CommonAnnotationBeanPostProcessor进行感知,进行处理,如果使用JNDI进行查询解析,需要配置一个[SimpleJndiBeanFactory](https://docs.spring.io/spring-framework/docs/5.3.6/javadoc-api/org/springframework/jndi/support/SimpleJndiBeanFactory.html),
#### @value
通过@PropertySource("classpath:application.properties")可以导入一个资源文件,前面提到表达式通过PropertySourcesPlaceholderConfigurer进行处理;默认情况下spring提供了一个这样的bean,但是如果你需要提供自己的,例如:
```java
@Configuration
public class AppConfig {

     @Bean
     public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
           return new PropertySourcesPlaceholderConfigurer();
     }
}
```
由于configuration的特殊性，你需要设置此方法为static,并且由于该bean本身就是一个后置处理器,所以不能和上下文绑定,导致其他bean过早实例化;
然后value可以使用SPEL解析表达式,其次It is also possible to use methods like setPlaceholderPrefix, setPlaceholderSuffix, or setValueSeparator to customize placeholders.
可以设置占位符前缀,或者占位符后缀,或者设置值分隔符去定制占位符!<br/>
对于springBoot来说,默认从application.properties and application.yml读取属性!<br/>
对于多个逗号分割的数据,可以自动转换为字符串数组,这是spring默认内建支持的一种形式,允许简单类型转换!
<font color=red>默认情况下,spring会使用后置处理器联合Conversion Service进行@Value的表达式转换为目标类型</font>
```java
@Configuration
public class AppConfig {

    @Bean
    public ConversionService conversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new MyCustomConverter());
        return conversionService;
    }
}
```
由于SPEL表达式可以动态计算,那么以下例子也是合理的:
```java
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("#{systemProperties['user.catalog'] + 'Catalog' }") String catalog) {
        this.catalog = catalog;
    }
}
//或者更加复杂的
@Component
public class MovieRecommender {

    private final Map<String, Integer> countOfMoviesPerCatalog;

    public MovieRecommender(
            @Value("#{{'Thriller': 100, 'Comedy': 300}}") Map<String, Integer> countOfMoviesPerCatalog) {
        this.countOfMoviesPerCatalog = countOfMoviesPerCatalog;
    }
}
```
<font color=red>特别注意: java9以后,@Resource, the @PostConstruct and @PreDestroy注解被剥离出jdk标准库,所以需要导入javax.annotation-api,如果你使用的jdk不是1.8</font>


### @component 以及模板化的注解
@Component, @Service, and @Controller ,@Repository
### 使用元数据注解以及复合注解
例如@RestController就是一个复合注解!
除此之外,复合注解可以重新声明元注解的属性并定制,例如你想暴露一个元注解属性的子集时这特别有用,例如@SessionScope,硬编码指定作用域名为session,同时仍然允许自定义proxyMode,且自定义代理模式就是元注解Scope的proxyMode属性;
比如其他的例子:

```java
@Service
@SessionScope
public class SessionScopedService {
    // ...
}
//或者
@Service
@SessionScope(proxyMode = ScopedProxyMode.INTERFACES)
public class SessionScopedUserService implements UserService {
    // ...
}
```
For further details, see the [Spring Annotation Programming Model wiki](Spring Annotation Programming Model) page.

<font color="red>为了扫描这些注解,你可以使用ComponentScan,同时使用逗号或者分号或者空格分割basePackage,全部写入都basePackages属性上,等价于xml形式的 </font>
```xml
<context:component-scan base-package="org.example"/>
<!--此标签将隐式启动<context:annotation-config/>-->
```
扫描类路径包需要在类路径中存在相应的目录条目。使用Ant构建JAR时，请确保不要激活JAR任务的仅文件开关。另外，在某些环境中，基于安全策略可能不会公开类路径目录。例如，JDK 1.7.0_45及更高版本上的独立应用程序（这需要在清单中设置“受信任的库”。）请参见https://stackoverflow.com/questions/19394570/java-jre-7u45-breaks-classloader-getresources）。
在JDK 9的模块路径（Jigsaw）上，Spring的类路径扫描通常可以按预期进行。但是，请确保将组件类导出到模块信息描述符中。如果您期望Spring调用您的类的非公共成员，请确保它们是“打开的”（也就是说，它们在模块信息描述符中使用了opens声明而不是export声明）<br/>
当启用了组件扫描将注册两个后置处理器,	You can disable the registration of AutowiredAnnotationBeanPostProcessor and CommonAnnotationBeanPostProcessor by including the annotation-config attribute with a value of false.
#### 开启组件扫描过滤器进行处理
[查看官网获取更多](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-resource-annotation)
一些使用方式:
```java
@Configuration
@ComponentScan(basePackages = "org.example",
        includeFilters = @Filter(type = FilterType.REGEX, pattern = ".*Stub.*Repository"),
        excludeFilters = @Filter(Repository.class))
public class AppConfig {
    ...
}
//The following listing shows the equivalent XML:

```
```xml
<beans>
    <context:component-scan base-package="org.example">
        <context:include-filter type="regex"
                expression=".*Stub.*Repository"/>
        <context:exclude-filter type="annotation"
                expression="org.springframework.stereotype.Repository"/>
    </context:component-scan>
</beans>
``` 
注意: 
<font color=red>
You can also disable the default filters by setting useDefaultFilters=false on the annotation or by providing use-default-filters="false" as an attribute of the <component-scan/> element. This effectively disables automatic detection of classes annotated or meta-annotated with @Component, @Repository, @Service, @Controller, @RestController, or @Configuration.
</font>
####  定义bean元数据,通过components
在spring4.3开始,bean工厂方法可以声明一个InjectionPoint类型的参数,或者更加精确的一个类型(DependencyDescriptor)去访问当前请求注入点;
对于其他作用域，factory方法仅在给定作用域中看到触发创建新bean实例的注入点（例如，触发创建懒惰单例bean的依赖项）。在这种情况下，可以将提供的注入点元数据与语义一起使用,这个选择对于原型bean可能超有用!
最后，一个类可以为同一个bean保留多个@Bean方法，这是根据运行时可用的依赖项来安排使用多个工厂方法的安排。这与在其他配置方案中选择“最贪婪”的构造函数或工厂方法的算法相同：在构造时选择具有最大可满足依赖关系数量的变量，类似于容器在多个@Autowired构造函数之间进行选择的方式。

#### 自动检测名字组件
当bean名称没有设置时,可以spring默认会使用BeanNameGenerator进行bean名称生成,首字母小写,可以实现BeanNameGenerator进行改写默认的命名规则!对于多个包下的相同名称的类名(发生了命名冲突,则需要定义一个BeanNameGenerator),spring5.2.3开始FullyQualifiedAnnotationBeanNameGenerator 默认将设置为全路径修饰类名,但你可以改变这种策略!
```xml
<beans>
    <context:component-scan base-package="org.example"
        name-generator="org.example.MyNameGenerator" />
</beans>
```
```java
@Configuration
@ComponentScan(basePackages = "org.example", nameGenerator = MyNameGenerator.class)
public class AppConfig {
    // ...
}
```
#### 为自动检测的组件提供作用域
这很简单,默认情况时一个单例,通过@Scope注解进行指定即可!
@Scope注解的好处在于没有bean定义继承的概念,同时不存在类级别的继承,直白说它于具体的类配置相关,不存在继承!<br/>
如果你想使用编程式而不是注解式的方式,你可以继承ScopeMetadataResolver接口进行处理,并在配置扫描器的时候提供它!
```java
@Configuration
@ComponentScan(basePackages = "org.example", scopeResolver = MyScopeResolver.class)
public class AppConfig {
    // ...
}
```
```xml
<beans>
    <context:component-scan base-package="org.example" scope-resolver="org.example.MyScopeResolver"/>
</beans>
```
同时在扫描某些类的同时为其生成代理非常重要,因为作用域不同的时候,存在不同的运用场景,这里有一些可能的值:no, interfaces, and targetClass,于是乎:
```java
@Configuration
@ComponentScan(basePackages = "org.example", scopedProxy = ScopedProxyMode.INTERFACES)
public class AppConfig {
    // ...
}
```
```xml
<beans>
    <context:component-scan base-package="org.example" scoped-proxy="interfaces"/>
</beans>
```
前面说到可以@Qualifier注解对组件进行标注,形成更加细腻化的配置,然后在xml形式的bean definition来说,可以设置qualifier元素或者meta元素(在不存在qualifier的前提下回退到meta)来定义组件元数据,这样就可以对指定的bean进行选取,同时前面提到可以通过@Qualifier元注解开发自己的自定义注解进行元数据定义,注意元数据是提供在每一个实例上,而不是每一个类上!
例如:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="MovieQualifier">
            <attribute key="format" value="VHS"/>
            <attribute key="genre" value="Action"/>
        </qualifier>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="MovieQualifier">
            <attribute key="format" value="VHS"/>
            <attribute key="genre" value="Comedy"/>
        </qualifier>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <meta key="format" value="DVD"/>
        <meta key="genre" value="Action"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <meta key="format" value="BLURAY"/>
        <meta key="genre" value="Comedy"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

</beans>
```

其他bean的类配置如下:
```java
public class MovieRecommender {

    @Autowired
    @MovieQualifier(format=Format.VHS, genre="Action")
    private MovieCatalog actionVhsCatalog;

    @Autowired
    @MovieQualifier(format=Format.VHS, genre="Comedy")
    private MovieCatalog comedyVhsCatalog;

    @Autowired
    @MovieQualifier(format=Format.DVD, genre="Action")
    private MovieCatalog actionDvdCatalog;

    @Autowired
    @MovieQualifier(format=Format.BLURAY, genre="Comedy")
    private MovieCatalog comedyBluRayCatalog;

    // ...
}
```
