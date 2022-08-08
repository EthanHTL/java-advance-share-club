# AOP with Spring
Aop(面向切面编程 aspect oriented programing) 它完善了面向对象编程(Object Oriented programing)(通过提供另一种程序解构的方式) .. \
OOP的模块化单元是类,但是AOP的模块化单元是切面 .. 它启用了关注点的模块化(例如事务管理) - 跨越多个类型和对象 ..(在AOP的术语中叫做 横切) ... \
Spring并不依赖Aop,但是它为Spring 提供了强大的中间件解决方案 ..
### Spring aop with AspectJ pointcuts
Spring 提供了简单的以及强有力的方式编写一个自定义切面(通过使用基于schema-base 方式 或者@AspectJ 注解的方式),这两种方式都提供了全面的类型通知(advice)并且使用Aspect 切面语言,然后使用Spring AOP 进行编织 .. \
Aop 在Spring 框架中被用来:
- 提供声明式企业级服务,这种服务最重要的是声明式事务管理 ..
- 让用户实现自定义切面,通过AOP 补全它们的OOP使用 ... 

## 5.1 AOP 概念
- Aspect: 横切多个类的关注点的模块化,事务管理是一个好的例子
  在Spring AOP中,aspects 通过普通类实现(例如基于schema的方式)或者通过使用@Aspect注解注释的类(@AspectJ 风格)
- 连接点
    一个程序在执行期间的连接点,例如方法的执行或者异常的处理,在Spring的AOP中,这个连接点总是代表着方法执行 ...
- 通知
    通过一个切面在指定的连接点调用的动作,包含不同类型的advice,例如"around" / "before" / "after"通知,再许多AOP 框架中,包括Spring,模块化一个通知作为拦截器并围绕着连接点维护一个连接器链 ...
    例如Spring 通过责任链模式调用 所有的advice(如果一个连接点附近有这些advice) ..
- 切入点
    匹配连接点的条件,Advice通过切入点表达式关联并且再任何连接点匹配切入点的时候运行(例如,具体名称的方法执行),与切入点表达式匹配的连接点的概念是 AOP 的核心,并且Spring使用AspectJ 切入点表达式语言处理(默认) ..
- 引入(介绍)
    声明可选的方法或者字段(代表类型声明),Spring Aop让你能够引入新的接口(以及一个对应的实现)到任何advised 对象,举个例子你能够使用一个引入去让一个bean实现 IsModified接口,实现简单缓存(再Aspect社区一个引入被称为中间类型声明) ..
- 目标对象
    一个能够被一个或者多个切面通知(建议)的对象,也成为 advised 对象(被建议对象),Spring Aop通过使用运行时代理实现(这个对象总是代理对象) ...
- Aop 代理
    由Aop框架创建的对象(为了实现切面约定(通知方法的执行)) .. Spring框架中,一个Aop代理是JDK动态代理或者CGLIB 代理 ..
- 编织
    关联其他应用类型或者对象去创建一个advised 对象,这能够在编译时完成(通过AspectJ 编译器,举个例子),加载时或者运行时.  Spring Aop就像其他纯java AOP框架,在运行时执行编织 ...
- Spring Aop支持的通知类型
    - 前置通知
    - 后置返回通知
    - 后置异常通知
    - 后置通知(不管连接点是否正常或者异常退出)
    - 环绕通知(它等价于其他前面几种通知的合并)
    
- 所有通知的参数都是静态强类型,因此需要合适的类型进行工作(例如从方法返回的返回值的类型必须是确定的,不可以是Object数组) ..
- 通过切入点匹配连接点的概念是AOP的关键,它不同于仅提供拦截(切入点让通知的目标独立于面向对象体系) ..例如通过声明式事务管理 环绕通知一组方法或者多个对象(所有服务层的业务操作) ..

## 5.2 Spring Aop 能力以及目标
- Spring Aop 目前仅支持连接点方法执行(Spring bean上的方法执行通知),字段拦截并没有实现(尽管这是可以实现的),如果你需要通知字段访问或者更新连接点(考虑AspectJ 了解更多) ..
- Spring Aop 并不是完整的aop实现,仅仅是为了和Spring 容器紧密集成(解决一些问题) ..
- Spring Aop的目标是和成熟的Aop框架相辅相成,例如SpringAop和IOC通过AspectJ 紧密集成,不会影响SpringAop API 或者Aop Alliance API ..

## 5.3 Aop 代理
Spring Aop 默认使用JDK动态代理,能够让任何接口或者接口列表被代理 ..,Cglib也是允许的,但是最好建议基于接口编程 ..

## 5.4 @AspectJ 支持
它是在AspectJ5引入的,Spring 使用相同的注解提供SpringAop支持,这个AOP 运行时是纯的Spring Aop,不依赖于AspectJ的编译器或者编织器 .. \
SpringAop 基于@AspectJ和自动代理Bean决定是否支持切面的通知 ...  -> 通过为bean 生成代理拦截方法调用确保通知能够按需运行 .. \
但是你还是需要引入依赖 aspectjweaver.jar ...
### 基于Java 配置启用@AspectJ 支持
```java
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

}
```
### 声明一个切面
```java
package org.xyz;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class NotVeryUsefulAspect {

}
```
### 注意
切面 类能够有方法和字段,同其他类一样,并且它们能够包含切入点,通知并引入中间类型声明 ..
### 通过组件扫描自动检测切面
仅仅只需要为切面类加上@Component 注解即可 ... 只要符合自动扫描规则即可...并且切面不能够作为其他切面的通知目标(它们仅仅是一个切面,因此它们从自动代理中排除) ...

## 5.4.3 声明一个切入点
切入点决定了我们对那些方法的执行感兴趣,切入点签名通过普通的方法定义决定(并且切入点表达式通过@Pointcut注解表示)且此方法返回值为 void ... \
例如:
```text
@Pointcut("execution(* transfer(..))") // the pointcut expression
private void anyOldTransfer() {} // the pointcut signature
```
完整的Aop切入点语言可以查看Aspectj 编程指南(https://www.eclipse.org/aspectj/doc/released/progguide/index.html),扩展[Aspectj 5 Developer's Notebook](https://www.eclipse.org/aspectj/doc/released/adk15notebook/index.html) \
### 支持切入点符号
- execution
    匹配方法执行连接点,这是主要的切入点符号(当和Spring AOP工作的时候) ..
- within
    限制匹配的连接点必须在某些类型中(当使用在Spring Aop中时表示某个匹配类型的声明的方法执行),也就是说它限定了连接点声明的类型范围 ...
- this
    限制匹配的连接点(当使用Spring Aop时表示方法的执行)这里的bean 引用(Spring Aop代理, bean 代理对象)必须是给定类型的实例 ..
- target
    限制匹配连接点(Spring Aop表示方法的执行) - 这里的目标对象(被代理的目标对象)是给定类型的实例 ..
- args
    限制匹配连接点(.... 同上) 这里的参数必须是给定类型的实例(运行时参数的类型)
- @target
    限制匹配连接点(...同上),表示执行对象的类必须有给定类型的注解 ...
- @args
    限制匹配连接点,传递的实际参数的运行时类型必须是包含给定类型的注解(参数所携带的注解) ..
- @within
    限制匹配连接点必须保证类型拥有给定的注解(当使用Spring Aop时表示声明给定注解的类型中声明的方法执行) 方法被限定在类上注释了对应的注解(它给定了一个范围) ...
- @annotation
    限制连接点匹配需要连接点的主体拥有给定注解 ...(在Spring Aop中连接点是方法执行) ...

- 其次由于Spring Aop仅仅限制 方法执行连接点,对于Aspectj自己的类型语义,在切入点,this / target 都指向同一个对象,执行方法的对象 .. \
    Spring Aop是一个基于代理的系统并且在代理对象自身(this)和目标对象是不一样的(target),因此基于代理的本质,在目标对象中调用将没有任何效果(所以不会被拦截) ...
    对于JDK代理,仅仅在代理上的公共接口方法调用才能够被拦截,使用CGLIB,public / protected 方法调用(在代理上的)都能够被拦截(并且甚至是包可见的方法,如果有必要) .. \
    然而大多数交互(通过代理)仅仅总是设计为通过公共签名处理 ..,请注意，切入点定义通常与任何拦截的方法匹配。如果切入点严格来说是只公开的，即使在 CGLIB 代理场景中，通过代理进行潜在的非公开交互，也需要相应地定义它。
  - 如果你的拦截需要包括在目标类中进行方法调用或者甚至是构造器调用(也就是考虑目标类的方法调用拦截),考虑使用Spring驱动的 [native Aspectj weaving](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop-aj-ltw) 代替
  Spring的基于代理的Aop框架,这就构成了具有不同特点的不同AOP使用模式，所以在做决定之前一定要让自己熟悉编织。
  - Spring Aop同样支持额外的PCD 命名的bean,PCD 让你限制了切入点的匹配到一个特殊命名的Spring Bean或者一组命名的Spring bean(当使用通配符的时候),bean PCD如下定义
  ```text
  bean(idOrNameOfBean)
  ```
  通过这样你就能够 表达式结合,例如 && || !  .. bean PCD 仅仅支持在Spring Aop中,它是标准的Spring 特定的PCD扩展(对于使用@Aspect模型声明的切面是不可用的) .. \
  bean PCD 操作在实例级别(构建与Spring bean 名称的概念)而不是 仅类型级别(对于基于编织的AOP 收到影响) .. 基于实例的切入点符号是Spring 基于代理框架的特殊能力 ..它提供了与Spring bean工厂的紧密集成(这非常直观,通过名称标识一个特定的bean) ..

### 切入点表达式混合
```text
@Pointcut("execution(public * *(..))")
private void anyPublicOperation() {} 

@Pointcut("within(com.xyz.myapp.trading..*)")
private void inTrading() {} 

@Pointcut("anyPublicOperation() && inTrading()")
private void tradingOperation() {} 
```

