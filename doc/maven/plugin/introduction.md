# maven 插件开发
maven 由一个核心的引擎组成(它提供了基本的项目处理的能力 以及构建过程的管理,并且托管了一些插件能够被用来执行实际的构建任务)..
## 插件是什么
"maven" 实际上仅仅是maven 插件集合的一个核心框架,换句话说,插件表示了有多少真实的动作执行,插件可以被用来创建 jar,war文件,编译代码,单元测试代码,创建项目文档,以及其他动作 .. 几乎你能够想到执行在项目上的任何动作都可以实现为maven 插件 .. 

插件是maven的一个核心特性,能够允许去跨越多个项目去重用相同的构建逻辑. 它们通过在项目的描述的上下文 - 项目对象模型(POM - project object model)中执行 "action" 来实现这样的目的(例如,创建一个war文件或者编译单元测试). 插件行为能够通过一组独一无二的参数进行定制 - 它们能够通过每一个插件的目标(goal) 或者Mojo 进行暴露 ..

最简单的插件之一就是clean plugin,它负责[maven clean plugin](https://maven.apache.org/plugins/maven-clean-plugin/) 负责移除maven 项目的target 目录. 当你运行 `mvn clean`的时候,maven 将会执行"clean" 目标(由clean 插件内置定义的),并且target 目录将会移除 .. 并且此插件也定义了一个参数来指定插件的行为,这个参数叫做outputDirectory并且默认值是`${project.build.directory}` .

## Mojo 是什么(并且为什么它的名字叫做Mojo)
一个Mojo 实际上仅仅是Maven中的一个goal(目标),并且插件可以由多个goals(Mojos) 组成,Mojo 能够定义为注解注释的Java类或者Beanshell 脚本. 一个Mojo 指定了有关一个goal的元数据(goal 名称,它融入的生命周期阶段,以及它期待的参数) .

Mojo 仅仅是一个POJO(plain old java object), 仅仅使用"Maven" 替代"Plain", Mojo 也一个有趣的单词(查看[Wikipedia](http://www.wikipedia.org/) - "mojo" 被定义为: 衣服下方别着的一个小包). 这样的包有着超自然的能力,例如保护侵害,带来好运,等等 ..

## 构建生命周期是什么(概述)
构建生命周期是一系列的共同的阶段(所有的项目构建都会自然的在通过这些阶段)

## 资源
1. [权限开发中心](https://maven.apache.org/plugin-developers/index.html)
2. [配置插件](https://maven.apache.org/guides/mini/guide-configuring-plugins.html)
