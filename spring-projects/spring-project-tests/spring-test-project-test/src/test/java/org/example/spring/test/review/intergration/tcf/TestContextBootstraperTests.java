package org.example.spring.test.review.intergration.tcf;

import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.test.context.*;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;

import java.util.LinkedList;
import java.util.List;

/**
 * @author jasonj
 * @date 2023/11/3
 * @time 16:48
 *
 * @description {@link org.springframework.test.context.TestContextBootstrapper} 的测试使用
 **/
public class TestContextBootstraperTests {

    public static class TestContextBootstraperTestClass {

        @Resource
        private Version version;

        public void test() {

        }
    }


    public static class Version {

    }
    @Test
    public void test() throws Exception {


        DefaultTestContextBootstrapper bootstrapper = new DefaultTestContextBootstrapper(){
            @Override
            protected List<ContextCustomizerFactory> getContextCustomizerFactories() {
                List<ContextCustomizerFactory> factories = super.getContextCustomizerFactories();
                LinkedList<ContextCustomizerFactory> list = new LinkedList<>(factories);

                // 这里使用了一些 上下文自定义器,查看 spring的其他实现了解其他使用方式 ...
                list.add(new ContextCustomizerFactory() {
                    @Nullable
                    @Override
                    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
                        return new ContextCustomizer() {
                            @Override
                            public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
                                if (context.getBeanFactory() instanceof BeanDefinitionRegistry) {
                                    BeanDefinitionRegistry factory = (BeanDefinitionRegistry) context.getBeanFactory();
                                    factory.registerBeanDefinition(
                                            "versionHandle",
                                            new RootBeanDefinition(
                                                    Version.class
                                            )
                                    );
                                }
                            }
                        };
                    }
                });
                return list;
            }
        };
        bootstrapper.setBootstrapContext(new DefaultBootstrapContext(
                TestContextBootstraperTestClass.class,
                new DefaultCacheAwareContextLoaderDelegate()
        ));
        TestContextManager testContextManager = new TestContextManager(bootstrapper);

        TestContextBootstraperTestClass testInstance = new TestContextBootstraperTestClass();
        testContextManager.prepareTestInstance(testInstance);


        Assert.assertNotNull(testInstance.version);

    }
}
