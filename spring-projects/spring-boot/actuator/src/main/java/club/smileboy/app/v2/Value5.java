package club.smileboy.app.v2;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@ConditionalOnBean(name = "v7")
public class Value5 implements InitializingBean {

    @Bean
    public Value6 value6() {
        return new Value6();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("v5");
    }
}


