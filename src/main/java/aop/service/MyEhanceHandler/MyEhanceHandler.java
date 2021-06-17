package aop.service.MyEhanceHandler;

import aop.service.simple.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author JASONJ
 * @dateTime: 2021-05-31 10:41:22
 * @description: handler
 */
@Service
public class MyEhanceHandler {

    @Autowired
    private MyService myService;

    @Autowired
    private AnnotationConfigApplicationContext applicationContext;

    private int index = 0;

    public MyService enhance(){
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(MyService.class);
        enhancer.setCallback(new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
//                在此处证实 perThis,perTarget并无较大差异 也不知道spring aop 系统如何定义 this 和target的一个关系
                // aspect中 this 和 target 都指向一个对象
                // 所以只需要关心代理对象是否为同一个,如果不是 那么切面也会重新创建!
//                return method.invoke(myService,objects);
                applicationContext.registerBean("myService"+index,MyService.class);
                return method.invoke(applicationContext.getBean("myService"+index ++),objects);
            }
         });
        final Object o = enhancer.create();
        return (MyService) o;
    }
}
