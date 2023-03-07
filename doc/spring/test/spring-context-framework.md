# spring 对集成测试的支持 以及对单元测试的最佳实践
spring 团队提倡测试驱动开发(TDD), spring 团队将控制反转使用在单元测试或者集成测试上让测试更加容易(通过预设某些setter 方法 以及在类上使用合适的构造器使得它们更容易在测试中
相互关联 而无需配置服务定位器注册比奥以及类似的结构 ...

# spring 测试介绍
测试是企业级软件开发的不可分割的一部分,通过ioc原理增加的数据到单元测试以及spring框架对集成测试的支持好处 ..

# 单元测试
构成应用程序的pojo能够通过JUnit或者TestNG测试,只需要通过new 操作符实例化,而无需spring 或者任何其他的容器.. 通过使用mock 对象(通过结合其他有价值的测试技术)去进行隔离测试 ..
如果遵循spring的架构推荐,那么将导致你的代码组件化以及更加清晰的层次 促进单元测试更加容易 ..
举个例子,你能够通过填充或者吗模拟dao 或者仓库结构来测试服务层对象,无需在单元测试期间访问持久化数据 。。。

单元测试通常运行非常的快,因此这里不需要配置任何运行时基础设施.. 强调真正的单元测试作为开发方法的一部分可以提高您的生产力.
你也许不需要这一部分去帮助你编写高效的单元测试(对于你的基于ioc的应用) .. 对于某些单元测试场景,spring 框架提供了mock 对象以及 测试支持类,了解它们并使用 ..

## mock 对象
spring 包括了大量的包 - 针对于mock
1. environment
2. jndi
3. servlet api
4. spring web reactive
### 环境
org.springframework.mock.env包 包含了有关Environment 以及 PropertySource mock实现的抽象..  MockEnvironment 以及 MockPropertySource对于依赖于指定环境属性的
容器外的测试开发来说非常有用 ..
### JNDI
org.springframework.mock.jndi 包包含了jndi spi的一部分实现, 你能够用来为测试套件配置简单的jndi 环境或者标准单机应用中配置jndi环境 .. 例如jdbc DataSource 实例在测试代码中
使用相同的 jndi名称绑定到jndi环境 - 如同它们在Jakarta EE容器中一样 .. 你能够重用应用代码以及配置到测试场景中而无需修改 ..
> 这个jndi的mock支持从spring 5.2开始不推荐,更推荐完整的第三方库解决方案([simple-jndi](https://github.com/h-thurow/Simple-JNDI))

### Servlet api
org.springframework.mock.web包包含了广泛的Servlet api mock 对象集合 - 能够测试 web 上下文,controllers 以及 filters ... 这些mock对象目的是与Spring web mvc 框架使用,
相比于动态mock对象来说更加方便(例如 [EasyMock](https://easymock.org/) 或者 其他的Servlet api mock 对象(例如 [MockObjects](http://www.mockobjects.com/))
> 从spring framework 6.0开始,在org.springframework.mock.web包中的mock对象基于Servlet 6.0 api ...

spring mvc 测试框架基于mock Servlet api 对象提供了对spring mvc的集成测试(查看[MockMvc](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework))

### spring web reactive
org.springframework.mock.http.server.reactive包包含了在WebFlux应用中使用的ServerHttpRequest以及 ServerHttpResponse的mock 实现. 
org.springframework.mock.web.server包包含了一个 mock ServerWebExchange(依赖于mock 请求和响应对象)..

同时MockServerHttpRequest 以及 MockServerHttpResponse 同时继承相同的抽象基类(作为server特定的实现)并且在它们之间共享行为 ..
举个例子,mock 请求是不可变的 - 一旦创建,但是你可以从ServerHttpRequest中调用mutate()方法取创建一个可以修改的实例 ..
为了mock 响应取正确的实现写约定 并且返回一个写完成句柄(也就是Mono<Void>),它默认使用具有cache().then()的Flux,这会缓存数据并使它在测试中能够
进行可用进行断言 .. 应用能够设置一个自定义的写函数(例如,为了测试一个无穷流)..
[WebTestClient](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#webtestclient) 基于mock 请求
以及响应去提供webflux 应用的测试而无需http 服务器. 客户端还可以用于对正在运行的服务器进行端到端测试。

## 单元测试支持的类
spring 包含了大量的类能够帮助进行单元测试,它们分为两类
1. 通用测试工具
2. spring mvc 测试工具

### 通用测试工具集合
org.springframework.test.util 包包含了各种常见目的的工具类可以在单元测试或者集成测试中使用 .
- AopTestUtils 是aop相关的工具方法集合,可以用来隐藏在一层或者多层Spring代理上获取对底层最终目标对象. 例如如果你有一个配置的bean 作为动态mock  - 通过
使用例如EasyMock 或者Mockito, 并且它被Spring proxy 包装了,你也许需要直接访问底层的mock去在它之上配置期望并执行校验,对于Spring核心的Aop 工具类,查看AopUtils 以及 AopProxyUtils ..
ReflectionTestUtils 是基于反射的工具方法集合, 你能够在测试场景中使用 ,去改变一个常量的值,设置一个非public字段,执行一个非public setter方法,或者执行一个非public配置或者生命周期回调,
当你的测试代码有如下使用情况下:
  - orm 框架(例如jpa / hibernate) 纵容private / protected 字段访问 -对应域实体中的属性的public setter方法 ..
  - spring对注解的支持(例如各种依赖注入注解), 提供对private / protect字段 / setter方法 / 配置方法的依赖注入..
  - 使用例如@PostConstruct 以及 @PreDestroy 进行生命周期回调方法标识 ..
TestSocketUtils 是一个简单的工具类能够发现本地上可用的TCP端口(为了在集成测试场景中使用)
  > TestSocketUtils 能够在集成测试中使用-例如在随机可用端口上启动外部服务器的场景 .. 然而这些工具并不能保证给定端口的后续可用性,因此它是不可信任的 .. 替代TestSocketUtils
  > 去为服务器发现一个可用的本地端口,更推荐的方式是依赖于服务器的能力去在随机临时可用的端口上启动(根据它的选择或者由操作系统分配), 为了和此服务器交互,你应该查询当前服务器使用的端口 ..

### spring mvc 测试工具类
org.springframework.test.web 包包含了ModelAndViewAssert, 能够与JUnit,TestNG以及其他测试框架合并使用在单元测试中 - 处理spring mvc ModelAndView 对象 ..
> 单元测试spring mvc Controllers
> 为了测试作为pojo的spring mvc Controller, 使用 ModelAndViewAssert 连同 MockHttpServletRequest 以及 MockHttpSession,以及其他来自spring Servlet api mocks的类 ..
> 要结合 Spring MVC 的 WebApplicationContext 配置对 Spring MVC 和 REST Controller 类进行全面的集成测试，请改用[Spring mvc Test]框架替代 ..


# 3. 集成测试
主要是能够执行某些集成测试而不需要部署到应用服务器或者连接到其他企业级基础设施上 ..
1. 正确的关联到你的Spring Ioc容器上下文
2. 使用JDBC或者ORM工具进行数据访问,包括SQL语句的正确性,Hibernate查询,JPA Entity映射以及其他 ..
3. 对于Spring集成测试支持包含了以下方面：
   * jdbc 测试支持
   * spring 测试上下文支持
   * webTestClient
   * MockMvc
   * Testing Client Applications
   * Annotations

##3.1 集成测试的目标是
1. 管理测试之间的Spring Ioc容器缓存 ..
2. 提供test装置实例的依赖注入
3. 提供对集成测试合适的事务管理
4. 提供了Spring特定的基类(帮助开发者编写集成测试) ..

###3.1.1 上下文管理以及缓存
spring 测试上下文框架提供了一致的spring应用上下文加载 以及 webApplicationContext(且包含其他上下文的缓存) ...
上下文缓存很重要. \
主要是启动时间是一个大的问题,不是因为Spring本身的消耗,是因为由spring容器花时间实例化的对象 ..
例如,50 - 100个hibernate 映射文件的项目可能需要花10 - 20 秒加载映射文件,这会增加每一次运行的测试的时间(这会导致
整个集成测试更慢，减少开发者的效率) ... \
测试类通常声明要么一个xml 资源路径数组/ groovy配置元数据（通常是类路径) .. 这些东西用来配置应用上下文..
默认来说一旦加载，配置的应用上下文将会对测试重用,这样仅仅每一个测试套件的整体时间会增加一次(因为仅仅是配置消耗）,后续的测试执行将会更快,
这里的测试套件指的是运行在相同jvm中的所有测试 ... 例如来自ant / maven / gradle构建的所有测试(为给定项目或者模块运行的) ... \
非这种情况（例如需要应用上下文重载等测试可能需要在下一次测试之前进行应用上下文重构建 以及配置的刷新)..
这种情况无能为力 ..(无法加速测试时间)

### 3.1.2 测试装置的依赖注入
也就是spring 框架提供给我们的依赖注入

### 3.1.3 事务管理
默认回滚,但是你可以提供注解改变事务提交策略 ..(@Commit) ...
事务管理,注意: 仅仅支持 PlatformTransactionManager,也就是非响应式的事务管理器 ..
如果使用React类型的,没有任何效果 ...
一个事务性测试方法将会在测试方法执行完毕之后默认进行回滚,但是你可以通过Commit 进行事务提交...

### 3.1.4 集成测试支持的类
spring TestContext 框架提供了各种抽象支持类能够简化集成测试的编写.. 这些基础测试类提供了已知的回调到测试框架,同样包括方便的实例变量
以及方法,这让你能够访问:
- ctx,应用上下文
- JdbcTemplate 执行sql 查询数据, ....
除此之外,你可以创建你自己的自定义的,应用世界的超类 - 包含实例变量和方法 - 特定于你的项目的实现 ..
支持类如下所述[support class](./spring-context-framework.md#512-)


## 4. jdbc 测试支持
### 4.1 JdbcTestUtils
包含了简化标准的数据库测试场景的工具方法,特别是,JdbcTestUtils 提供了以下的静态工具方法 ..
详情查看 javadoc ..
- countRowsInTable(..)
- countRowsInTableWhere(..)
- delete...

AbstractTransactionalJUnit4SpringContextTests 以及 AbstractTransactionalTestNGSpringContextTests 提供了各种代理到
JdbcTestUtils方法的方法 ..
###4.2  内嵌数据库
spring-jdbc模块提供了配置和启动一个内嵌的数据库 .. 这能够在集成测试中进行数据库交互 ...
查看[内嵌数据库支持](../core/new/data.access/dao-support.md#394-)  以及与内嵌数据进行测试数据访问逻辑  详情了解 ..


## 5. Spring TCF(testContext framework)
这个框架主要提供了通用，注解驱动单元 以及集成测试支持(无感知测试框架) .. 并且非常重视约定大于配置,包含了
许多默认值能够让你通过基于注解的配置进行覆盖 .. \
除了通用的测试基础设施,TCF同样提供了对JUnit4,JUnit5 以及 TestNG的隐式支持.. Spring提供了抽象支持类 .. \
因此Spring提供了一个自定义的Junit Runner 以及 自定义的JUnit Rules - 针对Junit4 并且为JUnit5增加了
自定义的Extension让你能够编写基于 POJO的测试类 .. \
POJO测试类不需                                                                                下                                                        要扩展特定的类体系,例如 abstract 支持类 .. \
根据需要了解相关内容,例如 如果你不对测试框架感兴趣,或者不扩展你自己的监听器 或者自定义loader,我们可以直接了解
如何定义配置并启动集成测试(例如上下文管理，依赖注入，事务管理),支持的类 以及注解支持部分 ..

### 关键抽象
框架核心由TestContextManager 类和 TestContext ,TestExecutionListener 以及 SmartContextLoader 接口组成 ..\
测试上下文管理器将会为每一个测试类创建(例如,所有JUnit5 的单个测试类的所有测试方法),测试上下文管理器,最终会管理当前测试的\
的上下文(TestContext) ,TestContextManager 同样会更新TestContext的状态（根据测试进度并代理到
TestExecutionListener 实现)这能够检测实际的测试执行 - 通过(依赖注入，管理事务以及其他所提供功能) .. \
一个SmartContextLoader 负责加载一个ApplicationContext - 为给定的测试类加载 .. \
查看javadoc 以及 Spring 测试套件了解更多信息以及各种实现的示例 ...

![img_1.png](img_1.png)

### 测试上下文
测试上下文封装了当前正在运行测试的上下文(不需要关心使用的实际测试框架)并且提供上下文管理 以及缓存支持(
为它负责的测试实例),这个测试TestContext 同样代理到 SmartContextLoader 去加载一个应用上下文(如果请求) ..
### 测试上下文管理器
这个管理器是一个进入Spring TCF的入口并且它负责管理单个TestContext并且 会触发事件到每一个注册的TestExecutionListener\
在已经定义好的测试执行点 ..
- 在任何特殊测试框架的"before class" / "before all"方法之前
- 测试示例的后置处理
- 在特殊测试框架的"before" / "before each" 之前
- 在测试启动之后且测试方法执行之前
- 测试方法执行之后但是在结束之前
- 在特殊的测试框架的"after" / "after each"的方法之后
- 在任何特殊的测试框架的"after class" / "after all"方法之后

### TestExecutionListener
这个监听器定义了一些api 用来交互由 TestContextManager发布的的测试执行事件(只要监听器注册到
TestContextManager上)
### 上下文加载器
ContextLoader 是一个策略接口用来加载由Spring 测试上下文框架所管理的 一个集成测试的应用上下文 .. \
你应该实现SmartContextLoader 而不是此接口去提供组件类，激活bean 定义profiles,测试属性资源，\
上下文体系以及WebApplicationContext支持 .. \
SmartContextLoader 是一个ContextLoader接口的扩展 取代原有的最小的ContextLoader SPI..
特别是,一个SmartContextLoader能够选择处理资源位置，组件类，或者上下文初始化器 . \
因此,一个SmartContextLoader 能设置激活的bean definition profiles 以及它所加载的上下文中的测试属性资源(test propertySource)..\

Spring提供了以下的实现:
- DelegatingSmartContextLoader
   默认加载器之一,它內部代理到一个AnnotationConfigContextLoader或者 GenericXmlContextLoader或者GenericGroovyXmlContextLoader \
   依赖于为测试类声明的配置或者默认位置或者默认配置类的呈现情况,Groovy支持仅当Groovy出现在类路径上才启用 ..
- WebDelegatingSmartContextLoader
   默认加载起之一,它內部代理到一个AnnotationConfigWebContextLoader,一个GenericXmlWebContextLoader或者 GenericGroovyXmlWebContextLoader \
   一个web ContextLoader 仅仅当@WebAppConfiguration 出现在配置类上才启用.. Groovy 支持同上 ..
- AnnotationConfigContextLoader
   从组件类中加载一个标准的ApplicationContext ..
- AnnotationConfigWebContextLoader 
   从组件类这能够加载一个WebApplicationContext ...
- GenericGroovyXmlContextLoader
  从资源路径上加载标准的ApplicationContext(要么通过Groovy脚本或者XML配置文件)
- GenericGroovyXmlWebContextLoader
   从.... 记载一个WebApplicationContext ..
- GenericXmlContextLoader
   从xml资源位置上加载标准的应用上下文
- GenericXmlWebContextLoader
   从xml资源位置上加载WebApplicationContext..

## 引导TCF
Spring TestContext Framework 内部的默认配置足以满足所有常见用例。然而,有些时候开发团队或者第三方框架\
可能会改变默认的ContextLoader,实现一个自定义的TestContext或者ContextCache,增加ContextCustomizerFactory的\
以及TestExecutionListener实现的默认集合 .. \
对于底层TCF如何操作,Spring提供了引导策略 ..,TestContextBootstrapper 定义了引导TestContext框架的SPI . \
一个TestContextBootstrapper 将会由TestContextManager 使用去加载当前测试的TestExecutionListener 实现 \
并且构建它所管理的TestContext,你能够配置自定义的引导策略(为测试类或者测试类体系) - 通过使用@BootstrapWith或者\
直接或者元注解的形式 ..,如果一个引导器没有显式通过@BootstrapWith 配置,要么 DefaultTestContextBootstrapper或者 
WebTestContextBootstrapper将会被使用,依赖于@WebAppConfiguration的出现 ... \
因此TestContextBootstrapper SPI 可能会在未来发生改变(适应新的需求),我们强烈建议实现者不要直接实现这个接口,相反扩展\
AbstractTestContextBootstrapper 或者它具体的子类之一进行替代 ..

## TestExecutionListener 配置
Spring 提供了以下的TestExecutionListener 实现(默认注册的),顺序如下：
- ServletTestExecutionListener 为WebApplicationContext 配置Servlet api .. mock ..
- DirtiesContextBeforeModesTestExecutionListener 
   处理"before" 模式下的@DirtiesContext注解 
- ApplicationEventsTestExecutionListener
   提供对ApplicationEvents 的支持
- DependencyInjectionTestExecutionListener
   提供了测试实例的依赖注入
- DirtiesContextTestExecutionListener
   处理 "after"模式下的 @DirtiesContext 注解
- TransactionalTestExecutionListener
   提供默认回滚语义的事务性测试执行
- SqlScriptsTestExecutionListener
   通过@Sql注解运行配置的SQL 脚本 ..
- EventPublishingTestExecutionListener
   派发测试执行事件到测试的应用上下文中(这里是测试执行事件,不同于应用事件)
### 默认的测试执行监听器实现的 自动发现
通过spring spi 实现自动发现,将全局自动配置的 测试执行监听器想要使用到整个测试套件上,那么 \
相比单个测试上添加默认和自定义的测试执行监听器来说要方便的很多 ... 通过SpringFactoriesLoader\
实现TestExecutionListener 实现发现,通过在META-INF/spring.factories文件中配置即可 ..
### 测试执行监听器的实现顺序
默认通过order注解或者 Ordered接口 ...

### 默认的和自定义测试执行器的合并
通过为@TestExecutionListeners的属性MergeMode.MERGE_WITH_DEFAULT 和默认的监听器进行合并 .. \
```java
@ContextConfiguration
@TestExecutionListeners(
    listeners = MyCustomTestExecutionListener.class,
    mergeMode = MERGE_WITH_DEFAULTS
)
class MyTest {
    // class body...
}
```
## 应用事件
从spring framework5.3.3开始,TestContext框架支持记录发布在applicationContext中的应用事件,
这样就能够根据在测试中断言某些事件 .. 在单个测试中的所有发布的事件能够有效的通过ApplicationEvents \
Api 获取(你能够像java.util.Stream)那样处理 .. \
为了在测试中使用ApplicationContext ..,我们应该这样做:
1. 确保你的测试类式通过@RecordApplicationEvents注解或者元注解 ..
2. 确保ApplicationEventsTestExecutionListener 已经注册,注意到,这个监听器默认已经注册,但是有些情况
   你需要手动注册(例如你没有注册默认的测试监听器 ..)
3. 通过@Autowired 注解ApplicationEvents 类型的字段 并且在测试中或者生命周期方法使用它(
  例如JUnit5中的@BeforeEach / @AfterEach方法)
   - 当个使用Junit5的扩展时,你也许可以在测试中或者生命周期方法中声明一个ApplicationEvents类型的参数,又或者进行依赖注入 ..
   
以下的测试类使用了JUnit5的SpringExtension 并使用AssertJ去断言已经发布事件的类型断言(当执行一个Spring管理的组件的方法时 ..)
```java
@SpringJUnitConfig(/* ... */)
@RecordApplicationEvents 
class OrderServiceTests {

    @Autowired
    OrderService orderService;

    @Autowired
    ApplicationEvents events; 

    @Test
    void submitOrder() {
        // Invoke method in OrderService that publishes an event
        orderService.submitOrder(new Order(/* ... */));
        // Verify that an OrderSubmitted event was published
        long numEvents = events.stream(OrderSubmitted.class).count(); 
        assertThat(numEvents).isEqualTo(1);
    }
}
```
这个示例很简单,能够了解执行的方法中发布了多少个这种类型的应用事件 ..

## 测试执行事件
EventPublishingTestExecutionListener 从spring 5.2开始引入去提供一种额外的方式去替代实现自定义的 \
TestExecutionListener.. 在测试的ApplicationContext的组件能够监听以下由EventPublishingTestExecutionListener发布的事件 ..
- BeforeTestClassEvent
- PrepareTestInstanceEvent
- BeforeTestMethodEvent
- BeforeTestExecutionEvent
- AfterTestExecutionEvent
- AfterTestMethodEvent
- AfterTestClassEvent

这些事件能够被各种原因进行消费，例如重设mock beans 或者 跟踪测试执行 .. 消费测试执行事件的一个好处是相比 \
实现一个自定义的TestExecutionListener来说,测试执行事件能够被当前测试中的ApplicationContext中的任何Spring bean \
进行消费,并且这样的bean能够直接从依赖注入中收益或者ApplicationContext的其他特性收益 ..,作为对比,一个TestExecutionListener
将不是一个ApplicationContext中的bean ...
> 这个EventPublishingTestExecutionListener默认已经被注册,但是仅仅在应用上下文已经加载之后才会发布事件 .. \
> 之前加载的应用上下文将没有任何用处 ..
> 因此,BeforeTestClassEvent 将不会发布直到ApplicationContext 已经被其他的TestExecutionListener 加载 ..
> 举个例子,默认的 TestExecutionListener 实现集合是注册的,一个BeforeTestClassEvent将不会发布(如果第一个测试类使用了特殊的测试ApplicationContext) \
> 但是一个BeforeTestClassEvent 将会在后续的相同测试套件中的测试类中发布(如果使用相同的测试应用上下文，因为上下文已经加载 - 当后续的测试类运行的时候) \
> 只要上下文不会从ContextCache中移除(通过@DirtiesContext 或者 最大尺寸 抛弃策略) ..
> 如果你想要确保BeforeTestClassEvent总是针对每一个测试类进行发布,你也许注册一个 TestExecutionListener 去在beforeTestClass回调中加载ApplicationContext .. \
> 并且此监听器必须注册在 EventPublishingTestExecutionListener ..
> 并且类似的,@DirtiesContext 被用来从上下文缓存中移除ApplicationContext(当给定测试类中的最近的测试方法执行完毕) \
> AfterTestClassEvent 那么这个测试类将不可能接收到这个事件(因为只有被加载的上下文才能够接收到此事件) ..

为了监听测试执行事件, Spring bean 可以选择实现 ApplicationListener接口,除此之外,监听器方法能够通过@EventListener注册并且 \
配置去监听一个或者多个事件类型... 由于这种方式很方便,以下注解增加了@EventListener注解作为元注解 .. 简化测试执行事件监听器的注解 ..
@BeforeTestClass

@PrepareTestInstance

@BeforeTestMethod

@BeforeTestExecution

@AfterTestExecution

@AfterTestMethod

@AfterTestClass

## 异常处理
默认情况下一个测试执行事件监听器抛出异常会导致传播到底层的测试框架,如果是同步的,没有任何问题,但是
如果是一个异步的执行事件监听器抛出了异常,那么这个异常将不会传递给底层的测试框架 ... 
本质上是因为异步异常处理,它

## 上下文管理


### 5.7  测试装置的依赖注入
当你使用DependencyInjectionTestExecutionListener(这是默认配置的), 你的测试实例的依赖将会自动从你使用@ContextConfiguration或者其他相关注解配置的应用上下文中
寻找bean 进行注入 .. 你能够使用setter 注入或者字段注入或者任选其一,依赖于你选择的是那种注解并且你是是否在方法或者字段上放置注解 ..
如果你使用Junit Jupiter,你能够可选的使用构造器注入(查看[使用SpringExtension 进行依赖注入]()), 本质上使用SpringExtension 进行依赖注入很简单,
因为它本质上实现了Junit Jupiter的ParameterResolver 扩展api, 能够对测试构造器 /测试方法 以及测试生命周期回调方法进行依赖注入... \
为了和Spring的基于注解的注入支持一致,你可以使用Spring的@Autowired 注解或者来自JSR-330的 @Inject 注解进行字段或者setter 注入 ..
> 当你使用不是Junit Jupiter的测试框架,TestContext框架将不会参与到测试类的实例化,因此对构造器使用@Autowried / @Inject注解将没有任何效果 ..
> 尽管字段注入是在生产代码中不建议的,但是字段注入在测试代码中非常的自然,差异的理由是，你永远不会直接实例化你的测试类. 因此这不再需要在测试类中执行公共的构造方法或者setter 方法 ..
> 应该直接字段注入即可搞定,不需要通过方法调用来实现相同目的 ..

如果你不想使用依赖注入功能到测试实例中, 那就不需要使用依赖注入注解 .. 或者你能够禁用依赖注入 - 通过使用@TestExecutionListeners 进行显式的配置测试类并
省略 DependencyInjectionTestExecutionListener.class .. 或者与默认的监听器列表进行合并 .. \
考虑测试 HibernateTitleRepository 类的场景,如前面的目标部分所述 ..下面的列出的代码说明了在字段上或者setter方法上使用@Autowired .. 应用上下文的配置列在所有示例代码之后..

> 如下的依赖注入行为并不是特定于JUnit Jupiter ... 相同的DI 技术也能够与任何受支持的测试框架结合使用 ..
> 以下的示例调用了各种断言方法,它们是来自 Assertions的方法...

```java
@ExtendWith(SpringExtension.class)
// specifies the Spring configuration to load for this test fixture
@ContextConfiguration("repository-config.xml")
class HibernateTitleRepositoryTests {

    // this instance will be dependency injected by type
    @Autowired
    HibernateTitleRepository titleRepository;

    @Test
    void findById() {
        Title title = titleRepository.findById(new Long(10));
        assertNotNull(title);
    }
}
```
或者 setter注入
```java
@ExtendWith(SpringExtension.class)
// specifies the Spring configuration to load for this test fixture
@ContextConfiguration("repository-config.xml")
class HibernateTitleRepositoryTests {

    // this instance will be dependency injected by type
    HibernateTitleRepository titleRepository;

    @Autowired
    void setTitleRepository(HibernateTitleRepository titleRepository) {
        this.titleRepository = titleRepository;
    }

    @Test
    void findById() {
        Title title = titleRepository.findById(new Long(10));
        assertNotNull(title);
    }
}
```
上下文配置如下:
```java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- this bean will be injected into the HibernateTitleRepositoryTests class -->
    <bean id="titleRepository" class="com.foo.repository.hibernate.HibernateTitleRepository">
        <property name="sessionFactory" ref="sessionFactory"/>
    </bean>

    <bean id="sessionFactory" class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
        <!-- configuration elided for brevity -->
    </bean>

</beans>
```
> 注意,如果你从spring提供的测试基类进行继承 - 并且想要使用依赖注入功能,你可能也包含了多个相同类型的多个bean在上下文中(例如多个数据源bean),
> 在这种情况下,你能够覆盖setter方法 并使用@Qualifer注解去指定一个特定的bean,如下所述(但是需要需要委派到到父类中覆盖的方法上)..
> ```java
> @Autowired
>    @Override
>    public void setDataSource(@Qualifier("myDataSource") DataSource dataSource) {
>        super.setDataSource(dataSource);
>    }
> ```
> 对于xml中的 <bean>定义 - 有对应的<qualifier>声明进行限定符匹配 ..

###  事务管理
#### 测试管理的事务
通过 TransactionalTestExecutionListener  声明式管理或者通过TestTransaction 编程式管理 ..
并且事务是通过测试加载的应用上下文进行管理 - 并且应用代码中的编程式管理是由测试执行的 ...
spring 管理以及应用管理的事务通常将参与到测试管理的事务中.. 然而,你应该小心使用 - 如果spring管理的或者应用管理的事务配置是
Required / Supports 之外的传播类型 ... 
也就是说测试方法本身包含在一个事务中,那么不需要创建额外事务,只需要前面提到的两种事务特性 ...j
> 警告:
> 抢占式超时 以及测试管理的事务
> 当结合来自测试框架的任何形式的抢占式超时和Spring的测试管理的事务时需要消息:
> 特别是，Spring的测试支持将绑定事务状态到当前线程(通过java.lang.ThreadLocale变量) -在当前测试方法执行之前 ..
> 如果一个测试框架在新的线程中执行当前的测试方法来执行抢占式超时,那么在当前测试方法中执行的任何动作将不会在测试管理的事务中执行 ..
> 因此这种情况下任何动作的结果将不会导致使用测试管理的事务进行回滚 .. 并且动作将会提交给持久化存储,例如 - 关系型数据库 .. 即使测试管理的事务正确的被spring回滚 。。
> 例如在关系型数据库中,尽管测试管理的事务已经正确被Spring回滚了 ..
> 
> 包括以下情形但是不限于这些:
> 1. Junit4的@Test(timeout=...)支持 以及 TimeOut 规则
> 2. 在org.junit.jupiter.api.Assertions 类中的Junit Jupiter的assertTimeoutPreemptively(...)
> 3. TestNG的@Test(timeout=...) 支持

#### 启用和禁用事务
@Transactional 注解的测试方法 导致测试方法将运行在事务中,默认自动在测试完成之后形成事务回滚 .. 如果@Transactional 注释在测试类上, 当前类体系中的
每一个方法都将运行在一个事务中. 没有使用@Transactional注解得测试方法(在类或者方法层级上)将不会运行在测试中 ..
注意到@Transactional 不支持在测试生命周期方法上使用 - 例如注释Junit Jupiter的@BeforeAll / @BeforeEach等等 ..
因此,注解了@Transactional的测试 - 但是propagation传播行为属性设置为NOT_SUPPORTED或者 NEVER的将不会运行在事务之内 ..
![img.png](img.png)

> 提示:
> 方法级别的生命周期方法,例如,注释了Junit Jupiter的@BeforeEach / @AfterEach的方法,将会运行在测试管理的事务中 ..
> 套件级别以及类级别的生命周期方法:
> 举个例子: Junit Jupiter的@BeforeAll 或者 @AfterAll 以及 TestNG的@BeforeSuite / @AfterSuite,@BeforeClass或者@AfterClass
> 将不会运行在测试管理的事务中 ...
> 如果你需要在事务中运行套件级别或者类级别方法,你需要注入相关的PlatformTransactionManager到测试类中并使用TransactionTemplate 进行编程式
> 事务管理 ..

注意到: AbstractTransactionalJUnit4SpringContextTests  以及 AbstractTransactionalTestNGSpringContextTests 是预配置来在类级别上
进行事务管理
```java
@SpringJUnitConfig(TestConfig.class)
@Transactional
class HibernateUserRepositoryTests {

    @Autowired
    HibernateUserRepository repository;

    @Autowired
    SessionFactory sessionFactory;

    JdbcTemplate jdbcTemplate;

    @Autowired
    void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void createUser() {
        // track initial state in test database:
        final int count = countRowsInTable("user");

        User user = new User(...);
        repository.save(user);

        // Manual flush is required to avoid false positive in test
        sessionFactory.getCurrentSession().flush();
        assertNumUsers(count + 1);
    }

    private int countRowsInTable(String tableName) {
        return JdbcTestUtils.countRowsInTable(this.jdbcTemplate, tableName);
    }

    private void assertNumUsers(int expected) {
        assertEquals("Number of rows in the [user] table.", expected, countRowsInTable("user"));
    }
}
```
这个示例编写了集成测试,同样根据事务回滚和提交行为,在测试代码执行完毕之后将自动rollback .. - 被 TransactionalTestExecutionListener ..
### 5.9.3 事务回滚和提交行为
默认测试事务将自动的在测试完成之后自动回滚;然而,事务性提交以及回滚行为能够通过注解进行配置(@Commit / @Rollback注解)...

### 5.9.4 编程式事务管理
通过TestTransaction的静态方法进行测试管理的事务编程式交互.. 你能够在测试/before/after 方法中使用TestTransaction来开启或者结束当前的测试管理的事务
或者配置当前的测试管理的事务去rollback或者提交 .. 当TransactionalTestExecutionListener启用的时候TestTransaction将自动可用 ..
```java
@ContextConfiguration(classes = TestConfig.class)
public class ProgrammaticTransactionManagementTests extends
        AbstractTransactionalJUnit4SpringContextTests {

    @Test
    public void transactionalTest() {
        // assert initial state in test database:
        assertNumUsers(2);

        deleteFromTables("user");

        // changes to the database will be committed!
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertFalse(TestTransaction.isActive());
        assertNumUsers(0);

        TestTransaction.start();
        // perform other actions against the database that will
        // be automatically rolled back after the test completes...
    }

    protected void assertNumUsers(int expected) {
        assertEquals("Number of rows in the [user] table.", expected, countRowsInTable("user"));
    }
}
```
上面的示例中,通过TestTransaction进行编程式管理 ...
### 5.9.5 在事务之外运行代码
偶尔,你可能需要在事务性方法之前或者之后运行某些代码 - 但是需要在事务上下文之外处理 - 例如,为了在运行你的测试或者校验在测试之后期望事务提交行为(如果测试被配置提交事务)之前验证数据库的最初状态... 
. TransactionalTestExecutionListener 支持@BeforeTransaction / @AfterTransaction 注解来
针对这样的每一个场景 .. 你能够在测试类中将这些注解放置在任何void方法之上,或者任何接口的default void方法之上.. TransactionalTestExecutionListener
确保在合适的实践运行before 事务方法或者 after 事务方法 ..
> 提示:
> 任何before 方法(例如使用JUnit Jupiter的@BeforeEach注释的方法) 以及 任何之后方法(例如Junit Jupiter的AfterEach方法)将会运行在事务内 ..
> 除此之外,使用@BeforeTransaction / @AfterTransaction将不会运行 - 如果测试方法没有配置在事务中运行 ...

### 5.9.6 配置事务管理器
TransactionalTestExecutionListener  希望在测试的Spring应用上下文中定义一个 PlatformTransactionManager ... 如果在测试应用上下文中包含了
多个 PlatformTransactionManager的实例,你能够声明限定符 - 通过使用@Transactional("myTxMgr")或者@Transactional(transactionManager = "myTxMgr")
或者由任何@Configuration类提供的 TransactionManagementConfigurer  实现 ...
考虑TestContextTransactionUtils.retrieveTransactionManager()的文档了解使用在测试的应用上下文中查询事务管理器的算法 ..

### 5.9.7 所有事务相关注解的说明
一下基于JUnit Jupiter的测试用例来展示一个虚拟的集成测试场景 - 高亮所有事务相关的注解 ..
这个实例并不打算说明最佳实践 - 相反说明这些注解如何使用 .. 查看注解支持部分了解更多信息以及配置示例,对于@Sql的事务管理包含了使用@Sql 进行声明式
sql 脚本执行并使用默认的事务回滚语义的额外示例,以下展示了如何使用相关注解:
```java
@SpringJUnitConfig
@Transactional(transactionManager = "txMgr")
@Commit
class FictitiousTransactionalTest {

    @BeforeTransaction
    void verifyInitialDatabaseState() {
        // logic to verify the initial state before a transaction is started
    }

    @BeforeEach
    void setUpTestDataWithinTransaction() {
        // set up test data within the transaction
    }

    @Test
    // overrides the class-level @Commit setting
    @Rollback
    void modifyDatabaseWithinTransaction() {
        // logic which uses the test data and modifies database state
    }

    @AfterEach
    void tearDownWithinTransaction() {
        // run "tear down" logic within the transaction
    }

    @AfterTransaction
    void verifyFinalDatabaseState() {
        // logic to verify the final state after transaction has rolled back
    }

}
```
> 当测试orm 代码的时候避免假阳性
> 当你的测试应用代码 - 如果操作Hibernate session 或者 JPA持久化上下文的状态,确保在运行对应代码的测试方法内刷新工作单元 ... 失败去刷新底层的工作单元
> 可能会产生假阳性: 也就是你的测试通过,但是在生产环境中相同的代码会抛出异常,注意到这适用于任何管理基于内存的工作单元的orm框架, 如下面的基于Hibernate的示例,
> 一个方法说明了假阳性,另一个方法正确的暴露了刷新会话的结果 ...

```java
// ...

@Autowired
SessionFactory sessionFactory;

@Transactional
@Test // no expected exception!
public void falsePositive() {
    updateEntityInHibernateSession();
    // False positive: an exception will be thrown once the Hibernate
    // Session is finally flushed (i.e., in production code)
}

@Transactional
@Test(expected = ...)
public void updateWithSessionFlush() {
    updateEntityInHibernateSession();
    // Manual flush is required to avoid false positive in test
    sessionFactory.getCurrentSession().flush();
}

// ...
```
对于jpa 是类似的
```java
// ...

@PersistenceContext
EntityManager entityManager;

@Transactional
@Test // no expected exception!
public void falsePositive() {
    updateEntityInJpaPersistenceContext();
    // False positive: an exception will be thrown once the JPA
    // EntityManager is finally flushed (i.e., in production code)
}

@Transactional
@Test(expected = ...)
public void updateWithEntityManagerFlush() {
    updateEntityInJpaPersistenceContext();
    // Manual flush is required to avoid false positive in test
    entityManager.flush();
}

// ...
```
> 测试orm entity生命周期回调
> 类似于测试orm 代码的假阳性避免, 如果你的应用利用entity的生命周期回调(也称为entity 监听器),确保刷新在运行对应代码的测试方法中刷新底层工作单元 ..
> 失败去刷新或者清理工作单元可能导致某些生命周期回调不会执行 ...
> 例如,当使用JPA的时候,@PostPersist, @PreUpdate, and @PostUpdate callbacks 将不会被调用 - 直到entityManager.flush()在一个entity保存了或者
> 更新之后调用之后这些钩子才会被执行 .. 类似的,如果一个entity 已经与当前工作单元关联(和当前持久化上下文关联), 尝试重载一个entity,将不会导致@PostLoad 回调执行
> 除非entityManager.clear() 在尝试重载之前执行 ..
> 以下的示例展示了如何刷新EntityManager 去确保@PostPersist 回调在entity 持久化时调用 .. 一个使用了@PostPersist 回调的entity监听器已经针对示例中使用的
> Person entity 进行注入
> ```java
>  // ...
>
>@Autowired
>JpaPersonRepository repo;
>
>@PersistenceContext
>EntityManager entityManager;
>
>@Transactional
>@Test
>void savePerson() {
>// EntityManager#persist(...) results in @PrePersist but not @PostPersist
>repo.save(new Person("Jane"));
>
>    // Manual flush is required for @PostPersist callback to be invoked
>    entityManager.flush();
>
>    // Test code that relies on the @PostPersist callback
>    // having been invoked...
> }
>
> // ...
>
>
> ```
查看在spring 框架的测试套件中使用的 [JpaEntityListenerTests](https://github.com/spring-projects/spring-framework/blob/main/spring-test/src/test/java/org/springframework/test/context/junit/jupiter/orm/JpaEntityListenerTests.java) 了解
使用所有JPA 生命周期回调的工作示例 ..

## 5.12 测试上下文框架支持的类
### 5.12.1 Spring Junit 4 Runner
Spring TestContext框架提供了对JUnit4的完整集成 - 通过自定义runner(支持JUnit4.12或者更高版本) .. 通过使用@RunWith(SpringJUnit4ClassRunner.class)注解测试类或者更简短的变种@RunWith(SpringRunner.class)变种 ..
开发者可以实现标准的基于JUnit4单元以及集成测试 并同时获得TestContext框架的好处, \
例如加载应用上下文的支持,测试实例的依赖注入,事务性测试方法执行以及其他 .
如果你想要使用其他的runner 与Spring TestContext框架(例如JUnit4的Parameterized runner) 或者第三方的runner(例如 MockitoJUnitRunner),你能够可选的
使用Spring对JUnit规则的支持进行替代 ...

以下的代码列出了配置一个测试类去运行自定义的Spring Runner的最小需要:
```java
@RunWith(SpringRunner.class)
@TestExecutionListeners({})
public class SimpleTest {

    @Test
    public void testMethod() {
        // test logic...
    }
}
```
前面的示例中,TestExecutionListeners 配置了一个空列表,将禁用所有的监听器,否则需要通过@ContextConfiguration 配置一个
应用上下文 ...

### 5.12.2 Spring Junit 4 Rules
org.springframework.test.context.junit4.rules 包提供了以下的JUnit4 规则(支持JUnit4.12 以及更高)
- SpringClassRule
- SpringMethodRule

SpringClassRule 是一个JUnit TestRule - 支持Spring TestContext框架的类级别的特性 ..但是SpringMethodRule 是一个Junit
MethodRule(Spring TestContext框架的支持实例级别或者方法级别的特性).. \
针对SpringRunner,Spring基于规则的Junit 支持包含了独立于org.junit.runner.Runner任何实现的优势,因此它能够和现有的其他runner进行
合并(例如Junit4的Parameterized)或者第三方runner(例如MockitoJUnitRunner) ...
为了支持TestContext框架的完整功能,你必须合并SpringClassRule 与SpringMethodRule. 以下的示例展示了正确声明这些规则在集成测试中:
```java
// Optionally specify a non-Spring Runner via @RunWith(...)
@ContextConfiguration
public class IntegrationTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Test
    public void testMethod() {
        // test logic...
    }
}
```
这样你不需要做任何事情,即可享受Spring TestContext框架带来的好处...
### 5.12.3 JUnit4 支持类
org.springframework.test.context.junit4 包提供了以下的支持类 - 针对基于JUnit4的测试情况 - 支持JUnit4.12以及更高
- AbstractJUnit4SpringContextTests
- AbstractTransactionalJUnit4SpringContextTests

AbstractJUnit4SpringContextTests  是一个基础测试类 - 它集成了Spring TestContext 框架在Junit4环境中显式使用ApplicationContext
测试支持 .. 当你基于这个类扩展,你能够访问applicationContext实例 ...  你能够用来执行任何显式的bean 查询或者测试上下文的状态.. \
AbstractTransactionalJUnit4SpringContextTests  很显然这是一个支持事务的扩展 - 增加了一些进行jdbc访问的便捷方法.. 这个类期待一个
javax.sql.DataSource bean 以及 PlatformTransactionManager bean(也就是说这些bean需要定义在应用上下文中) .. 当你扩展这个类,你能够
访问一个protected的 jdbcTemplate实例变量 - 你能运行sql语句查询数据库 ... 你能够确认数据库状态 - 在运行数据库相关应用代码前后 ..
spring会确保这些查询运行在和应用代码中相同事务范围内.. 当你结合orm 工具使用时,需要避免假阳性 .. 如[Jdbc 测试支持](spring-context-framework.md#jdbc-)
提到的,这个类(AbstractTransactionalJUnit4SpringContextTests ) 提供了便捷的方法能够代理到 JdbcTestUtils中的方法 - 通过jdbcTemplate实现这个动作 ...
除此之外,AbstractTransactionalJUnit4SpringContextTests  提供了 executeSqlScript(...) 方法针对给定配置的数据源运行SQL 脚本..

> 这些类便于扩展,如果你不想要你的测试类和spring 特定的类体系耦合.. 你能够配置你的测试类 - 通过注解@RunWith(SpringRunner.class)或者Spring JUnit rules

### 5.12.4 SpringExtension for JUnit Jupiter
SpringTestContext 框架提供了对Junit Jupiter测试框架的完整集成 .. 在Junit5中引入 .. 通过使用
@ExtendWith(SpringExtension.class)注释测试类,你能够实现标准的基于Junit Jupiter 以及 集成测试 ..
并使用TestContext框架的好处, 例如加载应用上下文的加载,测试实例的依赖注入,事务性测试方法执行,以及其他 .. \
因此,感谢JUnit Jupiter提供的丰富的扩展api,在Spring支持的JUnit 4和TestNG的功能集之外，Spring还提供了以下功能：
- 测试构造方法,测试方法 以及测试生命周期回调方法的依赖注入
- 基于SpEL表达式,环境变量,系统属性以及其他的[条件测试执行](https://junit.org/junit5/docs/current/user-guide/#extensions-conditions) 的强力支持,查看
@EnabledIf 以及 @DisabledIf了解详情和示例 ..
- 你能够使用组合的注解 - 合并来自Spring和JUnit Jupiter的注解,查看[Meta-Annotation Support for Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-meta) 对@TransactionalDevTestConfig 以及 @TransactionalIntegrationTest 
注解的使用...
这个示例结合SpringExtension 与@ContextConfiguration ..
```java
// Instructs JUnit Jupiter to extend the test with Spring support.
@ExtendWith(SpringExtension.class)
// Instructs Spring to load an ApplicationContext from TestConfig.class
@ContextConfiguration(classes = TestConfig.class)
class SimpleTests {

    @Test
    void testMethod() {
        // test logic...
    }
}
```
你能够使用Junit5中的注解作为源注解 .. Spring提供了@SpringJUnitConfig 以及 @SpringJUnitWebConfig组合注解去简化
测试应用上下文以及Junit Jupiter的配置..
以下的示例使用了@SpringJUnitConfig 去简化配置数量
```java
// Instructs Spring to register the SpringExtension with JUnit
// Jupiter and load an ApplicationContext from TestConfig.class
@SpringJUnitConfig(TestConfig.class)
class SimpleTests {

    @Test
    void testMethod() {
        // test logic...
    }
}
```
类似的@SpringJUnitWebConfig 去创建WebApplicationContext 用于JUnit Jupiter ..
```java
// Instructs Spring to register the SpringExtension with JUnit
// Jupiter and load a WebApplicationContext from TestWebConfig.class
@SpringJUnitWebConfig(TestWebConfig.class)
class SimpleWebTests {

    @Test
    void testMethod() {
        // test logic...
    }
}
```
查看 @SpringJUnitConfig 和 @SpringJUnitWebConfig  了解详情 ..
总结: 对于JUnit4,我们使用Runner 来实现和 SpringExtension 相同的TestContext框架为测试框架集成提供的功能 ..
那么对于JUnit5(也就是Junit Jupiter) - 使用SpringExtension(直接扩展测试框架) ..
也就是runner 和 扩展的区别也就是 - 使用junit4 还是5的区别 ..
同样junit4还有许多特定便于扩展的测试基类,包括Rules ..,而junit jupiter 扩展能力强,所以不需要这些 ..
#### 使用SpringExtension 进行依赖注入
SpringExtension 实现了 [ParameterResolver](https://junit.org/junit5/docs/current/user-guide/#extensions-parameter-resolution)  - 
来自JUnit Jupiter的扩展api ... 能够让Spring对测试构造器  / 测试方法 以及生命周期回调方法进行依赖注入 ..
这与junit 4不同,junit4不支持测试构造方法注入 / 测试方法依赖注入 ... \
特别是,SpringExtension 能够从测试的应用上下文中注入依赖到测试构造器以及注释了以下注解的方法上:
- @BeforeAll
- @AfterAll
- @BeforeEach
- @AfterEach
- @Test
- @RepeatedTest
- @ParameterizedTest
- other annotations..

#### 构造器注入
如果需要进行构造器注入, 需要使用依赖注入注解,同样Spring能够配置去注册一个测试构造器的所有参数,如果构造器是可自动装配的..-需要以下条件任意之一成立
1. 注释了@Autowired的构造器
2. @TestConstructor 出现或者源注解在测试类上(并且这个注解的autowireMode属性设置为ALL)
3. 默认的测试构造器自动装配模式设置为ALL

查看@TestConstructor 详情了解如何改变全局测试构造器自动装配模式 ..
> 警告
> 如果一个测试类的构造器考虑为可自动装配,Spring假设负责解析构造器中的所有参数的对象 .. 因此,其他注册到Junit Jupiter的
> ParameterResolver 将不能够解析这种构造器的参数..
> 构造器注入不能够结合JUnit Jupiter的@TestInstance(PER_CLASS) 支持 - 如果@DirtiesContext 被用来在测试方法前后关闭测试的应用上下文 ..
> 原因是@TestInstance(PER_CLASS)指示JUnit Jupiter去在测试方法调用之间缓存测试实例 ..,那么 测试实例将会保留之前从应用上下文注入的bean 引用,
> 而方法执行完毕之后(由于@DirtiesContext的原因,应用上下文已经关闭),所以这个测试类的构造器仅仅在这种场景下执行一次,导致依赖注入不会再次发生,结果就是后续的
> 测试将和已经关闭的应用上下文的bean 进行交互,这就导致错误 ..
> 为了结合@TestInstance(PER_CLASS)与 在测试方法之前或者之后上使用@DirtiesContext,你必须配置来自Spring的依赖能够通过字段或者setter注入,那么它们能够在测试方法执行之间
> 重新注册 ..

例如下面的示例,Spring 注册了来自应用上下文中TestConfig.class中加载的OrderService到OrderServiceIntegrationTests构造器中
```java
@SpringJUnitConfig(TestConfig.class)
class OrderServiceIntegrationTests {

    private final OrderService orderService;

    @Autowired
    OrderServiceIntegrationTests(OrderService orderService) {
        this.orderService = orderService;
    }

    // tests that use the injected OrderService
}
```
注意到这个特性让测试依赖能够是final,因此不可变 ..\
如果spring.test.constructor.autowire.mode属性设置all, 能够省略在构造器上声明的@Autowried注解 ..
```java
@SpringJUnitConfig(TestConfig.class)
class OrderServiceIntegrationTests {

    private final OrderService orderService;

    OrderServiceIntegrationTests(OrderService orderService) {
        this.orderService = orderService;
    }

    // tests that use the injected OrderService
}
```
#### 方法注入
如果在Junit Jupiter测试方法上有一些参数或者测试生命周期回调方法是Applicationcontext或者它的子类型 或者 这些方法
元注解了@Autowired ,@Qualifier / @Value,那么Spring 将会注入这个值 - 为特定的参数使用来自测试上下文的相关的bean .. \
在前面的示例中,Spring 注入了来自ApplicationContext的OrderService(本质上从TestConfig.class中加载)并注入到deleteOrder()
测试方法上 ..
```java
@SpringJUnitConfig(TestConfig.class)
class OrderServiceIntegrationTests {

    @Test
    void deleteOrder(@Autowired OrderService orderService) {
        // use orderService from the test's ApplicationContext
    }
}
```

由于Junit Jupiter对ParameterResolver支持的健壮性,你能够注册多个依赖到单个方法,不仅仅是来自Spring的,也可以是来自Junit Jupiter自己的或者
其他第三方扩展的 .. 以下的示例展示了如何同时注册Spring 以及 Junit Jupiter的依赖到 placeOrderRepeatedly()测试方法
```java
@SpringJUnitConfig(TestConfig.class)
class OrderServiceIntegrationTests {

    @RepeatedTest(10)
    void placeOrderRepeatedly(RepetitionInfo repetitionInfo,
            @Autowired OrderService orderService) {

        // use orderService from the test's ApplicationContext
        // and repetitionInfo from JUnit Jupiter
    }
}
```
注意到来自Junit Jupiter的@RepeatedTest使用让测试方法能够赢得 / 获得对RepetitionInfo的访问..
#### @Nested test 类配置
Spring TestContext框架支持使用测试相关的注解到@Nested 的JUnit Jupiter的测试类上, 从Spring框架5.0开始 ..
然而,知道Spring 5.3 类级别的测试配置注解不能被闭包类进行继承(就像从父类上继承一样) ... \
Spring5.3 引入了一流的支持 - 为了让闭包类继承测试类的配置, 并且这些配置默认将被继承 .. - 为了改变默认的INHERIT模式
到OVERRIDE 模式,你可以注释单独的@Nested 测试 - 使用@NestedTestConfiguration(EnclosingConfiguration.OVERRIDE) ..
显式的 @NestedTestConfiguration 声明将应用到注释的类 - 同样它的任何子类和内嵌类 ..
因此,你也许能够在顶层测试类使用@NestedTestConfiguration进行注释, 并且它将递归的应用到它的所有内嵌的测试类 .. \
为了允许开发团队改变默认行为到OVERRIDE,举个例子,为了兼容Spring 5.0 到 5.2,默认的配置能够通过jvm 系统属性或者 类路径上的根下的spring.properties
进行全局修改 ..查看 改变默认闭包配置继承模式了解详情 .. - 默认是INHERIT,但是能够通过spring.test.enclosing.configuration jvm 系统属性OVERRIDE .. \
尽管下面的"Hello world"示例非常的简单,它展示了如何声明一个通用配置在顶层类上(然后它可以被@Nested 测试类继承).. 在这个特殊的示例中,仅仅TestConfig配置类将继承 ..
每一个内嵌测试类可以提供它自己的激活方面的集合,导致可以为不同的内嵌测试类加载不同的应用上下文 ...(查看 上下文缓存了解详情 ..)
也可以查看 [支持的注解](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-nestedtestconfiguration) 了解
那些注解能够被内嵌测试类继承 ...
```java
@SpringJUnitConfig(TestConfig.class)
class GreetingServiceTests {

    @Nested
    @ActiveProfiles("lang_en")
    class EnglishGreetings {

        @Test
        void hello(@Autowired GreetingService service) {
            assertThat(service.greetWorld()).isEqualTo("Hello World");
        }
    }

    @Nested
    @ActiveProfiles("lang_de")
    class GermanGreetings {

        @Test
        void hello(@Autowired GreetingService service) {
            assertThat(service.greetWorld()).isEqualTo("Hallo Welt");
        }
    }
}
```

### 5.12.5 TestNG Support Classes
org.springframework.test.context.testng 包提供了对TestNG相关的支持 - 包括了一些TestNG相关的基础测试类 ..
- AbstractTestNGSpringContextTests
- AbstractTransactionalTestNGSpringContextTests

其实这些本质上对应了Jupiter的实现,一个是能够显式的在TestNG环境中使用ctx 测试支持 .. 另一个是可以支持事务扩展 ..
操作都是一样的 ....
> 同理,如果不想要和spring特定的类进行耦合,你能够配置你自己的测试类 - 通过使用@ContextConfiguration,@TestExecutionListeners并且
> 可以手动的通过TestContextManager 检测你的测试类 ..
> 查看AbstractTestNGSpringContextTests  的源代码了解如何测试你的测试类 ..