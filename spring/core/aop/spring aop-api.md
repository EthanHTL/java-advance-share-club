## spring AOP apis
前一个章节描述了对AOP的支持(使用@Aspect 以及基于方案,模式的切面定义),这个章节我们讨论spring aop api,对于普通应用来说,我们推荐spring aop(结合Aspectj 切入点结合使用)
#### spring 中的 pointcut api
##### 概念
spring 的切入点模型 启用了切入点重用独立于通知类型,你能够使用相同切入点 结合不同的目标通知 \
PointCut接口是一个核心接口,使用它连接目标通知到合适的类以及方法:
```java
public interface Pointcut {

    ClassFilter getClassFilter();

    MethodMatcher getMethodMatcher();
}
```
从这里我们可以看出他需要一个ClassFilter 以及 MethodMatcher 就是判断是否支持、是否需要执行 \
所以大体工作分为两部分,一个是类重新使用以及方法匹配以及细腻化的组合操作(例如  执行一个联合 其他方法匹配器进行方法匹配) \
ClassFilter 接口用来限制切入点应该对那些类进行方法通知,如果matches方法总是返回true,那么目标类就匹配:
```java
public interface ClassFilter {

    boolean matches(Class clazz);
}
```
MethodMatcher 接口更加重要:
```java
public interface MethodMatcher {

    boolean matches(Method m, Class<?> targetClass);

    boolean isRuntime();

    boolean matches(Method m, Class<?> targetClass, Object... args);
}
```
matches(Method,Class)方法被用来测试此切入点是否匹配目标类的给定方法，此评估能够执行(当aop代理创建完毕且避免在每一次方法执行时进行测试需要),如果两个参数的matches方法返回true,那么isRuntime方法如果返回true,那么三参数匹配方法在每次方法执行时就是执行,否则表示静态匹配,那么就不会执行,在目标通知执行之前这些方法就是评估,条件成立才执行 \
大多数MethodMatcher实现是静态的,意味着isRuntime返回false,这种情况下,three 参数matches不会执行; \
如果可能最好pointcut是静态的,这样能够使得当aop代理创建完毕之后能够对这个切入点评估结果进行缓存; 
#### pointcut的操作
spring 支持操作(并或者交集)在pointcut上 \
并意味着方法要么切入点匹配任意之一即可,交集意味着此方法需要所有的切入点匹配才执行,并集通常更有用,如果你是一个组合切入点-被静态方法(例如Pointcuts类中)或者被ComposablePointcut类使用,然而 使用Aspect 切入点表达式通常是更简单的方式 
#### AspectJ 表达式point
从2.0开始 大多数重要的pointcut的类型能够被spring 的org.springframework.aop.aspectj.AspectJExpressionPoint使用,这是一个切入点能够使用Aspectj提供的库来解析一个AspectJ切入点表达式 字符串;
#### 便利的切入点实现
spring 提供了大量方便的切入点实现,有些能够直接使用,其他需要特定于应用的切入点实现
##### 静态切入点
静态切入点是基于方法以及目标类以及不能够统计方法参数个数,静态切入点已经足够,并且是最棒的,能够应付大多数场景,spring 能够只需要评估静态切入点一次(提高性能),当每一个方法第一次执行,那么会评估,之后就不必再次评估表达式 \
这个章节其余部分描述了静态切入点的实现以及如何放置在spring中
###### 普通表达式切入点
指定静态切入点就指定一个普通表达式即可,各种aop框架除了spring也是可以的,org.springframework.aop.support.JdkRegexpMethodPointcut是一个通用的普通表达式切入点能够使用被JDK支持的普通表达式 \
当使用JdkRegexpMethodPointcut 时,你能够提供一个表达式字符串的列表,如果任意之一被匹配,切入点评估为true,结果就是结果切入点有效的和指定模式结合 \
如何使用一个JdkRegexpMethodPointcut
```xml
<bean id="settersAndAbsquatulatePointcut"
        class="org.springframework.aop.support.JdkRegexpMethodPointcut">
    <property name="patterns">
        <list>
            <value>.*set.*</value>
            <value>.*absquatulate</value>
        </list>
    </property>
</bean>
```
spring 提供了一个便捷类叫做RegexpMethodPointcutAdvisor,我哦们可以将它作为一个通知引用(记住一个Advice可以作为一个拦截器,前置通知、异常通知、以及其他),在这些背景之下,spring 能够使用一个JdkRegexpMethodPoint,使用RegexpMethodPointcutAdvisor 能够简单注入,作为一个bean 包装切入点和通知方法,例如:
```xml
<bean id="settersAndAbsquatulateAdvisor"
        class="org.springframework.aop.support.RegexpMethodPointcutAdvisor">
    <property name="advice">
        <ref bean="beanNameOfAopAllianceInterceptor"/>
    </property>
    <property name="patterns">
        <list>
            <value>.*set.*</value>
            <value>.*absquatulate</value>
        </list>
    </property>
</bean>
```
#### 属性驱动的切入点
静态切入点的一个重要类型之一时元数据驱动的切入点,使用元数据属性的值(通常是,源代码级别的元数据)
#### 动态切入点
动态切入点评估更花费时间,它能统计方法参数信息也能够拥有静态信息,这意味着他必须在每次方法执行之前评估,且评估结果无法缓存,参数本身会变化,主要例子就是control flow 切入点;
##### 控制流切入点
此概念相似与Aspect cflow切入点,尽管 功能较弱(目前没有办法在指定一个切入点在另一个切入点匹配的连接点之下运行),一个控制流切入点匹配当给钱的调用堆栈,例如,它能够触发(如果连接点已经通过com.mycompany.web包下的一个方法执行或者通过SomeCaller类触发),控制流切入点能够通过使用org.springframework.aop.support.ControlFlowPointcut 指定 \
控制流切入点更加花费成本-相比于其他动态切入点,在java1.4中,成本是其他动态切入点的5倍;

#### 切入点父类
spring 提供了许多有用的切入点父类帮助你如何实现你自己的切入点\
因为静态的切入点非常有用,你应该可能实现StaticMethodMatcherPointcut,这鸡内金只需要实现一个抽象方法(尽管你能够覆写其他方法来支持自定义行为):
```java
class TestStaticPointcut extends StaticMethodMatcherPointcut {

    public boolean matches(Method m, Class targetClass) {
        // return true if custom criteria match
    }
}
```
对于动态切入点也有对应的父类,捏能够能够使用任意的通知类型定义切入点
#### 自定义切入点
因为spring aop的切入点是java 类 而不是Aspect 语言特性,你能够声明一个自定义的切入点,不管是否动态,自定义切入点在spring 能变得很复杂,然而我们推荐使用Aspect 切入点表达式语言,如果可以 \
spring 之后的版本也许会提供语义切入点支持（通过JAC),例如所有的方法-改变目标对象的实例变量的所有方法)

#### spring中的Advice api
##### advice 声明周期
每一个advice都是spring bean,一个advice实例能够被所有的advised 对象共享或者独一无二的advised 对象,这与每一个类或者每一个通知实例相关; \
每一个通知类经常被使用,它可以是通用通知、也可以是事务通知者,这些不依赖代理对象的状态或者新增状态,它们仅仅对方法以及参数作用 \
每个实例的建议适用于介绍，以支持混合。在这种情况下，建议将状态添加到代理对象\
你能够混合共享/每一个实例通知到相同的aop代理上
#### spring 的通知类型 (ADVICE type)
spring提供了各种各样的advice 类型以及支持对advice类型进行扩展
##### 拦截环绕通知
环绕通知是最基本的通知 \
spring 和AOP Alliance接口兼容环绕通知(对方法进行拦截),类需要实现MethodInterceptor 以及实现环绕通知的类也需要实现以下接口
```java
public interface MethodInterceptor extends Interceptor {

    Object invoke(MethodInvocation invocation) throws Throwable;
}
```
MethodInvocation 参数执行invoke方法暴露将要执行的方法,这是目标连接点,aop代理以及此方法需要的参数,invoke方法应该需要返回invocation的结果,连接点的返回值 \
以下是一个简单的MethodInterceptor实现
```java
public class DebugInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Before: invocation=[" + invocation + "]");
        Object rval = invocation.proceed();
        System.out.println("Invocation returned");
        return rval;
    }
}
```
注意调用MethodInvocation的proceed方法,这沿着拦截器链向连接点前进
大多数连接器执行此方法会返回一个值,然而一个MethodInterceptor,例如像环绕通知一样,能够返回一个不同的值或者抛出一个异常而不是执行proceed方法,但是,除非你有这样做的一个原因; \
MethodInterceptor 实现提供与其他符合 AOP 联盟的 AOP 实现的互操作性。本节剩余部分讨论的其他通知类型实现了常见的 AOP 概念，但以特定于 Spring 的方式实现。虽然使用最具体的通知类型有优势，但如果您可能希望在另一个 AOP 框架中运行方面，请坚持使用 MethodInterceptor 围绕通知。请注意，切入点目前无法在框架之间互操作，并且 AOP 联盟目前没有定义切入点接口
##### 前置通知
一个简单的通知类型是前置通知,它不需要一个MethodInvocation对象,因此它在进入方法之前调用 \
```java
public interface MethodBeforeAdvice extends BeforeAdvice {

    void before(Method m, Object[] args, Object target) throws Throwable;
}
```
Spring 的 API 设计允许在通知之前使用字段，尽管通常的对象适用于字段拦截，并且 Spring 不太可能实现它 \
注意此方法返回值为void,前置通知能够插入一个自定义行为(在拦截点运行之前)但是不能改变此返回值,如果一个前置通知抛出一个而一场,它会停止拦截器链的后续执行,这个异常会根据拦截器链向上传播,如果它是一个为检测或者签名指定的异常,他会直接传递给客户端,它会通过aop代理包装一个未检测的异常 \
```java
public class CountingBeforeAdvice implements MethodBeforeAdvice {

    private int count;

    public void before(Method m, Object[] args, Object target) throws Throwable {
        ++count;
    }

    public int getCount() {
        return count;
    }
}
```
#####   异常通知
org.springframework.aop.ThrowAdvice仅仅是一个标记接口,能够表示给定有一个或者多个类型的异常通知方法
```java
afterThrowing([Method, args, target], subclassOfThrowable)
```
仅仅是最后一个参数是必须的,方法参数或许有一个或者4个参数,依赖于通知方法是否对方法以及参数感兴趣,例如:
```java
public class RemoteThrowsAdvice implements ThrowsAdvice {

    public void afterThrowing(RemoteException ex) throws Throwable {
        // Do something with remote exception
    }
}
```
不像前面的通知,下面这个例子声明了4个参数,因此它能够访问执行的方法,以及方法参数、目标对象,例如下面的例子中抛出一个ServletException:
```java
public class ServletThrowsAdviceWithArguments implements ThrowsAdvice {

    public void afterThrowing(Method m, Object[] args, Object target, ServletException ex) {
        // Do something with all arguments
    }
}
```
最终的例子就是说明怎样将这个两个方法使用在一个类中同时处理两种异常,所有的throws 通知方法能够合并到一个类中,例如:
```java
public static class CombinedThrowsAdvice implements ThrowsAdvice {

    public void afterThrowing(RemoteException ex) throws Throwable {
        // Do something with remote exception
    }

    public void afterThrowing(Method m, Object[] args, Object target, ServletException ex) {
        // Do something with all arguments
    }
}
```
如果一个异常通知方法抛出一个一个异常本身,它覆盖了原始的异常(它改变了抛出一个用户的异常),这个覆盖的异常同行是一个运行时异常，它和任何方法签名兼容,然而如果一个异常通知方法抛出了检测异常,他必须与声明的异常（目标方法声明的异常匹配才行),因此 为了解耦特殊目标方法签名,不要抛出一个与目标方法签名不兼容的未声明的检测异常 \
##### after returning advice
后置通知必须实现org.springframework.aop.AfterReturningAdvice
```java
public interface AfterReturningAdvice extends Advice {

    void afterReturning(Object returnValue, Method m, Object[] args, Object target)
            throws Throwable;
}
```
返回通知可以访问返回值(但是不能够修改),执行方法、方法参数、以及目标对象
例如:
```java
public class CountingAfterReturningAdvice implements AfterReturningAdvice {

    private int count;

    public void afterReturning(Object returnValue, Method m, Object[] args, Object target)
            throws Throwable {
        ++count;
    }

    public int getCount() {
        return count;
    }
}
```
如果它抛出了一个异常,它会抛出异常到拦截器链中(而不是返回值)
##### introduction advice
spring 信任一个introduction advice 作为一个特殊类型的拦截通知 \
introduction 需要一个IntroductionAdvisor
```java
public interface IntroductionInterceptor extends MethodInterceptor {

    boolean implementsInterface(Class intf);
}
```
invoke方法继承于 aop Alliance MethodInterceptor 接口且必须被实现,introduction拦截器有责任处理方法调用(他不能够执行proceed()) \
introduction 通知不能被任何切入点使用,它仅仅应用在类中,而不是方法级别,仅仅能够通过IntroductionAdvisor 使用introduction advice
```java
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

    ClassFilter getClassFilter();

    void validateInterfaces() throws IllegalArgumentException;
}

public interface IntroductionInfo {

    Class<?>[] getInterfaces();
}
```

这里没有方法匹配器,因此没有切入点与之联系,仅仅类过滤是有效的 \
getInterfaces方法返回被此通知者支持的接口 \
内部使用validateInterfaces()方法来查看引入的接口是否可以通过配置的IntroductionInterceptor实现 \
例如我们想要引入一个以下接口的多个对象:
```java
public interface Lockable {
    void lock();
    void unlock();
    boolean locked();
}
```
这说明了混合。我们希望能够将建议的对象转换为 Lockable，无论它们的类型如何，并调用 lock 和 unlock 方法。如果我们调用 lock() 方法，我们希望所有 setter 方法都抛出 LockedException。因此，我们可以添加一个方面，该方面提供使对象不可变的能力，而他们对此一无所知：AOP 的一个很好的例子 \
首先，我们需要一个 IntroductionInterceptor 来完成繁重的工作。在这种情况下，我们扩展了 org.springframework.aop.support.DelegatingIntroductionInterceptor 便利类。我们可以直接实现 IntroductionInterceptor，但在大多数情况下使用 DelegatingIntroductionInterceptor 是最好的 \
DelegatingIntroductionInterceptor 被设计来代理一个Introduction 到实际的introducede 接口实现
```java
public class LockMixin extends DelegatingIntroductionInterceptor implements Lockable {

    private boolean locked;

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean locked() {
        return this.locked;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (locked() && invocation.getMethod().getName().indexOf("set") == 0) {
            throw new LockedException();
        }
        return super.invoke(invocation);
    }

}
```
总而言之这种通知是混合型的通知,将advice 和 需要拦截的结合进行混合,实现一个新的Advice,这样在执行目标方法时其实就是在拦截执行的一个方法执行 \
关键点在于需要的introduction  仅仅时持有一个LockMixin实例并指定一个感兴趣的接口,这样 一个更加复杂的例子是可以直接通过引用一个Introduction 拦截器即可实现相同功能(例如原型对象需要这种通知),不需要配置LockMixin的相关配置,仅仅只需要一个new,并设置感兴趣的接口
```java
public class LockMixinAdvisor extends DefaultIntroductionAdvisor {

    public LockMixinAdvisor() {
        super(new LockMixin(), Lockable.class);
    }
}
```
这样一个Advisor就将 introduction advice （通过原型bean的方式和目标对象联系起来) \
除此之外还可以通过编程式Advised.addAdvisor方法或者xml方式配置(或者其他的advisor进行设置),所有代理创建选择将在下面讨论,包括自动代理创建者,正确处理introduction以及有用的混合;
#### advisor api
在spring中,advisor 是一个切面包括了一个advice对象以及一个切入点表达式 \
作为特殊introduction的请求,任何一个advisor能够使用任意一个advice \
org.springframework.aop.support.DefaultPointcutAdvisor是最常用的advisor,它能够结合MethodInterceptor,前置通知,异常通知使用 \
它也可以混合advisor 以及 advice 类型(在spring的相同aop代理中),例如,能够使用一个拦截环绕通知,异常不同、前置通知(在一个代理配置中这样配置),spring 会自动创建必要的拦截器链
#### 使用ProxyFactoryBean 创建 aop 代理
如果你使用spring ioc容器(ApplicationContext 或者 BeanFactory)来加载你的业务对象,你想要使用spring aop FactoryBean实现之一,记住factory bean 是引入了一个层 (非直接方式),让他创建不同类型的对象) \
spring aop 支持在幕后使用工厂bean \
最基本的方式是通过org.springframework.aop.framework.ProxyFactoryBean创建aop 代理,这在切入点上给出了完整的控制,任何通知都能够使用,并排序,然而(如果你不需要这样的控制),可以简单点 
##### Basics
使用ProxyFactoryBean或者ioc感知类创建aop代理的好处是通知以及切入点能够被ioc管理,这个一个非常有效的特性,启用其他aop框架难以实现的某些方法,例如: advice本身也许引用了应用对象(除对象之外,它对于其他aop框架来说都是必要的),都是依赖注入的插件能力提供的好处;
##### JavaBean properties
大多数FactoryBean实现是通过spring 提供,ProxyFactoryBean 本身也是一个JavaBean,它有一些属性可以被使用:
* 指定想要代理的目标
* 指定是否使用CGLIB \
有些属性继承于org.springframework.ProxyConfig(在spring中的所有AOP 代理工厂的父类),例如:
* proxyTargetclass: true(如果目标类被代理,而不是目标类接口),如果此属性设置为true,则使用CGLIB进行代理
* optimize: 控制是否对通过 CGLIB 创建的代理应用积极的优化。除非您完全了解相关 AOP 代理如何处理优化，否则您不应随意使用此设置。这目前仅用于 CGLIB 代理。它对 JDK 动态代理没有影响 \
* frozen: 如果代理配置是冻结,无法改变它的配置,这是非常有用的(对于轻量优化 以及当你不想调用者能够操作代理的情况下-(通过Advised接口))-在代理已经创建之后,默认为false,因此配置改变是允许的(例如增加可选的通知)
* exposeProxy: 决定是否当前的代理应该暴露到一个ThreadLocal中,因此它能够被目标对象访问,如果一个目标需要包含此代理以及exposeProxy设置为true,那么目标对象能够通过AopContext.currentProxy()获取到代理; \
其他特定于ProxyFactoryBean的属性:
* proxyInterfaces 接口列表,如果不应该代理这些接口,那么会使用CGLIB代理
* interceptorNames: Advisor的数组,拦截器、或者其他通知名称,顺序很重要,先到先得; 那就是说第一个拦截器能够首先拦截执行链 \
这个名称是当前代理中的bean 名称,包括从父工厂获取的bean 名称,你不能够涉及bean 引用,因此这样做会导致ProxyFactoryBean 忽略此单例的通知设置 \
你能够增加一个拦截器名字(通过*),这样做会导致应用中的所有通知器(符合此名称)能够被应用,例如一个使用此特性的demo:[ Using “Global” Advisors.](https://docs.spring.io/spring-framework/docs/5.3.10-SNAPSHOT/reference/html/core.html#aop-global-advisors) \
* 单例: 工厂是否返回一个单例,不管getObject方法是否经常调用调用,各种各样的FactoryBean 实现提供了一个这样的方法,默认值为true,如果你想使用有状态的通知,举个例子,有状态混合,使用原型advice,此属性必然为false;
#### JDK 以及CGLIB 代理
这部分描述了如何让ProxyFactoryBean 选择合适的代理(为特殊的目标对象) \
ProxyFactoryBean的行为从spring1.2.x的JDK代理转变到了spring 2.0的CGLIB代理(默认行为),此ProxyFactoryBean 现在在自动检测接口方面表现出与TransactionProxyFactoryBean类相似的语义;\
如果目标类能够被代理且不用实现任何接口,那么CGLIB代理,这是最简单的场景,JDK 代理是基于接口的,并且没有接口意味着JDK无法代理,你能够增强这个目标bean(通过InterceptorNames 指定拦截器的列表),注意CGLIB的代理 会创建(即使你ProxyFactoryBean的proxyTargetClass设置为false)-并且显式设置为false,没有任何意义,还会令人困惑 \
如果目标类实现了接口,那么代理的类型依赖于ProxyFactoryBean的配置 \
如果proxyTargetClass 设置为true,那么CGLIB代理创建,这是有用的并且符合最小意外原则(即使proxyInterfaces 设置一个或者多个接口名称),实际上proxyTargetClass 设置为true具有更高的优先级 \
如果proxyInterfaces 设置了接口,那么JDK代理将会创建(前提是proxyTargetClass ==false),此接口的代理实现依赖于此属性,如果目标类实现了比此属性设置还多的接口,那么代理并不会实现其他多余的接口; \
如果proxyInterfaces没有被设置,但是目标类实现了一个或者多个接口,那么接口将会自动检测(作为实际实现的接口),JDK代理创建,但是等价于proxyInterfaces,而且此属性效果更好，能够很好工作且减少错误;

#### 代理接口
例如ProxyFactoryBean的例子
* 目标bean 代理将被代理,此例子中有一个personTarget bean 定义
* Advisor 以及 Interceptor 能够被用来提供通知
* aop代理 bean 定义 指定了目标对象(personTarget bean),接口将被代理,通知将会被应用
```xml
<bean id="personTarget" class="com.mycompany.PersonImpl">
    <property name="name" value="Tony"/>
    <property name="age" value="51"/>
</bean>

<bean id="myAdvisor" class="com.mycompany.MyAdvisor">
    <property name="someProperty" value="Custom string property value"/>
</bean>

<bean id="debugInterceptor" class="org.springframework.aop.interceptor.DebugInterceptor">
</bean>

<bean id="person"
    class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="proxyInterfaces" value="com.mycompany.Person"/>

    <property name="target" ref="personTarget"/>
    <property name="interceptorNames">
        <list>
            <value>myAdvisor</value>
            <value>debugInterceptor</value>
        </list>
    </property>
</bean>
```
注意interceptorNames 是一个字符串列表,包含了当前工厂的拦截器以及advisor的bean 名称, 通知的顺序很重要\
您可能想知道为什么该列表不包含 bean 引用。这样做的原因是，如果 ProxyFactoryBean 的 singleton 属性设置为 false，它必须能够返回独立的代理实例。如果其中任何一个advisor 本身是原型，则需要返回一个独立的实例，因此必须能够从工厂获取原型的实例。持有引用是不够的 \
person bean 定义是一个Person 的实现
```text
Person person = (Person)factory.getBean("person")
```
同一个 IoC 上下文中的其他 bean 可以表达对它的强类型依赖，就像普通 Java 对象一样。以下示例显示了如何执行此操作：
```xml
<bean id="personUser" class="com.mycompany.PersonUser">
    <property name="person"><ref bean="person"/></property>
</bean>
```
本示例中的 PersonUser 类公开了 Person 类型的属性。就其而言，AOP 代理可以透明地用于代替“真人”实现。但是，它的类将是动态代理类。可以将其转换为 Advised 界面（稍后讨论） \
你能够隐藏目标以及代理对象的区别(通过使用匿名内部类)
```xml
<bean id="myAdvisor" class="com.mycompany.MyAdvisor">
    <property name="someProperty" value="Custom string property value"/>
</bean>

<bean id="debugInterceptor" class="org.springframework.aop.interceptor.DebugInterceptor"/>

<bean id="person" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="proxyInterfaces" value="com.mycompany.Person"/>
    <!-- Use inner bean, not local reference to target -->
    <property name="target">
        <bean class="com.mycompany.PersonImpl">
            <property name="name" value="Tony"/>
            <property name="age" value="51"/>
        </bean>
    </property>
    <property name="interceptorNames">
        <list>
            <value>myAdvisor</value>
            <value>debugInterceptor</value>
        </list>
    </property>
</bean>
```
这样做的好处是应用上下文的用户去引用一个非通知对象并且避免歧义的spring  ioc 自动装配行为(因为当使用匿名内部类时，目标对象仅仅是目标类类型),这个优点是ProxyFactoryBean的定义自己提供的 \
然而有时我们能够从工厂中拿取一个非通知目标对象实际上是一个优势(例如测试场景)
#### 代理类
CGLIB代理的优劣势:
cglib主要是在运行时生成目标子类,spring 配置生成的子类去代理方法调用原始目标的对象,子类本身实现的就是装饰器模式,通过advice编织; \
cglib代理对于用户来说是透明的,然而它有一些问题:
* final 方法无法被通知,因为他不能够被覆盖;
* 不需要增加CGLIB到类路径,CGLIB在spring 3.2 导入到spring-core.jar中换句话说,基于CGLIB的AOP 工作(开箱即用),同JDK一样 \
这里有小小的性能不同(CGLIB和动态代理),性能不应该作为选择的决定性因素 
#### 使用"全局" 通知者
通过追加一个*(asterrisk) 到一个拦截器名称,所有匹配此名称的拦截器都会增加到拦截器链中,如果你需要增加一个"全局" 通知者的集合这可能能够派上用场:
```xml
<bean id="proxy" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="target" ref="service"/>
    <property name="interceptorNames">
        <list>
            <value>global*</value>
        </list>
    </property>
</bean>

<bean id="global_debug" class="org.springframework.aop.interceptor.DebugInterceptor"/>
<bean id="global_performance" class="org.springframework.aop.interceptor.PerformanceMonitorInterceptor"/>
```
#### 简洁的代理定义
尤其是定义事务代理时,你最终会得到许多类似的代理定义,父子定义的使用,伴随着内部bean 定义,能够导致比较清晰以及更加简洁的代理定义:
```xml
<bean id="txProxyTemplate" abstract="true"
        class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean">
    <property name="transactionManager" ref="transactionManager"/>
    <property name="transactionAttributes">
        <props>
            <prop key="*">PROPAGATION_REQUIRED</prop>
        </props>
    </property>
</bean>
```
这并没有实例化,且实际没有完成,因此 每一个代理需要通过子bean定义创建,这将包装 代理的目标对象 作为一个内部bean 定义,因为目标他自己并不会使用:
```xml
<bean id="myService" parent="txProxyTemplate">
    <property name="target">
        <bean class="org.springframework.samples.MyServiceImpl">
        </bean>
    </property>
</bean>
```
你能够覆盖这些属性(从父模板),例如:
```xml
<bean id="mySpecialService" parent="txProxyTemplate">
    <property name="target">
        <bean class="org.springframework.samples.MySpecialServiceImpl">
        </bean>
    </property>
    <property name="transactionAttributes">
        <props>
            <prop key="get*">PROPAGATION_REQUIRED,readOnly</prop>
            <prop key="find*">PROPAGATION_REQUIRED,readOnly</prop>
            <prop key="load*">PROPAGATION_REQUIRED,readOnly</prop>
            <prop key="store*">PROPAGATION_REQUIRED</prop>
        </props>
    </property>
</bean>
```
注意这个父bean 例子,我们显式的标记了父bean 定义作为一个抽象 bean 定义(设置abstract =true),因此它不会能够实例化,应用上下文(但不是简单bean 工厂),默认时
每一个预实例化所有单例,因此他是非常重要的,至少对于单例来说,如果你有一个父bean 定义(你打算将他作为一个模板),这个定义指定了一个类,你必须确保设置abstract=true,否则应用上下文会尝试实例化它;

#### 通过ProxyFactory 编程式创建aop代理
使用spring 创建aop 代理非常简单,只需要让你使用spring aopp(不需要依赖于spring ioc) \
此接口被目标对象实现将会自动代理,下面列出了如何创建目标对象的代理(包含了一个拦截器以及一个advisor) \
```xml
ProxyFactory factory = new ProxyFactory(myBusinessInterfaceImpl);
factory.addAdvice(myMethodInterceptor);
factory.addAdvisor(myAdvisor);
MyBusinessInterface tb = (MyBusinessInterface) factory.getProxy();
```
第一步构建了一个ProxyFactory,你需要使用目标对象创建它,在前面的例子中 或者指定需要被代理的接口(另一个构造器) \
你能够增加通知(使用拦截器作为一种特殊的建议),通知者,或者两者都是并通过ProxyFactory的生命周期进行操作,如果你增加了一个IntroductionInterceptionAroundAdvisor,你能够影响代理去实现可选的接口! \
在ProxyFactory上这也有方便的方法(继承来至于AdvisedSupport) -让你能够增加其他的通知类型,例如前置或者异常通知;AdvisedSupport 是ProxyFactory以及 ProxyFactoryBean的父类 \
继承AOP 代理(使用ioc框架)创建是最好的方式,我们推荐外部配置使用java结合aop,这是更常见的
#### 操作Advised Objects
当你创建aop代理之后,你能够操作它们(通过org.springframeowrk.aop.framework.Advised接口) \
任何aop 代理能够通过此接口进行强转,不论它们实现了什么接口,此接口包括了以下方法:
```java
Advisor[] getAdvisors();

void addAdvice(Advice advice) throws AopConfigException;

void addAdvice(int pos, Advice advice) throws AopConfigException;

void addAdvisor(Advisor advisor) throws AopConfigException;

void addAdvisor(int pos, Advisor advisor) throws AopConfigException;

int indexOf(Advisor advisor);

boolean removeAdvisor(Advisor advisor) throws AopConfigException;

void removeAdvisor(int index) throws AopConfigException;

boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;

boolean isFrozen();
```
getAdvisors 返回增加到此工厂(ProxyFactory)的Advisor,返回指定位置的的advisor是你增加的对象,如果你增加了拦截器或者其他通知类型,spring 会包装它们到一个advisor(使用一个总是返回true的切入点),因此你能够增加MethodInterceptor,这个advisor  - 返回指定位置的 是一个DefaultPointcutAdvisor(能够返回自己的MethodInterceptor以及一个切入点(匹配所有类和方法)) \
此addAdvisor方法能够增加任何类型的Advisor,通常情况下 advisor 持有一个pointcut以及 advice(通常是DefaultPointcutAdvisor),你能够使用任何advice以及切入点(对于introduction来说没有切入点) \
默认来说,代理创建完毕之后拦截器也能增加或者删除,仅有的一个限制就是它们不可能增加一个或者删除 introduction advisor,来自工厂的一个存在的代理不能够展示这个接口的改变(你能够从工厂中包含一个新的代理来避免这种问题) \
下面的例子展示了如何强转一个aop代理到Advised 接口以及测试并操作此advice:
```java
Advised advised = (Advised) myObject;
Advisor[] advisors = advised.getAdvisors();
int oldAdvisorCount = advisors.length;
System.out.println(oldAdvisorCount + " advisors");

// Add an advice like an interceptor without a pointcut
// Will match all proxied methods
// Can use for interceptors, before, after returning or throws advice
advised.addAdvice(new DebugInterceptor());

// Add selective advice using a pointcut
advised.addAdvisor(new DefaultPointcutAdvisor(mySpecialPointcut, myAdvice));

assertEquals("Added two advisors", oldAdvisorCount + 2, advised.getAdvisors().length);
```
尽管毫无疑问存在合法的用例，但在生产中修改有关业务对象的建议是否可取（无双关语）是值得怀疑的。但是，它在开发中非常有用（例如，在测试中）。我们有时发现能够以拦截器或其他通知的形式添加测试代码非常有用，进入我们想要测试的方法调用。 （例如，建议可以进入为该方法创建的事务中，可能是在将事务标记为回滚之前运行 SQL 以检查数据库是否已正确更新。） \
依赖于你怎样创建代理,你能够使用一个frozen 标志, 这种情况下,Advised isForzen方法返回true, 以及任何尝试修改通知(通过增加或者移除)都会导致AopConfigException,能够冻结一个对象的状态(有些情况下是有用的)-例如(阻止调用代码移除一个安全拦截器)
#### 使用 "auto-proxy" 便利(设施 工具)
目前为止: 我们考虑显式的aop 代理创建 (通过ProxyFactoryBean 或者类似的工厂bean创建) \
spring 也能让我们使用"自动代理" bean 定义特性,者能够自动对已选择的bean 定义自动代理,展示基于内置的 bean 后置处理器基础设施构建,它允许容器加载时修改bean 定义; \
在这种模式下, 你能够设置某些特定的bean 定义在你的xml bean 定义配置中(配置自动代理基础设施),让你能够声明声明的目标类能够合适的进行自动代理,你不需要使用ProxyFactoryBean; \
这里有两个方式能够进行自动代理工具使用:
* 使用自动代理创建器引用当前上下文的特定bean
* 自动代理创建的特殊情况(值得单独考虑): 自动代理创建衍生于源代码级别的元数据属性
#### 自动代理 bean 定义
这一部分描述自动代理创建器
#####e BeanNameAutoProxyCreator 
此类是一个后置处理器(能够自动创建AOP代理(对具有名称 => 匹配字符串或者通配符 的给定bean)),例如:
```xml
<bean class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator">
    <property name="beanNames" value="jdk*,onlyJdk"/>
    <property name="interceptorNames">
        <list>
            <value>myInterceptor</value>
        </list>
    </property>
</bean>
```
对于ProxyFactoryBean来说,这里是一个interceptorNames属性而不是拦截器列表,以允许原型通知者的正确行为,命令的”拦截器"可以是通知者或者任何通知类型 \
与一般的自动代理一样，使用 BeanNameAutoProxyCreator 的主要目的是将相同的配置一致地应用于多个对象，并且配置量最少。它是将声明式事务应用于多个对象的流行选择 \
相同的通知适用于所有匹配的 bean。请注意，如果使用了通知程序（而不是前面示例中的拦截器），切入点可能会以不同的方式应用于不同的 bean
##### DefaultAdvisorAutoProxyCreator
一个更加普遍并且更有用的方式是使用DefaultAdvisorAutoProxyCreator,将会拿取当前上下文中合适的通知器应用到目标的对象,不需要包括额外的bean名称到自动代理advisor bean 定义(所以可以看出 Advisor就是为了编织 advice到目标对象的一个使者),它提供了与 BeanNameAutoProxyCreator 相同的配置一致和避免重复的优点
* 指定一个DefaultAdvisorAutoProxyCreator bean 定义
* 指定任意数量的advisor到相同或者相关的上下文中,注意这些必须是advisor,并不是拦截器或者其他advice,这是有必要的,因为必须要使用一个pointcut执行,来检查每个通知到候选的bean 定义的合适性 \
DefaultAdvisorAutoProxyCreator 自动执行包含在每一个advisor中的切入点,判断此advice是否应该使用在业务对象上(例如businessObject1或者businessObject2) \
这意味着任何数量的advisor能够自动应用到每一个业务对象上,如果没有切入点在匹配业务对象上的任意一个方法的advisor,那么这个对象将不会代理,那么对于这些对象仅仅有必要的时候才进行自动代理 \
自动代理通常有着优势(可能让调用者或者依赖去包含一个非通知对象),例如调用getBean("businessObject1)-在应用上下文中调用返回一个aop代理,并不是目标业务对象(这个内部bean 也展示了这个好处)
```xml
<bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>

<bean class="org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor">
    <property name="transactionInterceptor" ref="transactionInterceptor"/>
</bean>

<bean id="customAdvisor" class="com.mycompany.MyAdvisor"/>

<bean id="businessObject1" class="com.mycompany.BusinessObject1">
    <!-- Properties omitted -->
</bean>

<bean id="businessObject2" class="com.mycompany.BusinessObject2"/>
```
这是非常拥有的,如果你想应用这些相同的advice到许多的业务对象上,只需要放置一个这样的基础设施bean即可,你能够创建许多的业务对象而无需包括特定的代理配置,你能够非常容易的删除额外的切面(例如 tracing or 性能检测的切面)让配置做最小改变 \
默认的AdvisorAutoProxyCreator 提供了对过滤器的支持(通过使用名称习惯,因此它仅仅有一些advisor会被执行),这允许多个使用,不同的配置的AdvisorAutoProxyCreator 到相同的工厂) 并排序; Advisor也能够实现 org.springframework.core.Ordered 接口来确保正确的顺序(如果它是一个问题),这个TransactionAttributeSourceAAdvisor (在前面例子中)已经配置过一个顺序值,默认设置是未排序的;
#### TargetSource 实现
Spring 提供了 TargetSource 的概念，在 org.springframework.aop.TargetSource 接口中表达。该接口负责返回实现连接点的“目标对象”。每次 AOP 代理处理方法调用时，都会要求 TargetSource 实现提供一个目标实例 \
使用 Spring AOP 的开发人员通常不需要直接使用 TargetSource 实现，但这提供了一种支持池化、热插拔和其他复杂目标的强大方法。例如，通过使用池来管理实例，池化 TargetSource 可以为每次调用返回不同的目标实例。 \
如果未指定 TargetSource，则使用默认实现来包装本地对象。每次调用都会返回相同的目标（如您所料） \
本节的其余部分描述 Spring 提供的标准目标源以及如何使用它们 \
使用自定义目标源时，您的目标通常需要是原型而不是单例 bean 定义。这允许 Spring 在需要时创建一个新的目标实例 \
##### Hot-swappable Target Sources
org.springframework.aop.target.HotSwappableTargetSource 的存在是为了让 AOP 代理的目标被切换，同时让调用者保持对它的引用。 \
更改目标源的目标会立即生效。 HotSwappableTargetSource 是线程安全的。\
您可以使用 HotSwappableTargetSource 上的 swap() 方法更改目标，如下例所示:
```java
HotSwappableTargetSource swapper = (HotSwappableTargetSource) beanFactory.getBean("swapper");
Object oldTarget = swapper.swap(newTarget);
```
xml
```xml
<bean id="initialTarget" class="mycompany.OldTarget"/>

<bean id="swapper" class="org.springframework.aop.target.HotSwappableTargetSource">
    <constructor-arg ref="initialTarget"/>
</bean>

<bean id="swappable" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="targetSource" ref="swapper"/>
</bean>
```
尽管此示例未添加任何通知（使用 TargetSource 无需添加通知），但任何 TargetSource 都可以与任意通知结合使用
####  Pooling Target Sources
使用池目标源提供了与无状态会话 EJB 类似的编程模型，其中维护了相同实例的池，方法调用将释放池中的对象 \
Spring 池化和 SLSB 池化之间的一个重要区别是 Spring 池化可以应用于任何 POJO。与一般的 Spring 一样，可以以非侵入性方式应用此服务 \
Spring 提供了对 Commons Pool 2.2 的支持，它提供了一个相当高效的池化实现。您需要应用程序的类路径上的 commons-pool Jar 才能使用此功能。您还可以继承 org.springframework.aop.target.AbstractPoolingTargetSource 以支持任何其他池 API \
注意: Commons Pool 1.5+ 也受支持，但自 Spring Framework 4.2 起已弃用
```xml
<bean id="businessObjectTarget" class="com.mycompany.MyBusinessObject"
        scope="prototype">
    ... properties omitted
</bean>

<bean id="poolTargetSource" class="org.springframework.aop.target.CommonsPool2TargetSource">
    <property name="targetBeanName" value="businessObjectTarget"/>
    <property name="maxSize" value="25"/>
</bean>

<bean id="businessObject" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="targetSource" ref="poolTargetSource"/>
    <property name="interceptorNames" value="myInterceptor"/>
</bean>
```
请注意，目标对象（前面示例中的 businessObjectTarget）必须是原型。这允许 PoolingTargetSource 实现创建目标的新实例以根据需要增加池。有关其属性的信息，请参阅 AbstractPoolingTargetSource 和您希望使用的具体子类的 javadoc。 maxSize 是最基本的，并且始终保证存在 \
在这种情况下， myInterceptor 是需要在同一 IoC 上下文中定义的拦截器的名称。但是，您无需指定拦截器即可使用池化。如果您只需要池化而不需要其他建议，则根本不要设置拦截器名称属性 \
您可以将 Spring 配置为能够将任何池对象转换为 org.springframework.aop.target.PoolingConfig 接口，该接口通过介绍公开有关池的配置和当前大小的信息。您需要定义一个类似于以下内容的顾问：
```xml
<bean id="poolConfigAdvisor" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
    <property name="targetObject" ref="poolTargetSource"/>
    <property name="targetMethod" value="getPoolingConfigMixin"/>
</bean>
```
该顾问(Advisor)程序是通过调用 AbstractPoolingTargetSource 类的便捷方法获得的，因此使用 MethodInvokingFactoryBean。此顾问程序的名称（此处为 poolConfigAdvisor）必须位于暴露池对象的 ProxyFactoryBean 中的拦截器名称列表中 \
强转如下:
```java
PoolingConfig conf = (PoolingConfig) beanFactory.getBean("businessObject");
System.out.println("Max pool size is " + conf.getMaxSize());
```
通常不需要池化无状态服务对象。我们不认为它应该是默认选择，因为大多数无状态对象自然是线程安全的，如果资源被缓存，实例池就会有问题 \
使用自动代理可以实现更简单的池化。您可以设置任何自动代理创建者使用的 TargetSource 实现
#### Prototype Target Sources
设置“原型”目标源类似于设置池化目标源,尽管在现代 JVM 中创建新对象的成本并不高，但连接新对象（满足其 IoC 依赖性）的成本可能会更高,尽量减少它的使用 \
修改如下配置:
```xml
<bean id="prototypeTargetSource" class="org.springframework.aop.target.PrototypeTargetSource">
    <property name="targetBeanName" ref="businessObjectTarget"/>
</bean>
```
唯一的属性是目标 bean 的名称。 TargetSource 实现中使用继承以确保命名一致。与池化目标源一样，目标 bean 必须是原型 bean 定义。
#### ThreadLocal Target Sources 
如果您需要为每个传入请求（即每个线程）创建一个对象，则 ThreadLocal 目标源非常有用。 ThreadLocal 的概念提供了一个 JDK 范围的工具，可以在线程旁边透明地存储资源。设置 ThreadLocalTargetSource 与针对其他类型目标源的解释几乎相同，如下例所示
```xml
<bean id="threadlocalTargetSource" class="org.springframework.aop.target.ThreadLocalTargetSource">
    <property name="targetBeanName" value="businessObjectTarget"/>
</bean>
```
ThreadLocal 实例在多线程和多类加载器环境中错误使用时会带来严重问题（可能导致内存泄漏）,您应该始终考虑将 threadlocal 包装在其他某个类中，并且永远不要直接使用 ThreadLocal 本身（包装类除外）,您应该始终记住正确设置和取消设置（后者只涉及调用 ThreadLocal.set(null)）线程本地的资源。在任何情况下都应该取消设置，因为不取消设置可能会导致有问题的行为,Spring 的 ThreadLocal 支持为您完成了这项工作，并且应该始终考虑支持使用 ThreadLocal 实例而无需其他适当的处理代码。
#### Defining New Advice Types
Spring AOP 被设计为可扩展的。虽然拦截实现策略目前在内部使用，但除了围绕通知、之前、抛出通知和返回通知之后的拦截之外，还可以支持任意通知类型 \
org.springframework.aop.framework.adapter 包是一个 SPI 包，它允许在不更改核心框架的情况下添加对新自定义建议类型的支持。自定义 Advice 类型的唯一约束是它必须实现 org.aopalliance.aop.Advice 标记接口

