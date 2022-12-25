package org.example.spring.test;
/**
 * @date 2022/12/24
 * @time 17:46
 * @author FLJ
 * @since 2022/12/24
 *
 *
 * 上下文会被缓存,对于每一个测试套件以及相同独一无二的上下文配置来说(会重用应用上下文) ..
 *
 * 所以我们需要理解独一无二 和 测试套件的含义 ..
 *
 * 一个应用上下文通过配置参数的合并实现独一无二的标识 ..
 * 因此,配置参数的合并会生成一个key,标识哪一个上下文被缓存 .. TCF 使用以下的配置参数来构建上下文缓存key ..
 *
 * 1. locations - @ContextConfiguration
 * 2. classes - @ContextConfiguration
 * 3. contextInitializerClasses  - @ContextConfiguration
 * 4. contextCustomizers  - ContextCustomizerFactory (包括各种@DynamicPropertySource方法) 以及 来自spring boot 测试支持的特性
 *      (例如,@MockBean / @SpyBean) ..
 * 5. contextLoader - @ContextConfiguration
 * 6. parent - @ContextHierarchy
 * 7. activeProfiles - @ActiveProfiles
 * 8. propertySourceLocations  - @TestPropertySource
 * 9. propertySourceProperties - @TestPropertySource
 * 10. resourceBasePath - @WebAppConfiguration
 *
 *
 * 测试套件 以及 forked processes
 *  spring TCF 会存储应用上下文到静态缓存中,这意味着缓存实际上是存储到一个静态变量中,如果测试运行在独立的程序中,那么静态缓存将会被清理(在测试执行
 *  之间),这实际上没有使用到缓存机制 ..
 *
 *  为了受益,所有的测试必须运行到相同进程或者测试套件中,者能够通过在IDE中对所有测试进行分组,类似的对于构建框架中运行的测试,例如Ant / Maven /
 *  Gradle,它们重要的是确保构建框架不会在任务之间fork .. 例如,maven的 surefire 插件的 forkMode 如果设置为always / pertest,那么TCF
 *  不能够在测试类之间缓存应用上下文,构建处理会变得非常慢 ..
 *
 *  并且上下文缓存的默认最大尺寸为32, 采用LRU抛弃策略(来丢弃陈旧的上下文),你能够改变它(通过设置命令行参数 / 或者构建脚本中添加jvm参数 spring.
 *  test.context.cache.maxSize, 除此之外你可以通过SpringProperties 机制进行相同属性设置 ..
 *
 *  由于大量的已经加载的应用上下文在给定的套件中可能导致套件花费不必要的长时间运行,你能够设置org.springframework.test.context.cache日志分类
 *  的日志级别为 DEBUG ..
 *
 *  应用上下文的生命周期 以及 控制台日志
 *  控制台日志很有用(debug),用于分析(SYSOUT / SYSERR流)
 *  某些构建工具和IDE 能与给定的测试关联控制台输出,某些控制台输出无法容易的与给定测试关联 ..
 *  首先需要理解应上下文合适被TCF 加载到测试套件中的 .
 *  也就是当一个测测试类的实例准备好了以后,就尝试依赖注入到测试实例的字段上,这意味着任何在应用上下文初始化阶段的控制台日志触发通常不能够关联到一个
 *  独立的测试方法(因为此时测试实例还没有处理好),但是如果上下文在测试方法执行之前立即发生了关闭(根据@DirtiesContext 语义),一个上下文的新实例将会
 *  被加载(在测试方法执行之前), 这种情况下,IDE / 构建工具也许能够关联控制台日志到单独的测试方法 ..
 *
 *  一个应用上下文(for test) 能够被以下的场景中进行关闭
 *   - 存在@DirtiesContext 语义
 *   - LRU 抛弃策略
 *   - JVM 关闭钩子关闭上下文(当jvm 执行测试条件中断)
 *
 *  在一个特殊的测试方法之后偶遇@DirtiesContext语义关闭了应用上下文, IDE / 构建工具可以关联控制台日志到单独的测试方法, 如果context在测试类之后
 *  根据@DirtiesContext语义关闭应用上下文,任何在应用上下文关闭阶段的控制台日志触发都无法与关联单独的测试方法关联,类似的,任何控制台日志在关闭阶段
 *  (通过jvm 关闭钩子)的控制台日志触发也不能关联到单独的测试方法 ..
 *
 *  当一个Spring 应用上下文通过 JVM 关闭钩子关闭时, 在关闭阶段执行的回调是通过SpringContextShutdownHook的线程执行官的,因此如果你希望禁用控制
 *  台日志触发(当应用上下文通过jvm 关闭钩子关闭时)你可以注册自定义的过滤器到日志框架中允许忽略由此线程初始化的任何日志 ..
 *
 *
 *
 *
 *
 *  在其他情况下(例如应用上下文很容易腐烂并且需要重新刷新的情况下), 通过修改bean 定义bean 定义或者应用上下文对象的状态改变 ..(举个例子)
 *  你能够标注测试类或者测试方法使用@DirtiesContext,这指示Spring 将从缓存中移除上下文并重新构建上下文(在运行下一个测试之前) ..
 *  注意到此注解的支持是通过 DirtiesContextBeforeModesTestExecutionListener  以及 DirtiesContextTestExecutionListener ..
 *  默认是启用的 ..
 *
 **/
public class ContextCachingTests {
}
