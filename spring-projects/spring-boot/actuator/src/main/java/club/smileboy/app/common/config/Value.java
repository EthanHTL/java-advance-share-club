package club.smileboy.app.common.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;


@Component
public class Value implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("init");
    }
}
