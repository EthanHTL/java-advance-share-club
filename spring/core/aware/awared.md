#Aware 感知接口
## ApplicationContextAware
此接口给与了一个你能够获取ApplicationContext的能力,它能够获取文件资源，发布事件，以及MessageSource,但是尽量避免使用这种方式，使用自动装配更好,通过将它作为一个构造器参数或者setter方法进行装配(并通过byType的自动装配模式),同时为了更加灵活可以通过@Autowired进行自动注入!
## BeanNameAware 
此感知将在一个实现了此感知的bean被创建之后,会通过setBeanName方法设置bean的名称!
将在收集了所有属性之后执行,在初始化回调之前执行!
## 其他Aware
更多请查看[aware](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-aware)

