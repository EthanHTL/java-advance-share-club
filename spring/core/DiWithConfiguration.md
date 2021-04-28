###隶属于springframework 1.4.2章节
###依赖注入和配置
spring支持对bean元数据定义通过构造器或者property元素引用其他依赖,
<property/> and <constructor-arg/>
例如:
```xml
    <bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <!-- results in a setDriverClassName(String) call -->
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
    <property name="username" value="root"/>
    <property name="password" value="misterkaoli"/>
</bean>
```
#### one 基础数据类型
默认情况下是人类可读的字符串形式,spring将使用 conversion service 将字符串形式数据转换到属性的实际类型或者参数
```xml
<bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <!-- results in a setDriverClassName(String) call -->
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
    <property name="username" value="root"/>
    <property name="password" value="misterkaoli"/>
</bean>
```
可以通过p-namespace对每一个配置进行精简（succinct ）
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource"
        destroy-method="close"
        p:driverClassName="com.mysql.jdbc.Driver"
        p:url="jdbc:mysql://localhost:3306/mydb"
        p:username="root"
        p:password="misterkaoli"/>

</beans>
```
在这种情况下,类型发现是运行时,而不是设计时,那么通常可以通过Idea等IDE开发工具进行提示;
#### 使用Properties
```xml
<bean id="mappings"
    class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">

    <!-- typed as a java.util.Properties -->
    <property name="properties">
        <value>
            jdbc.driver.className=com.mysql.jdbc.Driver
            jdbc.url=jdbc:mysql://localhost:3306/mydb
        </value>
    </property>
</bean>
```
这上面的例子中会使用PropertyEditor进行JavaBean属性设置,将<value>元素的数据转换到properties中,并且这是spring团队钟爱与使用<value>的一些新特性之一;
#### idref
是一种防止标识错误的方式,它指的是bean id的正确写入,而不是依赖,注意仅仅是标识bean的名称,例如:
```xml
<bean id="theTargetBean" class="..."/>

<bean id="theClientBean" class="...">
    <property name="targetName">
        <idref bean="theTargetBean"/>
    </property>
</bean>
```
等价于使用value属性的形式
```xml
<bean id="theTargetBean" class="..." />

<bean id="client" class="...">
    <property name="targetName" value="theTargetBean"/>
</bean>
```
两者的区别是idref会进行验证并抛出错误,而value并不会,所以如果client是一个原型bean,那么可能会导致这个错误在容器运行很久之后才会被发现!<br/>
Note:
在4.0 Bean XSD中不再支持idref元素上的local属性，因为它不再提供常规Bean引用上的值。升级到4.0模式时，将现有的idref本地引用更改为idref bean
一个常见的好处是在spring2.0之前,在ProxyFactoryBean bean定义中使用<idref>进行指定拦截器名称防止拦截器Id拼写错误!
#### 引用其他的bean
可以通过ref元素引用其他依赖,最终都会引用到bean上,作用域、验证依赖于指定的是id还是对象的name(可以通过bean或者parent属性进行设置)
```xml
<ref bean="someBean"/>
```
上述例子中 引用了一个bean,其中bean属性必须和目标bean id相同，或者和目标bean的名称相同(无论是否在同一个xml文件中,都能够在同一个容器或者父容器中配置)!
通过parent属性配置一个依赖(可以是当前容器或者父容器),同样和id，name相同,(目标bean必须在当前容器的一个父容器中),只有当你拥有层级关系的多个容器时并且需想要通过代理将(和parent指向的bean具有相同名称的bean)包装一个存在于父容器中的bean,下面是一个例子:
```xml
<!-- in the parent context -->
<bean id="accountService" class="com.something.SimpleAccountService">
    <!-- insert dependencies as required as here -->
</bean>
        <!-- in the child (descendant) context -->
        <!-- bean name is the same as the parent bean -->
<bean id="accountService"
        class="org.springframework.aop.framework.ProxyFactoryBean">
<property name="target">
<ref parent="accountService"/> <!-- notice how we refer to the parent bean -->
</property>
        <!-- insert other configuration and dependencies as required here -->
        </bean>

```
上面这个例子中包装了一个父容器中存在的相同名称bean!
Note:
在4.0bean xsd规范中不再支持local属性,因为它不会给普通bean引用上提供给任何值,将ref local引用改变为ref bean引用(在升级为4.0 schema方案时)
#### 可以在property定义一个内嵌bean
```xml
<bean id="outer" class="...">
    <!-- instead of using a reference to a target bean, simply define the target bean inline -->
    <property name="target">
        <bean class="com.example.Person"> <!-- this is the inner bean -->
            <property name="name" value="Fiona Apple"/>
            <property name="age" value="25"/>
        </bean>
    </property>
</bean>
```
由于这种情况下bean属于一个匿名内部类所以它不能被依赖、注入,其次在极端情况下,匿名内部类将共享所包含它的bean的作用域,这就导致外部类能够参与内部类的生命周期回调(这不危险吗?)!
#### 集合
list set map props元素都能够被使用来封装集合数据!
<props>是properties,但是和<properties>使用方式不一样
```xml
<bean id="moreComplexObject" class="example.ComplexObject">
    <!-- results in a setAdminEmails(java.util.Properties) call -->
    <property name="adminEmails">
        <props>
            <prop key="administrator">administrator@example.org</prop>
            <prop key="support">support@example.org</prop>
            <prop key="development">development@example.org</prop>
        </props>
    </property>
    <!-- results in a setSomeList(java.util.List) call -->
    <property name="someList">
        <list>
            <value>a list element followed by a reference</value>
            <ref bean="myDataSource" />
        </list>
    </property>
    <!-- results in a setSomeMap(java.util.Map) call -->
    <property name="someMap">
        <map>
            <entry key="an entry" value="just some string"/>
            <entry key ="a ref" value-ref="myDataSource"/>
        </map>
    </property>
    <!-- results in a setSomeSet(java.util.Set) call -->
    <property name="someSet">
        <set>
            <value>just some string</value>
            <ref bean="myDataSource" />
        </set>
    </property>
</bean>
```
对于map的key 或者value,或者set的value,能够是下面以下之一类型
```xml
bean | ref | idref | list | set | map | props | value | null
```
#### 属性合并(集合)
```xml
<beans>
    <bean id="parent" abstract="true" class="example.ComplexObject">
        <property name="adminEmails">
            <props>
                <prop key="administrator">administrator@example.com</prop>
                <prop key="support">support@example.com</prop>
            </props>
        </property>
    </bean>
    <bean id="child" parent="parent">
        <property name="adminEmails">
            <!-- the merge is specified on the child collection definition -->
            <props merge="true">
                <prop key="sales">sales@example.com</prop>
                <prop key="support">support@example.co.uk</prop>
            </props>
        </property>
    </bean>
<beans>
```
上面的例子中运用了Bean 定义继承,同时对父bean definition的属性进行合并!
这样的结果是导致子Bean定义解析被实例化之后,得到的属性是合并之后的!
合并行为是类似的,对于List类型的语义来说,可能比较重要,因为列表存在有序情况,此时parent的值在所有子列表数据之前排列,在其他类型的情况下不存在顺序,所以排序语义对set,Map,properties无效!
#### 集合合并限制
不同集合合并是拒绝的,其次merge属性在子定义中必须定义,在parent的集合定义上使用merge是多余并且无效!
#### 强类型集合
    spring能够通过反射获取类型信息,将(比如一个字符串数据)转换为对应的类型(这是一种优势)
```xml
public class SomeClass {

    private Map<String, Float> accounts;

    public void setAccounts(Map<String, Float> accounts) {
        this.accounts = accounts;
    }
}
<beans>
    <bean id="something" class="x.y.SomeClass">
        <property name="accounts">
            <map>
                <entry key="one" value="9.99"/>
                <entry key="two" value="2.75"/>
                <entry key="six" value="3.99"/>
            </map>
        </property>
    </bean>
</beans>
```

#### 空值
xml中可以使用<null>标签处理空数据,空数据和空字符串被会被spring信任!
#### 再次说p空间
p命名空间并不会是一个xsd定义,所以它能够直接设置属性名,所以你可能会觉得很奇怪,看一个例子:
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean name="john-classic" class="com.example.Person">
        <property name="name" value="John Doe"/>
        <property name="spouse" ref="jane"/>
    </bean>

    <bean name="john-modern"
          class="com.example.Person"
          p:name="John Doe"
          p:spouse-ref="jane"/>

    <bean name="jane" class="com.example.Person">
        <property name="name" value="Jane Doe"/>
    </bean>
</beans>
```
然后可以发现p直接设置了属性名并通过-ref设置属性引用的其他设置!

虽然可以这样用,但是和标准xml语法并不一致,建议不要混合使用三种xml语法格式,这有可能导致意外的情况产生!
#### c namespace
与p相似,但是用于替换构造参数的写法,如下:
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:c="http://www.springframework.org/schema/c"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="beanTwo" class="x.y.ThingTwo"/>
    <bean id="beanThree" class="x.y.ThingThree"/>

    <!-- traditional declaration with optional argument names -->
    <bean id="beanOne" class="x.y.ThingOne">
        <constructor-arg name="thingTwo" ref="beanTwo"/>
        <constructor-arg name="thingThree" ref="beanThree"/>
        <constructor-arg name="email" value="something@somewhere.com"/>
    </bean>

    <!-- c-namespace declaration with argument names -->
    <bean id="beanOne" class="x.y.ThingOne" c:thingTwo-ref="beanTwo"
        c:thingThree-ref="beanThree" c:email="something@somewhere.com"/>

</beans>
```
使用之前需要申明,并且在反编译的情况下,可能不知道参数名,可以通过下列方式进行设置参数:
```xml
<!-- c-namespace index declaration -->
<bean id="beanOne" class="x.y.ThingOne" c:_0-ref="beanTwo" c:_1-ref="beanThree"
    c:_2="something@somewhere.com"/>
```

#### 为合成属性设置值
例如为一个bean的多级层次的属性设置属性可以通过
```xml
a.ss.xx.cc 设置属性,比如:
<bean id="something" class="things.ThingOne">
<property name="fred.bob.sammy" value="123" />
</bean>
```
#### depends-on
依赖的对象后初始化,先摧毁,被依赖的bean先初始化,后摧毁;
#### lazy init
默认情况下,applicationContext将尽可能早的初始化单例,但是可以使用懒加载取消提前加载!
```xml
<bean id="lazy" class="com.something.ExpensiveToCreateBean" lazy-init="true"/>
<bean name="not.lazy" class="com.something.AnotherBean"/>
```
可以在容器级别上设置懒加载
```xml
<beans default-lazy-init="true">
    <!-- no beans will be pre-instantiated... -->
</beans>
```
### 自动装配合作者
自动装配的优点:
本质上来说,是让spring解析这些合作者(通过检测applicationContext的内容)
1) 减少需要指定的构造器参数或者需要指定的属性
2) 随着配置的修改,而不需要改变任何配置,让代码更加稳定,且自动更新配置;
   自动装配存在四种模式:
   如果是xml,可以在bean上开启autowire属性 <br/>
   a. no （默认）无自动装配。 Bean引用必须由ref元素定义。对于大型部署，不建议更改默认设置，因为明确指定协作者可提供更大的控制力和清晰度。在某种程度上，它记录了系统的结构<br/>
   b. byName spring将查找当前属性同名的bean将其自动装配到属性上!<br/>
   c. byType 这种模式下会根据bean的类型进行匹配,但是容器中只能存在一个,否则抛出异常,其次如果没有匹配，什么也不会发生！<br/>
   d.constructor 这种方式类似byType,但是应用在构造器参数上,如果容器中不存在此参数类型的Bean将抛出致命错误!
   在byType或者constructor自动装配的模式下,你能够使用array以及具有类型的集合,在这种情况下,所有满足类型的bean都会自动装配到参数上,对于map也是类似,尤其是强类型Map,这可能导致匹配的bean都会自动装配到map中并且key是bean 名称!