package club.smileboy.app.common.aop;

import club.smileboy.app.v2.Value6;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

//@Lazy
@Component
public class LazyComponent  implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("lazy component initialize ...");
    }


    @Bean
    public Value6 value6() {
        return new Value6() {{
            printf();
        }};
    }
}
