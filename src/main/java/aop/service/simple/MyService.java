package aop.service.simple;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author JASONJ
 * @dateTime: 2021-05-31 09:31:50
 * @description: service
 */
@Service("myService")
//@Scope(scopeName = "prototype")
public class MyService {
    public void test(){
        System.out.println("测试并触发代理!");
    }
}
