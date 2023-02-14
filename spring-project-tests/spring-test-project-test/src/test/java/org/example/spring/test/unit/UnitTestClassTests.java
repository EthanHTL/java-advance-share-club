package org.example.spring.test.unit;

/**
 * 单元测试类支持 ..
 *
 * AopTestUtils 包含了大量aop 相关的方法,例如你可能有一个配置的bean作为动态mock通过使用例如EasyMock或者 Mockito ..
 * 并且这个mock是包装到Spring 代理中，你能够直接访问底层的mock对象去配置期望并执行校验 ...
 * RefelectionTestUtils 包含了大量反射的工具方法 .
 *
 *
 * TestSocketUtils 能够进行发现可靠的TCP端口(localhost) ...
 * 它能够在集成测试中用来启动一个外部服务器（在可用的随机端口上) ..
 * 但是这些工具并不保证后续的给定端口的可用性因此它们不可信任，通过它去发现一个可用的端口,相反推荐依赖服务器的能力去在随机端口上进行启动 / 它通过
 * 选择或者底层操作系统给它分配是更好的,那么交互之前先询问当前服务器使用的端口 ..
 *
 * ModelAndViewAssert 能够合并测试框架使用...
 * 为了像POJO一样测试SpringMVC的Controller, 使用断言连同MockHttpServletRequest / MockHttpSession 以及来自Spring的 Servlet api mocks
 * 的其他 ... 对于Spring MVC完整的集成测试以及 针对Spring MVC的 WebApplicationContext配置的REST Controller的集成测试,使用
 * spring mvc 测试框架替代 ...
 */
public class UnitTestClassTests {
}
