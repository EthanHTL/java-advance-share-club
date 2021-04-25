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