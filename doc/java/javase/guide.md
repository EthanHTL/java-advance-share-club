# javase


## 序列化过滤器
### java 序列化过滤器
此机制将筛查传入的序列化对象流去提高安全以及健壮性 .. 过滤器能够在他们反序列化之前验证传入的类 ...

从JEP-290开始,java的序列化过滤器机制的目标是:
- 提供一种方式去减少能够反序列化的类为一组合适上下文的类 ..
- 提供指标去过滤图形尺寸 以及复杂性(在反序列化期间去验证常见的图形行为)
- 允许 RMI 导出的对象验证调用中预期的类。

你能够以以下方式实现序列化过滤器:
- 基于pattern 过滤器将不需要修改应用, 他们由一组模式组成 - 定义在属性中 / 配置文件中 / 或者命令行中 .. 基于表达式的过滤器能够接受或者拒绝特定的类,
    包或者模块 .. 他们能够限制数组的尺寸,图形深度,总的引用,以及流的长度 .. 一个常见的使用情况是使用一组黑名单类(已知标记为可能会危害java运行时)..
    基于表达式的过滤器能够基于一个应用或者在一个进程中的所有应用进行定义 ..
    自定义的过滤器使用ObjectInputFilter API实现, 他们允许一个应用去集成基于表达式的过滤器的完全控制 .. 因为它们能够为每一个ObjectInputStream 进行指定 ..
    自定义的过滤器可以设置在一个单独的输入流或者一个进程的所有流上 ..

在大多数情况下,一个自定义过滤器应该检查(如果一个进程范围的过滤器设置),如果存在,那么自定义过滤器应该执行它(进程范围的过滤器)并且使用进程范围的过滤器的结果,除非状态是`UNDECIDED`,
表示未声明 ..

对序列化过滤器的支持是从JDK9开始包括的,并且在Java CPU的从8u121,7u131,6u141版本开始 .

#### 白名单和黑名单
白名单和黑名单能够使用基于表达式的过滤器或者自定义过滤器实现 ... 这些列表允许你采用积极地 以及防御性的方式去保护应用 ..

积极的方式使用白名单去接受仅仅被识别以及信任的类 ... 你能够在代码中实现这些白名单(在开发应用的过程中),或者在后面通过定义基于表达式的过滤器 ..

如果你的应用仅仅处理一组稍小集合的类 - 那么这种方式能够很好的工作 .. 你能够通过指定允许的类名 / 包名 / 或者模块名实现白名单 ..

防御性的方式使用黑名单去拒绝不被信任的类 .. 通常黑名单是在一个攻击(显示了/ 暴露了一个类是一个问题的情况下)之后构建的..
一个类能够增加到黑名单中,不需要代码更改,通过定义基于表达式的过滤器 ..

#### 创建基于表达式的过滤器
基于表达式的过滤器 - 能够让你定义而不需要更改你的代码 .. 你能够在属性文件中定义进程范围的过滤器 .. 或者再java命令行中定义应用特定的过滤器 ..

基于表达式的过滤器是一组表达式 .. 每一个表达式根据在流或者有限资源的类的名称进行匹配 ..
基于类以及资源限制的表达式能够合并在一个过滤器字符串中, 使用一个分号分隔即可(每一个表达式) ...

#### 基于表达式的过滤器语法
当你创建一个组合表达式的过滤器,使用以下的指导方针:
- 通过分号分隔模版,例如
```text
pattern1.*;pattern2.*
```
- 空白格很重要并且考虑作为表达式的一部分
- 将限制放在字符串的第一位. 他们将会首先评估,因此先将它们放置能够加强顺序 .. 否则,模式将从左到右评估 ..
- 匹配表达式的类由`!` 前导则表示拒绝 ... 一个匹配没有`!` 的表达式将会被接受 .. 以下的过滤器拒绝了`pattern1.MyClass` 但是接收`pattern2.MyClass` ..
```text
!pattern1.*;pattern2.*
```
- 使用通配符(*) 去批量的指定类
  1. 匹配任何类
  2. 匹配在mypackage中的每一个类,使用mypackage.*
  3. 匹配在mypackage中以及它子类的任何类,使用mypackage.**
  4. 匹配以text开头的每一个类,使用text*

如果一个类不匹配任何过滤器,那么它将是被接受的.. 如果你想要接受一些类,那么你的过滤器必须拒绝一些不匹配的事情 .. 为了拒绝没有被指定的所有类,可以在
过滤器类中包含一个`!*`作为最后一个表达式 ..

有关完整语法的完整说明, 查看 conf/security/java.security文件, 或者[JEP 290](http://openjdk.java.net/jeps/290) ..

##### 限制
基于表达式的过滤器被用来进行简单的接受 或者拒绝 .. 这些过滤器有一些限制 ..
1. 表达式不允许设置基于类的数组的不同尺寸(也就是不能限制一个类集合的尺寸)
2. 表达式不能够基于类的父类或者接口来匹配类 .
3. 表达式没有状态并且不能够依赖于更早在流中反序列化的类来做出决定 ..

##### 为一个应用定义一个基于表达式的过滤器
你能够定义一个基于表达式的过滤器作为一个应用的系统属性 .. 一个系统属性替代了一个Security 属性值 ..
为了创建一个过滤器(仅仅应用于一个应用),并且仅仅为java的单一调用,在命令行中定义`jdk.serialFilter` 系统属性 ..
```shell
java -Djdk.serialFilter=maxarray=100000;maxdepth=20;maxrefs=500 com.example.test.Application
```

##### 为一个进程中的所有应用定义一个基于表达式的过滤器
你能够定义一个基于表达式的过滤器 作为Security 属性,对于一个进程中的所有应用 .. 一个系统属性替代了一个Security 属性值 ..
1. 编辑`java.security` 属性文件
   
    jdk 9以及更高: `$JAVA_HOME/conf/security/java.security` \
    jdk 8,7,6: `$JAVA_HOME/lib/security/java.security`
2. 增加表达式到`jdk.serialFilter` 安全属性中

###### 定义一个类过滤器
我们能够定义一个全局应用的基于表达式的类过滤器 .. 举个例子,模式可以是一个使用了通配符的类名或者包名 ..

在下面的示例中,这个过滤器拒绝了来自一个包的一个类(!example.somepackage.SomeClass), 并且接受这个包中的其他类:
```shell
jdk.serialFilter=!example.somepackage.SomeClass;example.somepackage.*;
```
前面的示例过滤器接受了所有其他的类,不仅仅是example.somepackage.*中的类,为了拒绝所有其他类,增加!*:
```shell
jdk.serialFilter=!example.somepackage.SomeClass;example.somepackage.*;!*
```
###### 定义一个资源限制的过滤器
一个资源过滤器限制了图复杂性和尺寸,你能够创建过滤器 - 对每一个应用使用以下的参数去控制资源使用
- 最大允许的数组值(例如maxarray=100000)
- 最大图的深度,例如(maxdepth=20)
- 在图中的对象引用的最大深度,例如(maxrefs=500)
- 在流中的最大字节数量,例如(maxbytes=500000)

#### 创建自定义过滤器
自定义过滤器可以使用在应用的代码中,他们设置在独立的流中或者一个进程中的所有流 .. 你能够实现一个自定义的过滤器作为表达式/ 方法或者lambda 表达式或者类 ..
##### 读取一个序列化对象的流
你能够在ObjectInputStream上设置自定义的过滤器 .. 或者应用相同的过滤器到每一个流上(设置一个进程范围的过滤器).. 如果一个ObjectInputStream 没有为它定义的过滤器..
如果存在进程范围的过滤器,那么将会被调用..

当流开始解码的时候,以下动作将会发生:
- 在流中的每一个新的对象,那么过滤器将会在对象实例化或者反序列化之前进行调用
- 对于流中的每一个类,这个过滤器会使用解析的类进行调用,它将会对在流中的每一个超类或者接口进行单独调用 ..
- 过滤器能够检查每一个在流中引用的每一个类,包括将要创建对象的类,这些类的超类以及它们的接口..
- 在流的每一个数组,无论他们是基础类型的数组或者字符串数组或者对象数组,此过滤器使用数组类以及数组长度进行调用 ..
- 对于每一个引用从流中读取的对象,过滤器将检查深度以及引用的数量 以及流的长度 .. 这个深度从1开始并且对于每一个内嵌对象都会增加一,在每一个内嵌调用返回的时候都会减少 1 ..
- 此过滤器将不会对基础类或者 java.lang.String 实例(已经在流中详细编码)进行调用
- 过滤器将会返回接受 / 拒绝或者undecided的状态
- 过滤器的动作将会被记录(如果日志启用)

除非过滤器对象拒绝了此对象,否则对象将会被接受 ..
##### 为一个单独的流设置自定义过滤器
我们能够在当流的输入是不信任 以及过滤器具有有限集合的类或者约束是强制的情况下在独立的ObjectInputStream上进行设置过滤器,
举个例子,你能够确保一个流仅仅包含数组 / 字符串 以及其他应用特定的类型 ..
一个自定义的过滤器是通过setObjectInputFilter方法进行设置的 .. 这个自定义的过滤器将在从流中读取对象之前设置 ..

在下面的示例中,`setObjectInputFilter` 方法通过`dateTimeFilter` 方法执行 .. 这个过滤器仅仅接受来自`java.time` 包的类 ..
这个dateTimeFilter 方法定义在更后的示例中
```java
    LocalDateTime readDateTime(InputStream is) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            ois.setObjectInputFilter(FilterClass::dateTimeFilter);
            return (LocalDateTime) ois.readObject();
        } catch (ClassNotFoundException ex) {
            IOException ioe = new StreamCorruptedException("class missing");
            ioe.initCause(ex);
            throw ioe;
        }
    }
```
###### 设置进程范围的自定义过滤器
你能够设置一个进程范围的过滤器 去应用到ObjectInputStream的每一个使用上(除非它已经在特定的流上覆盖) .. 如果你能够识别每一个类型 ... 并且在整个应用
中条件是必须的 ..  此过滤器能够允许一些类型并拒绝其他类型 .. 通常来说,进程范围的过滤器是被用来拒绝特定的类或者包 或者限制数组的长度，图的深度以及图的总数量 ..

一个进程范围的过滤器只需要使用ObjectInputFilter.Config类的方法设置一次即可 .. 这个过滤器能够是一个类的实例,一个lambda表达式,一个方法引用或者一个表达式 ..
```java
    ObjectInputFilter filter = ...
    ObjectInputFilter.Config.setSerialFilter(filter);
```
在下面的示例中,进程范围的过滤器能够使用lambda表达式设置
```java
    ObjectInputFilter.Config.setSerialFilter(info -> info.depth() > 10 ? Status.REJECTED : Status.UNDECIDED);
```
在下面的示例中,进程范围的过滤器使用一个方法引用进行设置
```java
    ObjectInputFilter.Config.setSerialFilter(FilterClass::dateTimeFilter);
```

##### 使用一个表达式设置自定义的过滤器
一个基于表达式的自定义过滤器,这对于简单的情况来说非常的方便,能够通过使用`ObjectInputFilter.Config.createFilter` 方法进行创建 ..
你能够创建基于表达式的过滤器作为一个系统属性或者Security 属性.. 实现基于表达式的过滤器作为一个方法或者表达式给应用更多的灵活性 ..

这个过滤器表达式能够接受或者拒绝特定的类或者包 以及模块,它们也能够在数组尺寸,图的深度,总的引用 以及流的长度上进行限制... 模式不能够匹配一个类的父类或者接口 ..

这是一个很正常的事情 ..

在下面的示例中,过滤器允许`example.File`  并且拒绝  `example.Directory` 类 ..
```java
    ObjectInputFilter filesOnlyFilter = ObjectInputFilter.Config.createFilter("example.File;!example.Directory");
```
或者示例允许`example.File` ,其他的所有类直接拒绝
```java
    ObjectInputFilter filesOnlyFilter = ObjectInputFilter.Config.createFilter("example.File;!*");
```
###### 设置一个自定义的过滤器作为类
一个自定义的过滤器能够实现为一个类(通过实现java.io.ObjectInputFilter接口) 或者lambda 表达式 或者一个方法 ..

一个过滤器通常是无状态的并且能够仅仅在输入参数上执行检查.. 然而你能够实现一个过滤器,举个例子,在`checkInput` 方法的调用之间去维护状态去统计在流中的工件 .

在下面的示例中,FilterNumber 类允许一个Number类的任何实例对象并且拒绝任何其他 ...
```java
    class FilterNumber implements ObjectInputFilter {
        public Status checkInput(FilterInfo filterInfo) {
            Class<?> clazz = filterInfo.serialClass();
            if (clazz != null) {
                return (Number.class.isAssignableFrom(clazz)) ? Status.ALLOWED : Status.REJECTED;
            }
            return Status.UNDECIDED;
        }
    }
```

在这个示例中:
- checkInput方法接受一个ObjectInputFilter.FilterInfo对象,这个对象的方法提供了一些能够检查类 / 数组长度  / 当前深度 / 引用存在对象的数量 / 以及到目前为止读取的流的长度 ..
- 如果一个serialClass 不为空,指示一个新的对象将会被创建,这个值能够用来检测这个对象的类是不是Number,如果是它将接受,否则拒绝 ..
- 一个参数的任何其他组合将返回UNDECIDED, 反序列化继续,并且任何存在的过滤器将会运行直到对象被接受或者拒绝 .. 如果这里没有其他过滤器,对象将会被接受 ...

###### 设置一个自定义的过滤器作为方法
也能够作为方法, 方法引用能够替代内联的lambda表达式 ..

注意: 这里的`dataTimeFilter` 方法 它使用在更后面的一个示例中
```java
    public class FilterClass {
    static ObjectInputFilter.Status dateTimeFilter(ObjectInputFilter.FilterInfo info) {
        Class<?> serialClass = info.serialClass();
        if (serialClass != null) {
            return serialClass.getPackageName().equals("java.time")
                    ? ObjectInputFilter.Status.ALLOWED
                    : ObjectInputFilter.Status.REJECTED;
        }
        return ObjectInputFilter.Status.UNDECIDED;
    }
}
```

###### 示例
过滤在java.base 模块中的类, 这是一个自定义过滤器,它能够实现为方法,允许仅仅来自在jdk的base 模块中发现的类. 这个示例使用jdk9以及之后进行测试 ..
```java
        static ObjectInputFilter.Status baseFilter(ObjectInputFilter.FilterInfo info) {
            Class<?> serialClass = info.serialClass();
            if (serialClass != null) {
                return serialClass.getModule().getName().equals("java.base")
                        ? ObjectInputFilter.Status.ALLOWED
                        : ObjectInputFilter.Status.REJECTED;
            }
            return ObjectInputFilter.Status.UNDECIDED;
       }
```

#### 内置的过滤器
Java远程方法调用（RMI）注册中心、RMI分布式垃圾收集器和Java管理扩展（JMX）都有过滤器，并包含在JDK中。 你能够通过RMI 注册机 以及 RMI 贡献的垃圾回收器去注册
自己的过滤器实现额外的保护 ...

##### RMI 注册机 - Filters
注意: 使用这些内置的过滤器仅仅是一个开始,编辑`sun.rmi.registry.registryFilter` 系统属性去配置黑名单 并且或者扩展白名单去增加额外的对RMI 注册机的保护) 
为了保护整个应用,增加模式到`jdk.serialFilter` 全局系统属性去增加对其他序列化用户(它们没有自己的自定义过滤器)的保护

此RMI注册机 有许多内置的白名单过滤器  在注册机中所约束的对象 ..它包含了`java.rmi.Remote` ,`java.lang.Number`,`java.lang.reflect.Proxy` ,`java.rmi.server.UnicastRef`
,`java.rmi.activation.ActivationId` ,`java.rmi.server.UID` ,`java.rmi.server.RMIClientSocketFactory` 以及 `java.rmi.server.RMIServerSocketFactory` 类的实例 ..

内置的过滤器包括的尺寸限制
```text
maxarray=1000000,maxdepth=20
```
通过使用`sun.rmi.registry.registryFilter` 系统属性使用表达式去替代内置的过滤器 .. 如果你定义的过滤器接受的类传递到此过滤器  或者拒绝了类或者尺寸,那么内置的过滤器将不会执行 ..
如果你过滤器不接受或者拒绝任何事情.. 那么内置的过滤器将执行 ..

###### RMI 发布的垃圾回收器 - filters
> 使用这些内置过滤器仅仅是一个开始,编辑`sun.rmi.transport.dgcFilter` 系统属性去配置黑名单或者配置白名单去对分布式垃圾收集器 进行额外的保护 ..
> 为了保护整个应用,增加模式到全局系统属性`jdk.serialFilter` 去增加对其他没有自定义的过滤器的序列化用户的保护

此垃圾回收器内置了白名单过滤器 - 接受一组有限的类 .. 他包含了 `java.rmi.server.ObjID,java.rmi.server.UID,java.rmi.dgc.VMID 以及java.rmi.dgc.Lease` 类的实例 ..

内置过滤器包括的尺寸是:
```text
maxarray=1000000,maxdepth=20
```
通过设置`sun.rmi.transport.dgcFilter` 系统属性 - 使用表达式定义过滤器来替代内置过滤器 ..

如果你的自定义过滤器执行了,那么内置过滤器不会执行,如果你的过滤器没有接受任何事或者拒绝任何事情,那么内置过滤器使用 ..

###### JMX 过滤器
同样,通过编辑`jmx.remote.rmi.server.serial.filter.pattern` 管理属性去配置黑名单或者扩展白名单去对jmx做额外的保护 ..
为了保护整个应用环境,我们应该增加jdk.serialFilter全局系统属性去增加对其他序列化用户的保护 ..

JMX有一些内置的过滤器  允许在RMI之上 反序列化参数发送到服务器 .. 这包括了一组允许的类 ..

过滤器但是默认是禁用的,为了启用,定义一个`jmx.remote.rmi.server.serial.filter.pattern` 管理属性 - 通过表达式定义一个自定义过滤器 ..

此表达式必须包含了被允许在RMI之上作为一个参数发送到服务器的类型 以及这个类所依赖的所有类型 .. 包括`javax.management.ObjectName` 以及 `java.rmi.MarshalledObject` 类型 ..

例如,为了限制允许的类集合去开放的MBean 类型 以及他们所依赖的类型,增加以下行到`management.properties` 文件中:
```text
com.sun.management.jmxremote.serial.filter.pattern=java.lang.*;java.math.BigInteger;java.math.BigDecimal;java.util.*;javax.management.openmbean.*;javax.management.ObjectName;java.rmi.MarshalledObject;!* 
```

#### 日志过滤器动作
你能够调整日志记录初始化 / 拒绝 / 接受的调用 在序列化过滤器上的级别,使用日志输出作为一个诊断工具 - 能够看到那些被反序列化, 并且当你配置了白名单或者黑名单的时候确认配置 ..

当日志启用的时候,过滤器动作将记录到`java.io.serialization` 日志器 ..

为了启用序列化过滤器日志,编辑`$JDK_HOME/conf/logging.properties` 文件

例如为了记录被拒绝的调用,增加
```text
java.io.serialization.level=FINER
```