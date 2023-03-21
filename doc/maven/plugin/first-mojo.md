# 介绍
此指南意图帮助用户开发maven 的java插件

- 重要注意: 插件命名规范以及 apache maven 商标
- 你的第一个插件
  - 你的第一个mojo
    - 简单moji
  - 项目定义
  - 构建一个插件
  - 执行第一个mojo
    - 命令行简写
    - 依附mojo到构建生命周期上
- mojo archetype
- 参数
  - 在mojo中定义参数
  - 在项目中配置参数
- 使用setters
- 资源

## 注意事项
通常命名插件为`<yourplugin>-maven-plugin`.

如果编写一个`maven-<yourplugin>-plugin` (它以maven 作为插件名称的开始) 是强烈不建议的,因为它作为apache 官方的由apache maven 团队维护的maven 插件的保留命名规范(并且分组id为 org.apache.maven.plugins), 使用这种命名模式是对apache maven 商标的一种侵权 ..

## 你的第一个插件
构建一个简单的插件(具有一个目标,无任何参数,当运行的时候仅仅在屏幕上展示一个消息), 通过这种方式,我们将涵盖配置一个项目去创建插件的基本内容,一个定义了goal代码的java mojo的最小内容,并且有几种方式可以执行此mojo ...

### 第一个mojo

通常来说,一个java mojo 由单个呈现一个插件的goal的类组成..  不需要类似于ejb的多个类,尽管一个插件它可能包含了大量的类似的mojo(都使用统一的mojo抽象)..

当处理这个资源树去发现mojo的时候,`plugin-tools` 将会查看要么使用了@Mojo的注解的类 或者具有"goal" java doc 注释的类,具有这种注释的类都将会包含在插件的配置文件中 ..
#### a simple mojo
下面列出了一个最简单的mojo类.
```java
package sample.plugin;
 
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
 
/**
 * Says "Hi" to the user.
 *
 */
@Mojo( name = "sayhi")
public class GreetingMojo extends AbstractMojo
{
    public void execute() throws MojoExecutionException
    {
        getLog().info( "Hello, world." );
    }
}
```
- `org.apache.maven.plugin.AbstractMojo` 提供了对于需要实现一个mojo的必要的大部分基础设施,除了`execute` 方法 ..
- `@Mojo` 注解是必须的(用来如何控制mojo执行)
- `execute` 方法将抛出两个异常
  - `org.apache.maven.plugin.MojoExecutionException` 如果不期待的异常发生,抛出这个异常将会导致构建错误的消息展示 ..
  - `org.apache.maven.plugin.MojoFailureException` 如果一个期待的问题(例如编译失败)发生,抛出构建失败的消息展示 ..
- `getLog` 方法(定义在AbstractMojo)将返回一个类似于log4j的logger,它将允许插件去创建'debug','info','warn',error'这几种级别的消息. 这个日志器
接收一些打算展示给用户的信息.. 查看[抓取mojo 日志器](https://maven.apache.org/plugin-developers/common-bugs.html#Retrieving_the_Mojo_Logger) 了解
适当使用的提示.. 

所有mojo 注解在[mojo api 规范中](mojo-api-reference.md)描述 ...

## 项目定义
当我们编写了一个插件,那么可以构建此插件,那么我们需要一个项目的描述符.. 且需要正确设置一些配置
- `groupId` 插件的组id,应该匹配由mojo使用的包的统一前缀..
- `artifactId` 标识插件的名称
- `version` 插件的版本
- `packaging` 应该设置为`maven-plugin`
- `dependencies` 它需要包含maven plugin tools api(用于解析`AbstractMojo` 以及相关的类),如果你使用了Mojo注解,那么还需要声明注解相关的类

下图列出了示例mojo项目的说明(标识了上表中描述的参数)
```xml
<project>
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>sample.plugin</groupId>
  <artifactId>hello-maven-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>maven-plugin</packaging>
 
  <name>Sample Parameter-less Maven Plugin</name>
 
  <dependencies>
    <dependency>
      <groupId>org.apache.maven</groupId>
      <artifactId>maven-plugin-api</artifactId>
      <version>3.0</version>
      <scope>provided</scope>
    </dependency>
 
    <!-- dependencies to annotations -->
    <dependency>
      <groupId>org.apache.maven.plugin-tools</groupId>
      <artifactId>maven-plugin-annotations</artifactId>
      <version>3.4</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>
</project>
```
## 构建一个插件
这里有一些少量的插件goal可以绑定到标准的构建生命周期(由使用`maven-plugin` 打包定义的)

- `compile` 编译插件的java 代码
- `process-classes` 抓取数据去构建插件的[描述符](https://maven.apache.org/ref/current/maven-plugin-api/plugin.html)
- `test` 运行插件的单元测试
- `package` 构建plugin jar
- `install` 安装plugin jar到本地仓库
- `deploy` 部署plugin jar到远程仓库

了解更多,查看 [`maven-plugin` 打包的绑定详情](https://maven.apache.org/ref/current/maven-core/default-bindings.html#Plugin_bindings_for_maven-plugin_packaging)

## 执行第一个mojo
我们可以在命令行指定插件的goal 进行执行,为了这样做,首先我们需要将插件加入到项目中 ..
```xml
<build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>sample.plugin</groupId>
          <artifactId>hello-maven-plugin</artifactId>
          <version>1.0-SNAPSHOT</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
```
并且你需要指定全限定 goal,如下形式:
```shell
mvn groupId:artifactId:version:goal
```
举个例子,你可以运行示例插件的一个mojo,你可以在命令行中执行`mvn sample.plugin:hello-maven-plugin:1.0-SNAPSHOT:sayhi` 

提示: 对于version,如果运行一个单机/独立的goal 那么是不需要的.. (可以说,无版本或者稳定独立的插件,具有唯一性,或者版本中最新的)

### 在命令行中简写
这里有几种形式可以减少书写:
- 如果运行安装到本地仓库中的最新版本的 插件,你能够省略它的版本号. 因此`mvn sample.plugin:hello-maven-plugin:sayhi` 可以运行插件
- 你能够为插件分配一个简短前缀,`mvn hello:sayhi` ,这是自动完成的(如果你采用`${prefix}-maven-plugin`  或者 `maven-${prefix}-plugin` - 如果项目是apache maven 项目的一部分 ) \
  你能够通过额外的配置分配,查看[plugin preifx mapping](https://maven.apache.org/guides/introduction/introduction-to-plugin-prefix-mapping.html).
- 最终,你能够增加你的插件的分组id到默认查询的groupId 列表中,你需要增加以下信息到`${user.home}/.m2/settings.xml` 文件 

最终,你能够通过`mvn hello:sayhi` 运行mojo

### 依附到Mojo到 构建生命周期
你能够配置你的插件去依附特定的goals 到构建生命周期中的特定阶段,如下所示:
```xml
  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>sample.plugin</groupId>
          <artifactId>hello-maven-plugin</artifactId>
          <version>1.0-SNAPSHOT</version>
        </plugin>
      </plugins>
    </pluginManagement>  
    <plugins>
      <plugin>
        <groupId>sample.plugin</groupId>
        <artifactId>hello-maven-plugin</artifactId>
        <executions>
          <execution>
            <phase>compile</phase>
            <goals>
              <goal>sayhi</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```
这将会导致java代码编译的时候才会执行 simple mojo.  了解绑定mojo到生命周期的阶段的更多信息,请参考[build lifecyle 文档](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)

## Mojo archetype
为了创建一个新的插件项目,你能够使用Mojo [archetype](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html) 
```shell
mvn archetype:generate \
  -DgroupId=sample.plugin \
  -DartifactId=hello-maven-plugin \
  -DarchetypeGroupId=org.apache.maven.archetypes \
  -DarchetypeArtifactId=maven-archetype-plugin
```
## 参数
一个没有参数的mojo 可能不是很有用,参数提供了一些重要的功能:
- 它提供钩子允许用户去调整插件的操作去适配他们的需要 ..
- 它提供了一种方式去容易的抓取来自pom的元素的值(而无需导航这些对象)

### 在mojo中定义参数
定义一个参数是非常简单的- 可以在mojo中创建实例变量并增加适当的注释,下面是一个示例mojo的参数示例 ..
```java
   /**
     * The greeting to display.
     */
    @Parameter( property = "sayhi.greeting", defaultValue = "Hello World!" )
    private String greeting;
```
注解之前的部分是参数的描述. `parameter` 注解将识别这个变量作为一个mojo的参数,注解的`defaultValue` 参数定义了变量的默认值. 这个值能够包括引用这个对象的表达式,例如`${project.version}` (更多的可以在[Parameter Expression 文档](https://maven.apache.org/ref/current/maven-core/apidocs/org/apache/maven/plugin/PluginParameterExpressionEvaluator.html) 中了解更多). 这个`property` 参数能够被用来接收用户在命令行通过`-D` 选项设置的系统参数的引用配置 ...
### 在项目中配置参数
在项目中配置插件的参数值 - 需要在`pom.xml` 中作为项目的插件的一部分 ..以下是配置一个插件的示例:
```xml
<plugin>
  <groupId>sample.plugin</groupId>
  <artifactId>hello-maven-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
  <configuration>
    <greeting>Welcome</greeting>
  </configuration>
</plugin>
```
在这个配置部分中,("greeting") 是参数的名称并且元素的内容是("Welcome") 将分配给这个参数 ..
> 注意:
> 更多详情可以在 [Guide to Configuring Plugins](https://maven.apache.org/guides/mini/guide-configuring-plugins.html) 中发现 ..

## 使用Setters
你并不需要限制去使用私有字段映射(这是一个好事情),如果你尝试在maven 上下文之外重用你的mojo, 使用上述示例代码,我们能够使用下划线约定命名我们的私有字段的名称并且提供setter(这是配置映射机制的使用方式),因此你的mojo看起来如下所示:
```java
public class MyQueryMojo
    extends AbstractMojo
{
    @Parameter(property="url")
    private String _url;
 
    @Parameter(property="timeout")
    private int _timeout;
 
    @Parameter(property="options")
    private String[] _options;
 
    public void setUrl( String url )
    {
        _url = url;
    }
 
    public void setTimeout( int timeout )
    {
        _timeout = timeout;
    }
 
    public void setOptions( String[] options )
    {
        _options = options;
    }
 
    public void execute()
        throws MojoExecutionException
    {
        ...
    }
}
```
注意每一个参数指定的属性名称都会告诉maven 应该使用什么样的setter 和getter(当字段名无法匹配插件配置中的参数的意向(预期的)名称)

## 资源
1. [Mojo 文档](./mojo-api-reference.md)
2. [maven plugin testing harness](./maven-plugin-testing.md)
3. [plexus](./plexus.md)
4. [plexus common utilities](./plexus-common-utilities.md)
5. [commons-io](http://commons.apache.org/io/)
6. [Common Bugs and Pitfalls](./common-bugs-pitfalls.md)