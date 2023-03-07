# bean的性质
spring 提供了大量接口用于自定义bean的性质,例如:
1) 生命周期回调
2) ApplicationContextAware  以及 BeanNameAware
3) 其他Aware 接口

## 生命周期回调
1) 实现InitializingBean 以及DisposableBean 即可对单例bean进行生命周期回调, 前者使用afterPropertiesSet() 后者使用destroy()<br/>
注意: @PostConstruct 或者@PreDestroy能够与上述接口作用相同!
其次还可以使用init-method 以及 destroy-method的实现!<br/>
在框架内部spring使用后置处理器进行处理对应方法,如果需要自定义行为，可以实现BeanPostProcessor,对于更加完整的信息查看[Container Extension Points](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-extension)
2) 除此之外,你也能够实现Lifecycle接口,这样这些对象能够参与启动、关闭的回调事件,并且是通过Spring自己的生命周期进行驱动的！
### Initialization Callbacks
用于bean实例化之后,进行初始化设置,推荐使用注解形式,而不是继承InitializingBean,因为这可能有不必要的耦合!
### 摧毁回调差不多的说法
能够在destroy-method设置一个infer(推断)值,指示spring自动检测公共的public 关闭或者shutdown方法(在某个对象上),对于实现了 java.lang.AutoCloseable或者java.io.Closeable可能会导致匹配,也能够在 beans元素的 default-destroy-method设置一个默认值(这将会应用到所有<bean>上)<br/>
注意的是: 这个初始化回调发生在原始对象上,那么这就意味着此时代理还没有发生在bean上,首先完全创建目标bean，然后应用带有其拦截器链的AOP代理,你当然可以分开定义aop代理和原始对象,并且你可也绕过aop直接和原始对象直接交互,但这会带来奇怪的语义，并且aop和目标对象的生命周期耦合在一起,并且在代理上使用Init是不合适的,所以代理上不应该存在这些回调!
### 合并生命周期接口
从spring2.5开始你就可以有三个选择控制bean生命周期
1) The InitializingBean and DisposableBean callback interfaces
2)  Custom init() and destroy() methods
3) @PostConstruct and @PreDestroy annotations. 
如果存在多个生命周期回调(通过不同名字设置的),那么会根据有顺进行调用,如果存在重名,那么方法只会运行一次!<br/>
比如:
```text
Methods annotated with @PostConstruct

afterPropertiesSet() as defined by the InitializingBean callback interface

A custom configured init() method

Destroy methods are called in the same order:

Methods annotated with @PreDestroy

destroy() as defined by the DisposableBean callback interface

A custom configured destroy() method
```
### 启动以及关闭回调
Lifecycle接口是为bean对象有自己独特的生命周期需求而准备的(例如停止某些后台程序)!
当实现了此接口,ApplicationContext自己接受到了这些信号,它会级联调用所有LifeCycle的实现(定义在当前上下文的所有实现接口的对应方法),整体面向外部来说是通过LifecycleProcessor代理实现的
```java
public interface LifecycleProcessor extends Lifecycle {

    void onRefresh();

    void onClose();
}
```
将在刷新、关闭的时候进行响应,并且是对启动、关闭的一个显式交互，意味着自动启动的时候将不会自动调用!对于需要在一个指定的bean中自动启动auto-startup的更加细腻化的配置(包括启动层,阶段)考虑实现org.springframework.context.SmartLifecycle进行替代!
在摧毁之前stop通知没有得到授权,在普通的摧毁中,所有的Lifecycle 对象在通用的摧毁回调被传播之前会接受到一个停止通知,然而在上下文热重载的时候或者停止刷新尝试时,仅仅只有摧毁方法会被回调!<br/>

####  执行startup shutdown order
顺序非常重要,比如depends-on 关系对象,将在依赖产生之后实例化,而在依赖之前摧毁!某些情况下，依赖关系可能变得很笼统,只知道有些依赖在之前处理,   在这样的情况下，SmartLifecycle 提供了其他的选择,在它的父类上定义了一个这样的方法getPhase(),Phased 提供了当前处于什么阶段!<br/>
那么此方法意味着什么,数值越小越先创建越后删除,数值越大越后创建越先死亡,对于默认的实现了LifeCycle的对象来说默认值为0(并没有实现SmartLifeCycle),因此负值将标识在普通对象之前创建,正值则与此相反!<br/>
SmartLifecycle 的stop 方法支持一个回调,可以传入回调，默认是异步处理shutdown(因为默认接口为LifecycleProcessor,实现者DefaultLifecycleProcessor),在每次对特定组的对象执行回调之前都会等待一定的超时时间,可以覆盖并设置超时时间;
```xml
<bean id="lifecycleProcessor" class="org.springframework.context.support.DefaultLifecycleProcessor">
    <!-- timeout value in milliseconds -->
    <property name="timeoutPerShutdownPhase" value="10000"/>
</bean>
```
前面已经提到对于容器刷新或者关闭时定义的那些回调方法同样是一致的,后者会在stop调用之后驱动关闭程序,但是当容器发生刷新的时候触发refresh,另一个方面,SmartLifecycle 的bean有一些其他的特性,容器刷新时，默认的生命周期处理器会处理SmartLifecycle 对象的isAutoStartup()的方法返回的boolean值,如果为true,那么对象将会立即开始，而不是等待上下文的显式执行或者自己调用start方法(不像刷新,对于一个标准的上席文实现来说上下文的start将不会自动发生),Phase的值和 depends-on决定了启动顺序!
### 优雅的在非web应用中关闭!
 通过配置registerShutdownHook,此方法声明在ConfigurableApplicationContext,这将会往jvm中注册一个回调,那么将会优雅的关闭应用!
