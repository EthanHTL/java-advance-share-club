package org.example.spring.test.service;

import org.example.spring.test.integration.support.each.ContextTypeMatchClassLoader;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.weaving.AspectJWeavingEnabler;
import org.springframework.context.weaving.DefaultContextLoadTimeWeaver;
import org.springframework.context.weaving.LoadTimeWeaverAwareProcessor;
import org.springframework.core.NativeDetector;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;

public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        // 启动编织
        if (InstrumentationLoadTimeWeaver.isInstrumentationAvailable()) {
            if (!NativeDetector.inNativeImage() && applicationContext.getBeanFactory().getTempClassLoader() == null) {
                beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
                beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));

                AspectJWeavingEnabler.enableAspectJWeaving(new DefaultContextLoadTimeWeaver(beanFactory.getBeanClassLoader()),beanFactory.getBeanClassLoader());
            }
        }
    }
}
