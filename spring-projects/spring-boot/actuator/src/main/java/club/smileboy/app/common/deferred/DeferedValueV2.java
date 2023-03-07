package club.smileboy.app.common.deferred;

import org.springframework.beans.factory.InitializingBean;

public class DeferedValueV2 implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        // sout
        System.out.println("deferred value 2");
    }
}
