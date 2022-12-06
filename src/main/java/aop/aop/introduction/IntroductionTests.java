package aop.aop.introduction;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.DeclareParentsAdvisor;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * @author FLJ
 * @date 2022/12/5
 * @time 11:49
 * @Description 引入介绍
 */
public class IntroductionTests {

    public static void main(String[] args) {
        //simpleSupport();
        //System.out.println("------------------------ conflictConfigSupport mixin --------------------------------");
        // conflictConfigSupport();
        annotationAndConflictSupport();
    }

    private static void annotationAndConflictSupport() {
        final AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(AnnotationConfig.class);
        final PlainBean bean = annotationConfigApplicationContext.getBean(PlainBean.class);
        System.out.println(AopUtils.isAopProxy(bean));

        final Lockable bean1 = (Lockable) bean;
        bean1.lock();

        bean1.unlock();

        // finish
    }

    private static void conflictConfigSupport() {
        final AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.register(ConflictConfig.class);
        annotationConfigApplicationContext.refresh();
        final PlainBean bean = annotationConfigApplicationContext.getBean(PlainBean.class);
        System.out.println(AopUtils.isAopProxy(bean));

        final Lockable bean1 = (Lockable) bean;
        bean1.lock();
        //bean.setValue("123123");
        bean.getValue();
        bean1.unlock();
        bean.setValue("123123123123");
        bean.setValue("123123123123");
        bean.setValue("123123123123");
        bean.setValue("123123123123");
    }

    private static void simpleSupport() {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        AopConfigUtils.registerAutoProxyCreatorIfNecessary(context);
        context.register(Config.class);
        context.refresh();
        final PlainBean bean = context.getBean(PlainBean.class);

        System.out.println(AopUtils.isAopProxy(bean));

        bean.setValue("123123");

        bean.setValue("1213123");

        // 但是你这样,我就需要将这个对象转换为 Lockable(那也得知道我必须是它)
        //  但是我们并没有强制了 Lockable 耦合 ..
        if (bean instanceof Lockable) {
            ((Lockable) bean).lock();
            // impossible success
            bean.setValue("123123");
        }
    }

    @Configuration
    public static class Config {

        //@Bean
        //public LockMixin advice() {
        //    return new LockMixin();
        //}

        /**
         * 使用了它,没必要使用LockMixin(因为它会为每一个特定的bean 提供一个 lockMixin 对象）
         *
         * @return
         */
        @Bean
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        public  static LockMixinAdvisor lockMixinAdvisor() {
            return new LockMixinAdvisor();
        }



        @Bean
        public static PlainBean plainBean() {
            return new PlainBean();
        }

    }


    public static class PlainBean {

        private Object value;

        void setValue(Object value) {
            this.value = value;
            System.out.println("set value success !!!");
        }

        Object getValue() {
            return this.value;
        }
    }


    @Configuration
    public static class ConflictConfig {

        @Bean
        public Advisor introductionAdvisor(LockableSupport lockableSupport) {
            return new DefaultIntroductionAdvisor(
                    new DelegatingIntroductionInterceptor(lockableSupport) {
                        @Override
                        public Object invoke(MethodInvocation mi) throws Throwable {
                            lockableSupport.checkIfPossible(mi.getThis());

                            // 需要将unlock方法排除之外 ..
                            //if(mi.getMethod().getName().startsWith("unlock")) {
                            //  return super.invoke(mi);
                            //}
                            //

                            // 也就是需要将 Lockable 排除之外
                            if (mi.getMethod().getDeclaringClass() == Lockable.class) {
                                return super.invoke(mi);
                            }

                            if (lockableSupport.locked()) {
                                throw new LockedException();
                            }
                            return super.invoke(mi);
                        }
                    }
            );
        }

        @Bean
        public static DefaultAdvisorAutoProxyCreator apc() {
            return new DefaultAdvisorAutoProxyCreator();
        }

        @Bean
        public PlainBean plainBean() {
            return new PlainBean();
        }




        /**
         * 它做委排对象 ..(负责每一个目标对象的 具体实现的生成)
         *
         * @return
         */
        @Bean
        public LockableSupport lockableDelegate() {
            return new LockableSupport();

        }
    }

    @Configuration
    public static class AnnotationConfig {

        @Bean
        public PlainBean plainBean() {
            return new PlainBean();
        }


        @Bean
        public static DefaultAdvisorAutoProxyCreator apc() {
            return new DefaultAdvisorAutoProxyCreator();
        }

        /**
         *  我们只需要 关心PlainBean 来关联Lockable 接口
         * @return
         */
        @Bean
        public static Advisor advisor() {
            return new DeclareParentsAdvisor(
                    Lockable.class,
                    // *.. 表示多层级的匹配
                    "*..*.PlainBean",

                    // 在这个示例中, 此类仅仅是对Lockable 接口的实现,至于它所继承的一些接口,完全没有用上 ...
                    // 而且每一个bean 对应了一个 Lockable 实现 .
                    LockMixin.class
            );
        }

    }

}
