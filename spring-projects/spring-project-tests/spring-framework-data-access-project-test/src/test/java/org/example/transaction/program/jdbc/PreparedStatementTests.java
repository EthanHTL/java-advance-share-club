package org.example.transaction.program.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.Driver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringJUnitConfig
public class PreparedStatementTests {

    private DataSource dataSource;

    @BeforeAll
    public void dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:h2:mem:testdb");
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("123456");
        hikariConfig.setDriverClassName(Driver.class.getName());
        this.dataSource = new HikariDataSource(hikariConfig);


        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        System.out.println(jdbcTemplate.execute("create table people(" +
                "id varchar(20) default not null," +
                "username varchar(20) default not null)", new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                return ps.execute();
            }
        }));

//        jdbcTemplate.execute("show tables", new PreparedStatementCallback<Object>() {
//            @Override
//            public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
//                ResultSet resultSet = ps.getResultSet();
//                ResultSetMetaData metaData = ps.getMetaData();
//                int columnCount = metaData.getColumnCount();
//                while(resultSet.next()) {
//                    List<Object> values = new LinkedList<>();
//                    for (int i = 0; i < columnCount; i++) {
//                        Object object = resultSet.getObject(i + 1);
//                        values.add(object);
//                    }
//
//                    System.out.println(values);
//                }
//                return "success";
//            }
//        });

        jdbcTemplate.batchUpdate("insert into people values(:id,:username)",
                SqlParameterSourceUtils.createBatch(
                        Arrays.asList(
                                new User("1","zs"),
                                new User("2","ls")
                        )
                )
        );
    }


    /**
     * 这种情况下,没有对null 有很好的类型转换 ...
     *
     * 所以要么为MapSqlParameterSource 注册sql 类型,或者直接使用
     * BeanPropertySqlParameterSource 直接根据bean 属性进行sql 类型抓取 ..
     *
     * 但是基于bean 属性的方式,最好基于NamedParameterTemplate 操作类使用 ...
     * 通过SqlParameterSourceUtils.create 创建SqlParameterSource ..
     */
    @Test
    public void test() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<Object[]> values = Arrays.asList(
                new Object[] {"1","zs"},
                new Object[]{"2","ls"}
        );

        jdbcTemplate.batchUpdate("update people set username = ? where id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] objects = values.get(i);
                        for (int i1 = 0; i1 < objects.length; i1++) {
                            ps.setObject(i1 + 1,objects[i]);
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return values.size();
                    }
                });
    }

    static class User {
        private String username;

        private String id;

        public User(String id,String username) {
            this.id = id;
            this.username = username;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return "User{" +
                    "username='" + username + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

    // 上面的示例改进为 ..
    @Test
    public void test1() {
        NamedParameterJdbcTemplate parameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        parameterJdbcTemplate.batchUpdate("update people set username = :username where id = :id",
                SqlParameterSourceUtils.createBatch(
                        Arrays.asList(
                                new User("1","zs"),
                                new User("2","ls")
                        )
                ));
    }

    //当然你可以使用mapSqlParameterSource 并注册sql 类型 ..

    @Test
    public void test2() {
        NamedParameterJdbcTemplate parameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        List<Map<String,Object>>  values=  Arrays.asList(
                new HashMap<>() {
                    {
                        put("id", "1");
                        put("username","zs-modify");
                    }
                },
                new HashMap<>() {
                    {
                        put("id", "2");
                        put("username","zs-modify");
                    }
                }
        );

        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(values);
        for (SqlParameterSource sqlParameterSource : batch) {
            MapSqlParameterSource sqlParameterSource1 = (MapSqlParameterSource) sqlParameterSource;
            sqlParameterSource1.registerSqlType("id", StatementCreatorUtils.javaTypeToSqlParameterType(String.class));
            sqlParameterSource1.registerSqlType("username",StatementCreatorUtils.javaTypeToSqlParameterType(String.class));
        }

        int[] ints = parameterJdbcTemplate.batchUpdate(
                "update people set username = :username where id = :id",
                batch
        );


        List<User> users = parameterJdbcTemplate.query("select * from people", new DataClassRowMapper<>(User.class));
        for (User user : users) {

            System.out.println(user);
        }


    }
}
