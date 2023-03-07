package org.example.spring.test.integration.support.context;

import org.example.spring.test.integration.support.each.ConfigApplicationTests;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

/**
 * TCF 支持的类
 *
 * 1. spring Junit4 Runner
 *  spring TCF 提供了对junit4的完整支持(通过一个自定义runner - 支持junit4.12 以及更高)
 *  通过@RunWith(SpringJUnit4ClassRunner.class)或者@RunWith(SpringRunner.class)实现 ..
 *  开发者能够实现标准的基于JUnit4的单元测试 / 集成测试,同时获取TCF的好处 ..
 *  例如加载ioc的支持,测试实例的依赖注入,事务性测试方法的执行,以及其他 ..如果你想要使用TCF和其他runner结合工作,
 *  例如JUnit4的Parameterized runner 或者第三方runner(例如 MockitoJUnitRunner),你可以使用
 *  Spring对JUnit规则的支持进行替代 .. -- https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-junit4-rules
 *
 *  同理 AbstractTestNGSpringContextTests / AbstractTransactionalTestNGSpringContextTests 用来支持 TestNG ..
 *
 *  these classes are a convenience for extension. If you do not want your test classes to be tied to a Spring-specific class hierarchy,
 *  you can configure your own custom test classes by using @ContextConfiguration, @TestExecutionListeners,
 *  and so on and by manually instrumenting your test class with a TestContextManager.
 *  See the source code of AbstractTestNGSpringContextTests for an example of how to instrument your test class.
 *
 *  这些类对于extension是一种便利. 如果你不想你的测试类绑定到Spring特定的类体系下,你能够配置你自己的自定义测试类 ...
 *  可以查看源码了解更多 ...
 */
public class TestContextSupportClassTests {

    /**
     * 例如开启spring对JUnit4的测试支持,最小化配置即可
     */
    @RunWith(SpringRunner.class)
    // 禁用默认的监听器,否则需要通过@Configuration配置一个ioc容器,用于依赖注入 ..等特性处理 ..
    @TestExecutionListeners({})
    public static class SimpleTests {

    }

    /**
     * spring JUnit 4 规则(支持junit 4.12以及更高)
     * 1. SpringClassRule
     * 2. SpringMethodRule
     *
     * SpringClassRule 是一个 JUnit TestRule(支持spring TCF的类级别特性),因此SpringMethodRule是一个支持Spring TCF
     * 的实例级别 / 方法级别特性
     *
     * 对比SpringRunner ,这些JUnit规则支持具有独立于任何org.junit.runner.Runner实现的优势,它能够合并存在的可选runner(例如 JUnit4
     * 的Parameterized 或者第三方runner MockitoJUnitRunner使用)
     *
     * 也就是规则可以和runner随意组合 ..
     * 为了支持TCF的所有功能,必须合并SpringMethodRule和SpringClassRule  ..
     */
    // Optionally specify a non-Spring Runner via @RunWith(...)
        // 这样也能够让非Spring Runner 支持Spring TCF框架的全部特性 ..
    @ContextConfiguration
    public static class IntegrationTest {

        @ClassRule
        public static final SpringClassRule springClassRule = new SpringClassRule();

        @Rule
        public final SpringMethodRule springMethodRule = new SpringMethodRule();

        @Test
        public void testMethod() {
            // test logic...
        }
    }

    /**
     * JUnit4 Support Classes
     * org.springframework.test.context.junit4 包提供了支持JUnit4的测试类
     * 1. AbstractJUnit4SpringContextTests
     *  它是一个抽象测试基类,集成了Spring TCF使用一个显式的ioc测试支持到 JUnit 4 环境 ..
     *  当你扩展它,你能够访问一个protected的ioc实例变量 来执行显式的bean 查找或者测试上下文的状态 ..
     * 2. AbstractTransactionalJUnit4SpringContextTests
     *  事务性扩展的抽象基类(它继承了AbstractJUnit4SpringContextTests,并增加了访问jdbc的方便功能）..
     *  它包含一个数据源和定义在应用上下文中的事务管理器 ..
     *  扩展它你能够访问jdbcTemplate实例变量,你能够运行sql 语句去查询数据库 ..
     *  能够在运行数据库相关代码前后的查询确保数据库状态 ...
     *  并且这种代码将运行在相同测试方法的相同事务范围中 ...
     *  当你联合ORM工具时, 为了避免 false positives ..,在jdbc 测试支持中提到,AbstractTransactionalJUnit4SpringContextTests
     *  提供了方便的方法通过jdbcTemplate代理到JdbcTestUtils中的方法 ..
     *  此外,AbstractTransactionalJUnit4SpringContextTests 提供了 executeSqlScript(..) 根据配置的数据源去运行SQL脚本 ..
     *
     *
     * 这些类很方便进行扩展,如果你不想你的测试类绑定到Spring特定的类体系,你能够配置你自己的测试类(通过@RunWith(SpringRunner.class))或者
     * Spring的 JUnit 规则 ..
     *
     **/

     // ---------------------------- junit jupiter ----------

    /*
     *
     * 5.12.4 JUnit Jupiter 的 SpringExtension
     * Spring TCF提供了与 JUnit Jupiter测试框架的完整集成 ..
     * 在JUnit5中引入 ...
     * 通过注解测试类(通过@ExtendWith(SpringExtension.class)),你能够实现标准的基于JUnit Jupiter的单元测试 以及集成测试 ..
     * 同步的获得TCF的好处,例如 加载应用上下文的好处,测试实例的依赖注入, 事务性测试方法执行 ... 等等 ..
     *
     * 除此之外,感谢JUnit Jupiter的丰富的扩展ap,(除了spring对JUnit 4 以及 TestNG的支持的特性之外)spring提供了以下的特性:
     * 1. 测试构造器，测试方法，测试生命周期回调方法的依赖注入 ...
     * 2. 基于SpEL表达式的条件测试执行,环境变量，系统属性 - 的强有力支持 ...
     * 查看 @EnabledIf 以及 @DisabledIf 了解更多以及 示例
     * 3. 自定义组合注解(合并来自Spring和JUnit Jupiter的注解), 查看 @TransactionalDevTestConfig 以及 @TransactionalIntegrationTest
     * 实例了解更多 (https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#integration-testing-annotations-meta)
     *
     * 以下的代码展示了如何配置测试类使用SpringExtension 合并@ContextConfiguration ..
     *
     */
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration
    static class baseSpringTests {
        @Configuration
        public static class Configure {

        }

        @Test
        void testMethod() {

        }
    }

    // 因此你能够在JUnit5 中使用注解作为元注解 .. Spring提供了@SpringJunitConfig 以及 @SpringJunitWebConfig 组合注解能够简化测试
    // 应用上下文 和 Junit Jupiter的配置

    // 例如 @SpringJUnitWebConfig(TestWebConfig.class), 能够 使用WebApplicationContext ...


    // SpringExtension 的依赖注入
    /**
     * SpringExtension 实现了来自JUnit Jupiter的ParameterResolver扩展api
     * 这样Spring 提供了测试构造器 / 测试方法 / 测试生命周期回调方法的依赖注入 ..
     * 特别是SpringExtension 能够注册来自测试的应用上下文到 测试构造器以及注释了 @BeforeAll / @afterAll @BeforeEach / @afterEach
     * @Test / @RepeatedTest / @ParameterizedTest 以及 其他的注解的方法 ..
     *
     * 1. 构造器注入 ..
     * 如果从JUnit Jupiter测试类的构造器的一个参数是应用上下文或者它的子类型 或者元注解了@Autowired ... 等自动注入语义的注解的属性或者方法 ..
     * 能够注册来自应用上下文中的对应Bean
     *
     * 1. 一个构造器可自动注入的特性有：
     *   -.@Autowired的构造器
     *   - @TestConstructor 注解出现 或者元注解出现在测试类上并具有autowireMode 属性为 ALL ..
     *   - 或者默认的测试构造器的自动注入模式 已经设置为ALL ..
     *
     *   了解@TestConstructor 如何改变全局的测试构造器自动注入模式 ..
     *
     *   考虑为自动装配的情况下,Spring 负责解析参数,因此其他注册到JUint Jupiter的ParameterResolver不会解析构造器的参数 ...
     *
     *   测试类的构造器注入不能联合JUnit Jupiter的@TestInstance(PER_CLASS)使用 --
     *   如果在测试方法的前后使用@DirtiesContext被用来关闭测试的应用上下文
     *
     *
     *   原因是 @TestInstance(PER_CLASS) 指示Junit Jupiter 在测试方法调用之间缓存测试实例 ..
     *   因此测试实例将会保留bean的引用(之前从已经被关闭的应用上下文中注册到这个测试实例中的bean) ..
     *
     *   因此这个测试类的构造器仅仅会执行一次(在这种场景下,依赖注入不会再次发生,并且后续的测试如果和这些来自关闭的应用上下文的bean交互,会发生错误)
     *   为了在这种情况下使用（也就是结合使用了@DirtiesContext 测试方法之前 / 测试方法之后 与@TestInstance(PER_CLASS))
     *   必须通过字段或者setter注入来配置来自Spring的依赖,这样它们能够再次被注入(在测试方法之间执行调度中)
     */

    @SpringJUnitConfig
    class OrderServiceIntegrationTests {

        @Configuration
        static class Configure {
            @Bean
            public ConfigApplicationTests.MyConfigApplication2Tests.OrderService orderService() {
                return new ConfigApplicationTests.MyConfigApplication2Tests.OrderService() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                };
            }
        }
        private final ConfigApplicationTests.MyConfigApplication2Tests.OrderService orderService;

        @Autowired
        OrderServiceIntegrationTests(ConfigApplicationTests.MyConfigApplication2Tests.OrderService orderService) {
            this.orderService = orderService;
        }

        // tests that use the injected OrderService
    }

    // 如果设置 spring.test.constructor.autowire.mode = all(查看@TestConstructor ..)
    // 那么我们完全可以不用不用添加@Autowired ...也就是如下
//    @SpringJUnitConfig(TestConfig.class)
    class OrderServiceIntegration2Tests {

        private final ConfigApplicationTests.MyConfigApplication2Tests.OrderService orderService;

        OrderServiceIntegration2Tests(ConfigApplicationTests.MyConfigApplication2Tests.OrderService orderService) {
            this.orderService = orderService;
        }

        // tests that use the injected OrderService
    }


    // 由于 ParameterResolver的支持 ,你能够注册多个依赖到单个方法,不仅仅是Spring 还可以是 JUnit Jupiter自己的或者第三方扩展的依赖 ..
    // 例如下面的示例展示了如何同时注册Spring 以及 JUnit Jupiter的依赖到 placeOrderRepeatedly() 测试方法 (同步的) ...

    // 这个注解的使用能够注册对应的RepetitionInfo 到测试方法中 代表当前方法的执行是第几次的详细信息 ..
//    @SpringJUnitConfig(TestConfig.class)
    static class OrderServiceIntegration1Tests {

        @RepeatedTest(10)
        void placeOrderRepeatedly(RepetitionInfo repetitionInfo,
                                  @Autowired ConfigApplicationTests.MyConfigApplication2Tests.OrderService orderService) {

            // use orderService from the test's ApplicationContext
            // and repetitionInfo from JUnit Jupiter
        }
    }
}
