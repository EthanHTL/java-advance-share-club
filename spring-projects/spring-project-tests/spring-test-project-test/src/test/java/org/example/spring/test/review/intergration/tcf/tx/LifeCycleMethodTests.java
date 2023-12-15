package org.example.spring.test.review.intergration.tcf.tx;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionContext;

import javax.sql.DataSource;

/**
 * @author jasonj
 * @date 2023/12/11
 * @time 15:35
 *
 * @description 生命周期方法测试 是否运行在事务中
 **/
@SpringJUnitConfig
public class LifeCycleMethodTests {

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

    @Nested
    public class SubTestClass {


    }


    @Transactional
    @Test
    public void test() {
        System.out.println("test 事务是否激活: " + TestTransaction.isActive());


    }


    @BeforeEach
    public void each() {
//        TestTransaction.start();
        System.out.println("beforeEach 事务是否激活: " + TestTransaction.isActive());
    }

    @BeforeTransaction
    public void beforeTransaction() {
        System.out.println("beforeTransaction 事务是否激活: " + TestTransaction.isActive());
    }

    @AfterTransaction
    public void afterTransaction() {
        System.out.println("afterTransaction 事务是否激活: " + TestTransaction.isActive());
    }
}
