package club.smileboy.app.common.config;

import club.smileboy.app.common.condition.MyConfigurationClassCondition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * phase_configuration 什么时候执行
 */
@Conditional(MyConfigurationClassCondition.class)
@ConditionalOnBean(name = "value4")
@Configuration
public class Value3 implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("value3");
    }
}
