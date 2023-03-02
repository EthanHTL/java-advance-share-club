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


# 集成测试
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

## 集成测试的目标是
1. 管理测试之间的Spring Ioc容器缓存 ..
2. 提供test装置实例的依赖注入
3. 提供对集成测试合适的事务管理
4. 提供了Spring特定的基类(帮助开发者编写集成测试) ..

### 上下文管理以及缓存
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

## 测试装置的依赖注入
也就是spring 框架提供给我们的依赖注入

## 事务管理
默认回滚,但是你可以提供注解改变事务提交策略 ..(@Commit) ...
事务管理,注意: 仅仅支持 PlatformTransactionManager,也就是非响应式的事务管理器 ..
如果使用React类型的,没有任何效果 ...
一个事务性测试方法将会在测试方法执行完毕之后默认进行回滚,但是你可以通过Commit 进行事务提交...

## jdbc 测试支持
### JdbcTestUtils
包含了简化标准的数据库测试场景的工具方法,特别是,JdbcTestUtils 提供了以下的静态工具方法 ..
详情查看 javadoc ..
- countRowsInTable(..)
- countRowsInTableWhere(..)
- delete...

AbstractTransactionalJUnit4SpringContextTests 以及 AbstractTransactionalTestNGSpringContextTests 提供了各种代理到
JdbcTestUtils方法的方法 ..
### 内嵌数据库
spring-jdbc模块提供了配置和启动一个内嵌的数据库 .. 这能够在集成测试中进行数据库交互 ...
查看内嵌数据库支持  以及与内嵌数据进行测试数据访问逻辑  详情了解 ..

## 5. Spring TCF(testContext framework)
这个框架主要提供了通用，注解驱动单元 以及集成测试支持(无感知测试框架) .. 并且非常重视约定大于配置,包含了
许多默认值能够让你通过基于注解的配置进行覆盖 .. \
除了通用的测试基础设施,TCF同样提供了对JUnit4,JUnit5 以及 TestNG的隐式支持.. Spring提供了抽象支持类 .. \
因此Spring提供了一个自定义的Junit Runner 以及 自定义的JUnit Rules - 针对Junit4 并且为JUnit5增加了
自定义的Extension让你能够写称为 POJO的测试类 .. \
POJO测试类不需要扩展特定的类体系,例如 abstract 支持类 .. \
根据需要了解相关内容,例如 如果你不对测试框架感兴趣,或者不扩展你自己的监听器 或者自定义loader,我们可以直接了解
如何定义配置并启动集成测试(例如上下文管理，依赖注入，事务管理),支持的类 以及注解支持部分 ..

### 关键抽象
框架核心由TestContextManager 类和 TestContext ,TestExecutionListener 以及 SmartContextLoader 接口组成 ..\
测试上下文管理器将会为每一个测试类创建(例如,所有JUnit5 的单个测试类的所有测试方法),测试上下文管理器,最终会管理当前测试的\
的上下文(TestContext) ,TestContextManager 同样会更新TestContext的状态（根据测试进度并代理到
TestExecutionListener 实现)这指示实际的测试执行通过(依赖注入，管理事务以及其他所提供) .. \
一个SmartContextLoader 负责加载一个ApplicationContext - 为给定的测试类加载 .. \
查看javadoc 以及 Spring 测试套件了解更多信息以及各种实现的示例 ...

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
如果你使用Junit Jupiter,你能够可选的使用构造器注入(查看[使用SpringExtension 进行依赖注入]), 本质上使用SpringExtension 进行依赖注入很简单,
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
测试管理的事务 - 通过 TransactionalTestExecutionListener  声明式管理或者通过TestTransaction 编程式管理 ..
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