package org.example.spring.test.integration.support.each;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

/**
 * 编程式执行sql 脚本
 *
 * 执行sql 脚本的好处是修改数据库 方案/设计 ,又或是插入测试数据到表中 ..
 *
 * spring-jdbc 模块提供了初始化一个内嵌或者已经存在数据库的支持（通过在spring ioc上下文加载的情况下执行sql 脚本) ..
 * 查看内嵌数据库支持 以及测试数据访问内嵌数据库的逻辑了解详情 ..[https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#jdbc-embedded-database-dao-testing]
 *
 *
 * 1. 编程式执行sql 脚本
 * 为了在集成测试方法中编程式执行sql 脚本,需要以下工具类
 * org.springframework.jdbc.datasource.init.ScriptUtils
 *  它提供了让sql脚本工作的静态工具方法 ..主要是框架內部使用
 *  并且它能够完整控制sql脚本如何解析并执行 ..
 *
 * org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
 *  提供了面向对象的api进行编程式收集 / 初始化 / 清理数据库 ..
 *  通过使用定义在外部资源的sql 脚本 ..
 *  此填充器提供了配置字符编码 / 语句分隔符 / 注释分隔符 / 错误处理标志(当解析并运行脚本时)
 *  每一个配置选项都有一个合适的默认值 ..
 *  为了运行配置在 ResourceDatabasePopulator中的脚本 ，你能够要么针对一个java.sql.Connection去使用popuate(Connection)
 *  去运行populator 或者通过execute(DataSource)方法去运行populator ...
 * org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests
 * org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests
 *
 */
@SpringJUnitConfig
public class ExecutingSqlScriptsTests {


    @Configuration
    static class Configure {
        @Bean
        public DataSource dataSource() {
            return new HikariDataSource(
                    new HikariConfig() {{
                        setDriverClassName(Driver.class.getName());
                        setJdbcUrl("jdbc:h2:mem:test_transaction");
                        setUsername("root");
                        setPassword("123456");
                    }}
            );
        }
    }


    @Autowired
    private DataSource dataSource;

    /**
     * 这个示例使用populator 进行测试schema和测试数据的执行,并设置了语句分隔符为@@,并根据Database进行脚本运行 ..
     *
     * 此populator 內部代理 ScriptUtils进行sql 脚本的解析和运行 ...
     * 类似的, AbstractTransactionalJUnit4SpringContextTests的executeSqlScript(..)
     * 以及 AbstractTransactionalTestNGSpringContextTests的executeSqlScript(..) 內部使用一个populator 运行脚本 ..
     */
    @Test
    void databaseTest() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(
                new ClassPathResource("test-schema.sql"),
                new ClassPathResource("test-data.sql")
        );
        populator.setSeparator("@@");
        populator.execute(dataSource);
    }


    /**
     * 通过@Sql 声明式执行sql 脚本 ..
     * 除了编程式运行sql脚本, 你可以在spring textContext 框架 声明式配置sql 脚本 ..
     * 特别是,你可以声明@Sql 注解到测试类 或者测试方法去配置独立的SQL语句和SQL脚本的资源路径(在集成测试方法前后根据给定的数据库执行)
     * 对@SQL的支持通过SqlScriptsTestExecutionListener 提供,这是默认启用的 ..
     *
     * 方法级的@SQl声明默认可以覆盖类级别的声明,从spring 5.2开始,这个行为可以根据每一个测试类或者测试方法上通过@SqlMergeMode进行配置 ..
     * 查看 使用@SqlMergeMode 进行 合并和覆盖配置了解详情 ..
     *
     *
     * 路径资源语义:
     *  默认是处理为类路径资源(相对于定义的测试类的包),以/开头的处理为绝对路径资源，一个路径资源支持通过URL形式加载(classpath: /
     *  file: / http:通过特定的资源协议进行加载) ..
     *
     *  默认脚本检测:
     *  等价于默认配置类检测,同理如果声明了@Sql,但是没有路径资源配置,那么根据注释的配置类进行检测
     *  1.类级别声明
     *      相关的默认脚本是: classpath: com/example/MyTest.sql
     *  2. 方法级别的声明
     *      如果方法为testMethod(),类com.example.MyTest,那么sql默认检测 classpath:com/example.MyTest.TestMethod.sql ..
     *
     *  同样@Sql是一个可重复注解 ..
     *  你可以配置@Sql多个sql脚本 set(具有不同语法配置,不同的错误处理规则,不同的执行阶段(per-set),你能够声明多个@Sql实例 ..
     *  通过Java8，你能够使用@Sql作为可重复注解 ..同样你可以使用@SqlGroup作为一个容器注解对@Sql使用 ..
     */
    @SpringJUnitConfig
    @Sql("classpath:test-schema.sql")
    static class SqlIntegrationTest {
        @Test
        void emptySchemaTest() {
            // run code that uses the test schema without any test data
        }

        /**
         * 以下sql文件相对于当前测试类所定义的包进行解析 ..
         */
        @Test
        @Sql({"test-schema.sql", "test-user-data.sql"})
        void userTest() {
            // run code that uses the test schema and test data
        }
    }

    @SpringJUnitConfig
    // 对于单行注释语法,使用 ` ..
    // 在java8,@SqlGroup 是可选的,容器注解自动兼容普通注解(在单个注解的情况下,多个可重复注解自动转换为容器注解) ..
    // 但是为了兼容其他jvm 语言(kotlin),需要使用@SqlGroup ..
    @Sql(scripts = "/test-schema.sql", config = @SqlConfig(commentPrefix = "`"))
    @Sql("/test-user-data.sql")
    static class MultiSqlSetTests {



    }

    /**
     * 脚本执行阶段,
     * 默认在测试方法之前执行,然而你可以设置在测试方法之后执行,例如清理数据库状态 ..
     * 通过@Sql的executionPhase属性进行配置 ..
     * 详情查看 Sql.ExecutionPhase
     */

    static class SqlSetInvokePhaseTests {
        @Test
        @Sql(
                scripts = "create-test-data.sql",
                config = @SqlConfig(transactionMode = ISOLATED)
        )
        @Sql(
                scripts = "delete-test-data.sql",
                config = @SqlConfig(transactionMode = ISOLATED),
                executionPhase = AFTER_TEST_METHOD
        )
        void userTest() {
            // run code that needs the test data to be committed
            // to the database outside of the test's transaction
        }
    }

    /**
     * @SqlConfig 进行脚本配置
     *
     * 可以配置脚本解析 / 错误处理 , 声明到类上进行全局配置（对所有Sql脚本生效,在当前测试类体系中的) ..
     * 通过@Sql注解的config属性直接声明,那么@SqlConfig 作为对应@Sql 注解的对应脚本的本地配置 ..
     * 此注解的每一个配置属性都有合理的默认值 ..
     * 由于java 语言规范的原因,注解属性值不可能为null,如果需要覆盖去全局配置,@SqlConfig属性需要有一个显式的默认值(例如"", {} / DEFAULT
     * for enum),这种方式让@SqlConfig的本地声明可以选择性的覆盖来自全局@SqlConfig的声明配置,必须提供值 ..(而不是 "",{} , DEFAULT)
     *
     * 也就是合理的默认值仅仅是为了保留覆盖的可能性(本质上是继承原有配置)
     *
     * 只要本地 @SqlConfig 属性不提供除“”、{} 或 DEFAULT 之外的显式值，就会继承 @SqlConfig 属性。因此，显式本地配置会覆盖全局配置。
     *
     * 由@Sql 和@SqlConfig提供的配置选项等价的由ScriptUtils 以及 ResourceDatabasePopulator的支持 ...
     * 但是是<jdbc:initialize-database/ > xml配置命名空间的超集 。。
     *
     * @Sql可以进行事务管理 ..
     * 默认情况,SqlScriptsTestExecutionListener 自动推断@Sql对事务配置的 渴望的事务语义 ..
     * 但是事务的支持,需要应用上下文中的 PlatformTransactionManager支持 ..
     * 也就是说,必须出现数据源 ...(作为最小化支持)
     *
     * 假设SqlScriptsTestExecutionListener 根据数据源和事务管理器推断出来的事务语义不满足你的需求 ..
     * 你可以显式的制定(通过配置@SqlConfig的数据源和事务管理器属性配置)
     *
     * 请注意任何测试方法的事务性执行之后,默认都会自动回滚 ..
     * TransactionalTestExecutionListener 默认支持,也就是你默认是不需要在方法执行之后进行必要的数据库清理 ...
     */

    @SpringJUnitConfig
    @Transactional
    class TransactionalSqlScriptsTests {

        @Configuration
        static class TestDatabaseConfig {

            @Bean
            public DataSource dataSource() {
                return new HikariDataSource(
                        new HikariConfig() {{
                            setDriverClassName(Driver.class.getName());
                            setJdbcUrl("jdbc:h2:mem:test_transaction");
                            setUsername("root");
                            setPassword("123456");
                        }}
                );
            }
        }

        final JdbcTemplate jdbcTemplate;

        @Autowired
        TransactionalSqlScriptsTests(DataSource dataSource) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        @Test
        @Sql("/test-data.sql")
        void usersTest() {
            // verify state in test database:
            assertNumUsers(2);
            // run code that uses the test data...
        }

        int countRowsInTable(String tableName) {
            return JdbcTestUtils.countRowsInTable(this.jdbcTemplate, tableName);
        }

        void assertNumUsers(int expected) {
            assertEquals(expected, countRowsInTable("user"),
                    "Number of rows in the [user] table.");
        }
    }

    //合并和覆盖配置,通过@SqlMergeMode
    // 从 spring 5.2开始，能够合并方法级别的@Sql声明和类级别的声明
    // 允许你提供数据库设计的配置和某些通用测试数据(根据每一个测试类)并且根据每一个测试方法提供额外的,使用情况特定的测试数据 ...
    // 为了启用@Sql合并,注解测试类或者测试方法(使用@SqlMergeMode(Merge)
    // 也就是说,默认是Sql覆盖,为了禁止合并(特定测试方法 / 测试子类),你可以切换为默认模式(@SqlMergeMode(OVERRIDE) ..



}
