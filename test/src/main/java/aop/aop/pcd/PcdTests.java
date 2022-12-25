package aop.aop.pcd;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

public class PcdTests {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(AopConfig.class);
        //AopConfigUtils.registerAspectJAutoProxyCreatorIfNecessary(annotationConfigApplicationContext);
        annotationConfigApplicationContext.refresh();
        User bean = annotationConfigApplicationContext.getBean(User.class);
        bean.printf();
        System.out.println(bean);
    }

    /**
     * 不生效
     */
    @Aspect
    @Component
    public static class AopConfig {

        @Pointcut("bean(user)")
        public void pointcut() {

        }

        @Before("pointcut()")
        public void before() {
            System.out.println("invoke before");
        }

        @Bean
        public User user() {
            return new User();
        }

        @Bean
        public static BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
            return new BeanNameAutoProxyCreator() {{
                setBeanNames("user");
                setInterceptorNames("beanNamePcdAdvisor");
            }};
        }

        @Bean
        public static Advisor beanNamePcdAdvisor() {
            return new DefaultPointcutAdvisor(
                    new NameMatchMethodPointcut(){{
                        setMappedName("printf");
                    }},
                    new MethodBeforeAdvice() {
                        @Override
                        public void before(Method method, Object[] objects, Object o) throws Throwable {
                            System.out.println("invoke before");
                        }
                    }
            );
        }

    }

    public static class User {

        public void printf() {
            System.out.println("user printf");
        }

        //@Override
        //public String toString() {
        //    return super.toString();
        //}
    }
}

