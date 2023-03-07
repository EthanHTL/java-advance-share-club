package club.smileboy.app.common.aop.commons;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// proxy A   / proxy proxy B

// 对原始对象的代理
@Order(Integer.MIN_VALUE + 1)
@Aspect
@Component
public class IntroductionAspect {

    @DeclareParents(value = "club.smileboy.app.common.aop.commons.candidate..*",defaultImpl = DefaultLockable.class)
    public Lockable lockable;


    @Pointcut(value = "execution(* club.smileboy.app.common.aop.commons.candidate..*.*(..)) && this(proxy)", argNames = "proxy")
    public void lockableRecord(Lockable proxy) {

    }

    // proxy = > A

    @Before(value = "lockableRecord(proxy)", argNames = "proxy")
    public void lockablePrintf(Lockable proxy) {

        System.out.println("执行lockable 处理,目标对象不知道 !!!");
        // B
        proxy.isLock();

    }
}
