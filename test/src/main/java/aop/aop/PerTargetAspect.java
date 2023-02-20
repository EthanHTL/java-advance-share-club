package aop.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author JASONJ
 * @dateTime: 2021-05-31 10:27:58
 * @description: perTarget
 */
@Aspect("pertarget(execution(* aop.service.simple.*.*(..)))")
@Component
@Scope("prototype")
public class PerTargetAspect {

    private int somState = 0;


    @Before("execution(* aop.service.simple.*.*(..))")
    public void recordServiceUsage() {
        System.out.println(somState ++ );
    }
}
