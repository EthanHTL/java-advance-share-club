package club.smileboy.app.common.deferred;


import org.springframework.beans.factory.InitializingBean;

public class DeferedValue implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("defered value");
    }
}
