package org.example.spring.test.review.intergration.tcf;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

/**
 * @author jasonj
 * @date 2023/11/3
 * @time 14:40
 *
 * @description TestPropertySource的测试
 **/

@TestPropertySource(properties = {"k1=v1"})
@TestPropertySource(properties = {"k2=v2"})
@TestPropertySource(properties = {"k3=v3"},inheritProperties = false)
@TestPropertySource(properties = {"k4=v4"})
@TestPropertySource(properties = {"k5=v5"})
@SpringJUnitConfig
public class TestPropertySourceTests {


    /**
     * 执行失败,因为 测试上下文引导器在合并 属性的时候会校验
     *
     * {@link org.springframework.test.context.support.TestPropertySourceUtils#mergeTestPropertySourceAttributes(List)}}
     * 其中
     * {@code  TestPropertySourceAttributes#mergeWith(TestPropertySourceAttributes)} 会校验{@link TestPropertySource} 以及
     * 上的inheritxxx相关属性必须一致 ..(在多个属性源出现的情况下)h
     * @param environment 环境
     */
    @Test
    public void test(Environment environment) {
        String k5 = environment.getProperty("k5");
        Assert.assertEquals("v5",k5);
    }

}
