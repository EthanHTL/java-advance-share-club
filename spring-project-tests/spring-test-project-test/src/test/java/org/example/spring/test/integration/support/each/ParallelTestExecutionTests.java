package org.example.spring.test.integration.support.each;

/**
 * 并行测试执行 ..
 *
 * 单个jvm中并行执行从测试(当使用TCF时),通常情况下大多数测试类或者测试方法能够天然并行化（无需代码修改或者配置修改)
 *
 * 引入并行测试可能导致副作用 ,奇怪的运行时行为,间歇性失败或者看似随机的失败 ..
 * spring team 提供了不并行运行测试的准则:
 * 1. 使用了@DirtiesContext支持
 * 2. @MockBean / @SpyBean 支持 .
 * 3. JUnit4的@FixMethodOrder 支持或者任何测试特性(设计来确保测试方法以特定顺序执行) 但是请注意，如果整个测试类并行运行，则这不适用。
 * 改变共享服务的状态或者系统的状态(例如数据库 / 消息broker / 文件系统 以及其他） ..这同样适用于内嵌 / 外部化系统 ..
 *
 * 对于并行测试执行失败(以应用上下文中当前测试不可用(不在激活)的情况下,通常是ioc 从不同的线程中从ContextCache中移除了) ..
 * 这可能是@DirtiesContext的使用或者ContextCache的自动抛弃策略引起的 ..
 * 如果@DirtiesContext 是一个罪魁祸首,你可以避免@DirtiesContext的使用或者从并行测试中排除这些测试(让它们非并行运行) ..
 * 如果是ContextCache的最大容量溢出,你能够增加缓存的最大尺寸 ...
 *
 * 在spring TCF中的并行测试运行仅仅是底层的TestContext实现提供一个copy 构造器 ,查看TestContext了解详情 ...
 * 默认的在spring中的DefaultTestContext提供了这样的一个构造器 ..
 * 然而如果你使用了一个第三方库(提供了TestContext的自定义实现),你需要校验它是否对并行测试执行可行 ..
 */
public class ParallelTestExecutionTests {
}
