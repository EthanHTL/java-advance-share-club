package org.example.spring.test.review.util;

import org.example.spring.test.review.util.it.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.*;
import org.springframework.lang.Nullable;
import org.springframework.test.util.AopTestUtils;

import java.lang.reflect.Method;

/**
 * @author jasonj
 * @date 2023/10/28
 * @time 20:23
 *
 * @description 使用 AOP Test Utils
 **/
public class UseAopTestUtils {

    @Test
    public void getNativeTarget() {


        DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();

        AdvisedSupport support = new AdvisedSupport(
                User.class
        );

        User target = new User() {
            @Override
            public String getName() {
                return "mock-name";
            }

            @Override
            public String getPassword() {
                return "mock-password";
            }
        };
        support.setTarget(
                target);
        support.addAdvice(new MethodBeforeAdvice() {
            @Override
            public void before(Method method, Object[] args, @Nullable Object target) throws Throwable {
                System.out.println("方法执行之前");
            }
        });


        Object aopProxy = proxyFactory.createAopProxy(support).getProxy();


        Object object = AopTestUtils.getTargetObject(aopProxy);

        Assertions.assertSame(target, object);


        User proxy = (User) aopProxy;
        proxy.getName();
    }
}
