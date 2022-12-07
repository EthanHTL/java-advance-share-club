package club.smileboy.app.common.aop.weave;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.weaving.AspectJWeavingEnabler;
import org.springframework.context.weaving.DefaultContextLoadTimeWeaver;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.util.Assert;

import static org.springframework.context.ConfigurableApplicationContext.LOAD_TIME_WEAVER_BEAN_NAME;

public class BugFixApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ClassLoader beanClassLoader = applicationContext.getBeanFactory().getBeanClassLoader();


        Assert.state(beanClassLoader != null, "No ClassLoader set");
        LoadTimeWeaver loadTimeWeaver = new DefaultContextLoadTimeWeaver(beanClassLoader);

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String property = environment.getProperty("spring.aspectj.enableLTW","DISABLED");

        try {
            EnableLoadTimeWeaving.AspectJWeaving aspectJWeaving = EnableLoadTimeWeaving.AspectJWeaving.valueOf(property);
            switch(aspectJWeaving) {
                case DISABLED:
                default:
                    // 不支持 ..
                    throw new UnsupportedOperationException();
                case AUTODETECT:
                    if (beanClassLoader.getResource("META-INF/aop.xml") != null) {
                        AspectJWeavingEnabler.enableAspectJWeaving((LoadTimeWeaver)loadTimeWeaver, beanClassLoader);
                    }
                    break;
                case ENABLED:
                    AspectJWeavingEnabler.enableAspectJWeaving((LoadTimeWeaver)loadTimeWeaver, beanClassLoader);
            }
        }catch (Exception e) {
            return ;
        }

        applicationContext.getBeanFactory()
                .registerSingleton(LOAD_TIME_WEAVER_BEAN_NAME,loadTimeWeaver);

        System.out.println("提前注入了 load_time_weaver !!!!");
    }
}
