# 关于bean的一些小东西
对spirig来说，首先是bean的定义,包括很多东西。有类名,构造器参数，懒加载,属性,作用域。还有别名。
还有这个自动装配模式以及这个摧毁回调方法以及初始化方法。

其三是，我们在容器外部注入bean的时候呢,可以通过那个applicationcontext这个类的实例进行bean注册，
    那他能够获取一个bean工厂(DefaultListableBeanFactory)，然后bean工厂不能够并发的进行这个bean的注册或者说bean定义的注册。
    
Defaultlistablefactory，他能够有两个方法，第一个是registersingleton。还有一个是registerbeandefinition。
    
第二就是bean的注入时机，一般如果是通过spring扫描的，组件的话不需要我们去干预，
    但是如果在容器外我们需要自己注入bean的话来需要更早的将bean注入到容器中，因为某些原因,需要进行bean自动装配和这个aop处理。
    
并且在某种程度上支持覆盖存在的元数据以及存在的单例，可以在运行时进行注册（切记不能够并发访问工厂）【出现并发访问错误和不一致状态】
## bean 名称
 1) bean的名称、id都能够标识一个bean对象,对于需要引用的对象来说，给定一个标识是非常重要的,id是主要标识,而name是附属标识,一般来说,spring会从类路径加载组件,对未命名的组件进行命名,(命名规则为首字母小写),但是在特殊情况下(当第一个和第二个字母都是大写的时候,将保留此状态),这个规则与java.bean.Introspector.decapitalize 差不多;
 2) bean 定义之外的别名
    使用bean别名能够使得依赖引用变得更加简单,通常情况下,指定id,和全部的name并不可能完全足够,因为在一个大型系统中有可能配置分离在每个子系统中，它需要别名定义(alias,它能够描述在别处),每个子系统有自己的bean 定义(definition)集合,那么这种情况下可能存在冲突,所以通过别名解决这种问题:
    ```txt
    <alias  name="fromName" alias="toName"/>
    ```
    可能还无法理解,比如A子系统或许想要引用一个名称为subsystemA-datasource,对于b系统来说可能想引用subsystemB-datasource,当使用这些子系统组成一个应用系统的时候，主系统引用myApp-datasource引用数据库,那么这时有三个引用关系,你能够给这写配置元数据配置别名!
 ```xml
 <beans>
    <alias name="myApp-dataSource"                  alias="subsystemA-dataSource"/>
    <alias name="myApp-dataSource" alias="subsystemB-dataSource"/>
</beans>
 ```
现在每一个组件都能够正确的被引用，不会被其他定义所冲突（有效的创建一个命名空间），但是它们引用的同样是一个bean!
@Bean注解也能够提供别名!
## bean 实例化
### 实例化bean
一个bean实例化本质上可以接受一个或者多个对象,简单来说当使用bean definition[内部封装了配置元数据]实际创建一个对象的时候，会查询相关的name的bean(查询并使用),如果不存在bean，那么使用bean definition 构建!
   如果你使用xml形式,那么class属性是必须的(在bean元素上),除此之外,可以通过工厂方法以及bean definition继承进行实例化;
   class 属性的两种使用方式: <br/>
   a) 能够直接通过构造器方式反射创建一个bean实例的时候,尤为重要!
   b) 能够指定从静态工厂创建返回的bean 的类型,少数情况下可以是静态工厂方法标识的返回的相同类型，亦可以是完全不同的类型! <br/>
   Note: <br/>
   内嵌的类名 如果想要在内嵌的类上配置元数据，那么可以使用类的资源名或者二进制名(就是主类名加上$加上内嵌类名)：
   举个例子: <br/>
   如果存在这样一个形式:
   ```java
   public class something{
    
       public static class otherthing{
                
        }
   }
   ```
那么类属性名可以设置为something.otherthing 或者 something<b>$</b>otherthing!
#### 构造器创建实例
默认来说，你需要一个默认的构造器方法已经足够!
大多数用户来说,一个标准的javaBean只需要一个默认构造方法以及合适的getter和setter即可!
   
Note: 如果需要在构造器上使用参数或者对象构造之后进行属性设置,可以参考依赖注入!

#### 工厂方法创建实例
##### 静态工厂方法创建实例
标准格式是,以xml来说,需要class 指定包含工厂方法的类名,factory-method指定工厂方式, 此时你能够调用此方法(通过可选参数,将在后面描述)并返回一个实际的对象,如果已经通过此构造器创建了对象,那么后续将会被信任,这种bean definition仅仅是在遗留代码中调用静态工厂方法!
 Note: 如果class 属性没有设置,那么工厂方法必须是一个静态工厂方式
```java
 public class ClientService {
    private static ClientService clientService = new ClientService();
    private ClientService() {}

    public static ClientService createInstance() {
        return clientService;
    }
}
```
大概形式如上,对于使用可选参数到工厂方法上并且在对象实例化之后(从工厂方法返回之后)设置实例属性,查看依赖以及[配置详细](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-properties-detailed)
##### 实例化工厂创建实例
 和静态工厂方法使用是相似的,class 属性可以为空,但是使用factory-bean 来指定包含实例工厂方法的对象, factory-method指定实例工厂方法!
```java
    public class DefaultServiceLocator {

    private static ClientService clientService = new ClientServiceImpl();

    public ClientService createClientServiceInstance() {
        return clientService;
    }
}
```
比如 xml定义元数据形式如下:
```xml
<beans>
    <bean id="serviceLocator" class="examples.DefaultServiceLocator">
        <!-- inject any dependencies required by this locator bean -->
    </bean>

    <bean id="clientService"
          factory-bean="serviceLocator"
          factory-method="createClientServiceInstance"/>

    <bean id="accountService"
          factory-bean="serviceLocator"
          factory-method="createAccountServiceInstance"/>
</beans>
```
可以注意到上述的工厂方法存在多个!
对应的java编码格式:
```java
public class DefaultServiceLocator {

    private static ClientService clientService = new ClientServiceImpl();

    private static AccountService accountService = new AccountServiceImpl();

    public ClientService createClientServiceInstance() {
        return clientService;
    }

    public AccountService createAccountServiceInstance() {
        return accountService;
    }
}
```
同样的这种方式能够通过依赖注入管理和配置,详情查看[详细依赖和配置](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-properties-detailed)
Note: 在Spring文档中，factory bean指示是一个特殊的Bean,这个bean 定义已经配置在Spring容器中并且通过一个实例或者静态工厂方法创建(都是工厂方法),作为对比,FactoryBean(注意大写),将引用一个spring指定的FactoryBean实现类(两者是不一样的)!
##### 检测bean的运行时类型
确定特定bean的运行时类型并非易事。 Bean元数据定义中的指定类只是初始类引用，可能与声明的工厂方法结合使用，或者是FactoryBean类，这可能导致Bean的运行时类型不同，或者在实例的情况下完全不进行设置级别的工厂方法（通过指定的factory-bean名称解析）。此外，AOP代理可以使用基于接口的代理包装bean实例，而目标Bean的实际类型（仅是其实现的接口）的暴露程度有限。
推荐方式使用BeanFactory.getType(通过指定的bean名称进行调用),通过BeanFactory.getBean(通过相同的bean名)返回指定类型的Bean的类型与前一个方法返回类型一致!


##### bean dependencies
 依赖关系是无法避免的,一个系统必然存在依赖关系,那么依赖注入解决了什么问题?
 并不需要你关心依赖处于什么位置,更容易进行测试,如果是基于接口或者抽象类的情况下,可以直接通过单元测测模拟测试(或者使用本地存根进行测试)
 依赖注入有两种变体,一个构造器注入,一个是属性注入
 ###### 构造器注入
构造器依赖注入能够通过构造器进行依赖注入,它能够执行具有任意参数的构造器,每一个参数就是一个独立的依赖,它的使用和使用静态工厂方法构造Bean行为几乎是等价的，参数的传递也是几乎等价的！
 a) 构造器的参数解析
    当不存在二义性的时候,构造器注入非常简单,例如:
一个例子:
```xml
<beans>
    <bean id="beanOne" class="x.y.ThingOne">
        <constructor-arg ref="beanTwo"/>
        <constructor-arg ref="beanThree"/>
    </bean>

    <bean id="beanTwo" class="x.y.ThingTwo"/>

    <bean id="beanThree" class="x.y.ThingThree"/>
</beans>

```
在这个例子中,Thingone和ThingTwo并没有任何关系,所以使用构造器注入并不需要显式指定参数index,或者参数类型!
对于参数值为基本数据类型的情况下,类型匹配没有任何帮助,其次如果为构造器参数指定了类型,那么将会使用此类型进行类型匹配,例如:
```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg type="int" value="7500000"/>
    <constructor-arg type="java.lang.String" value="42"/>
</bean>
```
由于存在多个基本数据参数的情况下,我们可以选择指定参数下标:
例如:
```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg index="0" value="7500000"/>
    <constructor-arg index="1" value="42"/>
</bean>
```
构造器参数名:
一般情况下,可以通过指定构造器参数名进行依赖注入,但是需要通过debug 标志进行编译代码,spring就能够自动检测参数名,如果不能或者不想启用debug模式,可以使用JDK主机 @ConstructorProperties显式指定构造器参数名!
```xml
package examples;

public class ExampleBean {

    // Fields omitted

    @ConstructorProperties({"years", "ultimateAnswer"})
    public ExampleBean(int years, String ultimateAnswer) {
        this.years = years;
        this.ultimateAnswer = ultimateAnswer;
    }
}
```
2) 基于setter的依赖注入
调用时机是在于容器调用无参构造器或者(无参)静态工厂方法实例化对象之后,通过调用setter方法进行注入!
   它的好处在于可以对一些可选依赖进行注入,否则在任何情况下使用依赖注入都应该进行参数验证！
   ApplicationContext支持它管理的bean的基于构造函数和基于setter的DI。在已经通过构造函数方法注入了某些依赖项之后，它还支持基于setter的DI。您可以以BeanDefinition的形式配置依赖项，将其与PropertyEditor实例结合使用以将属性从一种格式转换为另一种格式。但是，大多数Spring用户并不直接（即以编程方式）使用这些类，而是使用XML bean定义，带注释的组件（即以@ Component，@ Controller等进行注释的类）或@Bean方法来处理这些类(基于Java的@Configuration类),然后将这些源在内部转换为BeanDefinition的实例，并用于加载整个Spring IoC容器实例</br>
并且构造器参数注入可以使用@Required 表示是一个必须的依赖,同时提倡具有程序化验证参数的构造器依赖注入!
 虽然spring提倡构造器注入,但是大量的构造器注入代表着这个bean承担了太多的责任,应将其重构分离关注点来解决问题!
通过[JMX](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#jmx)
   管理bean是一个非常好的setter注入的使用案例。
   setter注入在大多数场景已经足够,但是在比如依赖第三方的类的时候你并没有这个资源,此时setter注入可能不管用了，这是<b>构造器注入可能是唯一一种可用的方式</b>
3) 依赖解析的过程
 a.首先将创建并初始化包含描述bean信息的配置元数据的applicationContext
 b.对于每一个bean来说它可以是属性形式、或者构造器参数形式、或者说是传递给构造器参数的形式(除非它使用了普通构造器),都是有效的,将会在bean实际创建的时候,作为依赖提供给它!
 c.每一个属性或者构造器参数可以是一个实际的bean,亦可以引用其他bean的一个引用!
 d.每一个属性或者构造器参数都可以是从指定类型转换到实际类型的值,亦可以是内建类型,比如boolean ,string等等!

Note:
容器创建的时候将会对每一个bean配置进行验证,这就意味着在bean创建之前它的属性将不会被设置,Beans 都是单例的并且在容器创建的时候都会提前创建好(默认情况),作用域详情见Bean scope,否则只有当bean需要的时候才会被创建,创建bean可能会导致创建一个bean图(什么意思? 我认为可能是一个bean的依赖关系都被创建了,这些bean的联系就是一个bean 图),当这些bean的依赖或者依赖的依赖创建并赋值的时候，这个解析可能会产生错配,这可能对第一次创建的bean来说是有影响的!
4).循环依赖
    使用构造器注入最有可能产生循环依赖,发生循环依赖的时候会抛出 throws a BeanCurrentlyInCreationException,一个解决方式是使用setter去将循环依赖链打破,循环依赖就是鸡和蛋的问题;
通常来说你可以信赖spring正确的做好事情,它会在创建容器的过程中对错误的事物进行报告，这就是为什么容器创建时会提前创建好bean，而不是创建好之后进行验证,当然你可以延迟问题显现的时间,通过懒加载模式实现这样的效果!
 在没有循环依赖的情况下,当一个依赖注入到另一个bean之前将会被完全配置,这就意味着@Bean A完全配置之前将会执行B的配置,另一方面说,如果bean是已经实例化了(而不是提前初始化单例),那么它的依赖同样是设置过了,并且回调过了对应的生命周期方法(例如初始化方法,以及InitializingBean 的初始化回调)
5) xml的setter依赖注入形式
```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <!-- setter injection using the nested ref element -->
    <property name="beanOne">
        <ref bean="anotherExampleBean"/>
    </property>

    <!-- setter injection using the neater ref attribute -->
    <property name="beanTwo" ref="yetAnotherBean"/>
    <property name="integerProperty" value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.AnotherBean"/>
<bean id="yetAnotherBean" class="examples.YetAnotherBean"/>
```
6) 构造器的注入形式就不再多说,说一下静态工厂注入形式
```xml
    <bean id="exampleBean" class="examples.ExampleBean" factory-method="createInstance">
    <constructor-arg ref="anotherExampleBean"/>
    <constructor-arg ref="yetAnotherBean"/>
    <constructor-arg value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.AnotherBean"/>
<bean id="yetAnotherBean" class="examples.YetAnotherBean"/>
```
对应的java class
```java

public class ExampleBean {

    // a private constructor
    private ExampleBean(...) {
        ...
    }

    // a static factory method; the arguments to this method can be
    // considered the dependencies of the bean that is returned,
    // regardless of how those arguments are actually used.
    public static ExampleBean createInstance (
        AnotherBean anotherBean, YetAnotherBean yetAnotherBean, int i) {

        ExampleBean eb = new ExampleBean (...);
        // some other operations...
        return eb;
    }
}
```
使用方式和构造器注入完全一样,非静态工厂方法调用也是类似!
   