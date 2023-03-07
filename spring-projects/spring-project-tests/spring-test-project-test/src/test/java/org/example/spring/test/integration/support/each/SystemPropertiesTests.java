package org.example.spring.test.integration.support.each;

import org.junit.jupiter.api.Test;

/**
 * @date 2022/12/24
 * @time 13:20
 * @author FLJ
 * @since 2022/12/24
 *
 *
 * 系统环境变量测试
 **/
public class SystemPropertiesTests {

    @Test
    public void test() {
        System.getProperties().forEach((key,value) -> {
            System.out.println("key " + key + "-> value " + value);
        });
    }
}
