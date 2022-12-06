package proxy;

import club.smileboy.app.common.aop.AopTargetBean;
import club.smileboy.app.common.aop.AopTargetBean1;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JdkProxyTests {
    @Test
    public void test() {

        final AopTargetBean1 aopTargetBean1 = new AopTargetBean1();
        final Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{AopTargetBean.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(aopTargetBean1, args);
            }
        });

    }
}
