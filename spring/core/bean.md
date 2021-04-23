1.关于bean的一些小东西
```txt
对spirig来说，首先是bean的定义,包括很多东西。有类名,构造器参数，懒加载,属性,作用域。还有别名。
还有这个自动装配模式以及这个摧毁回调方法以及初始化方法。

    其三是，我们在容器外部注入bean的时候呢,可以通过那个applicationcontext这个类的实例进行bean注册，
    那他能够获取一个bean工厂(DefaultListableBeanFactory)，然后bean工厂不能够并发的进行这个bean的注册或者说bean定义的注册。
    
    Defaultlistablefactory，他能够有两个方法，第一个是registersingleton。还有一个是registerbeandefinition。
    
    第二就是bean的注入时机，一般如果是通过spring扫描的，组件的话不需要我们去干预，
    但是如果在容器外我们需要自己注入bean的话来需要更早的将bean注入到容器中，因为某些原因,需要进行bean自动装配和这个aop处理。
    
    并且在某种程度上支持覆盖存在的元数据以及存在的单例，可以在运行时进行注册（切记不能够并发访问工厂）【出现并发访问错误和不一致状态】
```
