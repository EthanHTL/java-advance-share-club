package org.example.spring.test.integration.support.each;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @author FLJ
 * @date 2022/12/24
 * @time 11:20
 * @since 2022/12/24
 * <p>
 * <p>
 * 通过@ContextConfiguration  注解配置为测试准备的应用上下文 ..
 * <p>
 * 配置一个应用上下文的手段有很多种,我们只通过注解,上下文初始化器 完成测试 ..
 * 另外可以使用自定义的SmartContextLoader 进行高级使用场景测试 ..
 **/
public class ConfigApplicationTests {

    @Configuration
    public static class AppConfig {

    }

    /**
     * 通过ContextConfiguration 显式指定application context 的配置
     */
    @ContextConfiguration(classes = AppConfig.class)
    @ExtendWith(SpringExtension.class)
    public static class MyConfigApplicationTests {

        @Autowired
        private ApplicationContext context;

        @Test
        public void test() {
            AppConfig bean = context.getBean(AppConfig.class);
            Assertions.assertNotNull(bean);
        }
    }

    /**
     * 不显式指定的,那么默认会自动从测试类的所有内嵌静态类上尝试寻找@Configuration实现类 ..
     */
    @SpringJUnitConfig
    // ApplicationContext will be loaded from the static nested Config class
    public static class MyConfigApplication2Tests {

        public interface OrderService {

        }

        static class OrderServiceImpl implements OrderService {

        }

        @Configuration
        static class Config {

            // this bean will be injected into the OrderServiceTest class
            @Bean
            OrderService orderService() {
                OrderService orderService = new OrderServiceImpl();
                // set properties, etc.
                return orderService;
            }
        }


        @Autowired
        private ApplicationContext applicationContext;


        @Test
        public void test() {
            OrderService bean = applicationContext.getBean(OrderService.class);
            Assertions.assertNotNull(bean);
        }
    }


    /**
     * 有一个注意事项,初始化器的支持的 ConfigurableApplicationContext 类型通常应该和 使用中的SmartContextLoader 的创建的应用上下文类型兼容(通常是GenericApplicationContext)
     * 所以这里泛型我使用了 GenericApplicationContext ...
     * <p>
     * 其次初始化执行顺序可以通过order相关语义进行修改(如果存在多个初始化器)
     * <p>
     * 并且其他方式能够干的事情,初始化器一样能干(只不过是编程式处理,没有屏蔽细节而已)
     */
    public static class MyApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
        @Override
        public void initialize(GenericApplicationContext applicationContext) {
            applicationContext.registerBean(Apple.class);
        }

        public static class Apple {

        }
    }

    /**
     * 通过上下文初始化器进行 应用上下文配置
     */
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(initializers = {MyApplicationContextInitializer.class})
    public static class MyConfigApplicationWithContextInitializerTests {

        @Autowired
        ApplicationContext context;

        @Test
        public void test() {
            MyApplicationContextInitializer.Apple bean = context.getBean(MyApplicationContextInitializer.Apple.class);
            Assertions.assertNotNull(bean);
        }
    }


// ---------------------------------------------------------------------------------------------------------------------

    /**
     * 上下文配置继承配置 ..
     *
     * @ContextConfiguration 支持 继承来自父类的 resource location / ctx initializer... / component class ...分别由
     * inheritLocations / inheritInitializers 属性来决定,默认是true, 继承(如果测试类继承了父类) ...
     * <p>
     * 那么测试类的所有resource location / component class 将会追加到由超类声明的标注的类或者 资源位置列表中,类似的,初始化器也是一样的 .
     * 这意味着子类有扩展资源locations / component class / 上下文初始化器的选择 ..
     * <p>
     * 当设置为false,意味着测试类将实际上遮盖由超类定义的配置 ...
     * <p>
     * 从spring 5.3 开始,测试配置也可以从闭包类中继承,查看 @Nested相关注解了解更多 ..
     */
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = AppConfig.class)
    public static class BaseTests {

    }

    @Configuration
    public static class SubTestAppConfig {

    }

    // 继承

    /**
     * 同时加载AppConfig 配置 以及 SubTestAppConfig 配置
     */
    @ContextConfiguration(classes = SubTestAppConfig.class)
    public static class SubConfigTests extends BaseTests {

    }

    // 上面这种通过@ExtendWith的方式可以替换为如下方式
    @SpringJUnitConfig(classes = AppConfig.class)
    public static class Base1Tests {

    }

    // 继承
    @SpringJUnitConfig(classes = SubTestAppConfig.class)
    public static class SubConfig1Tests {

    }

// ---------------------------------------------------------------------------------------------------------------------


    /**
     * environment with profiles
     */
//    @ActiveProfiles(profiles = {"default","product"})
    @ActiveProfiles(profiles = {"default", "development"})
//    @ActiveProfiles(profiles = {"product"})
    @SpringJUnitConfig
    public static class EnvironmentWithProfilesTests {
        public static class DataSource {
            private final String name;

            public DataSource(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return name;
            }
        }

        @Profile("default")
        @Configuration
        public static class DefaultDataConfig {

            @Bean
            public DataSource dataSource() {
                return new DataSource("default-database");
            }
        }

        @Profile("development")
        @Configuration
        public static class DevelopmentDataConfig {

            /**
             * bean 方法如果已经注册过,第二次无法注册 ...
             * 详情查看 ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForBeanMethod
             *
             * @return
             */
            @Bean
            public DataSource dataSource() {
                return new DataSource("development-database");
            }


        }

        @Autowired
        ApplicationContext applicationContext;

        @Test
        public void test() {
            DataSource bean = applicationContext.getBean(DataSource.class);
            Assertions.assertNotNull(bean);

            System.out.println(applicationContext.getBeansOfType(DataSource.class).size());

            System.out.println(applicationContext.getBean(DevelopmentDataConfig.class));
        }

    }


    @Profile("development")
    @Configuration
    public static class AbstractProfileConfig {
        @Bean
        public EnvironmentWithProfilesTests.DataSource dataSource() {
            return new EnvironmentWithProfilesTests.DataSource("development-datasource");
        }
    }

    /**
     * 你还可以使用继承关系来让ActiveProfiles 更好管理,
     * 同样你也能覆盖父类的activeProfiles 配置
     */
    @SpringJUnitConfig({AbstractProfileConfig.class})
    @ActiveProfiles("development")
    abstract static class AbstractIntegrationTest {


    }

    // 实现并执行测试
    static class DefaultIntegrationTest extends AbstractIntegrationTest {
        @Autowired
        EnvironmentWithProfilesTests.DataSource dataSource;

        @Test
        public void test() {
            Assertions.assertNotNull(dataSource);
        }
    }


    static class OperatingSystemActiveProfilesResolver implements ActiveProfilesResolver {
        @Override
        public String[] resolve(Class<?> testClass) {

            String property = System.getProperty("os.name");
            if (property.startsWith("Windows")) {
                return new String[]{"win"};
            } else if (property.contains("unix")) {
                return new String[]{"unix"};
            }
            return new String[0];
        }
    }

    /**
     * 除了上述的静态激活profile之外,我们或许可能需要编程式判断,例如是否在build server中运行,可能采用不同的策略 .
     * 或者操作系统,某些环境变量 ,某些类上的注解元数据 .. / 其他条件 ..
     * <p>
     * 所以可以通过 ActiveProfilesResolver  进行激活 profile 配置,同样使用方式很简单
     */
    @ActiveProfiles(resolver = OperatingSystemActiveProfilesResolver.class)
    @SpringJUnitConfig
    static class ActiveProfilesResolverWithConfigTests {

        @Configuration
        @Profile("win")
        public static class DefaultConfig {


            @Bean
            public EnvironmentWithProfilesTests.DataSource dataSource() {
                return new EnvironmentWithProfilesTests.DataSource("win-datasource");
            }
        }

        @Configuration
        @Profile("unix")
        public static class UnixConfig {
            @Bean
            public EnvironmentWithProfilesTests.DataSource dataSource() {
                return new EnvironmentWithProfilesTests.DataSource("unix-datasource");
            }
        }

        @Autowired
        EnvironmentWithProfilesTests.DataSource dataSource;

        @Test
        public void test() {
            System.out.println(dataSource);
        }
    }
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * 通过@TestPropertySource  达到和 对应注解的一样效果,向测试应用上下文中加入对应的属性源 ..
     *
     * 加入resource location /  使用properties 属性进行(key-value键值对字符串输出) ..(这些被增加的key-value 键值对会增加到一个
     * Environment 作为具有最高优先级的  test PropertySource ..
     *
     * key-value 语法支持:
     * - key=value
     * - key:value
     * - key value
     *
     *
     * 最后 这个注解可重复,并且它可以作为元注解出现在测试类上,但是距离测试类越近的优先级越高 ...
     *
     * Default Properties File Detection
     * 当设置此注解,但是没有对应的配置,那么它会根据注释它的类进行约定查找 ..
     * 假设类是 com.example.MyTest,
     * 那么它会查找相应的 classpath:com/example/MyTest.properties ..
     * 如果找不到抛出IllegalStateException  ..
     *
     * 并且测试属性源的优先级相比于应用的属性集 / 或者系统属性 .. 都具有更高的优先级 ..
     * 也就是测试属性集可以用来覆盖来自系统和应用的属性资源级 ..
     * 因此内联属性具有更高的优先级相比于从资源位置加载的 ..
     * 然而通过@DynamicPropertySource 注册的相比于@TestPropertySource的属性集优先级更高 ..
     *
     *
     * 同样,这个注解支持 inheritLocations  / inheritProperties  标志支持 ..
     * 如果设置为false,则表示丢弃来自父类定义的配置 ...(如果你提供多个配置,但是没有提供相同的值,那么spring 会让你强制统一)
     */
    @ContextConfiguration
    @ExtendWith(SpringExtension.class)
    @TestPropertySource(properties = {"timezone=GMT","port: 4343"})
    static class CtxConfigWithPropertySourcesTests {

        @Autowired
        Environment environment;
        @Test
        public void test() {
            Assertions.assertEquals(environment.getProperty("port"),"4343");
        }
    }

    @TestPropertySource(properties = {"key:value"})
//    @TestPropertySource(properties = {"key valuevalue"},inheritLocations = false,inheritProperties = false)
    @TestPropertySource(properties = {"key valuevalue"})
    static class SubCtxConfigWithPropertySourcesTests extends CtxConfigWithPropertySourcesTests {

        @Value("#{environment['key']}")
        private String key;

        @Test
        public void test() {
            System.out.println(key);
        }
    }

    /**
     * dynamic property source 进行动态属性的注入(仅在需要的时候进行解析) ..
     *
     * 通过@DynamicPropertySource  注解支持(原来是为了设计去允许来自TestContainers的测试的中的属性轻松暴露到Spring 集成测试中),但是
     * 这个特性也能够使用在任何生命周期维持在测试的应用上下文之外的外部资源的情况下 ..
     *
     * 使用方式有一点需要注意,这个注解使用在方法上,并且方法必须接收一个DynamicPropertyRegistry参数被用来增加key-value 对到Environment ...
     * 值是动态的并通过Supplier进行提供,仅当属性解析时进行执行 ..
     *
     * 如果使用此注解到 base class 并且发现在子类中的测试失败了,是因为动态属性在子类之间发生了改变,你需要通过@DirtiesContext标注基类去确保子类
     * 获得具有正确动态属性的自己的ApplicationContext ...
     *
     * 以下是一个测试用例,这里并没有使用testContainer 依赖 ... 仅仅注释,不是不要
     */
    @SpringJUnitConfig
//    @TestContainer
    // with Dynamic property source
    static class DynamicPropertySourceWithCtxTests {

        public static class RedisContainer {
            public String getHost() {
                return "localhost";
            }

            public String getMappedPort() {
                return "8090";
            }
        }
//        @Container
        static RedisContainer redis = new RedisContainer();

        @DynamicPropertySource
        static void redisProperties(DynamicPropertyRegistry registry) {
            registry.add("redis.host", redis::getHost);
            registry.add("redis.port", redis::getMappedPort);
        }

        // tests ...
    }

// ---------------------------------------------------------------------------------------------------------------------

    /**
     * 替换加载的上下文,例如使用WebApplicationContext
     *
     * 使用它会导致,测试框架(TCF) 会创建一个MockServletContext并且提供给测试的web应用上下文(WAC - webapplicationcontext),base资源路径
     * 设置为 src/main/webapp .. 这将根据jvm的根进行相对路径解析(通常是项目的路径)
     * 这个可以进行覆盖,通过注解提供额外的路径(例如 src/test/webapp),如果你希望从类路径代替文件系统配置,你可以使用classpath: 前缀 ..
     *
     * WebApplicationContext 和标准上下文没有什么不同,它同样可以通过各种spring 提供的或者 tcf提供的注解来进行配置形成测试,也包括其他的
     * 例如@TestExecutionListeners  @Sql @Rollback等等 ..
     *
     * 所以测试 简单化
     *
     * 详情查看 org.springframework.test.context.web.AbstractGenericWebContextLoader 了解原理
     */
    @WebAppConfiguration
    @SpringJUnitConfig
    static class LoadingWebApplicationContextTests {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Test
        public void test() {
            Assertions.assertNotNull(webApplicationContext);
        }
    }


    // work with web mocks ..
    /**
     * 为了提供广泛的web 测试支持,TCF 有一个 ServletTestExecutionListener 来基于线程变量的方式保证每一个test方法执行时都能够注入相应
     * 的资源是per-method 形式的 ..
     *
     * 在每一个方法之前,它会创建 MockHttpServletRequest,MockHttpServletResponse,并且一个基于@WebAppConfiguration配置的base 资源路
     * 经的ServletWebRequest,并且此监听器也确保 MockHttpServletResponse  和 ServletWebRequest 能够注册到测试实例中,一旦测试完成
     * 清理掉线程本地状态 ..
     *
     * 当在web中,你可以会和web mocks进行交互,那么除了WebApplicationContext / MockServletContext将在测试套件中缓存起来之外,其他的mock
     * 将根据per-method 被 ServletTestExecutionListener 进行管理 ..
     *
     */
    @SpringJUnitWebConfig
    static class WacTests {

        @Autowired
        WebApplicationContext wac; // cached

        @Autowired
        MockServletContext servletContext; // cached

        @Autowired
        MockHttpSession session;

        @Autowired
        MockHttpServletRequest request;

        @Autowired
        MockHttpServletResponse response;

        @Autowired
        ServletWebRequest webRequest;

        //...
    }


// ---------------------------------------------------------------------------------------------------------------------

}
