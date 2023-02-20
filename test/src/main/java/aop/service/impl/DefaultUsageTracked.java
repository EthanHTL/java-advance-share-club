package aop.service.impl;

import aop.service.UsageTracked;
import org.springframework.stereotype.Component;

/**
 * @author JASONJ
 * @dateTime: 2021-05-31 09:11:46
 * @description: default impl
 */
public class DefaultUsageTracked implements UsageTracked {

    @Override
    public void incrementUseCount() {
        System.out.println("增加了用户账户数量");
    }

    public void printf(){
        System.out.println("打印之前就修改了账户数量!");
    }
}
