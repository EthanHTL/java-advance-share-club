package org.example.spring.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

/**
 * @date 2022/12/24
 * @time 20:05
 * @author FLJ
 * @since 2022/12/24
 *
 * 上下文体系
 *
 * 也就是TCF 支持上下文体系的测试支持,  顶层的应用上下文作为其他上下文的父上下文用于共享组件等其他基础设施 ..
 * 最终自动装配的也是子应用上下文 ...
 *
 * 在上下文体系中的脏上下文
 *  如果在测试中使用@DirtiesContext,那么这个应用上下文将配置为上下文体系的一部分,你能使用hierarchyMode 标志来控制
 *  如何清理这个上下文的缓存 ...
 *
 **/
public class ContextHierachiesTests {
    /**
     * 抽象 顶层应用上下文
     */
    @Configuration
    static class TestAppConfig {

    }

    /**
     * 子类web ctx
     */
    @Configuration
    static class WebConfig {

    }

    //简单使用
    @ExtendWith(SpringExtension.class)
    @WebAppConfiguration
    @ContextHierarchy({
            @ContextConfiguration(classes = TestAppConfig.class),
            @ContextConfiguration(classes = WebConfig.class)
    })
    static class ControllerIntegrationTests {

        @Autowired
        WebApplicationContext wac;

        // ...
    }



    @Configuration
    static class AbstractConfig {

    }

    @ExtendWith(SpringExtension.class)
    @WebAppConfiguration
    @ContextConfiguration(classes = AbstractConfig.class)
    public  static abstract class AbstractWebTests {}

    @Configuration
    static class SoapConfig {

    }

    @ContextHierarchy(@ContextConfiguration(classes = SoapConfig.class))
    public static class SoapWebServiceTests extends AbstractWebTests {

        @Test
        public void test() {
            // noting
        }
    }


    // 类体系中 named-level 配置合并(指定命名的 level)
    // 当named level 不一致,则标识使用了不同level的配置 ..

    // 以下仅为示例,无法执行
    @ExtendWith(SpringExtension.class)
    @ContextHierarchy({
            @ContextConfiguration(name = "parent", locations = "/app-config.xml"),
            @ContextConfiguration(name = "child", locations = "/user-config.xml")
    })
    static class BaseTests {}

    @ContextHierarchy(
            @ContextConfiguration(name = "child", locations = "/order-config.xml")
    )
    static class ExtendedTests extends BaseTests {}



    // 类体系中 named-level 覆盖
    // 本质上还是使用的是 @ContextConfiguration的 特性 ..
    @ExtendWith(SpringExtension.class)
    @ContextHierarchy({
            @ContextConfiguration(name = "parent", locations = "/app-config.xml"),
            @ContextConfiguration(name = "child", locations = "/user-config.xml")
    })
    static class Base1Tests {}

    @ContextHierarchy(
            @ContextConfiguration(
                    name = "child",
                    locations = "/test-user-config.xml",
                    inheritLocations = false
            ))
    static class Extended1Tests extends BaseTests {}



}
