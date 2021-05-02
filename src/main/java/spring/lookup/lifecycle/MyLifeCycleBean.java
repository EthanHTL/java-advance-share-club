package spring.lookup.lifecycle;

import org.springframework.context.Lifecycle;

/**
 * @author JASONJ
 * @dateTime: 2021-05-02 10:50:11
 * @description: life cycle test
 */
public class MyLifeCycleBean implements Lifecycle {
    @Override
    public void start() {
        System.out.println("my life cycle bean 启动");
    }

    @Override
    public void stop() {
        System.out.println("my life cycle bean stop!");
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
