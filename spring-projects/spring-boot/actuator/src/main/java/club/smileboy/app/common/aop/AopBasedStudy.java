package club.smileboy.app.common.aop;

import org.springframework.aop.*;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;

import java.lang.reflect.Method;

/**
 * 基于 aop 的学习 ...
 *
 * 对于 aop,我们可能需要 先查看 @Enable.... 注解 了解相关的 后置处理器 ...
 *
 *
 *
 * 1. aspectJ aop 解析
 * 2. introduction aop
 * 3. Weaving / loader
 *
 *
 * // 切入点类型
 *  1. Static Pointcuts ( 不是类和静态方法的关系), 他仅仅考虑类和方法 ..
 *  2. Regular Expression Pointcuts
 *  3. attributes driven pointcuts(注解)
 *
 *  // RegexpMethodPointcutAdvisor
 *
 *
 */
//@Import(DefaultImportor.class)
@Configuration
public class AopBasedStudy {

    // annotation ..

    // targetSource ..
    // aop -> auto proxy creator
    // aop 升级(基础设施) advice / advisor   => bean factory advice/advisor => / annotation  / aspect

    // infrastructure -> 用户定义的 advisor的一些处理 -> aspect -> annotation
    // enableCaching
    //

   // spring 建议我们

    /**
     * 这里存在多个 apc的情况下,已经被代理过的aop 不会被二次代理 ....
     * @return
     */
    @Bean
//    @Scope(proxyMode = ScopedProxyMode.INTERFACES)
    public AopTargetBean1 aopTarget() {
        return new AopTargetBean1();
    }



//    @Bean
//    public AopTargetBean2 aopTarget2() {
//        return new AopTargetBean2();
//    }
//    @Bean
//    @ConditionalOnMissingBean(InfrastructureAdvisorAutoProxyCreator.class)
//    public InfrastructureAdvisorAutoProxyCreator infrastructureAdvisorAutoProxyCreator() {
//        return new InfrastructureAdvisorAutoProxyCreator();
//    }

    @Bean
    public static DefaultAdvisorAutoProxyCreator dapc() {
        final DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setAdvisorBeanNamePrefix("dapc");
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(false);
        return defaultAdvisorAutoProxyCreator;
    }


    @Bean
    public static  DefaultAdvisorAutoProxyCreator bapc() {
        final DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setAdvisorBeanNamePrefix("bapc");
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(false);
        return defaultAdvisorAutoProxyCreator;
    }




//    /**
//     * 小型的数据源切换 ..
//     *
//     * @return
//     */
//    @Bean
//    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
//    public Advisor advisor() {
//
//
//
//        return new DefaultPointcutAdvisor(
//
//
//
//                new Pointcut() {
//                    @Override
//                    public ClassFilter getClassFilter() {
//                        return new RootClassFilter(AopTargetBean1.class);
//                    }
//
//                    @Override
//                    public MethodMatcher getMethodMatcher() {
//                        return new MethodMatcher() {
//                            @Override
//                            public boolean matches(Method method, Class<?> targetClass) {
//                                System.out.println("static invoke match !!!");
////                                return true;
//                                return method.getName().equals("printf") && targetClass.equals(AopTargetBean1.class);
//                            }
//
//                            @Override
//                            public boolean isRuntime() {
//                                return false;
//                            }
//
//                            @Override
//                            public boolean matches(Method method, Class<?> targetClass, Object... args) {
//
//                                // request % 2 == 0
//
//                                System.out.println("dynamic invoke match");
//                                return true;
//                            }
//                        };
//                    }
//                },
//                new MethodBeforeAdvice() {
//                    @Override
//                    public void before(Method method, Object[] args, Object target) throws Throwable {
//                        System.out.println("infrastructure advice invoke ...");
//                    }
//                }
//        );
//    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor advisor() {
        return new DefaultPointcutAdvisor(
                new AnnotationMatchingPointcut(AopExecute.class,AopExecute.class),
                new MethodBeforeAdvice() {
                    @Override
                    public void before(Method method, Object[] args, Object target) throws Throwable {
                        System.out.println("infrastructure advice invoke ...");
                    }
                }
        );
    }


    /**
     * 当多个advisor 应用的同一个bean时,并且多个advisor是通过不同的 apc 处理时,那么有可能 切入点匹配条件不成立(有可能是因为我们没有正确设置参数)
     * 例如第一个apc 可能让bean 动态代理成cglib 子类,例如这个示例中,我们检测类上有没有注解,那么此时目标类就是当前cglib子类的父类,
     * 如果没有inherit 关系检查,肯定无法查找父类上的注解 ..
     * 解决方式,使用jdk 动态代理(依旧是一个$proxy,无法解决问题),最终还是需要  ->  开启inherit 检查
     * @return
     */
    @Bean(name = "dapc.afterAdvice")
    public  static  Advisor afterAdvisorFromDapc() {
        return new DefaultPointcutAdvisor(
                // 坑太多了(checkInherited 很重要)
                new AnnotationMatchingPointcut(AopExecute.class,ApcExecute1.class,true),
                new MethodBeforeAdvice() {
                    @Override
                    public void before(Method method, Object[] args, Object target) throws Throwable {
                        System.out.println("infrastructure advice invoke ...");
                    }
                }
        );
    }


    @Bean
    public static Advisor advisorToAopBaseStudy() {
        return new DefaultPointcutAdvisor(
                // 坑太多了(checkInherited 很重要)
                new Pointcut() {
                    @Override
                    public ClassFilter getClassFilter() {
                        return new ClassFilter() {
                            @Override
                            public boolean matches(Class<?> clazz) {
                                return AopBasedStudy.class.equals(clazz);
                            }
                        };
                    }

                    @Override
                    public MethodMatcher getMethodMatcher() {
                        return new MethodMatcher() {
                            @Override
                            public boolean matches(Method method, Class<?> targetClass) {
                                return method.getName().equals("printf");
                            }

                            @Override
                            public boolean isRuntime() {
                                return false;
                            }

                            @Override
                            public boolean matches(Method method, Class<?> targetClass, Object... args) {

                                // 策略( .. 分析参数)
                                // 它属于那种方法类型? 包含了参数是什么? 做什么动作
                                // handler nodes
                                return false;
                            }
                        };
                    }
                },
                new MethodBeforeAdvice() {
                    @Override
                    public void before(Method method, Object[] args, Object target) throws Throwable {
                        System.out.println("infrastructure advice invoke ...");
                    }
                }
        );
    }


    @Bean
    public static Advisor staticAdvisor() {
        return new StaticMethodMatcherPointcutAdvisor() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return false;
            }
        };
    }

    public void printf() {
        System.out.println("我是 aopBasedStudy ....");
    }
}
