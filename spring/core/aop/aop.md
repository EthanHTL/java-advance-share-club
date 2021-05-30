#Aop
spring 本身提供了Aop编程,根OOP编程类似,一个是面向切面编程,一个是面向对象编程,Aop提出了几大概念:
1.pointCut 切入点(目前支持方法拦截,但是切入点表达式可以是任意的,比如拦截某个类的所有方法执行),就是定义你需要拦截的方法
2.jointPoint 就是连接点(连接点和advice是相联系的),通过的pointcut拦截的对应类型的advice方法执行时所给出的jointPoint参数;
3.advice 是对pointCut 切入点指定的拦截通知,然后将对应的程序参数和jointPoint相联系,advice分为 前置、返回、返回异常、环绕通知;
4.AspectJ 一个切面(因为一个方法可能会被多个切面拦截),切面和对象是独立分开的; \
在spring中,Aop不是一个特殊的概念,它和AspectJ是互补的,通过AspectJ来对切面进行生成代理(拦截方法执行,调用advice),
所以在使用SpringAop Api的时候,需要导入aspectjweaver.jar的包作为切面的支持;

#### 开启Aop代理
* @EnableAspectJAutoProxy 注解和@Configuration配合使用,
开启对切面的支持;
```java
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

}
```
对于xml schema形式的
```xml
<aop:aspectj-autoproxy/>
```
即可;
##### 声明一个切面
@Aspect到一个普通的java 类上即可
```java

@Aspect
public class NotVeryUsefulAspect {

}
```
xml形式
```xml
<bean id="myAspect" class="org.xyz.NotVeryUsefulAspect">
    <!-- configure properties of the aspect here -->
</bean>
```
切面会自动通过组件扫描作为组件,但是前提是你需要使用@Component标识他是一个组件;
##### 声明一个pointcut
它决定了joinPoint的类型以及数据;
```java
@Aspect
public class customAop {
    @Pointcut("execution(* transfer(..))")
    public void pointCut(){}
}
```
这就声明了一个切面并生成了一个pointCut,它会拦截任何方法名为transfer的函数;
@PointCut需要填入一个aspectj的切入点表达式;
##### 支持PointCut的代号(标识符)
1) execution 使用的最多的标识符表达式
2) within 限制joinPoint的类型(在执行某些具体类型内声明的方法时需要)
3) this 限制join point的匹配(同样需要标识当前aop代理是给定类型的实例才能够匹配)
4) target 限制joinPoint的匹配(需要标识目标对象是给定类型的实例(一般是被代理的应用对象))
5) args 限制jointPoint匹配(标识参数是给定的类型)
6) 这些标识符有着对应的注解形式(@target @args @within @annotation(限制joinPoint,限制执行的方法需要有指定的注解!)) \
对于其他完整的切面标识符将不允许,抛出IllegalArgumentException ; \
AspectJ 本身是具有基于类型的定义,this和Target指向同一个对象,但是spring aop 是一个aop系统,所以this指向原始对象,target标识代理后的对象; \
由于spring aop的代理性质,目标对象内的调用不会被拦截,对于jdk代理,那么只有公共的方法调用能够被拦截,对于cglib公共的、保护的、甚至包可视的方法都能够被拦截,然而大多数情况应该和公共(public)方法进行交互;
##### bean(beannameorid)
能够限制匹配的joinPoint,调用的方法的方法bean是那种springBean或者一个springbean的名称集合,可以使用通配符; \
它能和其他描述符使用 && || !,此描述符不支持原生aspectj 编织; \
它是基于实例级别的,而不是基于类级别的切面代理;
##### 合并表达式
切入点的表达式合并;
```java
@Pointcut("execution(public * *(..))")
private void anyPublicOperation() {} 

@Pointcut("within(com.xyz.myapp.trading..*)")
private void inTrading() {} 

@Pointcut("anyPublicOperation() && inTrading()")
private void tradingOperation() {} 
```
##### 共享普通的切入点表达式定义
大多数情况下,我们可能想要引用应用的模块以及从各种切面中使用各种操作集合,推荐创建一个CommonPointcuts  切面收集所有普通的切入点表达式;
```java
package com.xyz.myapp;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class CommonPointcuts {

    /**
     * A join point is in the web layer if the method is defined
     * in a type in the com.xyz.myapp.web package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.myapp.web..*)")
    public void inWebLayer() {}

    /**
     * A join point is in the service layer if the method is defined
     * in a type in the com.xyz.myapp.service package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.myapp.service..*)")
    public void inServiceLayer() {}

    /**
     * A join point is in the data access layer if the method is defined
     * in a type in the com.xyz.myapp.dao package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.myapp.dao..*)")
    public void inDataAccessLayer() {}

    /**
     * A business service is the execution of any method defined on a service
     * interface. This definition assumes that interfaces are placed in the
     * "service" package, and that implementation types are in sub-packages.
     *
     * If you group service interfaces by functional area (for example,
     * in packages com.xyz.myapp.abc.service and com.xyz.myapp.def.service) then
     * the pointcut expression "execution(* com.xyz.myapp..service.*.*(..))"
     * could be used instead.
     *
     * Alternatively, you can write the expression using the 'bean'
     * PCD, like so "bean(*Service)". (This assumes that you have
     * named your Spring service beans in a consistent fashion.)
     */
    @Pointcut("execution(* com.xyz.myapp..service.*.*(..))")
    public void businessService() {}

    /**
     * A data access operation is the execution of any method defined on a
     * dao interface. This definition assumes that interfaces are placed in the
     * "dao" package, and that implementation types are in sub-packages.
     */
    @Pointcut("execution(* com.xyz.myapp.dao.*.*(..))")
    public void dataAccessOperation() {}

}
```
例如以xml形式引用切入点:
```xml
<aop:config>
    <aop:advisor
        pointcut="com.xyz.myapp.CommonPointcuts.businessService()"
        advice-ref="tx-advice"/>
</aop:config>

<tx:advice id="tx-advice">
    <tx:attributes>
        <tx:method name="*" propagation="REQUIRED"/>
    </tx:attributes>
</tx:advice>
```
##### examples
首先是execution 使用的比较多,定义如下
```text
   execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern)
                throws-pattern?)
```
modifiers-pattern以及param-pattern以及throws-pattern是可选的,其次 *可以作为表达式的全部或者一部分,来通用匹配,其次name-pattern是可以通过对应组件(类)尾随一个.来连接一个方法,参数表达式通常是.. 标识零个或者多个参数,*标识一个任意类型的参数,(*,String)表示第一个为任意类型,第二个参数为string; \
根据这个表达式的说明:
```java
execution(public * *(..))
```
```java
  execution(* set*(..))
```
```java
execution(* com.xyz.service.AccountService.*(..))
```
AccountService类下的任意方法;
```java
 execution(* com.xyz.service.*.*(..))
```
service包下的xx类xx方法

```java
execution(* com.xyz.service..*.*(..))
```
那么这个就可以是service或者零个或者多个子包下的xx类xx方法拦截;
```java
 within(com.xyz.service.*)
```
这个也是表示方法需要是在com.xyz.service包下
```java
 within(com.xyz.service..*)
```
表示service以及子包
```java
this(com.xyz.service.AccountService)
```
表示此方法位于AccountService类中;
```java
   args(java.io.Serializable)
```
此处的args匹配条件和execution(* *(java.io.Serializable))不一致,前一个表示参数类型为Serializable即可,后一个表示第一个参数类型为Serializable;
```java
  @target(org.springframework.transaction.annotation.Transactional)
```
表示目标方法包含了此注解进行拦截;
##### 编写一个好的pointCut
由于需要编译,aspectj需要处理切入点来优化匹配性能,可以静态或者动态的检测匹配条件,匹配过程需要大量的处理,对于动态来说,静态分析无效,然后第一次遇见切入点声明时,aspectj会将其重写为匹配过程的最佳形式,就是通过切入点进行析取范式(DNF)重写,并且会对切入点的组件进行排序,然后首先对成本较低的组件进行评估,那么这就意味着你可以随意使用切入点表达式描述符,不需要关心它们的执行效率; \
aspectj需要告诉它应该怎么做,为了提高匹配的性能,需要思考如何创建优秀的切入点(减少匹配范围),需要从三方面进行考虑:
1) kinded
    包含了 execution ,get ,set,call,handler
2) scoped
    包含了within 以及withcode
3） Contextual
    包含了 this, target 以及@annotation

一个好的切入点应该同时包含kinded以及scoped的描述符;
这样aspectj能够通过scope的分组描述符快速的消除还未执行但不应该执行的描述符分组,提升性能;

##### 声明一个advice
advice是和切入点表达式相关联,包括before、after、around等等;
一个pointcut表达式可以是一个简单的pointcut的引用,也可以是pointcut的表达式集合;

##### before
前置通知@Before
```java
@Aspect
public class BeforeExample {

    @Before("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doAccessCheck() {
        // ...
    }
}
```
注解中写入切入点表达式
当然也可以是
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

    @Before("execution(* com.xyz.myapp.dao.*.*(..))")
    public void doAccessCheck() {
        // ...
    }
}
```
##### 后置返回通知
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;

@Aspect
public class AfterReturningExample {

    @AfterReturning("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doAccessCheck() {
        // ...
    }
}   
```
有些时候我们需要拿到返回值:
```java

@Aspect
public class AfterReturningExample {

    @AfterReturning(
        pointcut="com.xyz.myapp.CommonPointcuts.dataAccessOperation()",
        returning="retVal")
    public void doAccessCheck(Object retVal) {
        // ...
    }
}
```
就这样做即可,如果要严格限制匹配返回的类型的结果,那么将Object改为对应类型即可;
##### after throwing advice
```java
@Aspect
public class AfterThrowingExample {

    @AfterThrowing("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doRecoveryActions() {
        // ...
    }
}
```
如果要接收异常:
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterThrowing;

@Aspect
public class AfterThrowingExample {

    @AfterThrowing(
        pointcut="com.xyz.myapp.CommonPointcuts.dataAccessOperation()",
        throwing="ex")
    public void doRecoveryActions(DataAccessException ex) {
        // ...
    }
}
```
这不意味着它是一个异常回调方法,它的异常接收来自于目标方法的异常抛出类型,而不是来源于@After或者@AfterThrowing

##### after 后置通知
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;

@Aspect
public class AfterFinallyExample {

    @After("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doReleaseLock() {
        // ...
    }
}
```
它可以进行方法返回的收尾动作或者相似的目的;同时不同于try-finally来说,它接受目标方法返回的正确结果或者异常,而@AfterReturning 只接收正确结果!
##### 环绕通知
例如before以及after都需要运行(即使方法实际获取运行的结果),环绕通知是有用的(如果需要再before以及after的方法执行中保持线程安全,例如开启、关闭一个定时器);
环绕通知可以获取一个连接点(类型为ProceedingJoinPoint),它可以使用在通知中,通过proceed()调用目标方法执行并返回结果,proceed()方法支持参数; \
当用Object []进行调用时，proceed()的行为与AspectJ编译器编译的around advice的proceed()行为略有不同。对于使用传统 AspectJ 语言编写的环绕通知，传递给procedure 的参数数量必须与传递给环绕通知的参数数量（而不是底层连接点采用的参数数量）相匹配，并且传递给proceed() 的值在一个给定的参数位置取代了该值绑定到的实体的连接点处的原始值（如果这现在没有意义，请不要担心）。 Spring 采用的方法更简单，并且更符合其基于代理的仅执行语义。如果您编译为 Spring 编写的 @AspectJ 方面并使用继续与 AspectJ 编译器和编织器的参数，您只需要知道这种差异。有一种方法可以编写跨 Spring AOP 和 AspectJ 100% 兼容的方面,更多参考[兼容](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop-ataspectj-advice-params)
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;

@Aspect
public class AroundExample {

    @Around("com.xyz.myapp.CommonPointcuts.businessService()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        // start stopwatch
        Object retVal = pjp.proceed();
        // stop stopwatch
        return retVal;
    }
}
```
##### 访问当前JoinPoint
任何通知方法的第一个参数都可以是连接点,类型为org.aspectj.lang.JoinPoint,在环绕通知中类型为ProceedingJoinPoint;
有以下方法可以使用:
* getArgs() 返回方法参数
* getThis() 返回代理对象
* getTarget() 返回目标对象
* getSignature 返回已经通知完毕的方法描述
* toString() 打印出被通知的方法的有用描述

##### 给advice传递参数
可以使用具有args的切入点表达式,例如
```java
@Before("com.xyz.myapp.CommonPointcuts.dataAccessOperation() && args(account,..)")
public void validateAccount(Account account) {
    // ...
}
```
第二种方式可以是给切入点匹配入参,如果你需要:
```java
@Pointcut("com.xyz.myapp.CommonPointcuts.dataAccessOperation() && args(account,..)")
private void accountDataAccessOperation(Account account) {}

@Before("accountDataAccessOperation(account)")
public void validateAccount(Account account) {
    // ...
}
```
这样匹配到了具体方法会传递此参数,代理对象(this),目标对象target以及注解@within,@target @annotation 以及@args 都能够通过相似的方式绑定,例如匹配被@Auditable注解的方法:
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    AuditCode value();
}
```
注解定义如上,
```java
@Before("com.xyz.lib.Pointcuts.anyPublicMethod() && @annotation(auditable)")
public void audit(Auditable auditable) {
    AuditCode code = auditable.value();
    // ...
}
```
前置通知实现如上;
##### 通知参数以及泛型
spring aop能够处理在类上声明的泛型以及方法参数,假设你有一个定义:
```java
public interface Sample<T> {
    void sampleGenericMethod(T param);
    void sampleGenericCollectionMethod(Collection<T> param);
}
```
你能够限制方法类型的拦截(使得参数类型必须是某些类型),通过通知方法的参数取限制目标方法的参数类型:
```java
@Before("execution(* ..Sample+.sampleGenericMethod(*)) && args(param)")
public void beforeSampleMethod(MyType param) {
    // Advice implementation
}
```
例如以下定义通知不会生效:
```java
@Before("execution(* ..Sample+.sampleGenericCollectionMethod(*)) && args(param)")
public void beforeSampleMethod(Collection<MyType> param) {
    // Advice implementation
}
```
除此之外,你可以将类型声明为collection<?>,这样唯一的后果就是自己检查集合中的元素类型;

##### 决定参数名称
绑定在通知执行的参数依赖于切入点方法签名和切入点表达式中声明的参数名称匹配的名称,对于java反射来说,参数名不必要,因此aop使用了以下策略明确参数名:
* 如果参数名被显式声明,那么将使用此参数名,在通知和切入点表达式上有一个可选的argNames属性(你能够指定被注解方法的参数名称),这个参数名称在运行时是必要的,例如:
```java
@Before(value="com.xyz.lib.Pointcuts.anyPublicMethod() && target(bean) && @annotation(auditable)",
        argNames="bean,auditable")
public void audit(Object bean, Auditable auditable) {
    AuditCode code = auditable.value();
    // ... use code and bean
}
```
如果第一个参数为JoinPoint, ProceedingJoinPoint, or JoinPoint.StaticPart中的其中一种,你不需要在argNames中设置切入点的名称
```java
@Before(value="com.xyz.lib.Pointcuts.anyPublicMethod() && target(bean) && @annotation(auditable)",
        argNames="bean,auditable")
public void audit(JoinPoint jp, Object bean, Auditable auditable) {
    AuditCode code = auditable.value();
    // ... use code, bean, and jp
}
```
当只有连接点的时候,不需要使用argNames,其次使用argNames是比较蠢的,例如spring aop查看类的调试信息并尝试从局部变量表中确定参数名称,只要使用了调试信息编译此类,就存在此信息(通过 -g:vars)启动此标志编译:
    1) 代码会更容易理解
    2) 类文件大小会稍微大一点
    3) 优化删除未使用的本地变量(编译器不会应用) \
如果一个@AspectJ 切面通过AspectJ 编译器编译(但是没有使用 debug 信息),不需要添加argNames属性，因为编译器会保留所需的信息 \
* 如果代码没有通过必要的调试信息编译(已编译),spring aop 尝试绑定参数 到变量参数的配对推断(例如,如果切入点表达式中只有一个变量绑定,那么通知方法应该只有一个参数,和之前的进行配对),如果变量的绑定(和给定的必要信息存在二义性,那么会抛出AmbiguousBindingException \
* 如果上面的策略都失败了,抛出IllegalArgumentException

##### 使用参数执行
这里给出如何将proceed方法并传递给定参数在spring aop 和Aspectj中工作一致,解决方案就是让通知方法签名绑定的参数顺序和目标方法参数顺序一致;
```java
@Around("execution(List<Account> find*(..)) && " +
        "com.xyz.myapp.CommonPointcuts.inDataAccessLayer() && " +
        "args(accountHolderNamePattern)")
public Object preProcessQueryPattern(ProceedingJoinPoint pjp,
        String accountHolderNamePattern) throws Throwable {
    String newPattern = preProcess(accountHolderNamePattern);
    return pjp.proceed(new Object[] {newPattern});
}
```
请注意,com.xyz.myapp.CommonPointcuts.inDataAccessLayer()是一个切入点;
##### 通知的顺序
当同一个切面的多个通知都在同一个连接点运行,那么必然存在执行顺序,高优先级先执行,但是后返回,低优先级后执行先返回;
当在同一个连接点运行的不同切面的通知时,除非指定了顺序,否则执行顺序是未知的,你能够控制顺序,只需要实现Ordered接口提供顺序,或者声明@Order注解; \
注意:  通知都是直接和连接点接入,所以@AfterThrowing不建议从从@After或者@AfterReturning 接收异常; \
在spring5.2.7开始,相同@Aspect类的通知方法运行在相同连接点基于通知类型的优先级顺序进行执行;
@After方法的执行建议放在同一个切面的@AfterReturning 或者 @AfterThrowing通知方法之后,考虑"after finally advice"的概念 \
由于同一个切面的多个相同类型在于同一个连接点的通知方法执行顺序未知这导致通过javac编译类的时候无法获取源码声明顺序,考虑将方法变成一个通知方法或者分别放入不同切面,并通过Order接口或者注解进行执行顺序调节;
