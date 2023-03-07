# bean定义继承
bean定义信息包含了大量配置信息,例如bean的类,是否懒加载、自动装配,构造器参数、属性值、初始化方法、静态工厂方法以及等等,于是spring支持bean 定义继承,可以根据需要覆盖某些属性,这属于一种模板形式，并节省了大量输入!<br/>
如何使用:
请注意子bean定义展现形式为ChildBeanDefinition,但是大多数使用者不会使用这个级别的对象,下面有一个使用方式:
```xml
<bean id="inheritedTestBean" abstract="true"
        class="org.springframework.beans.TestBean">
    <property name="name" value="parent"/>
    <property name="age" value="1"/>
</bean>

<bean id="inheritsWithDifferentClass"
        class="org.springframework.beans.DerivedTestBean"
        parent="inheritedTestBean" init-method="initialize">  
    <property name="name" value="override"/>
    <!-- the age property value of 1 will be inherited from parent -->
</bean>
```
子类会继承父bean定义的一切,作用域、构造器参数、属性、方法，同时也可以使用相关的设置进行覆盖;
其余设置始终从子定义中获取：依赖项，自动装配模式，依赖项检查，单例和惰性初始化 abstract 属性必须为true,由于bean定义是抽象的,所以他不能实例化,将它作为引用或者通过getBean获取对象将会报错,它仅仅是存粹用于为子bean definition服务,同时容器内部的preInstantiateSingletons方法将忽略它!<br/>
此方法将预先初始化所有单例,默认实现,这对于单例来说非常重要,因此如果你只是作为一个父定义,那么必须是abstract,否则容器将会创建它!  
