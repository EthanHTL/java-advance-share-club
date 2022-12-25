package aop.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author JASONJ
 * @dateTime: 2021-05-31 10:15:32
 * @description: perthis
 */
@Aspect("perthis(execution(* aop.service.simple.*.*(..)))")
//@Component
//@Scope(scopeName = "prototype")
public class PerThisAspect {

    private int somState = 0;

    // 如果是相同this(表示相同的被通知的对象上的方法),那么somState 将会剧增
    // 由于这里perthis,所以目标对象每次都是新创建的一个,那么somstate 依旧是0 因为切面是新对象
    @Before("execution(* aop.service.simple.*.*(..))")
    public void recordServiceUsage() {
        System.out.println(somState ++ );
    }
}
