package club.smileboy.app.common.aop.commons.candidate;

import org.springframework.stereotype.Component;

@Component
public class DefaultService {

    public void printf() {
        System.out.println("defaultService !!!");
    }
}
