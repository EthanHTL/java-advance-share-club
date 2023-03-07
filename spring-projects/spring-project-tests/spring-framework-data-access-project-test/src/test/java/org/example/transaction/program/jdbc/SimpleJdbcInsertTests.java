package org.example.transaction.program.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.util.Arrays;

@Sql(scripts = "classpath:user.sql")
@SpringJUnitConfig
public class SimpleJdbcInsertTests {

    @Configuration
    public static class ConfigurationTests {


        @Bean
        public DataSource dataSource() {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:h2:mem:testdb");
            hikariConfig.setUsername("root");
            hikariConfig.setPassword("123456");
            hikariConfig.setDriverClassName(Driver.class.getName());
            return new HikariDataSource(
                 hikariConfig
            );
        }

    }

    @Autowired
    private DataSource dataSource;

    @Test
    public void test() {

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource);
        simpleJdbcInsert.setTableName("people");
        simpleJdbcInsert.executeBatch(SqlParameterSourceUtils.createBatch(
                Arrays.asList(
                        new PreparedStatementTests.User("1","zs"),
                        new PreparedStatementTests.User("2","ls")
                )
        ));
    }

}
