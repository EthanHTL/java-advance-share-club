package org.example.spring.test.review.intergration.tcf;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.*;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.cache.DefaultContextCache;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DefaultBootstrapContext;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

/**
 * @author jasonj
 * @date 2023/11/2
 * @time 22:07
 *
 * @description tcf 测试上下文框架 测试集合
 *
 *
 * 框架核心: 测试上下文管理器 / 测试上下文 / 测试执行监听器 / 智能上下文加载器
 *             tcManager / textContext / tclistener / smarttccontextloader
 **/

public class TCFTests {


    /**
     * 测试上下文
     *
     * 测试上下文封装了运行当前测试的上下文 .. 并且提供了上下文管理 以及和上下文缓存支持 ..
     *
     * 这个测试上下文将会代理行为到SmartContextLoader 去加载一个应用上下文
     */
    @Test
    public void testContext() {


    }

    /**
     * 引导器 可以通过注解在测试类上注释 ..
     *
     * 或者如果存在{@link org.springframework.test.context.web.WebAppConfiguration} 配置,则
     * 使用{@link org.springframework.test.context.web.WebTestContextBootstrapper}
     */
    @BootstrapWith(DefaultTestContextBootstrapper.class)
    public static class MyTestClass {

        protected void test() {

        }
    }

    /**
     * 负责 管理单个TestContext 并且会在每一个定义好的测试执行点上触发事件代理到注册的TestExecutionListener ..
     *
     * 测试执行点:
     * 1. 任何测试框架 步入 beforeClass / beforeAll 之前
     *
     * 其他的执行点,查看 {@link TestExecutionListener} 进行执行点钩子回调 ..
     */
    @Test
    public void testContextManager() throws Exception {

        TestContextManager manager = new TestContextManager(MyTestClass.class);

        manager.registerTestExecutionListeners(new TestExecutionListener() {
            @Override
            public void beforeTestClass(TestContext testContext) throws Exception {
                System.out.println("before test class");
            }

            @Override
            public void prepareTestInstance(TestContext testContext) throws Exception {
                System.out.println("prepareTest Instance");
            }

            @Override
            public void beforeTestMethod(TestContext testContext) throws Exception {
                System.out.println("before-test-method");
            }

            @Override
            public void beforeTestExecution(TestContext testContext) throws Exception {
                System.out.println("before-test-exection");
            }

            @Override
            public void afterTestExecution(TestContext testContext) throws Exception {
                System.out.println("after-test-execution");
            }

            @Override
            public void afterTestMethod(TestContext testContext) throws Exception {
                System.out.println("after-test-method");
            }

            @Override
            public void afterTestClass(TestContext testContext) throws Exception {
                System.out.println("after-test-class");
            }
        });

        // before test class
        manager.beforeTestClass();
        MyTestClass myTestClass = new MyTestClass();
        manager.prepareTestInstance(myTestClass);
        // 方法执行之前
        Method test = MyTestClass.class.getDeclaredMethod("test");
        manager.beforeTestMethod(myTestClass, test);
        manager.beforeTestExecution(myTestClass, test);
        manager.afterTestExecution(myTestClass,test,null);
        manager.afterTestMethod(myTestClass,test,null);
        manager.afterTestClass();
    }

    /**
     * 引导TCF
     *
     * 例如增加自定义的测试执行监听器,自定义测试上下文 或者上下文缓存 或者改变上下文加载器 ..
     *
     * 上下文加载器用来加载上下文 ..
     *
     * 如果我们自行引导,那么上下文加载器或许无用 ..
     *
     * {@link  BootstrapUtils#resolveTestContextBootstrapper(Class)}
     *
     * {@link org.springframework.test.context.junit.jupiter.SpringExtension} 会触发测试上下文管理器的创建 ..
     * 以及后续的流程 ...
     *
     *
     * 上下文引导器的 流程查看上述的方法即可 ..
     *
     *
     */
    @Test
    public void testContextBootstrap() throws Exception {
        //上下文引导

        // 引导器  可以
        DefaultTestContextBootstrapper bootstrapper = new DefaultTestContextBootstrapper();

        bootstrapper.setBootstrapContext(
                new DefaultBootstrapContext(
                        MyTestClass.class
                        ,
                        new DefaultCacheAwareContextLoaderDelegate(new DefaultContextCache())
                )
        );
        TestContextManager manager = new TestContextManager(
                // 一般使用默认的,或者web感知相关的 ..
                bootstrapper
        );

        manager.prepareTestInstance(new MyTestClass());
    }

    @Test
    public void testContextBootstrapAuto() {
        TestContextBootstrapper contextBootstrapper = BootstrapUtils.resolveTestContextBootstrapper(MyTestClass.class);
        System.out.println(contextBootstrapper.getBootstrapContext());
    }
}
