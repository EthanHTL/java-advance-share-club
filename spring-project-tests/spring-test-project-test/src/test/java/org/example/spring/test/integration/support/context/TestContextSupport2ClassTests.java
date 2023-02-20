package org.example.spring.test.integration.support.context;

import org.example.spring.test.entity.User;
import org.junit.Assert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * 内嵌配置继承 ...
 *
 * 切记 需要內部类 ..(非静态内部类)
 */
@SpringJUnitConfig(TestConfig.class)
public class TestContextSupport2ClassTests {

    @Nested
    class MyChild1TestContextTests {
        @Autowired
        User user;

        @Test
        public void test() {
            Assert.assertNotNull(user);
        }
    }
}
