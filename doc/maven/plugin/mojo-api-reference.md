## 介绍
maven 插件能够通过大量的脚本语言或者java语言编写,插件由一个或者多个mojo组成,每一个实现作为插件的一个goal,maven 尝试使用新的Mojo API让程序员置身事外,这给了在Maven之外重用Mojos的机会,或者桥接外部系统,例如ant到Maven的机会 ..

注意: 如今,我们仅仅讨论基于java的Mojos,因此每一个脚本语言都保留了这些与各种实现形式的相同基本要求 ..

尽管这些要求在Mojo设计上已经是最小化的,他们仍然有一些小的需求要求Mojo 开发者必须坚持 .. 本质上,这些Mojo 要求通过`org.apache.maven.plugin.Mojo` 接口呈现 .. 这要求Mojo 必须实现前者或者继承它的抽象类(接口的对等物).
这个接口保证了对mojo正确的执行约定: 无参,空返回值,并且仅允许`org.apache.maven.plugin.MojoExecutionException` 的抛出声明或者此异常衍生的异常.. 它也保证了Mojo 将可以访问标准的maven 用户反馈机制-`org.apache.maven.plugin.logging.Log`,因此Mojo能够交流重要的事件到控制台或者其他日志接收器 ..

由前面所提到的,每一个插件,或者Mojo的打包的集合- 必须提供一个`META-INF/maven/plugin.xml` [描述符文件](./maven-plugin-api.md)到插件jar文件中 ... 幸运的是,Maven 也提供了一个javadoc 注解的集合(名为[Mojo Javadoc Tags](https://maven.apache.org/plugin-tools/maven-plugin-tools-java/index.html)),Java 注解(名为[Maven Plugin Tools Java5 Annotations](https://maven.apache.org/plugin-tools/maven-plugin-tools-annotations/index.html)) 以及 工具(名为[plugin tools](https://maven.apache.org/plugin-tools/) 去生成这个描述符),因此开发者不需要关心直接编写或者维护一个独立的xml 元数据文件 .. 

为了作为一个对开发者的快速参考,这个页面的剩余部分将记录这些特性(例如api,包含这些注解)这将考虑为对开发mojo的最佳实践 ..

## api 文档
### org.apache.maven.plugin.Mojo
此接口对于Mojo是必须的约定形式 为了和maven 基础设施进行交互,它包含一个`execute` 方法,这触发mojo的构建过程的行为,它能够抛出一个`MojoExecutionException`  - 如果存在一个错误情况发生..

查看以下的描述以正确使用此异常类. 同样包含了`setLog(..)` 方法,这简单的允许maven 去注入一个日志机制去允许mojo 用来和外面的世界交互 - 通过标准的maven 管道 ..

方法总结:
- `void setLog(org.apache.maven.plugin.logging.Log)` \
注入一个标准的maven 日志机制去允许mojo 交流事件以及反馈给用户 ..
- `void execute() throws org.apache.maven.plugin.MojoExecutionException` \
执行此mojo 实现的构建过程行为 .. 这主要触发maven 系统中的mojo,允许mojo 交流致命失败(通过抛出一个MojoExecutionException).. \
这个异常(所有此mojo内发生的错误情况)应该非常小心的处理. 简单的包装一个底层的异常而没有提供任何用户友好可能的原因提示是严格不鼓励的 ..事实上,更好的做法是为 Mojo 执行中的每个连贯步骤提供错误处理代码（try/catch 节）..

开发者然后在更棒的位置上去诊断任何错误的原因,并且在MojoExecutionException的消息中提供用户友好性的反馈 ..
### org.apache.maven.plugin.AbstractMojo
当前,此抽象基类简单的关心具体衍生物的maven 日志,与它保持一致,它提供了一个`protected` 方法,`getLog():Log`,去供应日志访问给这些具体的实现 ..

方法总结:
- `public void setLog(org.apache.maven.plugin.logging.Log)` \
注入一个maven 日志机制,用于日志记录
- `protected Log getLog()` \
提供标准的maven 日志访问机制 - 能够管理在此基类中
- `void execute() throws org.apache.maven.plugin.MojoExecutionException` \
抽象的,执行mojo 具体实现的动作,查看 Mojo 接口文档了解更多信息 ..

### org.apache.maven.plugin.logging.Log
这个接口提供了api 能够从mojo 返回反馈给用户, 通过标准的maven 管道.. 这应该不是一个大的惊喜 .. 尽管你可能已经注意到此方法接收一个`java.lang.CharSequence` 而不是 `java.lang.String`,这主要是作为一种便利,为了让开发者去类似于StringBuffer 直接给日志器, 而不是通过调用`toString()` 来首先实现格式化 ..

Method 总结:
- `void debug(java.lang.CharSequence)` \
发送消息给用户,以debug错误级别
- `void debug(java.lang.CharSequence,java.lang.Throwable)`
发送消息(以及伴随的异常)给用户 - 通过debug 错误级别,这个错误的堆栈将会输出(当错误级别启动的时候)
- `void debug(java.lang.Throwable)` \
发送一个异常给用户 - 根据debug错误级别,这个错误的堆栈将会输出 - 当错误级别启用的时候
- `void info(java.lang.CharSequence)` \
发送消息给用户(根据info 错误级别)
- `void info(java.lang.CharSequence,java.lang.Throwable)` \
发送消息(以及伴随的异常)给用户 - 以info 错误级别形式,这个错误堆栈将会输出(当错误级别启用)
- `void info(java.lang.Throwable)` \
发送消息给用户(根据错误级别),这个异常堆栈将会输出(当错误级别启用的时候)
- `void warn(java.lang.CharSequence)` \
发送消息给用户(在warn 错误级别)
- `void warn(java.lang.CharSequence,java.lang.Throwable)` \
包括异常堆栈输出 ..
- `void warn(java.lang.Throwable)` \
发送异常以warn 错误级别给用户,当错误级别开启的时候,输出异常的堆栈 ..
- `void error(java.lang.CharSequence)` \
发送错误 - 根据error 错误级别给用户
- `void error(java.lang.CharSequence,java.lang.Throwable)` \
发送消息以及伴随的异常以error 错误级别给用户,如果错误级别启用,则输出错误的堆栈 ..
- `void error(java.lang.Throwable)` \
发送异常给用户 - 根据error 错误级别,当错误级别启用时异常堆栈将会输出 ..

## 描述符和注解
除了在接口或者抽象基类上需要实现的正常java 需求之外,一个插件描述符必须包含这些类到插件jar中 .. 这个描述符文件被用来提供有关参数的元数据 以及其他组件需要 - 对于mojo 的集合,因此,maven 能够初始化mojo并且在执行它之前验证它的配置... 正如此,插件描述符有一组特定的信息，每个 Mojo 规范都需要这些信息才能有效,同样对于整个插件描述符自身也有要求 ..

注意: 在如下讨论中,加粗的项 都属于描述符的元素名(连同 Mojo javadoc tag,如果可用)支持插件描述符的一部分.. 一些例子为: `someElement(@annotation parameterName="parameterValue")` 或者 `someOtherElement(@annotation <rawAnnotationValue>)

注意: 从maven-plugin-plugin 3.0开始,它现在能够使用[Maven plugin tools Java Annotations](https://maven.apache.org/plugin-tools/maven-plugin-tools-annotations/index.html) 等价于 [Mojo javadoc tags](https://maven.apache.org/plugin-tools/maven-plugin-tools-java/index.html),查看[使用注解的文档](https://maven.apache.org/plugin-tools/maven-plugin-plugin/examples/using-annotations.html)

[插件描述符 参考](https://maven.apache.org/ref/current/maven-plugin-api/plugin.html) 必须提供在jar中,并且路径为`META/maven/plugin.xml`,并且它必须包含以下内容:

|描述符元素| 必须| 注意事项|
|---|---|---|
|mojos| yes | 由此插件提供的每一个mojo的描述符,每一个放在mojo 子元素中,Mojo描述符将会在下面进行详细介绍,明显的是,一个插件不会存在没有意义的mojo,因此mojos元素是必须的,至少后跟一个mojo 子元素 |
|dependencies| yes | 为了执行功能的此插件所需要的依赖集合,每一个依赖需要放置在dependency 子元素下配置. 依赖规范将会在下面进行描述,因此所有的插件必须有一个`maven-plugin-api` 的依赖,所以元素是实际上必须的,使用这个插件工具集,这些依赖能够从pom的依赖中进行抓取 ..(所以这里的工具集是什么意思,猜测就是自动生成插件描述符的工具集合,这样自动从pom 的dependencies中抓取依赖并打包到jar中 ..)

每一个在插件描述符中的Mojo 必须提供以下的内容(这里所描述符的注释在类级别上),这是mojo自身的描述配置

| 描述符元素 | Mojo Javadoc tag | Required? | Notes |
|---|---|---|---|
|aggregator | @aggregator | no | 标志此mojo 运行在多模块方式下,例如将项目集合视为模块进行构建 |
|configurator | @configurator <roleHint> | no | 此配置器类型被用来指定注入参数值到mojo时的策略,这个值通常通过Mojo的实现语言进行推断,但是能够指定去允许一个自定义的ComponentConfigurator 去使用,注意: 这仅仅使用在非常特殊的情况,使用高度受控的可能值词汇表(像这样的元素就是为什么使用描述符工具是个好主意)..|
|execute | 1. @execute phase="<phaseName" lifecycle="<lifecyleId>"  2. @execute phase="<phaseName>" 3. @execute goal="<goalName>" | no | 当goal 执行的时候,在给定的阶段结束时 - 它将首先执行一个并行的生命周期. 如果一个goal 提供而没有阶段,那么此goal 将会隔离执行. 直接将不会影响当前项目,但如果需要，可以使用 ${executedProject} 表达式,来获得支持。 一个额外的生命周期也能提供,了解[构建生命周期](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)的文档 |
|executionStrategy | @executionStrategy <strategy> | no | 指定执行策略,注意: 从maven 3.0 不支持 |
| goal | @goal <goalName> | yes | mojo 的名称,用户可以从命令行直接进行引用去直接执行Mojo,或者在Pom去提供特定于Mojo的特定配置 |
| inheritByDefault | @inheritByDefault <true | false> | No. Default: true | 指定此Mojo 是继承的,注意从 maven 3.0 不支持 |
| instantiationStrategy | @instantiationStrategy <per-lookup> | No.Default per-lookup | 指定实例化策略|
| phase | @phase <phaseName> | No | 定义绑定一个mojo 执行的默认阶段 - 如果用户没有在pom中显式的指定一个阶段, 注意: 这个注解或者说注释将不会自动的执行一个mojo(当插件声明增加到一个项目中时). 它仅仅让用户可以从周围的<execution> 元素中省略. |
|requiresDependencyResolution | @requiresDependencyResolution <requiredClassPath> | No | 此标志要求在mojo执行之前确保指定的类路径的依赖能够被解析. 以下的矩阵说明这些值能够对requiredClassPath 支持的值 以及请求解析的依赖作用域 依赖范围: [`system`,`provided`,`compile`,`runtime`,`test` ], 当请求依赖解析的范围为 `compile`: system: x,provided: x,compile: x,runtime: -,test: -,同样`runtime`: - , - ,x ,x, - ,如果是`compile+runtime` 从maven3.0开始, x ,x ,x ,x -, `test` 则是 x,x,x,x,x .. 如果这个注释出现但是没有指定scope,那么scope 默认是runtime,如果注释没有出现,那么mojo 必须不得对与 Maven 项目关联的工件做出任何假设。 |
|requiresDependencyCollection | @requiresDependencyCollection <requiredClassPath> | No | 这个标志要求组成指定的类路径的必要信息, 根据名称猜测, 注释类似于@requiresDependencyResolution 并且支持相同的<requiredClassPath> 值,重要不同是此注释将不解析依赖的文件,例如与maven 项目关联的项目能够缺少一个文件,因此,此注释意味着对于mojo(那些仅仅想要分析传递性依赖才有用),尤其是在早期生命周期阶段 - 完全的依赖解析可能会失败 - 由于项目可能尚未构建完成). 一个mojo 也许能够同时使用这个注释以及 @requiresDependencyResolution. 任何依赖的解决状态目前是收集了但是无法请求去解析的都是未定义,从maven 3.0开始 ... |
| requiresDirectInvocation | @requiresDirectInvocation <true|false> | No.Default: false | 标志Mojo 将被直接执行,在maven 3.0 不支持 |
|requiresOnline | 	@requiresOnline <true|false> | No,Default: false | 此标志确定需要在在线模式下执行 |
| requiresProject | @requiresProject <true|false> | No.Default: false | 标识此Mojo 需要运行在一个项目中 |
| requiresReports | @requiresReports <true|false> | No.Default: false | 标识此Mojo 需要报告,在maven 3.0不支持 |
| threadSafe | 	@threadSafe <true|false> | No.Default: false | 标识此Mojo是一个线程安全的,例如此mojo安全的在并行构建期间 支持并发执行, 没有这个注释的mojo 将让maven输出一个警告,当你在并行构建场景下使用,简写形式是@threadSafe 它等价于 @threadSafe true,自从spring 3.0开始可能才有这个注释 |
| description | none(detected) | No | mojo 功能的描述,使用工具集,将自动提供类级别上的javadoc 描述 ,注意: 然而它并不是mojo 规范中必须的一部分,它可以提供去启用进一步工具支持(例如进行浏览,等等 并且为了清晰,明确性) |
| implementation | none(detected) | Yes | mojo 的全限定类名(或者非java mojo的情况下,脚本的路径) |
| language | none(detected) | No.Default: java | 此Mojo的实现语言(java,beanshell,等等) |
|deprecated | 	@deprecated <deprecated-text> | No | mojo 不建议的原因描述,类似于javadoc deprecated,这将触发一个警告(当一个用户尝试使用一个标记为 不建议的mojo ).. |
| since | @since <since-text> | No | 当此Mojo已经增加此api时指定版本,类似于javadoc since |

每一个mojo 规定了这些参数 - 为了如期工作, 这些参数是mojo 对外面世界的链接,并且能够合并pom / project的值,插件配置(来自pom 以及 默认的配置) 以及系统属性的值..

NOTE[1]: 对于Mojo 参数的讨论, 对于一个参数的单个注释可能包含了描述符规定中的多个元素, 这一部分重复的注释声明将被用来单独的详细描述注释的每一个参数 ..

NOTE[2]: 在大多数情况,简单通过@parameter 注释一个Mojo字段已经足够去允许使用POM configuration 元素来注入参数的值, 下面讨论了此注解的高级用法,包含了其他东西 .. 

对于Mojo的每一个参数能够在插件描述符中指定:(mojo的内部参数配置等等)
1. alias \
    等价于@parameter alias="myAlias" ,非必须,注意: 指定一个别名能够被用来从pom中配置此参数,主要用于优化用户友好性,如果mojo 字段名称是对于用户来说不直观的或者不利于在POM中进行配置 .. 
2. configuration \
@component role="..." roleHint="..." 非必须, 通过Plexus 组件的实例填充此字段. 这类似于声明一个对Plexus组件的需要. 默认要求是有一个角色 - 等价于字段的声明类型, 并且会使用role Hint "default", 能够自定义(通过提供一个role 或者 roleHint 参数进行自定义),例如 `@component role="org.apache.maven.artifact.ArtifactHandler" roleHint="ear"` ,注意: 这是等价于不建议的参数形式 `@parameter expression="${component.yourpackage.YourComponentClass#roleHint}"` 
3. configuration \
   maven-plugin-plugin 2.x:
   @parameter expression="${aSystemProperty}" default-value="${anExpression}" \
   maven-plugin-plugin 3.x:
   @parameter property="aSystemProperty" default-value="${anExpression}",非必须, 指定的这个表达式被用来计算需要注入到Mojo的参数的值(在运行时).. 由default-value提供的表达式通常被用来引用在pom中的其他元素,例如`${project.resources}` - 这引用的资源列表意味着伴随这些类到最终的jar文件中.. 当然,默认值不需要是一个表达式,而且可以是一个简单的常量(例如true / 1.5), 并且类型String的参数 能够通过自变量值和表达式进行混合: `${project.artifactId}-${project.version}-special` . \
 通过在maven-plugin-plugin 3.x中的property 提供的系统属性 或者 在maven-plugin-plugin 2.x的expression 让用户能够覆盖来自命令行的默认值(通过-DSystemProperty=value),这样能够覆盖系统属性 .. \
 注意: 如果没有default-value 或者property 或者expression 被指定,那么参数能够从pom中进行配置,并且在默认值中的'${','}' 必须要分割为实际能够被评估的表达式 ... 
4. editable \
  @readonly 非必须,指定了此参数不能直接被用户配置(例如通过pom 配置的情况). 这是有用的(当你想要强制用户使用常用pom 元素而不是插件配置), 在这种情况下,你可能想要使用工件的最终名称作为参数,因此,你可能想要用户去修改<build><finalName/></build>, 而不是在插件配置部分中直接指定一个finalName的值,它确信是有用的(例如,一个List类型的参数它期待Artifact 类型的List不能获得一个全是字符串的List),注意: 此注释的规范标志此参数是不可编辑的,因此这里没有true / false 值 ..
5. required \
 @required  非必须,指定此参数对于mojo 工作是非必须的,这个被用来验证mojo的配置(在注入mojo参数之前 以及从某种半状态指定的mojo之前),注意: 这个注释的规范标志此参数是必须的,所以没有true /false 值 ..
6. description none(detected) \
    mojo功能的描述,使用工具集,将提供类级别的javadoc 描述, 注意,这在mojo规范中不是必须的,它可以提供更进一步的工具支持(例如为了浏览,以及为了清晰性) ..
7. implementation none(detected) yes mojo 的完全限定类名(非java mojo的情况下,脚本路径)
8.language none(detected) none,默认java, 标识mojo的实现语言(例如 java ,beanshell等等)
8. deprecated @deprecated <deprecated-text> 非必须, 描述mojo 不建议的原因描述,类似于javadoc ..
9. since @since <since-text> 非必须, 当mojo 增加为此插件的api时,你可以指定版本,类似于javadoc since 

插件描述符的最终组件就是依赖, 这让插件的功能独立于它的pom(或者至少要声明需要去运行的最小需要的库),依赖项取自插件计算的依赖项的运行时范围(根据POM). 依赖的指定方式完全和在pom中相同,除了<scope> 元素(在插件描述符中的所有依赖都假设为runtime,因为这是插件的运行时方面) ..

不同的是构建这个插件和 插件运行所需要的依赖是独立的,在pom中配置一个插件所需的依赖,所考虑的范围是runtime,而编译插件所考虑的语义并不仅仅是runtime .

## 插件工具
现在,我们多次提到了插件工具 - 而没有告诉你它是什么或者如何使用他们 .. 除了手动编写或者维护上面提到的元数据之外,maven 携带了一些工具旨在处理这些工作, 事实上,这仅仅是一个插件开发者需要做的事情 - 在pom中声明它的项目作为一个插件即可 ..一旦完成,maven 将调用合适的描述符生成器,例如,为了生成一个可以在maven 构建中使用的工件.  额外的元数据能够通过javadoc 注解注入(或者jdk5 注解), 这能够让mojo 和用户之间进行丰富的交互.. 下面描述了pom的改变去必要的创建一个插件工件.
## 项目描述符(POM) 要求
从pom来说,maven 插件项目看起来十分类似其他项目,对于纯java 插件,差异甚至小于基于脚本的插件 ... 如下pom元素的详细描述 - 这是必要去构建一个maven 插件工件所需要的:
1. packaging \
必须, 简单声明`<packaging> maven-plugin </packaging> ` , 必须声明此项目为一个maven 插件项目
2. scriptSourceDirectory No, `<scriptSourceDirectory> src/main/scripts </scriptSourceDirectory>` ,在基于脚本的mojos情况下,这将在另外的文档中进行描述.. POM必须包括一个额外的元素来区分脚本源和（可选）Java支持类,这个元素是scriptSourceDirectory - 在build部分, 这个目录包含了资源列表,其中包含在任何编译到最终工件的代码..  它与构建部分中的资源分开指定，以表示其作为脚本可选源目录的特殊状态。

当上述的配置完毕之后,我们可以简单安装插件 ... 然后就可以使用此插件了 ..

## IDE 集成
如果你使用JetBrains Idea 去开发插件,你能够使用如下方式配置java doc 注释作为一个live templates;
1. 下载[此文件](https://maven.apache.org/developers/maven.xml) 并放置它到`$USER_HOME/.IntelliJIdea/config/templates` 
2. 重启IDEA(模板将在启动时进行加载)
3. 增加以下的列表到  Settings -> IDE -> Errors -> General -> Unknown javadoc tags -> Additional javadoc tags \
 例如:aggregator, execute, goal, phase, requiresDirectInvocation, requiresProject, requiresReports, requiresOnline, parameter, component, required, readonly

## 资源
这部分简单的给出了更多信息的链接
1. QDox 项目(解析javadoc 注释)([link](https://github.com/paul-hammant/qdox))
2. Plexus 项目(Plexus 容器)([link](https://codehaus-plexus.github.io/))
3. maven 插件 api[link](https://maven.apache.org/ref/current/maven-plugin-api/apidocs/index.html)
4. MojoDescriptor API[[link](https://maven.apache.org/ref/current/maven-plugin-api/apidocs/org/apache/maven/plugin/descriptor/MojoDescriptor.html)]




















