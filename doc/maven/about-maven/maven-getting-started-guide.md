# maven 开始指南
主要是作为一个快速指南参考,对于第一次使用maven的人来说,建议循序渐进.. 更熟悉maven的人这提供了为手头的需要处理的事情提供快速解决参考 ..

maven 主要负责项目基础设施的管理,让团队成员集中于利益相关者对项目的需求 ..

# 大纲
1. 什么是maven
2. 如何从开发流程中受益
3. 如何设置maven
4. 如何构建第一个maven项目
5. 如何编译应用资源
6. 如何编译测试资源并运行单元测试
7. 创建一个jar并安装到本地仓库
8. 什么是快照版本
9. 如何使用插件
10. 如何增加资源到jar中
11. 如何过滤资源文件
12. 如何使用外部依赖
13. 如何部署jar到远程的仓库中..
14. 如何创建文档
15. 如何构建其他类型的项目
16. 如何一次性构建多个项目

## maven 是什么
咋一看maven似乎可以包含很多东西，但简而言之，maven是一种强势浆膜适用于项目的基础设施，然后我们通过提供最佳实践的清溪路近来促进理解和生产力，
本质上是一个项目管理和理解工具，因此他提供了项目管理的方法.

1. 构建
2. 文档
3. 报告
4. 依赖关系
5. scms
6. 发布

## 如何使用maven 在开发流程中受益
可以通过标准的约定和时间来加速您的开发周期，同时帮助您获得更高的成功率，从而为你的构建过程带来好处。现在我们已经介绍了一些卖房的历史和目的，让我们通过一些真实的例子来帮助你启动和运行maven.

## 如何设置maven
maven的默认值已经足够,但是我们可能需要更改缓冲位置，例如HTTP代理,那么有关详细信息请参阅[配置maven指南](https://maven.apache.org/guides/mini/guide-configuring-maven.html) ..

## 创建第一个maven 项目
maven创建项目很简单，我们可以使用maven的原型机制 ..

maven中的原型模式被定义为一种原始模式或者模型,所有同类事物均由其制成，在maven中,原型是一个项目模板，他与一些用户输入相结合，
请以生成一个根据用户需求定制的工作maven项目，现在我们可以展示原型机制是如何工作，如果我们要了解更多关于原型的信息，请参阅[原型简介](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html)
```shell
mvn -B archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4
```
移到你执行这个命令，你将能够注意到一些事情将会发生，首先你可能会注意到有一个名为my-app的目录已经被创建对这个新项目来说，
这个目录中也包含了一个pom.xml的文件，那么他看起来如下所示:
```xml

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
 
  <name>my-app</name>
  <!-- FIXME change it to the project's website -->
  <url>http://www.example.com</url>
 
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>1.7</maven.compiler.source>
    <maven.compiler.target>1.7</maven.compiler.target>
  </properties>
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
 
  <build>
    <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
       ... lots of helpful plugins
    </pluginManagement>
  </build>
</project>
```
pom.xml包含了项目的项目对象模型 .. pom是maven工作的基本单元 .. 这很重要,需要记住,因为maven 是以项目为中心,一切围绕着项目的概念 ..
嗯，这个pom包含了就是说项目的主要信息且信息是分块配置的，并且呢，他本质上是一种一站式购物，那能够去发现有关你项目的任何事情，然后这个理解这个pom非常重要,
新的用户的鼓励去参考[这个pom介绍](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html).

当前，这个展示pom中所包含的关键元素，让我们就是说能够去一个一个了解来熟悉这个pom的根本要素..
1. project 表示所有maven pom.xml 文件中的顶级元素
2. modelVersion 指示了pom所使用的对象模型的版本,这个版本自身改变的不是很频繁. 但是也能够去确保稳定性而做出改变(如果maven的开发者确实需要改变model时) ..
3. groupId 表示创建这个项目的分组或者组织的唯一标识符,那么他是这个项目的一个关键的身份通常是基于这个组织的完全限定域名，例如org.apache.maven.plugins 设计作为所有\
maven插件的groupId ..
4. artifactId 标识主工件的独一无二的基本名称的元素, 主工件更可能是一个jar文件,那么二等的货说二等工件的看起来可能像这个源代码打包文件，那么他也可以使用这个ARTIFACT ID作为他的一个最终名称的一部分通常一个ARTIFACT - 由 \
maven 所生产的工件它的形式是<artifactId>-<version>.<extension>(例如: myapp-1.0.jar)
5. version 这个元素我是项目所生成的工件的版本，那么maven中能够让你在版本控制中大所获益，我看到一个设计符号那么他表示一个快照版本，然后他表示项目正处于开发的一个状态，有关这个[快照信息](https://maven.apache.org/guides/getting-started/index.html#What_is_a_SNAPSHOT_version) 的一个使用,点击链接了解更多 ..
6. name 标识项目的名称,这通常被用在maven生成的文档中
7. url 指示项目的站点,这通常使用在maven生成的文档中
8. properties 标识了包含了在pom中能够随时随地使用的值占位符的元素(element)..
9. dependencies 标识了依赖的[相关信息列表](https://maven.apache.org/pom.html#dependencies),这是pom的基石..
10. build 它处理了一些事情(例如声明项目的目录结构并管理插件)

对于每一个在pom中可用的元素的参考 - 查看[pom reference](https://maven.apache.org/ref/current/maven-model/maven.html), 现在让我们继续看这个示例

在生成了第一个项目之后,你能够注意到以下的结构(已经创建完毕)
```text
my-app
|-- pom.xml
`-- src
    |-- main
    |   `-- java
    |       `-- com
    |           `-- mycompany
    |               `-- app
    |                   `-- App.java
    `-- test
        `-- java
            `-- com
                `-- mycompany
                    `-- app
                        `-- AppTest.java
```
正如你所见,根据原型创建的项目有一个pom文件,以及应用的资源的源树 以及测试资源的源树 .. 这是一个maven项目的标准布局 ..(应用资源放在`${basedir}/src/main/java`) 并且
测试资源放置在`${basedir}/src/test/java·,这里的${basedir}标识pom.xml文件存在的目录,对于这种方式创建的项目这是推荐的目录使用结构 ..

这是一个maven约定并且你可以详细阅读[introduction the standard directory layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) .. 

## 如何编译应用资源
仅仅只需要执行以下明亮进行应用资源编译
```shell
mvn compile
```
当第一次执行此命令的时候,maven 需要下载所有的插件以及相关的依赖(为了履行此命令),那么再次运行命令的时候,maven将包含了它需要的东西,所以则不需要下载任何新的东西并且
可以更快执行此命令..

从输出中可以知道编译后的类放在`${basedir}/target/classes` ,这是maven 采用的另一个标准约定,因此如果观察敏锐,那么能够注意到通过使用标准约定,那么pom会非常小 ..
我们不需要告诉maven 源在哪里或者输出应该去哪里,通过标准的maven 约定,那么事半功倍,最为一个随意的比较,我们能够知道在[ant](http://ant.apache.org/) 可能必须做什么事情才能完成相同的事情 ..

现在我们仅仅编译单个应用程序的源树,显示的ant脚本和maven pom相差无几,但是pom能够做更多事情,且更简单 ...

## 编译测试源并运行单元测试
现在只需要执行以下命令即可
```shell
mvn test
```
- 我们能够得到输出,从输出中我们一般可以得出,maven下载了为了支持单元测试的其他依赖项(这是执行测试所需要的依赖项或者插件),
当下载之后,将会被缓存使用,而不会再次下载 ..
- 在编译和执行测试之前,maven 编译主源代码(这些类是最新的,当前示例我们自从上一次编译以来没有改变任何东西)

如果我们仅仅只是为了编译测试源代码,但是不执行测试(我们可以执行以下命令)
```shell
mvn test-compile
```
### 如何创建jar并安装到本地存储库中
制作jar文件很简单,我们可以通过以下命令完成
```shell
mvn package
```
将会将打包的文件放置在`${basedir}/target` 目录,我们将看到生成的例如jar文件 ..

现在我们需要在本地存储库中`${user.home}/.m2/repository` 中安装生成的工件(jar文件),有关存储库的更多信息,查看
[存储库简介](https://maven.apache.org/guides/introduction/introduction-to-repositories.html). 现在我们继续安装
```shell
mvn install
```
这里需要提一下surefire插件- 用来执行构建过程中的单元测试(它会查找具有特定命名约定的文件中的测试),默认情况,包含的测试是:
- **/*Test.java
- **/Test*.java
- **/*/TestCase.java
默认排除的是:
- **/Abstract*Test.java
- **/Abstract*TestCase.java

现在我们已经完成了配置,构建,测试,打包和安装典型maven项目的过程,这可能是大多数项目使用maven执行的操作.. 并且到目前为止,所有的操作仅仅都由18行文件
驱动的.. 即项目的模型或者pom. 如果插件一个典型的ant[构建文件](https://maven.apache.org/ant/build-a1.xml),提供目前相同的功能,则会发现它是pom的两倍配置.. 而现在才刚刚开始 .. 我们可以从maven中获得更多功能 ...
而无须要增加任何内容到pom中,要从示例的ant 构建文件中获得功能,你必须进行容易出错的添加 ..

现在我们还能免费得到其他什么东西? 大量的maven插件开箱即用,甚至像上面的pom一样,这里我们提到一个maven备受推崇的功能之一: 无序任何工作,pom就可以有足够的信息为项目生成网站 ..
我们可能需要自定义maven站点,但是时间紧迫,我们可以提供项目的基本信息,只需要执行以下命令则可以创建站点内容:
```shell
mvn site
```
并且构建过程中还有其他丰富的独立目标可以执行,例如
```shell
mvn clean
```
这个命令将会在构建数据之前移除掉target目录(因此编译内容是新鲜的)

## snapshot 版本
携带了`-SNAPSHOT` 在pom.xml文件中的version 标签中那么,标识一个快照版本 ..
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  <groupId>...</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <name>Maven Quick Start Archetype</name>
```
snapshot 标识了开发分支当前最新的代码,不保证代码稳定或者不改变,相对的,发行版本的代码(没有snapshot的后缀)的将是不会改变的或者稳定 .. 

换句话说,snapshot 标识发行版本之前的开发版本,快照版本代码比发行版本代码更旧 ..

在发行过程中,一个x.y-SNAPSHOT的版本改变为x.y. 这个发行过程中也会增加开发版本到x.(y+1)-SNAPSHOT,举个例子,version 1.0-SNAPSHOT的发行版为1.0,
并且新的开发版本为1.1-SNAPSHOT..

## 使用插件
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.3</version>
      <configuration>
        <source>1.5</source>
        <target>1.5</target>
      </configuration>
    </plugin>
  </plugins>
</build>
```
上述代码配置了java 编译器允许jdk 5.0资源 / 或者源代码级别..  

你能够注意到在maven中的所有插件特别像依赖(在某些方式下他们确实是),查看会自动的下载并使用,你也可以请求特定版本的插件(默认是 最新可用版本) ..

`configuration` 元素将会应用参数到来自编译器插件的每一个goal,在上面的情况中,编译插件已经作为构建处理的一部分并且仅仅稍微调整了配置.
它也可能去增加新的goals 到流程中并配置特定的goals(增加之后,配置去定制goal行为)... 了解[构建生命周期的详情](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) ..

为了发现插件的可用配置,你能够查看 [plugin list](https://maven.apache.org/plugins/) 并导航到插件主页并查看goal的详细配置信息,对于如何配置插件的必要参数,
你能够查看 [guide to configuring plugins](https://maven.apache.org/guides/mini/guide-configuring-plugins.html) ...

## 如何增加资源到jar中
通常情况下不需要改变pom就已经可以满足了(打包资源到jar中),maven 根据[标准的目录结构](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html),使用标准的maven 约定能够打包资源到
jar中 - 通过将资源放在标准的目录结构中 ...

在我们的示例中,标准路径结构是`${basedir}/src/main/resources` 它将打包这个目录下的任何资源到jar中,maven使用的简单规则是: 放置在${basedir}/src/main/resources目录中的任何目录或文件都以完全相同的结构打包到 JAR 中，从 JAR 的底部开始。
```text
my-app
|-- pom.xml
`-- src
    |-- main
    |   |-- java
    |   |   `-- com
    |   |       `-- mycompany
    |   |           `-- app
    |   |               `-- App.java
    |   `-- resources
    |       `-- META-INF
    |           `-- application.properties
    `-- test
        `-- java
            `-- com
                `-- mycompany
                    `-- app
                        `-- AppTest.java
```
示例中看到我们有一个META-INF目录，该目录中有一个application.properties文件。如果您解压 Maven 为您创建的 JAR 并查看它，您会看到以下内容：
```text
|-- META-INF
|   |-- MANIFEST.MF
|   `-- application.properties
|   `-- maven
|       `-- com.mycompany.app
|           `-- my-app
|               |-- pom.properties
|               `-- pom.xml
`-- com
    `-- mycompany
        `-- app
            `-- App.class
```
${basedir}/src/main/resources如您所见，可以从 JAR 的底部开始找到的内容，我们的application.properties文件位于该META-INF目录中。
您还会注意到那里的一些其他文件META-INF/MANIFEST.MF以及一个pom.xml和pom.properties文件。这些都是在 Maven 中生成 JAR 的标准。如果您愿意，
您可以创建自己的清单，但如果您不选择，Maven 将默认生成一个清单。（您也可以修改默认清单中的条目。我们稍后会谈到这一点。）pom.xml和pom.properties文件打包在 JAR 中，
因此 Maven 生成的每个工件都是自描述的，并且还允许您在需要时在自己的应用程序中使用元数据。一个简单的用途可能是检索应用程序的版本。操作 POM 文件需要您使用一些 Maven 实用程序，
但可以使用标准 Java API 使用属性，如下所示：
```text
#Generated by Maven
#Tue Oct 04 15:43:21 GMT-05:00 2005
version=1.0-SNAPSHOT
groupId=com.mycompany.app
artifactId=my-app
```
要将资源添加到单元测试的类路径，您遵循与将资源添加到 JAR 相同的模式，除了您放置资源的目录是 ${basedir}/src/test/resources。此时，您将拥有一个如下所示的项目目录结构：
```text
my-app
|-- pom.xml
`-- src
    |-- main
    |   |-- java
    |   |   `-- com
    |   |       `-- mycompany
    |   |           `-- app
    |   |               `-- App.java
    |   `-- resources
    |       `-- META-INF
    |           |-- application.properties
    `-- test
        |-- java
        |   `-- com
        |       `-- mycompany
        |           `-- app
        |               `-- AppTest.java
        `-- resources
            `-- test.properties
```
在一个单元测试中我们能够访问测试资源,以下是一个代码片段 ..
```java

// Retrieve resource
        InputStream is = getClass().getResourceAsStream( "/test.properties" );

// Do something with the resource

```
## 如何过滤资源文件
有时资源文件需要包含只能在构建时提供的值。要在 Maven 中完成此操作，请使用语法将包含该值的属性的引用放入您的资源文件中${<property name>}。
该属性可以是 pom.xml 中定义的值之一、用户的 settings.xml 中定义的值、外部属性文件中定义的属性或系统属性。

要在复制时让 Maven 过滤资源，只需将filtering您的资源目录设置为 true 到pom.xml：
```text
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>
 
  <name>Maven Quick Start Archetype</name>
  <url>http://maven.apache.org</url>
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
 
  <build>
    <resources>
      <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
      </resource>
    </resources>
  </build>
</project>
```
您会注意到我们必须添加之前没有的build、resources和元素。resource此外，我们必须明确声明资源位于 src/main/resources 目录中。所有这些信息以前都是作为默认值提供的，
但由于默认值为filteringfalse，我们必须将其添加到我们的 pom.xml 中以覆盖该默认值并设置filtering为 true。

要引用在 pom.xml 中定义的属性，属性名称使用定义值的 XML 元素的名称，允许“pom”作为项目（根）元素的别名。so${project.name}指的是项目的名称，
${project.version}指的是项目的版本，${project.build.finalName}指的是构建的项目打包时最终创建的文件名等等。注意POM的一些元素是有默认值的，
所以不要需要在您的中明确定义pom.xml才能在此处使用这些值。settings.xml同样，可以使用以“settings”开头的属性名称来引用用户的值（例如，${settings.localRepository}指用户本地存储库的路径）。

application.properties为了继续我们的示例，让我们向文件（我们放在目录中）添加几个属性，src/main/resources它们的值将在过滤资源时提供：
```properties
# 应用程序属性
application.name=${project.name}
application.version=${project.version}
```
现在我们可以执行命令,进行资源复制和资源过滤的构建生命周期阶段
```shell
mvn process-resources
```
application.properties下的文件（target/classes最终将进入 jar）如下所示：
```properties
# 应用程序属性
application.name=Maven 快速入门原型
application.version=1.0-SNAPSHOT
```
要引用外部文件中定义的属性，您需要做的就是在 pom.xml 中添加对该外部文件的引用。首先，让我们创建我们的外部属性文件并调用它src/main/filters/filter.properties：
```text
# filter.properties
my.filter.value=hello!
```
pom.xml配置
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>
 
  <name>Maven Quick Start Archetype</name>
  <url>http://maven.apache.org</url>
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
 
  <build>
    <filters>
      <filter>src/main/filters/filter.properties</filter>
    </filters>
    <resources>
      <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
      </resource>
    </resources>
  </build>
</project>
```
然后，如果我们在文件中添加对此属性的引用application.properties：
```properties
# 应用程序属性
application.name=${project.name}
application.version=${project.version}
消息=${my.filter.value}
```
下一次执行该mvn process-resources命令会将我们的新属性值放入application.properties.

作为在外部文件中定义 my.filter.value 属性的替代方法， 您也可以在properties您的部分中定义它pom.xml，并且您会得到相同的效果（注意我不需要对src/main/filters/filter.properties任何一个的引用）：
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>
 
  <name>Maven Quick Start Archetype</name>
  <url>http://maven.apache.org</url>
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
 
  <build>
    <resources>
      <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
      </resource>
    </resources>
  </build>
 
  <properties>
    <my.filter.value>hello</my.filter.value>
  </properties>
</project>

```
过滤资源也可以从系统属性中获取值；Java 内置的系统属性（如java.version或user.home）或使用标准 Java -D 参数在命令行上定义的属性。为了继续该示例，让我们将application.properties文件更改为如下所示：
```properties
# 应用程序属性
java.version=${java.version}
command.line.prop=${command.line.prop}
```
现在，当您执行以下命令时（请注意命令行中 command.line.prop 属性的定义），该application.properties文件将包含系统属性中的值。
```shell
mvn process-resources "-Dcommand.line.prop=hello again"
```

## 使用外部依赖项
您可能已经注意到dependencies我们用作示例的 POM 中的一个元素。事实上，您一直以来都在使用外部依赖项，但在这里我们将更详细地讨论它是如何工作的。更详尽的介绍请参考我们的[依赖机制简介](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)。

pom.xml部分dependencies列出了我们的项目构建所需的所有外部依赖项（无论是在编译时、测试时、运行时还是其他任何时候都需要该依赖项）。现在，我们的项目仅依赖于 JUnit（为了清楚起见，我去掉了所有资源过滤的东西）：
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>
 
  <name>Maven Quick Start Archetype</name>
  <url>http://maven.apache.org</url>
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
```
对于每个外部依赖项，您至少需要定义 4 个内容：groupId、artifactId、version 和 scope。pom.xmlgroupId、artifactId 和版本与构建该依赖项的项目中给出的相同。scope 元素指示您的项目如何使用该依赖项，并且可以是compile/test/runtime。有关可以为依赖项指定的所有内容的更多信息，请参阅[项目描述符参考](https://maven.apache.org/ref/current/maven-model/maven.html)。

有关整个依赖机制的更多信息,查看[依赖机制的简介](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)

有了有关依赖项的信息，Maven 就可以在构建项目时引用依赖项。Maven 从哪里引用依赖项？Maven 在您的本地存储库（${user.home}/.m2/repository默认位置）中查找所有依赖项。在上一节中，我们将项目 (my-app-1.0-SNAPSHOT.jar) 中的工件安装到本地存储库中。一旦它安装在那里，另一个项目可以通过将依赖信息添加到它的 pom.xml 来引用该 jar 作为依赖：
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-other-app</artifactId>
  ...
  <dependencies>
    ...
    <dependency>
      <groupId>com.mycompany.app</groupId>
      <artifactId>my-app</artifactId>
      <version>1.0-SNAPSHOT</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
</project>
```
在其他地方建立的依赖关系呢？他们如何进入我的本地存储库？每当项目引用本地存储库中不可用的依赖项时，Maven 将从远程存储库下载依赖项到本地存储库。当您构建第一个项目时，您可能注意到 Maven 下载了很多东西
（这些下载是用于构建项目的各种插件的依赖项）。默认情况下，可以在https://repo.maven.apache.org/maven2/找到（和浏览）Maven 使用的远程存储库。您还可以设置自己的远程存储库（可能是您公司的中央存储库）
以代替或补充默认远程存储库。有关存储库的更多信息，您可以参考存储库简介.

让我们向我们的项目添加另一个依赖项。假设我们已经在代码中添加了一些日志记录并且需要添加 log4j 作为依赖项。首先，我们需要知道 log4j 的 groupId、artifactId 和版本是什么。Maven Central 上的相应目录称为[/maven2/log4j/log4j](https://repo.maven.apache.org/maven2/log4j/log4j/)。
在该目录中有一个名为 maven-metadata.xml 的文件。这是 log4j 的 maven-metadata.xml 的样子：
```xml
<metadata>
  <groupId>log4j</groupId>
  <artifactId>log4j</artifactId>
  <version>1.1.3</version>
  <versioning>
    <versions>
      <version>1.1.3</version>
      <version>1.2.4</version>
      <version>1.2.5</version>
      <version>1.2.6</version>
      <version>1.2.7</version>
      <version>1.2.8</version>
      <version>1.2.11</version>
      <version>1.2.9</version>
      <version>1.2.12</version>
    </versions>
  </versioning>
</metadata>
```
从这个文件可以看出，我们要的groupId是“log4j”，artifactId是“log4j”。我们看到许多不同的版本值可供选择；
现在，我们将只使用最新版本 1.2.12（一些 maven-metadata.xml 文件也可能指定哪个版本是当前发布版本：请参阅[存储库元数据](https://maven.apache.org/ref/current/maven-repository-metadata/repository-metadata.html)参考). 
在 maven-metadata.xml 文件旁边，我们可以看到与每个版本的 log4j 库对应的目录。在每个文件中，我们将找到实际的 jar 文件（例如 log4j-1.2.12.jar）
以及一个 pom 文件（这是依赖项的 pom.xml，指示它可能具有的任何进一步依赖项和其他信息) 和另一个 maven-metadata.xml 文件。还有一个对应于每个文件的 md5 文件，其中包含这些文件的 MD5 哈希值
。您可以使用它来验证库或找出您可能已经在使用的特定库的哪个版本。
现在我们知道了我们需要的信息，我们可以将依赖项添加到我们的 pom.xml 中：
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>
 
  <name>Maven Quick Start Archetype</name>
  <url>http://maven.apache.org</url>
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>log4j</groupId>
      <artifactId>log4j</artifactId>
      <version>1.2.12</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
</project>
```
现在，当我们编译项目 ( mvn compile) 时，我们将看到 Maven 为我们下载 log4j 依赖项。
## 远程部署jar
要将 jar 部署到外部存储库，您必须在 pom.xml 中配置存储库 url，并在 settings.xml 中配置用于连接到存储库的身份验证信息。

这是一个使用 scp 和用户名/密码身份验证的示例：
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>
 
  <name>Maven Quick Start Archetype</name>
  <url>http://maven.apache.org</url>
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.apache.codehaus.plexus</groupId>
      <artifactId>plexus-utils</artifactId>
      <version>1.0.4</version>
    </dependency>
  </dependencies>
 
  <build>
    <filters>
      <filter>src/main/filters/filters.properties</filter>
    </filters>
    <resources>
      <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
      </resource>
    </resources>
  </build>
  <!--
   |
   |
   |
   -->
  <distributionManagement>
    <repository>
      <id>mycompany-repository</id>
      <name>MyCompany Repository</name>
      <url>scp://repository.mycompany.com/repository/maven2</url>
    </repository>
  </distributionManagement>
</project>
```
settings.xml配置
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  ...
  <servers>
    <server>
      <id>mycompany-repository</id>
      <username>jvanzyl</username>
      <!-- Default value is ~/.ssh/id_dsa -->
      <privateKey>/path/to/identity</privateKey> (default is ~/.ssh/id_dsa)
      <passphrase>my_key_passphrase</passphrase>
    </server>
  </servers>
  ...
</settings>
```
请注意，如果您要连接到 openssh ssh 服务器，该服务器在 sshd_confing 中将参数“PasswordAuthentication”设置为“no”，则每次都必须输入密码以进行用户名/密码身份验证（尽管您可以使用另一个 ssh 登录客户端通过输入用户名和密码）。在这种情况下，您可能希望切换到公钥身份验证。

如果在settings.xml. 有关详细信息，请参阅[密码加密](https://maven.apache.org/guides/mini/guide-encryption.html)。

## 创建文档
为了让您快速开始使用 Maven 的文档系统，您可以使用原型机制为您现有的项目生成一个站点，使用以下命令：
```shell
mvn archetype:generate \
  -DarchetypeGroupId=org.apache.maven.archetypes \
  -DarchetypeArtifactId=maven-archetype-site \
  -DgroupId=com.mycompany.app \
  -DartifactId=my-app-site

```
现在转到[创建站点指南](https://maven.apache.org/guides/mini/guide-site.html)，了解如何为您的项目创建文档。

## 创建其他类型的项目
请注意，生命周期适用于任何项目类型。例如，回到基本目录，我们可以创建一个简单的 Web 应用程序：
```shell
mvn archetype:generate \
    -DarchetypeGroupId=org.apache.maven.archetypes \
    -DarchetypeArtifactId=maven-archetype-webapp \
    -DgroupId=com.mycompany.app \
    -DartifactId=my-webapp
```
请注意，这些都必须在一行中。这将创建一个名为的目录，my-webapp其中包含以下项目描述符：
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>my-webapp</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>war</packaging>
 
  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
 
  <build>
    <finalName>my-webapp</finalName>
  </build>
</project>
```
请注意<packaging>元素 - 这告诉 Maven 构建为 WAR。切换到 webapp 项目的目录并尝试：

然后执行命令
```shell
mvn package
```
您会看到target/my-webapp.war已构建，并且执行了所有正常步骤。

## 如何一次构建多个项目
Maven 内置了处理多个模块的概念。在本节中，我们将展示如何构建上面的 WAR，并一步包含前面的 JAR。

首先，我们需要pom.xml在其他两个之上的目录中添加一个父文件，因此它应该如下所示
```text
+- pom.xml
+- my-app
| +- pom.xml
| +- src
|   +- main
|     +- java
+- my-webapp
| +- pom.xml
| +- src
|   +- main
|     +- webapp
```
您将创建父项目的 POM 文件应包含以下内容：
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <groupId>com.mycompany.app</groupId>
  <artifactId>app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pom</packaging>
 
  <modules>
    <module>my-app</module>
    <module>my-webapp</module>
  </modules>
</project>
```
我们需要依赖来自 webapp 的 JAR，因此将其添加到my-webapp/pom.xml：
```xml
..
  <dependencies>
    <dependency>
      <groupId>com.mycompany.app</groupId>
      <artifactId>my-app</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
    ...
  </dependencies>
```
最后，将以下元素添加到子目录中的pom.xml的<parent>元素上
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <parent>
    <groupId>com.mycompany.app</groupId>
    <artifactId>app</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>
```
现在，尝试一下...从顶级目录运行：
```shell
mvn verify
```
现在已经在中创建了 WAR my-webapp/target/my-webapp.war，并且包含了 JAR
```text
$ jar tvf my-webapp/target/my-webapp-1.0-SNAPSHOT.war
   0 Fri Jun 24 10:59:56 EST 2005 META-INF/
 222 Fri Jun 24 10:59:54 EST 2005 META-INF/MANIFEST.MF
   0 Fri Jun 24 10:59:56 EST 2005 META-INF/maven/
   0 Fri Jun 24 10:59:56 EST 2005 META-INF/maven/com.mycompany.app/
   0 Fri Jun 24 10:59:56 EST 2005 META-INF/maven/com.mycompany.app/my-webapp/
3239 Fri Jun 24 10:59:56 EST 2005 META-INF/maven/com.mycompany.app/my-webapp/pom.xml
   0 Fri Jun 24 10:59:56 EST 2005 WEB-INF/
 215 Fri Jun 24 10:59:56 EST 2005 WEB-INF/web.xml
 123 Fri Jun 24 10:59:56 EST 2005 META-INF/maven/com.mycompany.app/my-webapp/pom.properties
  52 Fri Jun 24 10:59:56 EST 2005 index.jsp
   0 Fri Jun 24 10:59:56 EST 2005 WEB-INF/lib/
2713 Fri Jun 24 10:59:56 EST 2005 WEB-INF/lib/my-app-1.0-SNAPSHOT.jar
```
这是如何运作的？首先，创建的父 POM（称为app）具有一个包装pom和定义的模块列表。这告诉 Maven 在项目集上运行所有操作，而不仅仅是当前项目（要覆盖此行为，您可以使用命令--non-recursive行选项）。

接下来，我们告诉 WAR 它需要my-appJAR。这做了一些事情：它使它在 WAR 中的任何代码的类路径上可用（在这种情况下没有），它确保 JAR 总是在 WAR 之前构建，并且它指示 WAR 插件将 JAR 包含在它的库目录。

您可能已经注意到这junit-4.11.jar是一个依赖项，但并没有在 WAR 中结束。这样做的原因是<scope>test</scope>元素——它只在测试时需要，因此不像编译时依赖项那样包含在 Web 应用程序中my-app。

最后一步是包含父定义。这确保了即使项目是通过在存储库中查找并且项目与其父项目分开分发的，也始终可以找到 POM。