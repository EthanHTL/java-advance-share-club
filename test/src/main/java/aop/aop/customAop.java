package aop.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author JASONJ
 * @dateTime: 2021-05-30 08:49:58
 * @description: aop
 */
@Aspect
@Component
public class customAop {
    @Pointcut("execution(* transfer(..)))")
    public void pointCut(){}

    @AfterReturning(
            pointcut="pointCut()",
            returning="retVal")
    public void doAccessCheck(Object retVal) {
        // ...
    }
}
