package club.smileboy.app.common.aop.weave.bean;

import org.springframework.stereotype.Component;

/**
 * @author FLJ
 * @date 2022/12/7
 * @time 10:46
 * @Description 计算服务
 */
public class EntitlementCalculationService {


    public void calculateEntitlement() throws InterruptedException {
        //Thread.sleep(2000);
        System.out.println("计算 ...");
    }
}
