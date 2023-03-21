# 插件开发者中心
这个文档中心是对那些需要开发maven插件的人准备的. 这可以是你自己的构建或者作为你的第三方工具的一个携带品 ...

mojo 是一个 maven plain old java object. 每一个mojo 是一个可执行的goal(在maven中),一个插件是可以包含一个或者多个相关的mojo的发布 ..,总体说来,我们开发插件以mojo 作为最小的可执行单元(但是它们可以包含在一个插件中,在任何你想要执行的阶段进行指定goal执行)..

- [introduction](./introduction.md) 了解概念
- [first mojo](./first-mojo.md) 学习编写第一个插件
- [first report mojo](./first-report-mojo.md) 了解如何编写第一个报告插件
- [testing plugin](./testing-plugin.md) 编写插件的测试
- [documenting plugin](./documenting-plugin.md) 为插件编写文档
- TODO: 创建并使用自定义打包方式(例如 maven-archetype packaging)
- [Common Bugs and Pitfalls](./common-bugs-pitfalls.md) 有问题的编码模式概述

## Reference

- [Mojo api 以及 注解参考](./mojo-api-reference.md)
- [maven 插件工具以及注解](./maven-plugin-tools.md)
- [maven api 参考](./maven-api-reference.md)
- [maven 类加载](./maven-class-loading.md)

## Extensions
- [maven 3 lifecycle extensions](./maven-lifecycle-extension.md)
- [how to upgrade from plexus javadoc tags to Plexus java annotations](./maven-goal-migration-to-annotation.md)
- [using jsr-330(instead of plexus java annotations)](./jsr-330-replace.md)