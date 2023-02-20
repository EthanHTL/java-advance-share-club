package org.example.spring.test.integration.support.each;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.spring.test.service.MyTestService;
import org.example.spring.test.service.TestService;
import org.h2.Driver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.*;
import org.springframework.context.weaving.LoadTimeWeaverAwareProcessor;
import org.springframework.core.NativeDetector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 切面和 事务的处理 ...
 */
@SpringBootTest
public class AspectWithTransactionTests {




    @Configuration
    @EnableAspectJAutoProxy
    @EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
    @EnableLoadTimeWeaving
    @ComponentScan(basePackages = "org.example.spring.test.service")
    public static class AspectConfig {



        @Bean
        public DataSource dataSource() {
            return new HikariDataSource(new HikariConfig() {{
                setUsername("user");
                setPassword("1234");
                setJdbcUrl("jdbc:h2:mem:testdb");
                setDriverClassName(Driver.class.getName());
            }});
        }


        @Bean
        public PlatformTransactionManager transactionManager() {
            return new JdbcTransactionManager(dataSource());
        }

//        @Bean
//        public TestService testService() {
//            return new MyTestService();
//        }
    }


    @Autowired
    private TestService testService;

    @Test
    public void test() {
        testService.test();
    }
}
