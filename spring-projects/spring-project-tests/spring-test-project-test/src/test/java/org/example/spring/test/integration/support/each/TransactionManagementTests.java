package org.example.spring.test.integration.support.each;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.spring.test.entity.User;
import org.example.spring.test.entity.UserRepository;
import org.h2.Driver;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * 事务管理
 * 事务管理是通过TransactionTestExecutionListener 执行的 .. 默认配置的 .
 * 1. 在测试中开启事务管理很简单,配置一个使用@ContextConfiguration语义配置的PlatformTransactionManager bean ..
 * 2. 通过@Transactional 注解开启声明式事务管理
 *
 *
 * 测试管理的事务通过TransactionalTestExecutionListener 声明式管理或者通过TestTransaction 编程式管理 ..
 *
 * 不管是spring事务 / application 管理的编程式事务 都能够参与到测试管理的事务中 ..
 *
 * 如果spring管理的或者应用管理的事务配置是非REQUIED 或者 SUPPORTS的情况下需要小心 ..
 *
 * 抢占式超时和测试管理的事务:
 *   spring测试支持绑定事务状态到当前线程(通过java.lang.ThreadLocal 变量) - 当 当前测试方法执行之前 ..
 *   如果一个测试框架在新的线程中执行当前测试方法 - 为了支持一个抢占式超时,在当前测试方法中的任何执行的 动作将
 *   不会在测试管理的事务中执行 ... 因此测试管理的事务将不会被任何action负责 ..
 *
 *   相反,任何action可能会提交到持久化存储,就算测试事务已经可能被spring 正确回滚 ..
 *   可能发生的情况如下，但是不仅限以下情况:
 *   1. JUnit 4的@Test(timeout=...) 支持 以及 TimeOut 规则 ..
 *   2. 在org.junit.jupiter.api.Assertions类中的 JUnit Jupiter的 assertTimeoutPreemptively(...)方法
 *   3.TestNG 的 @Test(timeout = ...) support
 *
 *
 *
 * 任何事务方法在测试执行完毕之后都会自动的回滚,如果测试类注解了@Transactional,那么测试类的每一个方法都运行在事务中 ..
 * 注意到当前@Transactional 不支持在测试生命周期方法上 ..(举个例子, JUnit Jupiter的@BeforeAll / @BeforeEach等等）
 *
 * 其次,@Transactional 标注了,但是使用了NOT_SUPPORT / NEVER 的传播属性,则永远不会运行在一个事务中 ..
 *
 *
 * https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-tx
 * 方法级别的生命周期方法,例如JUnit Jupiter的@BeforeEach ... 都会运行在测试管理的事务中 ..
 * 套件级别 / 类级别的生命周期方法(例如,被JUnit Jupiter的@BeforeAll / AfterAll 等方法 或者 TestNG的 @BeforeSuite
 *  / @AfterSuite / @BeforeClass / @AfterClass等生命周期方法都不会运行在测试管理的事务中)
 *
 *  为了能够在这些方法內运行在事务中,需要注册PlatformTransactionManager到测试类中并启用TransactionTemplate 进行编程式事务
 *  管理 ..
 *
 *  Note that AbstractTransactionalJUnit4SpringContextTests and AbstractTransactionalTestNGSpringContextTests
 *  are preconfigured for transactional support at the class level.
 *
 *
 * 为了显式的配置提交还是回滚
 * 可以使用@Commit / @Rollback
 *
 *
 */
public class TransactionManagementTests {
    @SpringJUnitWebConfig(classes = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @Transactional
    public static class TransactionalBasedClassTests {

//        @Autowired
//        HibernateUserRepository repository;

        @Resource
        SessionFactory sessionFactory;

        JdbcTemplate jdbcTemplate;
        
        @Autowired
        void setDataSource(DataSource dataSource) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }

        @Configuration
        public static class TransactionalConfig {

        }

        @Test
        void createUser() {
            // track initial state in test database:
            final int count = countRowsInTable("user");

//            User user = new User(...);
//            repository.save(user);

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

    // 编程式事务管理
    // 通过TestTransaction中的静态方法可以进行编程式测试管理的事务交互
    // 通过在测试方法中 / before method / after method 去开启或者结束当前的test-managed 事务或者配置当前的test-managed事务回滚
    // 还是提交 ..
    // TestTransaction在TransactionTestExecutionListener启用时自动可用 ..

    @SpringJUnitWebConfig
    public static class ProgrammaticTransactionManagementTests extends AbstractTransactionalJUnit4SpringContextTests {

        /**
         * 这个配置中需要提供具有ContextConfiguration语义配置的 事务管理器 ..
         *
         * 这个示例仅仅只是展示,不支持运行 .. 因为没有对应的数据库配置可用 ..
         */
        @Configuration
        static class Configure {


        }

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


    // 事务之外运行代码
    // 有时你可能需要在事务测试方法前后执行某些非事务性代码 ...
    // 例如初始化数据库状态 /或者在测试运行之后校验期待的事务性提交行为 ..
    // TransactionalTestExecutionListener 支持@BeforeTransaction / @AfterTransaction 注解 ...
    // 来实现这种场景 ..
    // 你能够注解到任何void 方法上（测试类)或者 任何测试接口的default方法上 ...
    // 剩下的交给 TransactionalTestExecutionListener即可 ..
    // 我们知道任何before / after方法是运行在事务中的,
    // 对于没有被配置为在事务中运行的测试方法(它执行不会触发对应的注解注释方法 BeforeTransaction / AfterTransaction)， @BeforeTransaction或@AfterTransaction注释的方法不会运行。


    @SpringJUnitConfig
    static class BeforeTransactionEtcTests extends AbstractTransactionalJUnit4SpringContextTests {

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

            @Bean
            public TransactionManager transactionManager() {
                return new JdbcTransactionManager(dataSource());
            }


        }

        @BeforeTransaction
        public void transactionBefore() {
            System.out.println("事务之前");
        }

        @Test
        public void test() {
            // 事务方法
            System.out.println("事务中");
        }

        @AfterTransaction
        public void transactionAfter() {
            System.out.println("事务之后");
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void noTransactionTest() {
            System.out.println("no transaction");
        }

    }


    // jpa 相关的false positive ...
    // 需要实际进行代码刷新,否则 本应该出现的问题(可能被逃狱了,称为false positive)
    // https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-tx-false-positives

    @EnableJpaRepositories(basePackages = "org.example.spring.test.entity")
    @EntityScan(basePackages = "org.example.spring.test.entity")
    @Configuration
    @TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=update","spring.jpa.database=h2","spring.jpa.show-sql=false"})
    public static class DatabaseConfig {
        @Bean
        public DataSource dataSource() {
            return new HikariDataSource(
                    new HikariConfig() {{
                        setDriverClassName(Driver.class.getName());
                        setJdbcUrl("jdbc:h2:mem:testdb");
                        setUsername("root");
                        setPassword("123456");
                    }}
            );
        }

        @Bean
        public TransactionManager transactionManager(EntityManagerFactory entityManager) {
            return new JpaTransactionManager(entityManager);
        }
    }
    @SpringJUnitConfig
    @Sql(scripts = {"classpath:user.sql"})
    @ContextConfiguration(classes = {DatabaseConfig.class,HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
    public static class FalsePositiveTests  extends AbstractTransactionalJUnit4SpringContextTests {



        @Autowired
        JpaContext jpaContext;

        @PersistenceContext
        EntityManager entityManager;


        @Autowired
        UserRepository userRepository;


        JdbcTemplate jdbcTemplate;

        @Autowired
        void setJdbcTemplate(DataSource dataSource) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }



        @BeforeEach
        public void before() {
            jpaContext.getEntityManagerByManagedType(User.class);
            System.out.println("before - before");
        }

        @BeforeTransaction
        public void transactionBefore() {
            System.out.println("执行 。。。。 之前");
        }

        @Transactional
        @org.junit.Test // no expected exception!
        public void falsePositive() {
            updateEntityInJpaPersistenceContext();
            // False positive: an exception will be thrown once the JPA
            // EntityManager is finally flushed (i.e., in production code)
        }

        @Transactional
        @org.junit.Test
        public void updateWithEntityManagerFlush() {
            updateEntityInJpaPersistenceContext();
            // Manual flush is required to avoid false positive in test
            entityManager.flush();


        }

        /**
         * 假阳性 false positive ..
         * // 看起来在事务下是保存了,但是实际上没有save的情况下,根本拿不到想要的数据 .. 但是测试用例通过(前提是 事务管理器用错了
         * 我这里才出现了这种问题,无法获取数据 ..(之前使用的是jdbc的事务管理器))
         *
         *
         * 但是这依旧没有 理解假阳性 ..
         *
         *
         * https://stackoverflow.com/questions/3562399/need-explanation-on-the-necessity-of-a-prior-flushing-to-avoid-false-positives-w
         *
         * 一般来说 jpa的实现是hibernate
         * hibernate 有多级缓存 ..
         * 在事务性测试方法中,默认测试方法执行完毕之后会尝试rollback ..
         * 那么如果我们手动调用flush,那么 正常情况是我们需要在提交之前刷新所有的entity改变 翻译为 sql statement ..
         * 如果我们没有刷新,那么可能存在sql statement执行不完整的情况,按道理来说 回滚默认应该报错 ..
         * 但是 单元测试默认也有一个特性,就算底层系统执行失败,那么测试也应该成功(我不知道这一句话是否存在误导性)
         * 但是 我们可能在测试代码中会看到 false positive(也就是测试成功了,那么在生产环境 sql statement 翻译的差异性可能会更大,这可能导致
         * 没有刷新的情况下,与数据库的差异性非常大 - 也就是说一旦我们刷新缓存必然会出现 constraint violation  ...
         * 这也就是 避免false positive的解决方式就是需要刷新缓存同步到数据库,保证一致性,从而正确事务处理 ...
         *
         * 就理解成这样,如果后续理解到更深的含义 .. 继续改进 ...
         *
         *
         * 这里的理解可能存在较大的问题 (需要理解 constraint violation 不一定是数据库给我们的,有可能是转换为sql statement过程中检测出来的问题)
         * https://www.yuque.com/gaolengdehulusi/mfydqu/os1dcoo5f1nnpg5q
         *
         */
        private void updateEntityInJpaPersistenceContext() {
            User user = new User();
            user.setName("1213");
            entityManager.persist(user);
            //userRepository.save(user);

            entityManager.flush();

            System.out.println(userRepository.findByNameIs("1231"));

            System.out.println("find all");
            System.out.println(userRepository.count());

           //  System.out.println("----------------");
            // System.out.println(jdbcTemplate.query("select * from tuser", new DataClassRowMapper<>(User.class)));
        }
    }
}
