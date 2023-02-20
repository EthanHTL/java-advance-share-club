package spring.lookup.lifecycle;

import org.springframework.context.SmartLifecycle;

/**
 * @author JASONJ
 * @dateTime: 2021-05-02 11:02:34
 * @description: graceful bean
 */
public class GraceFulBean implements SmartLifecycle {
//    @Override
//    public boolean isAutoStartup() {
//        return SmartLifecycle.super.isAutoStartup();
//    }
//
//    @Override
//    public int getPhase() {
//        return SmartLifecycle.super.getPhase();
//    }

    @Override
    public void start() {
        System.out.println("开始");
    }

    @Override
    public void stop() {
        System.out.println("停止");
    }

    /**
     * 用来干嘛
     * @return
     */
    @Override
    public boolean isRunning() {
        return false;
    }
}
