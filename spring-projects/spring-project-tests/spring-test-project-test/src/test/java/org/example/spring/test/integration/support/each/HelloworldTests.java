package org.example.spring.test.integration.support.each;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @date 2022/12/24
 * @time 11:17
 * @author FLJ
 * @since 2022/12/24
 * hello world tests
 **/
public class HelloworldTests {

    @SpringJUnitConfig
    public static class MyApplicationTest {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        public void test() {
            System.out.println("this application context is  null = " + (applicationContext == null));
        }
    }
}
