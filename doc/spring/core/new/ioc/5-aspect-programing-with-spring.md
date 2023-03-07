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
    
环绕通知是最基础的通知类型,因此Spring Aop,就像AspectJ一样,提供了大量的通知类型,我们推荐你使用一些有用的通知类型去实现需要的行为,举个例子: \
如果你仅仅只需要根据方法的返回值更新缓存,那么你最好提供一个后置返回通知,而不是环绕通知,尽管环绕通知能够做到相同的事情,使用最恰当的类型去提供最简单的编程模型,\
会减少很多错误,例如你不需要执行连接点上的proceed()方法,那么你不应该导致环绕通知调用失败 ..
- 所有通知的参数都是静态强类型,因此需要合适的类型进行工作(例如从方法返回的返回值的类型必须是确定的,不可以是Object数组) ..
- 通过切入点匹配连接点的概念是AOP的关键,它不同于仅提供拦截(切入点让通知的目标独立于面向对象体系) ..例如通过声明式事务管理 环绕通知一组方法或者多个对象(所有服务层的业务操作) ..

## 5.2 Spring Aop 能力以及目标
- Spring Aop 目前仅支持连接点方法执行(Spring bean上的方法执行通知),字段拦截并没有实现(尽管这是可以实现的),如果你需要通知字段访问或者更新连接点(考虑AspectJ 了解更多) ..
- Spring Aop 并不是完整的aop实现,仅仅是为了和Spring 容器紧密集成(解决一些问题) ..
- Spring Aop的目标是和成熟的Aop框架相辅相成,例如SpringAop和IOC通过AspectJ 紧密集成,不会影响SpringAop API 或者Aop Alliance API .. \
    尽管SpringAop已经足够,但是你不能够通过advice 通知一个细腻化的对象(例如领域对象),AspectJ 是这种情况的更好的选择,然而Spring Aop已经提供了非常不错的解决方案(针对于大多数问题) \
    
## 5.3 Aop 代理
Spring Aop 默认使用JDK动态代理,能够让任何接口或者接口列表被代理 ..,Cglib也是允许的,但是最好建议基于接口编程 ..

## 5.4 @AspectJ 支持
它是在AspectJ5引入的,Spring 使用相同的注解提供SpringAop支持,这个AOP 运行时是纯的Spring Aop,
不依赖于AspectJ的编译器或者编织器 .. \
SpringAop 基于@AspectJ和自动代理Bean决定是否支持切面的通知 ...  -> 通过为bean 生成代理拦截方法调用确保通知能够按需运行 .. \
但是你还是需要引入依赖 aspectjweaver.jar ...(为了如果需要Aspect编译器以及编织器 启用完整的AspectJ 语言的支持)
### 5.4.1 基于Java 配置启用@AspectJ 支持
首先需要启用Spring对基于@AspectJ 切面的Spring Aop配置支持以及自动代理bean 支持(是否能够被这些切面进行通知),通过自动代理 \
意味着bean 能够通过一个或者多个切面进行通知, 能够自动为bean 生成代理并拦截方法执行确保通知按需执行 .. \
@Aspectj 支持xml / java 风格的配置,同样需要确保Aspectj的aspectjweaver.jar 库放置到类路径上(1.8及其以后的版本) ..
#### 使用java配置启用@Aspectj 支持
```java
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

}
```
or 
```xml
<aop:aspectj-autoproxy/>
```
对于xml 形式的方式不详细解释 ..


### 5.4.2 声明一个切面
这能够启用@Aspectj支持,将自动的被Spring 检测并被用来配置AOP ..
```java
package org.xyz;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class NotVeryUsefulAspect {

}
```
or
```xml
<bean id="myAspect" class="org.xyz.NotVeryUsefulAspect">
    <!-- configure properties of the aspect here -->
</bean>
```
#####  注意
切面 类能够有方法和字段,同其他类一样,并且它们能够包含切入点,通知并引入中间类型声明 ..
##### 通过组件扫描自动检测切面
仅仅只需要为切面类加上@Component 注解即可 ... 只要符合自动扫描规则即可... \
注意: 并且切面不能够作为其他切面的通知目标(它们仅仅是一个切面,因此它们从自动代理中排除) ... \
除此之外,也可以使用将@Component作为元注解的 模板注解(例如携带了选择条件@Qualifier ),只要能满足Spring的组件扫描器的规则 ..

## 5.4.3 声明一个切入点
切入点决定了我们对那些方法的执行感兴趣,切入点签名通过普通的方法定义决定(并且切入点表达式通过@Pointcut注解表示)且此方法返回值为 void ... \
一个切入点声明包含了两部分: 一个签名(包含一个方法名称和任何方法参数,表示切入点方法签名) 以及切入点表达式决定那些方法执行感兴趣 ..,在@AspectJ风格中,切入点签名通过普通方法定义表达式提供,切入点表达式通过@Pointcut注解指示(作为切入点签名的方法必须是void返回值内容) ..
例如:
```text
@Pointcut("execution(* transfer(..))") // the pointcut expression
private void anyOldTransfer() {} // the pointcut signature
```
完整的Aop切入点语言可以查看Aspectj [编程指南](https://www.eclipse.org/aspectj/doc/released/progguide/index.html) ,扩展[Aspectj 5 Developer's Notebook](https://www.eclipse.org/aspectj/doc/released/adk15notebook/index.html) \
又或是AspectJ相关的书记(such as Eclipse AspectJ)
### 支持切入点符号(AspectJ pointcut designator - PCD)
- execution
    匹配方法执行连接点,这是主要的切入点符号(当和Spring AOP工作的时候) ..
- within
    限制匹配的连接点必须限制在某些类型中(当使用在Spring Aop中时表示某个匹配类型的声明的方法执行),也就是说它限定了连接点声明的类型范围 ...
- this
    限制匹配的连接点(当使用Spring Aop时表示方法的执行)这里的bean 引用(Spring Aop代理, bean 代理对象)必须是给定类型的实例 ..
- target
    限制匹配连接点(Spring Aop表示方法的执行) - 这里的目标对象(被代理的目标对象)是给定类型的实例 ..
- args
    限制匹配连接点(.... 同上) 这里的参数必须是给定类型的实例(运行时参数的类型)
- @target
    限制匹配连接点(...同上),表示可执行对象的类必须有给定类型的注解 ...
- @args
    限制匹配连接点,传递的实际参数的运行时类型必须包含了给定类型的注解(参数所携带的注解) ..
- @within
    限制匹配连接点必须保证在连接点所属的类型上拥有给定的注解(当使用Spring Aop时表示声明给定注解的类型中声明的方法执行) 方法被限定在类上注释了对应的注解(它给定了一个范围) ...
- @annotation
    限制连接点匹配需要连接点的主体(方法上)拥有给定注解 ...(在Spring Aop中连接点是方法执行) ...
- 其他切入点类型
  目前spring 暂时不支持 ..
  The full AspectJ pointcut language supports additional pointcut designators that are not supported in Spring: call, get, set, preinitialization, staticinitialization, initialization, handler, adviceexecution, withincode, cflow, cflowbelow, if, @this, and @withincode. Use of these pointcut designators in pointcut expressions interpreted by Spring AOP results in an IllegalArgumentException being thrown. \
  The set of pointcut designators supported by Spring AOP may be extended in future releases to support more of the AspectJ pointcut designators

- 其次由于Spring Aop仅仅限制 方法执行连接点,对于Aspectj 自己的类型语义,在切入点,this / target 都指向同一个对象,执行方法的对象 .. \
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
  bean PCD 操作在实例级别(构建于Spring bean 名称的概念)而不是仅类型级别(对于基于编织的AOP 是有限的) .. 基于实例的切入点符号是Spring 基于代理框架的特殊能力 ..它提供了与Spring bean工厂的紧密集成(这非常直观,通过名称标识一个特定的bean) ..

#### 合并的切入点表达式
```java
@Pointcut("execution(public * *(..))")
private void anyPublicOperation() {} 

@Pointcut("within(com.xyz.myapp.trading..*)")
private void inTrading() {} 

@Pointcut("anyPublicOperation() && inTrading()")
private void tradingOperation() {} 
```
切入点表达式可以使用 && / || / ! 操作符进行合并, 最好的做法是用更小的命名组件构建更复杂的切入点表达式,当通过名称,正常的java 可见性规则使用(你能够在相同类型中查看到私有的切入点,在类体系中的protected 切入点,以及随处可见的public 切入点,等等)引用切入点 \
可见性并不会影响切入点匹配 ...
#### 共享公共的切入点定义
当与企业应用进行工作时,开发者经常想要引用一个应用的模块以及从各种切面中引入特定的操作集合,我们推荐定义一个CommonPointcuts 切面(捕获了创建切入点表达式)来实现这种目的,例如一个典型的切面如下:
```java
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
你能够引用定义在这些切面中的切入点到任何你需要切入点表达式的地方,例如让服务层存在事务,你可以如下写:
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
有关xml相关的元素在特定的部分进行解释,我们针对于java 配置开发,这个不在详细解释 .. 可查看官网 ..

#### 示例
spring aop 用户想要使用execution 切入点设计符号(大多数情况),表达式格式如下:
```text
 execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern)
                throws-pattern?)
```
可以知道除了ret-type-pattern 以及 name pattern / parameters pattern之外都是可选的 .. 返回类型模式决定了方法的返回值类型必须是什么-来匹配连接点 .. \
`* `是最频繁使用的返回类型模式, 他匹配任何返回类型,一个全限定类型名称仅仅匹配返回给定类型的方法,这个名称模式匹配方法名称,你能够使用* 通配符或者作为名称模式的一部分,如果\
`*`  你指定一个声明的类型模式,包含了尾随的`.` 去加入它到命名模式组件.这个参数模式是稍微复杂一点: () 匹配无参,因此(..) 标识匹配任何数量的参数(0个或者多个),`*` 标识匹配 \
一个任意类型的参数,(*,String)匹配携带了两个参数的方法. 第一个可以是任意类型,第二个必须是String, 查看AspectJ 编程指南相关的 [语言语义](https://www.eclipse.org/aspectj/doc/released/progguide/semantics-pointcuts.html)了解更多 .. \
以下展示了常见的切入点表达式:
- 任何public method的执行
```text
  execution(public * *(..))
```
- 任何以set开头的方法执行
```text
 execution(* set*(..))
```
- 通过AccountService接口定义的任何方法执行
```text
 execution(* com.xyz.service.AccountService.*(..))
```
- 定义在service 包中的任何方法的执行
```text
execution(* com.xyz.service.*.*(..))
```
- 定义在service包或者它的子包中的方法执行
```text
 execution(* com.xyz.service..*.*(..))
```
- 在service包中的任何连接点(仅仅在Spring aop中的方法执行)
```text
within(com.xyz.service.*)
```
- 在service包以及它的子包中的任何连接点(在spring aop中的方法执行)
```text
  within(com.xyz.service..*)
```
- 在任何连接点处,代理实现了AccountService 接口
```text
this(com.xyz.service.AccountService)
```
> this 通常使用在绑定形式中, 查看相关部分了解如何让代理对象在通知体中可用 ...

- 在任何连接点处, 目标对象实现了AccountService 接口
```text
 target(com.xyz.service.AccountService)
```
> target 通常使用在绑定形式中,后面声明通知部分会了解相关信息 ..

- 携带了单个参数并且参数在运行时传递的类型是 Serializable
```text
args(java.io.Serializable)
```
> args通常使用在绑定形式中 ..

注意到切入点在这个示例中不同于`execution(* *(java.io.Serializable))` ,这个参数版本匹配的是如果参数在运行时传递的是 Serializable,并且执行版本匹配(如果方法签名声明了单个Serializable类型的参数).
- 目标对象存在@Transactional 注解的任何连接点
```text
@target(org.springframework.transaction.annotation.Transactional)
```
> @target可以使用在绑定形式中 ..

- 目标对象的声明类型上存在@Transactional注解
```text
@within(org.springframework.transaction.annotation.Transactional)
```
> 可以使用在绑定形式中

- 执行的方法上存在@Transactional 注解的任何连接点
```text
  @annotation(org.springframework.transaction.annotation.Transactional)
```
> @annotation 可以使用在绑定形式中

- 任何携带了单个参数并且运行时类型的参数上传递了@Classified 注解
```text
 @args(com.xyz.security.Classified)
```
> @args可以使用在绑定形式中

- spring bean 名为 tradeService的任何连接点
```text
bean(tradeService)
```
- 在spring bean的名称匹配这样的通配符表达式(*Service)的连接点
```text
 bean(*Service)
```

#### 编写一个好的切入点
在编译期间,AspectJ 处理切入点为了优化匹配性能,检查代码并判断是否每一个连接点匹配(静态或者动态的)一个给定的切入点是一个昂贵的过程. 一个动态匹配意味着匹配不能够完全根据静态分析决定并且 \
将会在代码中放置测试决定是否当代码运行时实际会匹配,当第一个切入点声明的出现,AspectJ 重写它为最佳形式来进行匹配处理. 这意味着什么呢? 从根本上来说,切入点将会使用范式重写(Disjunctive Normal Form) \
并且切入点的组件会进行排序这样首先评估这些更加廉价的组件. 这意味着你不需要担心去理解各种切入点设计器的性能并可能根据任何顺序从切入点声明中提供它们 .. \
然而,AspectJ 仅仅会工作在它知道的东西上(也就是我们需要告知它处理那些东西),为了匹配的最佳性能,你应该思考它们想要实现什么并且在定义中尽可能的减少查询的空间..来匹配,这里存在的设计器本质上划分为三组中之一: \
类型,范围,上下文 ...
- 类型设计器选择特定的连接点种类: execution,get,set,call,handler
- 作用域设计器选择惯性去的连接点分组(可能许多种类): within / withincode
- 上下文设计器匹配(基于上下文): this / target / @annotation

一个好的切入点应该至少包含前两种类型(种类和作用域),你能够包含上下文设计器去基于连接点上下文匹配或者绑定使用在advice中的上下文. 仅仅提供种类设计器或者仅仅上下文设计器能够工作但是影响编织性能(事件和内存开销),由于额外的 \
处理以及分析,作用域设计器能够很快进行匹配,并且使用它们意味着AspectJ 能够非常快的消除后续不应该处理的连接点分组 .. 一个好的切入点应该总是包含一个作用域设计器..


### 5.4.4 声明通知
通知上可以应用一个命名切入点或者切入点表达式 ..
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

    @Before("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doAccessCheck() {
        // ...
    }
}

```
如果你使用一个适当的切入点表达式
```java
@Aspect
public class BeforeExample {

    @Before("execution(* com.xyz.myapp.dao.*.*(..))")
    public void doAccessCheck() {
        // ...
    }
}
```

## 后置返回通知
它标识一个方法正常返回时所执行,通过@AfterReturning 进行声明
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
同一个切面可以存在多个通知声明 .., 某些时候,你需要在通知方法中访问实际返回的值,你可以根据@AfterReturning 的形式绑定需要访问的返回值,例如:
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;

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
returning属性必须对应通知方法中的参数名, 当方法执行返回,这个返回值将会传递到通知方法中(作为相应的参数值). 一个returning 断言也可以限制匹配仅仅返回对应类型的返回值的方法执行才拦截(在这个情况中,Object,它匹配任何返回值)  .. \
请注意此通知无法返回完全不同的类型引用 ..(所以它的返回类型是void) ..
#### 后置异常通知
同上,可以访问异常信息
```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterThrowing;

@Aspect
public class AfterThrowingExample {

    @AfterThrowing("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doRecoveryActions() {
        // ...
    }
}


```
通过throwing属性进行限制匹配,如果可以Throwable也可以作为异常类型(去绑定抛出的异常到一个通知方法的参数)
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
这个throwing属性必须匹配通知方法中的参数名称, 当方法执行失败并抛出异常,这个异常将会传递给通知方法的相应参数中 .. 同样throwing可以限制匹配的异常类型,例如DataAccessException ...
> 注意到 @AfterThrowing 并不一个通用的异常处理回调, 它仅仅假设接收来自连接点(用户声明的目标方法)的异常(而不是来自@After / @AfterReturning方法的异常)

#### after(finally) 通知
不管有没有异常,只要匹配连接点都会执行,通常可以用它释放资源以及类似的目的 ...
```java
@Aspect
public class AfterFinallyExample {

    @After("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doReleaseLock() {
        // ...
    }
}
```
类似于try-catch语句中的finally 块 ...
#### 环绕通知
如果需要在线程安全的方式在方法执行的前后共享状态,例如,开启或者停止一个timer ...,它可以实现其他前面所有提到的通知 ... \
需要@Around注解,并且这个方法应该声明Object返回类型,并且方法的第一个参数必须是ProceedingJoinPoint, 在通知方法中的方法体中,你必须执行ProceedingJoinPoint方法的proceed() \
通过这个方法执行底层的方法 ...它可以接收Object[]类型的参数数组,这个数组中的值将传递给底层的方法 ..
> proceed的行为,当通过Object[]调用时和由AspectJ 编译器编译的环绕通知的proceed方法的行为有不同,对于使用传统Aspectj语言编写的环绕通知,传递给proceed方法的数量必须匹配传递给环绕通知的参数数量,\
不一定是底层连接带你的参数数量,并且在给定的参数位置上传递给Proceed的值会取代该值所绑定的实体在连接点上的原始值。也就是参数数组可以被替换(如果目前没有意义,则没什么好担忧的)
Spring采用的方式更简单并且更好去匹配基于代理,仅执行的语义,你仅仅需要感知这个不同(如果对Spring编译@Aspectj切面并且通过AspectJ 编译器和编制器使用带有参数的proceed,这能编写一种能够完全兼容Spring Aop 以及 AspectJ的切面)

同样环绕通知可以替代连接点方法的返回值 ..
> 如果环绕通知声明了返回类型为void, 总是为调用者返回null, 实际上忽略了proceed调用的结果,这就是为什么声明Object的类型,这个通知方法应该通常返回从proceed方法的执行中返回的值,即使底层的方法没有返回值,这个通知可选的返回一个缓存
值,一个包装的值或者依赖于使用情况的其他值 ..

```java
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
#### 通知参数
spring 提供了完整类型的通知,意味着你能够声明参数到通知签名给上(例如后置返回通知,异常通知),而不是通过Object[] 数组进行工作,我们能够了解如何利用一个参数以及其他的上下文值在通知体中使用,首先我们先查看如何写一个通用的顾问(能发现通知当前正在拦截的目标方法)
#### 访问 当前的连接点
任何通知方法都可以声明,并且是第一个参数,类型为 org.aspectj.lang.JoinPoint, 注意到环绕通知是需要声明ProceedingJoinPoint作为第一个参数, 它是JoinPoint的子类: \
这个接口提供了大量的有用的方法:
- getArgs(): 返回方法的参数
- getThis(): 返回代理对象
- getTarget(): 返回目标对象
- getSignature(): 返回被通知的方法描述
- toString(): 打印被通知方法的有用的信息

#### 传递参数到通知
我们已经学会了如何绑定一个返回值或者异常值,为了让参数值在通知体中可用,你能够使用args的绑定形式,如果你使用参数名替代了类型名(在args 表达式中),那么这个相关参数的值将会传递到通知作为参数值,一个示例可能能够解释的更加清楚: \
假设你想要通知DAO 操作的执行(假设目标方法的第一个参数是Account,你能够访问account - 在通知体中)
```java
@Before("com.xyz.myapp.CommonPointcuts.dataAccessOperation() && args(account,..)")
public void validateAccount(Account account) {
    // ...
}
```
这个切入点表达式的`args(account,..)` 部分提供了两个目的,第一,限制匹配仅仅符合条件的方法(携带了至少一个参数的方法),并且这个参数将传递一个Account的实例,第二,它让实际的Account对象能够被advice通过 account参数使用 .. \
另一种方式是声明一个切入点(提供Account对象值)- 当它匹配一个连接点时,并且在通知上引用这个命名的切入点,例如:
```java
@Pointcut("com.xyz.myapp.CommonPointcuts.dataAccessOperation() && args(account,..)")
private void accountDataAccessOperation(Account account) {}

@Before("accountDataAccessOperation(account)")
public void validateAccount(Account account) {
    // ...
}
```
查看 AspectJ 编程指南了解更多 ..,这个代理对象(this),目标(target)以及注解(@within,@target,@annotation,@args) 能够以类似的方式进行绑定,下面的实例展示如何匹配注释了@Auditable注解的方法执行并抓取audit 代码:
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    AuditCode value();
}



@Before("com.xyz.lib.Pointcuts.anyPublicMethod() && @annotation(auditable)")
public void audit(Auditable auditable) {
    AuditCode code = auditable.value();
    // ...
}

```

#### advice 参数和泛型
Spring aop 能够处理使用在类声明和方法参数上的泛型,假设你有如下泛型:
```java
public interface Sample<T> {
    void sampleGenericMethod(T param);
    void sampleGenericCollectionMethod(Collection<T> param);
}


```
你能够限制拦截的方法类型为某些参数类型(通过填入通知参数到要拦截的方法的参数类型)
```java
@Before("execution(* ..Sample+.sampleGenericMethod(*)) && args(param)")
public void beforeSampleMethod(MyType param) {
    // Advice implementation
}
```
也就是通过通知方法上的参数类型反推 目标实例方法是否支持 .., 这种方式对泛型集合不起作用,因此你不能定义如下切入点:
```java
@Before("execution(* ..Sample+.sampleGenericCollectionMethod(*)) && args(param)")
public void beforeSampleMethod(Collection<MyType> param) {
    // Advice implementation
}
```
为了让它工作,需要检测集合中的每一个元素,这是不合理的,因为我们不知道如何处理null值,为了实现类似于这样的事情,你可以设置参数类型为Collection<?> 并手动的检测元素的类型 ..

#### 判断参数名称
绑定到通知执行的参数依赖于切入点表达式中的名称 去匹配到在通知和切入点方法签名中的参数名称, 通过java反射参数名不可用,因此spring AOP 使用以下策略判断参数名称:
- 如果参数名已经显式的被用户指定,那么指定的参数名将会被使用,同时通知以及切入点注解有一个可选的argNames属性你能够指定注释方法的参数名称,那么这些参数名将在运行时可用,以下展示了如何使用argNames属性:
```java
@Before(value="com.xyz.lib.Pointcuts.anyPublicMethod() && target(bean) && @annotation(auditable)",
        argNames="bean,auditable")
public void audit(Object bean, Auditable auditable) {
    AuditCode code = auditable.value();
    // ... use code and bean
}
```
如果第一个参数时 JoinPoint / ProceedingJoinPoint 或者 Joinpoint.StaticPart类型,你能够从argNames属性中移除它们的名称,例如如果你修改前面的通知去接收一个连接点对象,那么argNames属性不需要包含它的名称:
```java
@Before(value="com.xyz.lib.Pointcuts.anyPublicMethod() && target(bean) && @annotation(auditable)",
        argNames="bean,auditable")
public void audit(JoinPoint jp, Object bean, Auditable auditable) {
    AuditCode code = auditable.value();
    // ... use code, bean, and jp
}

```
第一个参数的特殊处理非常方便(对于不需要收集任何其他连接点上下文的通知实例),在这种情况下,你可以省略argNames 属性
```java
@Before("com.xyz.lib.Pointcuts.anyPublicMethod()")
public void audit(JoinPoint jp) {
    // ... use jp
}


```
由于argNames属性使用很笨拙,可以不指定它,spring aop查看这个类的debug信息并尝试从本地变量表中判断参数名称... 这个信息将会存在,只要类使用debug信息进行编译(-g:vars 作为最小因素) \
使用这个标志进行编译因此: 1. 你的代码很容易理解(逆向工程),2. 文件尺寸稍微大一点(通常不重要),3. 编译器移除未使用的本地变量的优化,也就是说使用这个标志编译更好 .
> 如果@AspectJ 切面已经通过Aspectj 编译器(ajc)编译且没有debug信息,你不需要增加argNames属性,因为编译器(ajc)包含了需要的信息 ...

如果代码编译且没有必要的debug信息,Spring aop将尝试推断绑定变量对到参数(例如,如果仅仅在切入点表达式中只有一个变量,并且通知方法仅仅需要一个参数,配对很明显),如果变量的绑定是歧义的(根据给定的信息),那么 \
AmbiguousBidingException 将会抛出 .. \
如果上述的所有策略失败,则IllegalArgumentException 抛出 ..
#### 包含参数的Proceeding
前面已经谈论了如何编写一个调用参数的proceed(能够一致的跨spring aop 以及Aspectj),这个解决方案确保通知签名需要有序的绑定每一个方法参数,以下实例展示了怎么做:
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
#### 通知顺序
多个通知同时在一个连接点上运行,spring aop 使用和Aspectj相同的优先级规则决定通知的执行顺序,高优先级的通知首先执行,从连接点退出时,高优先级的通知则越晚执行 ...
(也就是before 通知,优先级越高,越先执行,后置通知,优先级越高越后执行) .. \
当定义在不同切面上的两个通知运行在相同连接点, 除非你有特殊指定,否则执行的顺序是不固定的, 你能够指定顺序(通过@Order注解或者 org.springframework.core.Ordered接口到切面类上实现),这样 \
切面之间的顺序就有了规定(那么order值越低优先级越高)
> 由于通知是直接应用到连接点上,那么后置异常通知不可能接收来自后置通知或者后置返回通知的异常 ...
从spring5.2.7开始,定义在相同的@Aspect类上的通知方法需要运行在相同连接点上的通知根据它们的顺序有序执行 ...(并且根据通知类型给出了优先级顺序,由高到低: @Around,@Before,@After,@AfterReturning \
,@AfterThrowing),注意到@After通知方法将实际上执行在相同切面下的任何@AfterReturning /@AfterThrowing通知方法(服从@After的 finally 语义) ...
当相同类型的通知出现在相同切面并且运行在相同连接点,顺序是未定义的,因为这里无法通过反射抓取javac编译类中的源代码声明顺序. 考虑折叠这些通知方法到一个通知方法 或者重构它们到不同的@Aspect切面并在切面级别上进行@Order 或者Ordered接口顺序定义 ..

### 5.4.5 引入
引入(在AspectJ中称为中间类型声明)启用一个切面去声明被通知的对象实现给定的接口,在这些对象背后-或者说代表这些对象(并且提供了接口的实现) .. \
你能够通过使用@DeclareParents 注解制造一个引入,这个注解被用来声明匹配的类型存在新的父母(由此得名),例如,给定一个指定名称的接口UsageTracked以及 \
此接口的默认实现命名为DefaultUsageTracked,以下的切面声明了服务接口的所有实现(同样也实现了 UsageTracked 接口)-例如,通过JMX进行统计 ..
```java
@Aspect
public class UsageTracking {

    @DeclareParents(value="com.xzy.myapp.service.*+", defaultImpl=DefaultUsageTracked.class)
    public static UsageTracked mixin;

    @Before("com.xyz.myapp.CommonPointcuts.businessService() && this(usageTracked)")
    public void recordUsage(UsageTracked usageTracked) {
        usageTracked.incrementUseCount();
    }

}
```
这个接口能够被实现是通过注解字段的类型决定的, 这个注解的属性值是一个AspectJ 类型表达式, 任何一个匹配类型的bean 将实现UsageTracked接口,注意: \
在之前的实例中的前置通知中,服务bean 能够直接的作为UsageTracked 接口的实现进行使用,如果编程式访问一个bean,你能够写如下代码：
```java
UsageTracked usageTracked = (UsageTracked) context.getBean("myService");
```
很明显,这个注解必然要和切面中属性field上才能够正常解析 ...,它不能使用在其他地方 ...

### 5.4.6 切面实例化模型
默认情况在ioc中每一个切面都存在一个实例,AspectJ 称此为单例实例化模型,它也可以为这些切面定义额外的生命周期,spring 支持AspectJ的perthis / pertarget实例化模型; \
percflow,percflowbelow,pertypewithin当前不支持 ... \
你能够通过在@Aspect注解中指定一个perthis 断言去声明一个perthis切面,考虑如下实例:
```java
@Aspect("perthis(com.xyz.myapp.CommonPointcuts.businessService())")
public class MyAspect {

    private int someState;

    @Before("com.xyz.myapp.CommonPointcuts.businessService()")
    public void recordServiceUsage() {
        // ...
    }
}
```
在前面的示例中,这个perthis断言的作用是为执行业务服务的每一个独一无二的服务对象创建一个切面示例(每一个对象将绑定this到(由切入点表达式匹配的连接点处)),这个切面示例将在服务对象上的一个方法 \
第一次执行时创建,这个切面将失效如果离开了服务对象的范围.  在切面示例创建之前,它里面的通知不可能运行.. 一旦切面实例创建,通知才会匹配连接点运行,但是仅仅切面和服务实例是一对一关联 ,查看AspectJ 编程\
指南了解per 断言的更多信息 .. \
这个pertarget 实例模型工作形式类似于perthis, 但是它为每一个独一无二的目标对象(匹配连接点的)创建切面实例 ...

### 5.4.7 aop 示例
注意到所有组成部分如何工作,下面完成一个例子: \
假设业务执行某事可能由于并发问题失败(例如死锁),如果操作触发,它可能成功进行一下次尝试,对于业务服务它可以重试(不需要返回给用户以解决冲突的幂等性操作),我们想要透明的重试操作去避免客户端 \
看见PessimisticLockingFailureException, 这需要干净的横切在服务层中的多个服务,因此通过切面实现是最好的:
由于我们需要重试,所以可能会调用proceed方法多次,实现如下:
```java
@Aspect
public class ConcurrentOperationExecutor implements Ordered {

    private static final int DEFAULT_MAX_RETRIES = 2;

    private int maxRetries = DEFAULT_MAX_RETRIES;
    private int order = 1;

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Around("com.xyz.myapp.CommonPointcuts.businessService()")
    public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
        int numAttempts = 0;
        PessimisticLockingFailureException lockFailureException;
        do {
            numAttempts++;
            try {
                return pjp.proceed();
            }
            catch(PessimisticLockingFailureException ex) {
                lockFailureException = ex;
            }
        } while(numAttempts <= this.maxRetries);
        throw lockFailureException;
    }
}


```
注意到实现Ordered接口我们能够设置切面的优先级相比于事务通知能够优先执行(每次重试需要一个新的事务),这个主要的动作发生在doConcurrentOperation 环绕通知,注意到,目前我们应用了重试逻辑到 \
businessService(),我们重试执行(proceed()),如果失败则会存在 PessimisticLockingFailureException, 然后重试,直到消耗了所有的重试尝试 ..
```xml
<aop:aspectj-autoproxy/>

<bean id="concurrentOperationExecutor" class="com.xyz.myapp.service.impl.ConcurrentOperationExecutor">
    <property name="maxRetries" value="3"/>
    <property name="order" value="100"/>
</bean>
```
相关的配置如上,为了重定义切面仅仅尝试幂等操作,我们引入一个注解:
```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    // marker annotation
}
```
我们能够使用这个注解去注释服务操作的实现,改变切面仅仅重试仅幂等操作(通过这个重定义切入点表达式的切面)
```java
@Around("com.xyz.myapp.CommonPointcuts.businessService() && " +
        "@annotation(com.xyz.myapp.service.Idempotent)")
public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
    // ...
}
```

## 5.6 aop 声明风格决定
### 5.6.1 spring aop 或者full aspectj
一切以简单为主,如果仅仅只需要在spring bean上进行执行操作的通知,spring aop 足够,如果通知的对象不是由spring容器管理(例如领域对象)，可以使用AspectJ ...  \
当使用AspectJ ,我们也可以选择AspectJ 语言语法(代码风格)或者@AspectJ 注解风格,也就是如果是java 5下，只能使用代码风格,如果切面扮演了设计的主要角色,我们可以使用 \
Eclipse的  AspectJ Development Tools (AJDT)插件,其他ide(或者不是主要角色的应用,可以直接使用@Aspectj注解风格),坚持使用普通java编译,并在构建脚本中增加切面编织阶段 ..

### 5.6.2 @Aspectj 或者xml (为spring aop 选择)
xml有优势也是缺点,优势是能够清晰的看出切面所包含的相关内容,但是缺点就是没有形成一个模块,比较分散,两种根据开发者喜好选择 ..

## 5.10 spring 结合AspectJ
## 5.10.1 切面通过spring 进行依赖注入

### 5.10.2 其他切面

### 5.10.3 通过使用ioc配置AspectJ 切面

### 5.10.4 在spring框架中使用Aspectj加载时编织

#### Aspects
在LTW中使用的切面也能够是AspectJ 切面, 你能够通过AspectJ 语言自身来编写它们,或者通过@AspectJ注解风格的方式编写切面,你的切面同时有效的AspectJ 以及 Spring Aop 切面 .. \
因此编译的切面类需要能够在类路径上 ..(那么同时都能够进行识别, 任选一种进行处理)

#### META-INF/aop.xml
切面的LTW基础设施是通过一个或者多个 在类路径上的META-INF/aop.xml文件进行配置(要么直接或者更常见的方式,jar文件中) ... \
这个文件的结构或者内容详细参考请见 [Aspect 参考文档](https://www.eclipse.org/aspectj/doc/released/devguide/ltw-configuration.html) . \
因为aop 文件是百分百的 Aspectj,所以需要了解AspectJ ..

#### 需要的库(jars)
最小化配置,需要以下库进行Spring框架支持 AspectJ LTW ..
- spring-aop.jar
- aspectjweaver.jar

如果你使用spring提供的代理去启用指令,需要
- spring-instrument.jar

#### spring 配置
spring对LTW的支持在于LoadTimeWeaver(接口),以及各种Spring发布的实现,一个LoadTimeWeaver 在运行时负责加载一个或者多个的java.lang.instrument.ClassFileTransformers \
到ClassLoader,这为各种有趣的应用程序打开了大门，其中一种就是LTW对切面的处理 ...
> 有关运行时类文件转换,可以查看 java.lang.instrument包的 javadoc ..(虽然文档不详细)

为特定的应用上下文配置一个LoadTimeWeaver 能够非常容易(注意到你几乎是使用ApplicationContext作为你的spring 容器),一个BeanFactory是不足够的,因为LTW支持是通过后置处理器完成的 \
而后置处理器是应用上下文对BeanFactory的扩展 .. \
为了启用Spring框架的LTW支持,你需要配置一个LoadTimeWeaver,这通常能够使用@EnableLoadTimeWeaving注解完成 ..
```java
@Configuration
@EnableLoadTimeWeaving
public class AppConfig {
}
```
除此之外,也可以使用xml ..
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:load-time-weaver/>

</beans>

```
前面的配置自动的定义和注册了大量的LTW相关的基础设施bean,例如LoadTimeWeaver 以及 AspectJWeavingEnabler,默认的LoadTimeWeaver是一个 DefaultContextLoadTimeWeaver  \
这会尝试装饰一个自动检测的 LoadTimeWeaver,LTW的实际类型是取决于运行环境的,例如各种LoadTimeWeaver实现:\

| 运行时环境 | LTW 实现 |
| ---- | ---- |
| tomcat | TomcatLoadTimeWeaver |
| glassFish(限制EAR 部署) | GlassFishLoadTimeWeaver |
| red hat的 JBoss AS / wildFly | JBossLoadTimeWeaver |
| ibm的webSphere | WebSphereLoadTimeWeaver |
| oracle的WebLogic | WebLogicLoadTimeWeaver |
| 通过 spring的InstrumentationSavingAgent启动的jvm,(java -javaagent:path/to/spring-instrument.jar) | InstrumentationLoadTimeWeaver |
| 降级,期待底层的类加载符合通用约定(具有addTransformer方法以及可选的 getThrowawayClassLoader方法) | ReflectiveLoadTimeWeaver |

注意到仅仅LoadTimeWeaver 是自动检测的(如果使用DefaultContextLoadTimeWeaver),你能够指定你需要使用的LoadTimeWeaver 实现. \
为了通过java配置指定一个LoadTimeWeaver,实现 LoadTimeWeavingConfigurer 接口并覆盖 getLoadTimeWeaver()方法,以下的示例指定了ReflectiveLoadTimeWeaver ...
```java
@Configuration
@EnableLoadTimeWeaving
public class AppConfig implements LoadTimeWeavingConfigurer {

    @Override
    public LoadTimeWeaver getLoadTimeWeaver() {
        return new ReflectiveLoadTimeWeaver();
    }
}


```
或者xml方式
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:load-time-weaver
            weaver-class="org.springframework.instrument.classloading.ReflectiveLoadTimeWeaver"/>

</beans>

```
spring 容器可以抓取到定义并注册的LTW,并包含一个已知的名称,记住LTW只是作为Spring的LWT基础设施去增加多个ClassFileTransformers ..  \
执行 LTW 的实际 ClassFileTransformer 是 ClassPreProcessorAgentAdapter（来自 org.aspectj.weaver.loadtime 包）类。 \
查看这个类的解释了解更多,因为如何实际有效的编织取决于代码中如何写的 .. \
最后还有一个属性需要讨论,aspectj-weaving属性(或者aspectjWeaving),控制LTW是否启动,它接收三个值,默认是自动检测(如果没有配置属性设置) \

| 注释名称 | xml 值 | 解释|
|--- |---| ---|
|enabled | on | 启用AspectJ 编织,在加载时作合适的切面编织 |
|disabled | off | ltw 关闭,没有切面将在加载时进行编织 |
| autodetect | autodetect | 通过判断是否存在META-INF/aop.xml文件来决定是否开启,这是默认值|

#### 环境特定的配置
如果我们想要在应用服务器或者web容器中启用ltw应该怎么做 ..
##### tomcat / jboss / webSphere / WebLogic
这些服务器都提供了一个常用的app ClassLoader能够具备本地处理指令,Spring的原生LTW能够利用这些类加载器实现提供AspectJ 编织,你能够简单的启用加载时编织 \
你不需要修改jvm 启动脚本(例如增加: -javaagent:path/to/spring-instrument.jar) \
注意到在JBoss的情况下,你也需要禁用app 服务器扫描阻止它在应用实际启动之前加载类,解决方法就是增加一个文件到工件中(WEB-INF/jboss-scanning.xml) \
包含以下内容:
```xml
<scanning xmlns="urn:jboss:scanning:1.0"/>
```

#### 通用的java 应用
那么就只有用 jvm agent,spring提供了 InstrumentationLoadTimeWeaver 需要spring 特定的jvm agent,spring-instrument.jar,自动通过@EnableLoadTimeWeaving 或者 \
xml配置的设置进行检测 .. \
使用方式通过以下jvm选项进行启用:
```text
-javaagent:/path/to/spring-instrument.jar
```
这需要修改jvm启动脚本,对于在应用服务器环境中使用很麻烦(依赖于你的服务器和你的操作策略),也就是说一个jvm一个应用的部署(例如单机spring boot应用),你通常能够完整的控制jvm 配置 ..
### 5.11 更多信息
More information on AspectJ can be found on the AspectJ website.

Eclipse AspectJ by Adrian Colyer et. al. (Addison-Wesley, 2005) provides a comprehensive introduction and reference for the AspectJ language.

AspectJ in Action, Second Edition by Ramnivas Laddad (Manning, 2009) comes highly recommended. The focus of the book is on AspectJ, but a lot of general AOP themes are explored (in some depth).