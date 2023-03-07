package club.smileboy.app.common.aop.commons;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


// 对原始对象的代理对象的代理
//@Order(Integer.MIN_VALUE + 2)
@Order(Integer.MIN_VALUE)
@Aspect
@Component
public class CommonAspect {


    // 动态切入点 ...
    // introduction  mixin(能力混合)
    @Pointcut(value = "this(club.smileboy.app.common.aop.commons.Lockable) && this(proxy) && target(target)", argNames = "proxy,target")
    public void inspectIntroductionWhoIs(Object proxy,Object target) {

    }



    // 这里的this(就能够拿到代理的代理)
    // APC
    @Before(value = "inspectIntroductionWhoIs(proxy,target)", argNames = "point,proxy,target")
    public void inspectIntroductionWhoThis(JoinPoint point,Object proxy, Object target) {
        System.out.println("method 签名" + point.getSignature().toString());
        System.out.println("目标对象是" + target);
//        proxy.method() advice chain invoke ...(这样你会导致死循环) .. => 为什么会出现oom ?
    }
}
