package org.example.spring.test.integration.support.each;

/**
 * ahead of time support
 * spring 支持在测试框架中使用 aot ...
 * 并且测试支持使用以下特性来扩展了spring的核心aot 支持 ..
 * 1. 在当前项目中使用TCF来加载一个应用上下文的所有集成测试都包含构建时间检测 ..
 *      1.1 提供对JUnit5 / JUnit4 以及TestNG的隐式支持 以及其他使用了spring 核型的测试注解的测试框架 .. 只要它运行在JUnit平台上的TestEngine
 *      上(为当前项目注册的)
 * 2. 构建时间aot处理
 *      在当前项目中的每一个独一无二的测试应用上下文将会被刷新(由于aot 处理)
 * 3. 运行时aot 支持:
 *      当执行在aot运行时模式中,一个spring 集成测试将会使用一个 aot优化的透明参与到上下文缓存的应用上下文 ...
 *
 * 当前@ContextHierarchy 注解 是不支持在aop 模式中使用的 ..
 *
 * 为了提供特定测试的运行时提示(例如使用在Graalvm 原生镜像中),你能够有以下选择：
 * 1. 实现一个自定义的TestRuntimeHintsRegistrar 并通过META-INF/spring/aot.factories全局注册它 ..
 * 2. 实现一个自定义的RuntimeHintsRegistrar 并通过META-INF/spring.aot.factories或者局部的通过在测试类上使用@ImportRuntimeHints进行
 * 注册 ..
 * 3. 通过@Reflective 或者@RegisterReflectionForBinding ..
 * 4. 查看Runtime Hints 了解Spring核心的运行时提示以及注解支持的详情 ..
 *
 * TestRuntimeHintsRegistrar作为 RuntimeHintsRegister的伴生 ap,如果你需要注册一个全局hints（为测试支持-并没有特定于一个特定的测试类)
 * 你可以全局注册hints(通过RuntimeHintsRegistrar 而不是test 特定的api)...
 *
 * 如果你实现了一个自定义的ContextLoader,你必须实现AotContextLoader 为了提供aot构建时处理 以及 aot 运行时执行支持 ..
 * 然而由spring框架提供的所有上下文加载实现 以及 spring boot 提供的都实现了aot 上下文加载器 ..
 *
 * 如果你实现了一个自定义的TestExecutionListener,你必须实现AotTestExecutionListener 为了参与到 aot 处理中 ..
 * 查看SqlScriptsTestExecutionListener(spring-test)模块中的 - 举个例子 ..
 */
public class AOTSupportTests {
}
