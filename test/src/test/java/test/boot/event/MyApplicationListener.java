package test.boot.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author JASONJ
 * @dateTime: 2021-05-26 17:47:42
 * @description: applicationListener
 */
public class MyApplicationListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        System.out.println(applicationEvent.getTimestamp());
    }
}
