package aop.aop;

import aop.service.UsageTracked;
import aop.service.impl.DefaultUsageTracked;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UsageTracking {

    // 给MyService增强
    @DeclareParents(value="aop.service.simple.MyService", defaultImpl= DefaultUsageTracked.class)
    public static UsageTracked mixin;

    @Pointcut("execution(* aop.service.simple.*.*(..))")
    public void pointCut(){

    }
    @Before(" pointCut() && this(usageTracked)")
    public void recordUsage(JoinPoint point,UsageTracked usageTracked) {
        System.out.println(point.getTarget() + "   "+ point.getThis());
        usageTracked.incrementUseCount();
    }

}
