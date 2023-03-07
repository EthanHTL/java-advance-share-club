package org.example.transaction.program.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 没有什么需要测试的 ..
 */
public class JdbcTemplateTests {

    private DataSource dataSource;

    @BeforeAll
    public void dataSourceConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mem:testdb");
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("123456");
        hikariConfig.setDriverClassName(Driver.class.getName());
        this.dataSource = new HikariDataSource(hikariConfig);
    }
    @Test
    public void test() {
    }
}
