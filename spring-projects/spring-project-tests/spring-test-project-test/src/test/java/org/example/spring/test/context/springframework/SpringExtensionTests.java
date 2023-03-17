package org.example.spring.test.context.springframework;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Nested;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author FLJ
 * @date 2023/2/21
 * @time 15:45
 * @Description 使用SpringExtension Api 进行 依赖注入,包括 test constructors, test methods, and test lifecycle callback methods.
 *
 * Specifically, SpringExtension can inject dependencies from the test’s ApplicationContext into test constructors and methods
 * that are annotated with @BeforeAll, @AfterAll, @BeforeEach, @AfterEach, @Test, @RepeatedTest, @ParameterizedTest, and others.
 *
 * 默认@SpringJunitConfig 或者@SpringJunitWebConfig 都包含了@ExtendWith(SpringExtension.class)
 * 所以可以直接使用 ... 并且你需要增加对应的依赖注入注解,否则无法使用Spring的扩展功能 ...
 *
 *
 */
@SpringJUnitConfig
//@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpringExtensionTests {

    static class A {

    }

    @Configuration
    public static class MyConfiguration {

        @Bean
        public A a() {
            return new A();
        }

        @Bean
        public A b() {
            return new A();
        }
    }

    @BeforeAll
    public void beforeAll(@Autowired A a) {
       Assertions.assertNotNull(a);
    }


    @Test
    public void test(@Autowired A b) {
        Assertions.assertNotNull(b);
    }

}
